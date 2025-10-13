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

package com.lmax.nanofix;

import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import com.lmax.nanofix.incoming.ByteChannelReader;
import com.lmax.nanofix.outgoing.OutboundMessageHandler;
import com.lmax.nanofix.transport.ConnectionObserver;
import com.lmax.nanofix.transport.Transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class ChannelInitializer implements ConnectionObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelInitializer.class);
    private final Transport transport;
    private final ByteChannelReader inputStreamReader;
    private final OutboundMessageHandler outboundMessageSender;
    private final Executor channelReaderExecutorService;

    private volatile CountDownLatch countDownLatch = new CountDownLatch(1);


    ChannelInitializer(final Transport transport, final ByteChannelReader inputStreamReader, final OutboundMessageHandler outboundMessageSender, Executor channelReaderExecutorService) {
        this.channelReaderExecutorService = channelReaderExecutorService;
        this.transport = transport;
        this.inputStreamReader = inputStreamReader;
        this.outboundMessageSender = outboundMessageSender;
    }

    @Override
    public void connectionEstablished() {
        outboundMessageSender.initialiseOutboundChannel(transport.getWritableByteChannel());
        channelReaderExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                final ReadableByteChannel readableByteChannel = transport.getReadableByteChannel();
                try {
                    inputStreamReader.blockingStart(readableByteChannel);
                } catch (RuntimeException e) {
                    LOGGER.error("Exception thrown while reading from stream, channel: " + readableByteChannel, e);
                }
            }
        });
        countDownLatch.countDown();
    }

    public void awaitConnection() throws InterruptedException {
        countDownLatch.await();
    }

    public boolean awaitConnection(final long timeout, final TimeUnit units) throws InterruptedException {
        return countDownLatch.await(timeout, units);
    }

    @Override
    public void connectionClosed() {
        countDownLatch.countDown();
        countDownLatch = new CountDownLatch(1);
    }
}
