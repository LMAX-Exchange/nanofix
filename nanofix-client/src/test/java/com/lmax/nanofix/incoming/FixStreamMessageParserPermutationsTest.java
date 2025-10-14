package com.lmax.nanofix.incoming;

import com.google.common.primitives.Bytes;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


import static java.util.Arrays.asList;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FixStreamMessageParserPermutationsTest {

    private static final int MAX_MESSAGE_SIZE = 2048;

    private static final byte[] MESSAGE_1 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=208|35=X|49=LMXBL|56=user|34=52|52=19700101-00:00:00.000|262=123456|268=2|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=55.9|290=2|279=1|269=1|" +
            "55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=35|290=1|10=067|");
    private static final byte[] MESSAGE_2 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=140|35=X|49=LMXBL|56=user|34=53|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=60.7|290=2|10=027|");
    private static final byte[] MESSAGE_3 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=139|35=X|49=LMXBL|56=user|34=54|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=52.3|290=1|10=232|");
    private static final byte[] MESSAGE_4 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=139|35=X|49=LMXBL|56=user|34=55|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=1|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=72.6|290=2|10=240|");
    private static final byte[] MESSAGE_5 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=279|35=X|49=LMXBL|56=user|34=56|52=19700101-00:00:00.000|262=123456|268=3|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=63.7|290=1|279=1|269=0|" +
            "55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=64.6|290=2|279=1|269=1|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=23.2|290=1|10=244|");
    private static final byte[] MESSAGE_6 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=140|35=X|49=LMXBL|56=user|34=57|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=67.3|290=2|10=034|");
    private static final byte[] MESSAGE_7 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=278|35=X|49=LMXBL|56=user|34=58|52=19700101-00:00:00.000|262=123456|268=3|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=57.5|290=1|279=1|269=0|" +
            "55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=65.1|290=2|279=1|269=1|55=XYZ|48=349857|22=8|207=LMAX|270=0.0001|271=77.6|290=2|10=200|");
    private static final byte[] MESSAGE_8 = FixMessageUtil.convertFixControlCharacters(
            "8=FIX.4.2|9=140|35=X|49=LMXBL|56=user|34=59|52=19700101-00:00:00.000|262=123456|268=1|279=1|269=0|55=XYZ|48=349857|22=8|207=LMAX|270=0.00009|271=73.7|290=2|10=037|");

    private static final List<byte[]> ALL_MESSAGES = asList(MESSAGE_1, MESSAGE_2, MESSAGE_3, MESSAGE_4, MESSAGE_5, MESSAGE_6, MESSAGE_7, MESSAGE_8);
    private static final byte[] ALL_MESSAGE_BYTES = Bytes.concat(MESSAGE_1, MESSAGE_2, MESSAGE_3, MESSAGE_4, MESSAGE_5, MESSAGE_6, MESSAGE_7, MESSAGE_8);

    @Test
    public void parseAllMessagesWithRandomSegmentLengths() {
        final ByteBuffer bb = ByteBuffer.wrap(ALL_MESSAGE_BYTES);

        final int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            performIteration(bb, ALL_MESSAGES, new RandomBytesConsumer(1, 32));
        }
    }

    @Test
    public void parseMessagesOneByteAtATime() {
        final ByteBuffer bb = ByteBuffer.wrap(ALL_MESSAGE_BYTES);

        performIteration(bb, ALL_MESSAGES, new RandomBytesConsumer(1, 1));
    }

    @Test
    public void parseMessagesThreeBytesAtATime() {
        final ByteBuffer bb = ByteBuffer.wrap(ALL_MESSAGE_BYTES);

        performIteration(bb, ALL_MESSAGES, new RandomBytesConsumer(3, 3));
    }

    @Test
    public void parseMessagesWithRandomEmptySegments() {
        final ByteBuffer bb = ByteBuffer.wrap(ALL_MESSAGE_BYTES);

        final int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            performIteration(bb, ALL_MESSAGES, new RandomBytesConsumer(0, 8));
        }
    }

    @Test
    public void messageBoundaryConditions() {
        final byte[] msgBytes = Bytes.concat(MESSAGE_1, MESSAGE_2, MESSAGE_3);
        final ByteBuffer bb = ByteBuffer.wrap(msgBytes);
        final List<byte[]> messages = asList(MESSAGE_1, MESSAGE_2, MESSAGE_3);

        final String wholeString = FixMessageUtil.convertFixControlCharacters(msgBytes);
        final int startOfFirstMessage = 0;
        final int offsetOfFirstMessageChecksum = wholeString.indexOf("|10=");
        final int offsetOfSecondMessageChecksum = wholeString.indexOf("|10=", offsetOfFirstMessageChecksum + 1);
        final int offsetOfLastMessageChecksum = wholeString.indexOf("|10=", offsetOfSecondMessageChecksum + 1);

        for (int i = 1; i <= 6; i++) {  // possible fragmentations of "8=FIX." prefix of message 1
            for (int j = 1; j <= 15; j++) {  // possible fragmentations of "|10=067|8=FIX." block between messages 1 and 2
                for (int k = 1; k <= 15; k++) {  // possible fragmentations of "|10=067|8=FIX." block between messages 2 and 3
                    for (int l = 1; l <= 9; l++) {  // possible fragmentations of "|10=067|" suffix of message 3
                        final int consumeFromStart = i;
                        final int consumeFromBoundaryMsg1Msg2 = j;
                        final int consumeFromBoundaryMsg2Msg3 = k;
                        final int consumeFromMsg3Checksum = l;
                        performIteration(bb, messages, fromPosition -> {
                            if (fromPosition == startOfFirstMessage) {
                                return consumeFromStart;
                            }
                            if (fromPosition < offsetOfFirstMessageChecksum) {
                                return offsetOfFirstMessageChecksum - fromPosition + consumeFromBoundaryMsg1Msg2;
                            }
                            if (fromPosition < offsetOfSecondMessageChecksum) {
                                return offsetOfSecondMessageChecksum - fromPosition + consumeFromBoundaryMsg2Msg3;
                            }
                            if (fromPosition < offsetOfLastMessageChecksum) {
                                return offsetOfLastMessageChecksum - fromPosition + consumeFromMsg3Checksum;
                            }
                            return wholeString.length() - fromPosition;
                        });
                    }
                }
            }
        }
    }

    @SuppressWarnings("checkstyle:regexpsinglelinejava")
    private void performIteration(final ByteBuffer bb, final List<byte[]> expectedMessages, final BytesToConsumeCalculator bytesConsumer) {
        final List<byte[]> messages = new ArrayList<>();
        final List<Integer> offsetsUsedForIteration = new ArrayList<>();

        final MessageParserCallback callback = new MessageParserCallback() {
            @Override
            public void onMessage(byte[] buffer, int offset, int length) {
                messages.add(Arrays.copyOfRange(buffer, offset, offset + length));
            }

            @Override
            public void onTruncatedMessage() {
            }

            @Override
            public void onParseError(String error) {
            }
        };

        final ByteStreamMessageParser parser = new FixStreamMessageParser(MAX_MESSAGE_SIZE);
        parser.initialise(callback);

        try {
            int position = 0;
            while (bb.position() < bb.capacity()) {
                final int bytesToConsume = Math.min(bytesConsumer.getBytesToConsume(position), bb.capacity() - bb.position());
                final int newLimit = position + bytesToConsume;

                bb.position(position);
                bb.limit(newLimit);
                offsetsUsedForIteration.add(newLimit);

                parser.parse(bb);
                position += bytesToConsume;
            }

            assertThat(messages.size(), is(expectedMessages.size()));
            assertThat(messages, containsAllItems(expectedMessages));
        } catch (AssertionError | RuntimeException e) {
            handleException(bb, offsetsUsedForIteration, e);
            throw e;
        }
        bb.clear();
    }

    private void handleException(ByteBuffer bb, List<Integer> offsetsUsedForIteration, Throwable e) {
        System.out.println("Failed! Message segments used for test: ");
        int start = 0;
        for (Integer end : offsetsUsedForIteration) {
            System.out.println(FixMessageUtil.convertFixControlCharacters(Arrays.copyOfRange(bb.array(), start, end)));
            start = end;
        }
    }

    private Matcher<? super List<byte[]>> containsAllItems(final List<byte[]> expectedMessages) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(List<byte[]> actualMessages) {
                if (actualMessages.size() != expectedMessages.size()) {
                    return false;
                }

                Iterator<byte[]> it1 = expectedMessages.iterator(), it2 = actualMessages.iterator();
                while (it1.hasNext() && it2.hasNext()) {
                    byte[] expected = it1.next();
                    byte[] actual = it2.next();

                    if (!Arrays.equals(expected, actual)) {
                        return false;
                    }
                }
                return !it1.hasNext() && !it2.hasNext();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Expecting list containing ")
                        .appendValue(expectedMessages.size())
                        .appendText(" items: ")
                        .appendValue(expectedMessages);
            }
        };
    }

    private interface BytesToConsumeCalculator {
        int getBytesToConsume(int fromPosition);
    }

    private static class RandomBytesConsumer implements BytesToConsumeCalculator {
        private final int minBytesPerSegment;
        private final int maxBytesPerSegment;

        RandomBytesConsumer(final int minBytesPerSegment, final int maxBytesPerSegment) {
            this.minBytesPerSegment = minBytesPerSegment;
            this.maxBytesPerSegment = maxBytesPerSegment;
        }

        @Override
        public int getBytesToConsume(final int fromPosition) {
            return ThreadLocalRandom.current().nextInt(minBytesPerSegment, maxBytesPerSegment + 1);
        }
    }
}