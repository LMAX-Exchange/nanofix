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

package com.lmax.nanofix.integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.lmax.nanofix.FixClient;
import com.lmax.nanofix.FixClientFactory;
import com.lmax.nanofix.fields.MsgType;
import com.lmax.nanofix.incoming.FixMessage;
import com.lmax.nanofix.incoming.FixMessageHandler;
import com.lmax.nanofix.integration.fixture.IntegrationSocketFactory;
import com.lmax.nanofix.outgoing.FixMessageBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class ReceivingFixMessagesTest {

    private static final String EXPECTED_MSG_1 = "8=FIX.4.2\u00019=65\u000135=A\u000149=SERVER\u000156=CLIENT\u000134=177\u000152=20090107-18:15:16\u000198=0\u0001108=30\u000110=062\u0001";
    private static final String EXPECTED_MSG_2 = "8=FIX.4.2\u00019=65\u000135=A\u000149=SERVER\u000156=CLIENT\u000134=177\u000134=178\u000152=20090107-18:15:16\u000198=0\u0001108=30" +
                                                 "\u000110=062\u0001";
    private CountDownLatch countDownLatch;
    private ReadableByteChannel readableByteChannel;
    private WritableByteChannel writableByteChannel;
    private ByteArrayOutputStream byteArrayOutputStream;

    @Before
    public void setUp() throws Exception {
        byteArrayOutputStream = new ByteArrayOutputStream();
        writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        countDownLatch = new CountDownLatch(1);
    }

    @Test
    public void shouldGetFixMessages() throws Exception {
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(EXPECTED_MSG_1.getBytes()));
        final FixClient fixClient = buildFixClient();
        fixClient.subscribeToAllMessages(new AssertingFixMessageHandler(EXPECTED_MSG_1));
        fixClient.connect();
        final boolean await = countDownLatch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(await);
    }

    @Test
    public void shouldGetFixMessageWithDuplicateKey() throws Exception {
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(EXPECTED_MSG_2.getBytes()));
        final FixClient fixClient = buildFixClient();
        fixClient.subscribeToAllMessages(new AssertingFixMessageHandler(EXPECTED_MSG_2));
        fixClient.connect();
        final boolean await = countDownLatch.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(await);
    }

    @Test
    public void shouldSendFixMessage() throws Exception {
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(new byte[0]));
        final FixClient fixClient = buildFixClient();
        fixClient.connect();
        final FixMessageBuilder fixMessageBuilder = new FixMessageBuilder();
        fixMessageBuilder.messageType(MsgType.MARKET_DATA_SNAPSHOT).refSeqNum(3).refSeqNum(7);

        fixClient.send(fixMessageBuilder.build());
        Assert.assertThat(byteArrayOutputStream.toString(), is("8=FIX.4.4\u00019=15\u000135=W\u000145=3\u000145=7\u000110=179\u0001"));
    }

    private final class AssertingFixMessageHandler implements FixMessageHandler {
        private final String expectedMessage;

        private AssertingFixMessageHandler(final String expectedMessage) {
            this.expectedMessage = expectedMessage;
        }

        @Override
        public void onFixMessage(final FixMessage fixMessage) {
            if (expectedMessage.equals(fixMessage.toFixString())) {
                countDownLatch.countDown();
            } else {
                throw new RuntimeException("Expected: '" + expectedMessage + "'  message does not match actual: " + fixMessage.toFixString());
            }
        }
    }

    private FixClient buildFixClient() {
        return FixClientFactory.createFixClient(new IntegrationSocketFactory(readableByteChannel, writableByteChannel));
    }
}
