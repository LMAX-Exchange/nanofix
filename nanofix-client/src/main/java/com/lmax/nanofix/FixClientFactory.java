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

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.nanofix.concurrent.NamedThreadFactory;
import com.lmax.nanofix.concurrent.ThreadBlocker;
import com.lmax.nanofix.incoming.ByteChannelReader;
import com.lmax.nanofix.incoming.FixMessagePublisher;
import com.lmax.nanofix.incoming.FixMessageStreamFactory;
import com.lmax.nanofix.incoming.FixStreamMessageParser;
import com.lmax.nanofix.incoming.FixTagParser;
import com.lmax.nanofix.incoming.RawFixMessageHandler;
import com.lmax.nanofix.outgoing.OutboundMessageHandler;
import com.lmax.nanofix.transport.AsyncTcpSocketFactory;
import com.lmax.nanofix.transport.PublishingConnectionObserver;
import com.lmax.nanofix.transport.SocketFactory;
import com.lmax.nanofix.transport.TcpTransport;
import com.lmax.nanofix.transport.Transport;
import com.lmax.nanofix.transport.TransportConfigImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FixClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FixClientFactory.class);

    static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            LOGGER.error("Uncaught Exception thrown in thread: " + thread.getName(), throwable);
        }
    };

    private FixClientFactory() {
    }

    /**
     * Max Fix Message size
     */
    private static final int MAX_MESSAGE_SIZE = 2000;

    /**
     * Create an initiating fix client that will connect to the host on connect()
     *
     * @param host hostname of server to connect to.
     * @param port tcp port number int between 0 and 65535
     */
    public static FixClient createFixClient(final String host, final int port) {
        return createFixClient(new InetSocketAddress(host, port), new SystemConfig(false));
    }

    /**
     * Create an listening fix client that will listen for inbound tcp connections on port
     *
     * @param port tcp port number int between 0 and 65535
     */
    public static FixClient createFixClient(final int port) {
        return createFixClient(new InetSocketAddress(port), new SystemConfig(false));
    }

    /**
     * Create a fix client
     *
     * @param host         to bind to if listening for a connection or host to connect to when attempting to connect.
     * @param port         tcp port number int between 0 and 65535
     * @param systemConfig additional NanoFix configuration
     */
    public static FixClient createFixClient(final String host, final int port, final SystemConfig systemConfig) {
        return createFixClient(new InetSocketAddress(host, port), systemConfig);
    }

    public static FixClient createFixClient(final SocketFactory socketFactory) {
        final PublishingConnectionObserver publishingTransportObserver = new PublishingConnectionObserver();
        final TcpTransport transport = new TcpTransport(publishingTransportObserver, null, socketFactory, new TransportConfigImpl(false));
        publishingTransportObserver.addObserver(transport);
        return buildFixClient(transport, publishingTransportObserver, MAX_MESSAGE_SIZE);
    }

    /**
     * Build an initiating or listening {@link FixClient} based on the values contained in {@link FixClientConfiguration}
     *
     * @param fixClientConfiguration Contains configuration that determines the type of {@link FixClient}
     */
    public static FixClient createFixClient(final FixClientConfiguration fixClientConfiguration) {
        final InetSocketAddress socketAddress = fixClientConfiguration.getSocketAddress();
        final SocketFactory socketFactory = fixClientConfiguration.getSocketFactory();
        final SystemConfig systemConfig = fixClientConfiguration.getSystemConfig();
        final int maxMessageSize = fixClientConfiguration.getMaxMessageSize();

        final PublishingConnectionObserver publishingTransportObserver = new PublishingConnectionObserver();
        final TcpTransport transport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, systemConfig);
        publishingTransportObserver.addObserver(transport);
        return buildFixClient(transport, publishingTransportObserver, maxMessageSize);
    }

    private static FixClient createFixClient(final InetSocketAddress socketAddress, final SystemConfig systemConfig) {
        final PublishingConnectionObserver publishingTransportObserver = new PublishingConnectionObserver();

        final ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("InboundConnection", true, UNCAUGHT_EXCEPTION_HANDLER));
        final AsyncTcpSocketFactory asyncTcpSocketFactory = new AsyncTcpSocketFactory(executorService);
        final TcpTransport transport = new TcpTransport(publishingTransportObserver, socketAddress, asyncTcpSocketFactory, systemConfig);
        publishingTransportObserver.addObserver(transport);
        return buildFixClient(transport, publishingTransportObserver, MAX_MESSAGE_SIZE);
    }

    private static FixClient buildFixClient(final Transport transport, final PublishingConnectionObserver publishingTransportObserver, final int maxMessageSize) {
        final FixStreamMessageParser fixStreamMessageParser = new FixStreamMessageParser(maxMessageSize);
        final ThreadBlocker messageConsumingThreadBlocker = new ThreadBlocker();
        final FixMessagePublisher fixMessagePublisher = new FixMessagePublisher();
        fixStreamMessageParser.initialise(new RawFixMessageHandler(new FixTagParser(new FixMessageStreamFactory(fixMessagePublisher))));
        final OutboundMessageHandler outboundMessageSender = new OutboundMessageHandler(publishingTransportObserver);

        final ByteChannelReader inputStreamReader = new ByteChannelReader(fixStreamMessageParser, messageConsumingThreadBlocker, publishingTransportObserver);
        final ExecutorService channelReaderExecutorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("channelReader", true, UNCAUGHT_EXCEPTION_HANDLER));
        final ChannelInitializer channelInitializer = new ChannelInitializer(transport, inputStreamReader, outboundMessageSender, channelReaderExecutorService);
        publishingTransportObserver.addObserver(channelInitializer);

        return new FixClient(fixMessagePublisher, channelInitializer, transport, new FixSession(outboundMessageSender), messageConsumingThreadBlocker);
    }
}
