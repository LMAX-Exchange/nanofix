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

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public final class FixStreamMessageParserTest
{
    private static final int MAX_MESSAGE_SIZE = 4096;

    @Test
    public void shouldParseFragmentedMessageAtFixMessageStartCodecBoundary()
    {
        final byte[] part1 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=279|35=X|49=LMXBL|56=user|34=56|52=19700101-00:00:00.000|262=123456|268=3|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=63.7|290=1|279=1|269=0|55=XYZ|" +
                "48=349857|22=8|207=LMAX|270=0.00009|271=64.6|290=2|279=1|269=1|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=23.2|290=1|10=244|" +
                "8=FI");

        ByteBuffer inputStream1 = ByteBuffer.wrap(part1);

        final byte[] part2 = FixMessageUtil.convertFixControlCharacters(
            "X.4.2|9=140|35=X|49=LMXBL|56=user|34=57|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=67.3|290=2|10=034|" +
                "8=FIX.4.2|9=140|35=X|49=LMXBL|56=user|34=59|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=73.7|290=2|10=037|");

        ByteBuffer inputStream2 = ByteBuffer.wrap(part2);

        final List<byte[]> messages = new ArrayList<byte[]>();

        MessageParserCallback callback = new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                messages.add(Arrays.copyOf(buffer, length));
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
        parser.parse(inputStream2);

        assertThat(messages.size(), is(3));
    }

    @Test
    public void shouldParseFragmentedMessageWhichHasIncompleteMessagePrefixAtEnd()
    {
        final byte[] part1 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=279|35=X|49=LMXBL|56=user|34=56|52=19700101-00:00:00.000|262=123456|268=3|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=63.7|290=1|279=1|269=0|55=XYZ|" +
                "48=349857|22=8|207=LMAX|270=0.00009|271=64.6|290=2|279=1|269=1|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=23.2|290=1|10=244|" +
                "8=FI");

        final byte[] part2 = FixMessageUtil.convertFixControlCharacters(
            "X.4.2|9=140|35=X|49=LMXBL|56=user|34=57|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=67.3|290=2|10=034|8");

        final byte[] part3 = FixMessageUtil.convertFixControlCharacters(
            "=FIX.4.2|9=140|35=X|49=LMXBL|56=user|34=59|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=73.7|290=2|10=037|");

        final ByteBuffer inputStream1 = ByteBuffer.wrap(part1);
        final ByteBuffer inputStream2 = ByteBuffer.wrap(part2);
        final ByteBuffer inputStream3 = ByteBuffer.wrap(part3);

        final List<byte[]> messages = new ArrayList<byte[]>();

        final MessageParserCallback callback = createMessageParserCallback(
            new MyMessageParserCallbackTestFactory(messages));
        final ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream1);
        parser.parse(inputStream2);
        parser.parse(inputStream3);

        assertThat(messages.size(), is(3));
    }

    @Test
    public void shouldParseFragmentedMessageWhichHasIncompleteEnd()
    {
        final byte[] part1 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=279|35=X|49=LMXBL|56=user|34=56|52=19700101-00:00:00.000|262=123456|268=3|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=63.7|290=1|279=1|269=0|55=XYZ|" +
                "48=349857|22=8|207=LMAX|270=0.00009|271=64.6|290=2|279=1|269=1|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=23.2|290=1|10=244|" +
                "8=FI");

        final byte[] part2 = FixMessageUtil.convertFixControlCharacters(
            "X.4.2|9=140|35=X|49=LMXBL|56=user|34=57|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=67.3|290=2|10=");

        final byte[] part3 = FixMessageUtil.convertFixControlCharacters(
            "034|8=FIX.4.2|9=140|35=X|49=LMXBL|56=user|34=59|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=73.7|290=2|10=037|");

        final ByteBuffer inputStream1 = ByteBuffer.wrap(part1);
        final ByteBuffer inputStream2 = ByteBuffer.wrap(part2);
        final ByteBuffer inputStream3 = ByteBuffer.wrap(part3);

        final List<byte[]> messages = new ArrayList<byte[]>();

        final MessageParserCallback callback = createMessageParserCallback(new MyMessageParserCallbackTestFactory(messages));
        final ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream1);
        parser.parse(inputStream2);
        parser.parse(inputStream3);

        assertThat(messages.size(), is(3));
    }


    @Test
    public void shouldParseMessageSegmentContainingNeitherCompleteStartPrefixNorEndSuffix() throws Exception
    {
        final byte[] part1 = FixMessageUtil.convertFixControlCharacters("8=FIX.");
        final byte[] part2 = FixMessageUtil.convertFixControlCharacters("4.2|9=");
        final byte[] part3 = FixMessageUtil.convertFixControlCharacters("208|10");

        // this segment should be detected as being the boundary between messages, even though
        // it doesnt contain enough data to be identified as either a message start nor a message end
        final byte[] part4 = FixMessageUtil.convertFixControlCharacters("=067|8=");

        final byte[] part5 = FixMessageUtil.convertFixControlCharacters("FIX.4.2");
        final byte[] part6 = FixMessageUtil.convertFixControlCharacters("|9=140|");
        final byte[] part7 = FixMessageUtil.convertFixControlCharacters("|290=2|");
        final byte[] part8 = FixMessageUtil.convertFixControlCharacters("10=027|");

        final ByteBuffer inputStream1 = ByteBuffer.wrap(part1);
        final ByteBuffer inputStream2 = ByteBuffer.wrap(part2);
        final ByteBuffer inputStream3 = ByteBuffer.wrap(part3);
        final ByteBuffer inputStream4 = ByteBuffer.wrap(part4);
        final ByteBuffer inputStream5 = ByteBuffer.wrap(part5);
        final ByteBuffer inputStream6 = ByteBuffer.wrap(part6);
        final ByteBuffer inputStream7 = ByteBuffer.wrap(part7);
        final ByteBuffer inputStream8 = ByteBuffer.wrap(part8);

        final List<byte[]> messages = new ArrayList<byte[]>();

        final MessageParserCallback callback = createMessageParserCallback(new MyMessageParserCallbackTestFactory(messages));
        final ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        parser.parse(inputStream1);
        parser.parse(inputStream2);
        parser.parse(inputStream3);
        parser.parse(inputStream4);
        parser.parse(inputStream5);
        parser.parse(inputStream6);
        parser.parse(inputStream7);
        parser.parse(inputStream8);

        assertThat(messages.size(), is(2));

    }

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

    private static MessageParserCallback createMessageParserCallback(final MessageParserCallbackTestFactory impl)
    {
        return new MessageParserCallback()
        {
            @Override
            public void onMessage(final byte[] buffer, final int offset, final int length)
            {
                impl.onMessage(buffer, offset, length);
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
    }

    private static class MyMessageParserCallbackTestFactory implements MessageParserCallbackTestFactory
    {
        private final List<byte[]> messages;

        MyMessageParserCallbackTestFactory(final List<byte[]> messages)
        {
            this.messages = messages;
        }

        @Override
        public void onMessage(final byte[] buffer, final int offset, final int length)
        {
            messages.add(Arrays.copyOfRange(buffer, offset, offset + length));
        }
    }
}
