/*
 * Copyright 2015 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lmax.nanofix.incoming;

import com.lmax.nanofix.FixUtil;
import com.lmax.nanofix.byteoperations.Bits;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

public final class FixStreamMessageParser
    implements ByteStreamMessageParser
{
    private static final Logger LOGGER = Logger.getLogger(FixStreamMessageParser.class);

    public static final byte ASCII_SOH = 1;
    public static final byte ASCII_0 = 48;
    public static final byte ASCII_1 = 49;
    public static final byte ASCII_8 = 56;
    public static final byte ASCII_EQUALS = 61;
    public static final byte ASCII_F = 70;
    public static final byte ASCII_I = 73;
    public static final byte ASCII_X = 88;

    private static final int FIX_MESSAGE_START_CODEC = Bits.readInt(new byte[] {
            FixStreamMessageParser.ASCII_8, FixStreamMessageParser.ASCII_EQUALS, FixStreamMessageParser.ASCII_F, FixStreamMessageParser.ASCII_I}, 0);
    private static final int FIX_MESSAGE_END_CODEC = Bits.readInt(new byte[] {
            FixStreamMessageParser.ASCII_SOH, FixStreamMessageParser.ASCII_1, FixStreamMessageParser.ASCII_0, FixStreamMessageParser.ASCII_EQUALS}, 0);

    private final ByteBuffer fragmentedMessage;
    private final int maxMessageSize;
    private MessageParserCallback messageParserCallback;

    public FixStreamMessageParser(final int maxMessageSize)
    {
        this.maxMessageSize = maxMessageSize;
        this.fragmentedMessage = ByteBuffer.allocate(maxMessageSize);
    }

    @Override
    public void initialise(final MessageParserCallback messageParserCallback)
    {
        this.fragmentedMessage.clear();
        this.messageParserCallback = messageParserCallback;
    }

    @Override
    public void parse(final ByteBuffer segment)
    {
        final int segmentLimit = segment.limit();
        try
        {
            if (fragmentedMessage.position() > 0)
            {
                handleFragmentContinuation(segment);
            }

            while (segment.position() < segmentLimit)
            {
                handleNextMessage(segment);
            }
        }
        catch (final Exception ex)
        {
            final String msg = String.format("Exception parsing segment:%n%s %n%s %n adding to %n%s %n%s",
                segment.toString(),
                new String(segment.array(), 0, segmentLimit, FixUtil.getCharset()),
                fragmentedMessage.toString(),
                new String(fragmentedMessage.array(), 0, fragmentedMessage.position(), FixUtil.getCharset()));

            LOGGER.warn(msg, ex);
            messageParserCallback.onParseError(msg);
            fragmentedMessage.clear();
        }
    }

    private void handleFragmentContinuation(final ByteBuffer segment)
    {
        if(!segment.hasRemaining())
        {
            return;
        }

        final int start = segment.position();
        final int end = findCurrentFragmentedMessageBoundary(segment);
        consumeIntoFragmentedMessage(segment, start, end);
        handleFragmentedMessage(segment);
    }

    private void consumeIntoFragmentedMessage(final ByteBuffer segment, final int start, int end)
    {
        final boolean foundMessageBoundary = end != -1;
        final int newEnd = foundMessageBoundary ? end : segment.limit();

        final int remainingFragmentCapacity = fragmentedMessage.remaining();
        final int bytesToConsume = Math.min(newEnd - start, remainingFragmentCapacity);

        fragmentedMessage.put(segment.array(), start, bytesToConsume);
        if (!foundMessageBoundary && 0 == fragmentedMessage.remaining() || bytesToConsume == 0)
        {
            handleTruncatedMessage();
        }
    }

    private void handleFragmentedMessage(final ByteBuffer segment)
    {
        final int currentPosition = fragmentedMessage.position();

        if (0 == currentPosition)
        {
            return;
        }

        fragmentedMessage.flip();

        final int begin = findBeginningOfNextMessage(fragmentedMessage);
        final int end = begin == -1 ? -1 : findEndOfCurrentMessage(fragmentedMessage);

        if (-1 != end)
        {
            callbackMessage(fragmentedMessage.array(), begin, end);
            // our fragmented message didnt need all the bytes in the incoming segment,
            // so rewind the segment by the exact number of bytes we didnt use
            segment.position(segment.position() - (fragmentedMessage.limit() - end));
            fragmentedMessage.clear();
        }
        else
        {
            fragmentedMessage.limit(fragmentedMessage.capacity());
            fragmentedMessage.position(currentPosition);
        }
    }

    private void consumeIntoFragmentedMessage(final ByteBuffer segment)
    {
        final int remainingFragmentCapacity = fragmentedMessage.remaining();
        if (segment.remaining() < remainingFragmentCapacity)
        {
            fragmentedMessage.put(segment);
        }
        else
        {
            fragmentedMessage.put(segment.array(), segment.position(), remainingFragmentCapacity);
            handleTruncatedMessage();
            segment.position(segment.position() + remainingFragmentCapacity);
        }
    }

    private int findCurrentFragmentedMessageBoundary(final ByteBuffer incoming)
    {
        final int origPos = incoming.position();
        int fragmentedMessageEndBoundary = findBeginningOfNextMessage(incoming);
        if (fragmentedMessageEndBoundary == -1)
        {
            incoming.position(origPos);
            fragmentedMessageEndBoundary = findEndOfCurrentMessage(incoming);
        }

        return fragmentedMessageEndBoundary;
    }

    private void handleTruncatedMessage()
    {
        messageParserCallback.onTruncatedMessage();
        callbackMessage(fragmentedMessage.array(), 0, fragmentedMessage.position());
        fragmentedMessage.clear();
    }

    private void handleNextMessage(final ByteBuffer segment)
    {
        final int origPos = segment.position();
        final int begin = findBeginningOfNextMessage(segment);
        if (-1 != begin)
        {
            final int end = findEndOfCurrentMessage(segment);
            if (-1 != end)
            {
                callbackMessage(segment.array(), begin, end);
            }
            else
            {
                segment.position(begin);

                consumeIntoFragmentedMessage(segment);
            }
        }
        else
        {
            segment.position(origPos);
            consumeIntoFragmentedMessage(segment);
        }
    }

    private int findBeginningOfNextMessage(final ByteBuffer segment)
    {
        final byte[] buffer = segment.array();
        int pos = segment.position();
        final int limit = segment.limit();

        while (pos < limit)
        {
            if (isStartOfMessage(buffer, pos, limit))
            {
                segment.position(pos);
                return pos;
            }

            pos++;
        }

        segment.position(pos);

        return -1;
    }

    private boolean isStartOfMessage(final byte[] buffer, final int pos, final int limit)
    {
        return
            pos + 4 < limit &&
                FIX_MESSAGE_START_CODEC == Bits.readInt(buffer, pos) &&
                buffer[pos + 4] == ASCII_X;
    }

    private int findEndOfCurrentMessage(final ByteBuffer segment)
    {
        final byte[] buffer = segment.array();
        int pos = segment.position();
        final int limit = segment.limit();

        while (pos < limit)
        {
            if (isStartOfChecksum(buffer, pos, limit))
            {
                pos += 4;
                while (pos < limit)
                {
                    if (ASCII_SOH == buffer[pos++])
                    {
                        segment.position(pos);
                        return pos;
                    }
                }
            }

            pos++;
        }

        segment.position(limit);

        return -1;
    }

    private boolean isStartOfChecksum(final byte[] buffer, final int pos, final int limit)
    {
        return pos + 3 < limit &&
            FIX_MESSAGE_END_CODEC == Bits.readInt(buffer, pos);
    }

    private void callbackMessage(final byte[] buffer, final int startPos, final int endPos)
    {
        if(endPos - startPos > maxMessageSize)
        {
            throw new IllegalStateException("Msg size [" + (endPos - startPos) + "], Max allowed size [" + maxMessageSize + "]");
        }
        messageParserCallback.onMessage(buffer, startPos, endPos-startPos);
    }
}

