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

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.lmax.nanofix.concurrent.ThreadBlocker;
import com.lmax.nanofix.incoming.FixMessageHandler;
import com.lmax.nanofix.incoming.FixMessagePublisher;
import com.lmax.nanofix.outgoing.FixMessage;
import com.lmax.nanofix.transport.ConnectionObserver;
import com.lmax.nanofix.transport.TransportOperations;

/**
 * The main class used to interact with the fix client.
 */
public class FixClient {
    private final FixMessagePublisher fixMessagePublisher;
    private final ChannelInitializer channelInitializer;
    private final TransportOperations transportOps;
    private final FixSession fixSession;
    private final ThreadBlocker messageConsumingThreadBlocker;

    FixClient(final FixMessagePublisher fixMessagePublisher, final ChannelInitializer channelInitializer, final TransportOperations transportOps,
              final FixSession fixSession, final ThreadBlocker messageConsumingThreadBlocker) {
        this.fixMessagePublisher = fixMessagePublisher;
        this.channelInitializer = channelInitializer;
        this.transportOps = transportOps;
        this.fixSession = fixSession;
        this.messageConsumingThreadBlocker = messageConsumingThreadBlocker;
    }

    /**
     * Sends a collection of FIX messages.
     *
     * @param messages a collection of messages.
     */
    public void send(final Collection<FixMessage> messages) {
        fixSession.send(messages);
    }

    /**
     * Sends a single FIX message.
     *
     * @param message a FIX messages.
     */
    public void send(final FixMessage message) {
        fixSession.send(message);
    }

    /**
     * Sends an arbitrary string.
     *
     * @param message a FIX messages.
     */
    public void send(final String message) {
        fixSession.send(message);
    }

    /**
     * Sends a array of bytes string.
     *
     * @param bytes a FIX messages.
     */
    public void send(final byte[] bytes) {
        fixSession.send(bytes);
    }

    /**
     * Initiates a TCP connection with the remote host specified on construction..
     */
    public void connect() {
        transportOps.connect();
        try {
            channelInitializer.awaitConnection();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for channel initialization");
        }
    }

    /**
     * Initiates a TCP connection with the remote host specified on construction..
     *
     * @param timeout the time to wait for a connection to be established
     * @param units   the {@link TimeUnit unit} of the timeout
     */
    public boolean connect(final long timeout, final TimeUnit units) {
        transportOps.connect();
        try {
            return channelInitializer.awaitConnection(timeout, units);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for channel initialization");
        }
    }

    /**
     * Waits for an inbound TCP connection on the interface and port specified on construction.
     */
    public void listen() {
        transportOps.listen();
    }

    /**
     * Stops listening on the TCP socket.
     */
    public void stopListening() {
        transportOps.stopListening();
    }

    /**
     * Terminates the TCP connection with a TCP ( RST, ACK).
     */
    public void killSocket() {
        transportOps.killSocket();
    }

    /**
     * Attempts to gracefully close the TCP socket.
     */
    public void close() {
        transportOps.close();
    }

    /**
     * Checks if the transport is connected.
     */
    public boolean isConnected() {
        return transportOps.isConnected();
    }

    /**
     * Blocks until a connection is established.
     */
    public void awaitConnection() throws InterruptedException {
        channelInitializer.awaitConnection();
    }

    /**
     * Registers an observer that will be notified on connect and disconnect events.
     *
     * @param connectionObserver receives connection events.
     */
    public void registerTransportObserver(ConnectionObserver connectionObserver) {
        transportOps.registerTransportObserver(connectionObserver);
    }

    /**
     * Removes a registered observer that will be notified on connect and disconnect events.
     *
     * @param connectionObserver receives connection events.
     */
    public void unregisterTransportObserver(ConnectionObserver connectionObserver) {
        transportOps.unregisterTransportObserver(connectionObserver);
    }

    /**
     * Subscribe to all inbound messages.
     */
    public void subscribeToAllMessages(final FixMessageHandler fixMessageHandler) {
        fixMessagePublisher.subscribeToAllMessages(fixMessageHandler);
    }

    /**
     * Stop reading messages from the transport's byte channel
     */
    public void pauseMessageConsumer() {
        messageConsumingThreadBlocker.pause();
    }

    /**
     * Resume reading messages from the transport's byte channel
     */
    public void resumeMessageConsumer() {
        messageConsumingThreadBlocker.resume();
    }
}
