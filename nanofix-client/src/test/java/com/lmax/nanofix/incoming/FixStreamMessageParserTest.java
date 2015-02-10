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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.lmax.nanofix.MessageParserCallback;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class FixStreamMessageParserTest
{
    private static final int MAX_MESSAGE_SIZE = 4096;

    @Test
    public void shouldParseSingleMessageInSegment()
    {
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        ByteBuffer inputStream = ByteBuffer.wrap(newOrderSingle);
        final byte[] result = new byte[newOrderSingle.length];

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                System.arraycopy(buffer, offset, result, 0, length);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream);

        assertArrayEquals(newOrderSingle, result);
    }

    @Test
    public void shouldParseSingleMessageAtEndOfSegmentAfterLateJoin()
    {
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        final int previousMessageFragmentSize = 70;
        ByteBuffer inputStream = ByteBuffer.allocate(newOrderSingle.length + previousMessageFragmentSize);

        for (int i = 0; i < previousMessageFragmentSize; i++)
        {
            inputStream.put((byte)88); // 'X'
        }

        inputStream.put(newOrderSingle);
        inputStream.flip();

        final byte[] result = new byte[newOrderSingle.length];

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                System.arraycopy(buffer, offset, result, 0, length);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream);

        assertArrayEquals(newOrderSingle, result);
    }

    @Test
    public void shouldParseTwoMessagesInSegment()
    {
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        ByteBuffer inputStream = ByteBuffer.allocate(newOrderSingle.length * 2);
        inputStream.put(newOrderSingle);
        inputStream.put(newOrderSingle);
        inputStream.flip();

        final List<byte[]> results = new ArrayList<byte[]>();

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                byte[] newMessage = new byte[length];
                System.arraycopy(buffer, offset, newMessage, 0, length);
                results.add(newMessage);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream);

        assertTrue(2 == results.size());
        assertArrayEquals(newOrderSingle, results.get(0));
        assertArrayEquals(newOrderSingle, results.get(1));
    }

    @Test
    public void shouldParseThreeMessagesInSegment()
    {
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        ByteBuffer inputStream = ByteBuffer.allocate(newOrderSingle.length * 3);
        inputStream.put(newOrderSingle);
        inputStream.put(newOrderSingle);
        inputStream.put(newOrderSingle);
        inputStream.flip();

        final List<byte[]> results = new ArrayList<byte[]>();

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                byte[] newMessage = new byte[length];
                System.arraycopy(buffer, offset, newMessage, 0, length);
                results.add(newMessage);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream);

        assertTrue(3 == results.size());
        assertArrayEquals(newOrderSingle, results.get(0));
        assertArrayEquals(newOrderSingle, results.get(1));
        assertArrayEquals(newOrderSingle, results.get(2));
    }

    @Test
    public void shouldParseLargeMessageFragmentedAcrossTwoBuffers()
    {
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        ByteBuffer inputStream1 = ByteBuffer.allocate(1500);
        inputStream1.put(newOrderSingle, 0, newOrderSingle.length - 70);
        inputStream1.flip();

        final List<byte[]> results = new ArrayList<byte[]>();

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                byte[] newMessage = new byte[length];
                System.arraycopy(buffer, offset, newMessage, 0, length);
                results.add(newMessage);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream1);

        ByteBuffer inputStream2 = ByteBuffer.allocate(1500);
        inputStream2.put(newOrderSingle, newOrderSingle.length - 70, 70);
        inputStream2.flip();

        parser.parse(inputStream2);

        assertTrue(1 == results.size());
        assertArrayEquals(newOrderSingle, results.get(0));
    }

    @Test
    public void shouldParseLargeMessageFragmentedAcrossTwoBuffersWithinChecksumField()
    {
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        ByteBuffer inputStream1 = ByteBuffer.allocate(1500);
        inputStream1.put(newOrderSingle, 0, newOrderSingle.length - 3);
        inputStream1.flip();

        final List<byte[]> results = new ArrayList<byte[]>();

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                byte[] newMessage = new byte[length];
                System.arraycopy(buffer, offset, newMessage, 0, length);
                results.add(newMessage);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream1);

        ByteBuffer inputStream2 = ByteBuffer.allocate(1500);
        inputStream2.put(newOrderSingle, newOrderSingle.length - 3, 3);
        inputStream2.flip();

        parser.parse(inputStream2);

        assertTrue(1 == results.size());
        assertArrayEquals(newOrderSingle, results.get(0));
    }

    @Test
    public void shouldParseLargeMessageFragmentedAcrossTwoBuffersWithinChecksumFieldWithFollowingMessage()
    {
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        ByteBuffer inputStream1 = ByteBuffer.allocate(1500);
        inputStream1.put(newOrderSingle, 0, newOrderSingle.length - 3);
        inputStream1.flip();

        final List<byte[]> results = new ArrayList<byte[]>();

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                byte[] newMessage = new byte[length];
                System.arraycopy(buffer, offset, newMessage, 0, length);
                results.add(newMessage);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream1);

        ByteBuffer inputStream2 = ByteBuffer.allocate(1500);
        inputStream2.put(newOrderSingle, newOrderSingle.length - 3, 3);
        inputStream2.put(newOrderSingle);
        inputStream2.flip();

        parser.parse(inputStream2);

        assertTrue(2 == results.size());
        assertArrayEquals(newOrderSingle, results.get(0));
        assertArrayEquals(newOrderSingle, results.get(1));
    }

    @Test
    public void shouldRecogniseTruncatedMessageLessThanBufferSize() throws Exception
    {
        final byte[] trunactedExecutionReport = FixMessageUtil.getTruncatedExecutionReport();
        final byte[] executionReport = FixMessageUtil.getExecutionReport();

        ByteBuffer firstBuffer = ByteBuffer.wrap(trunactedExecutionReport);
        ByteBuffer secondBuffer = ByteBuffer.wrap(executionReport);

        final List<byte[]> results = new ArrayList<byte[]>();
        final int[] truncated = {0};

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                byte[] newMessage = new byte[length];
                System.arraycopy(buffer, offset, newMessage, 0, length);
                results.add(newMessage);
            }

            @Override
            public void onTruncatedMessage()
            {
                truncated[0]++;
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(firstBuffer);
        parser.parse(secondBuffer);

        assertArrayEquals(trunactedExecutionReport, results.get(0));
        assertArrayEquals(executionReport, results.get(1));
        assertThat(truncated[0], is(1));
    }

    @Test
    public void shouldThrowErrorAndNotPublishWhenParsingMessageLongerThanMaxAllowedMessageSize() throws Exception
    {
        final int allowedMessageSize = 100;
        final byte[] newOrderSingle = FixMessageUtil.getNewOrderSingle();
        ByteBuffer inputStream = ByteBuffer.wrap(newOrderSingle);
        final byte[] result = new byte[newOrderSingle.length];
        final byte[] expectedResult = new byte[newOrderSingle.length];

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                System.arraycopy(buffer, offset, result, 0, length);
            }

            @Override
            public void onTruncatedMessage()
            {
            }

            @Override
            public void onParseError(final String error)
            {
            }
        };

        ByteStreamMessageParser parser = new FixStreamMessageParser(allowedMessageSize);
        parser.initialise(callback);

        parser.parse(inputStream);

        assertArrayEquals(expectedResult, result);
    }
}
