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
import java.util.concurrent.Executors;

import com.lmax.nanofix.concurrent.NamedThreadFactory;
import com.lmax.nanofix.transport.AsyncTcpSocketFactory;
import com.lmax.nanofix.transport.SocketFactory;

public final class FixClientConfiguration {
    private final InetSocketAddress socketAddress;
    private SystemConfig systemConfig = new SystemConfig(false);
    private SocketFactory socketFactory = new AsyncTcpSocketFactory(Executors.newSingleThreadExecutor(new NamedThreadFactory("InboundConnection", true, FixClientFactory.UNCAUGHT_EXCEPTION_HANDLER)));
    private int maxMessageSize = 2000;

    public static FixClientConfiguration createListeningFixClient(final int port) {
        return new FixClientConfiguration(port);
    }

    public static FixClientConfiguration createInitiatingFixClient(final String host, final int port) {
        return new FixClientConfiguration(host, port);
    }

    /**
     * Listening fix client that will listen for inbound tcp connections on port
     *
     * @param port tcp port number int between 0 and 65535
     */
    private FixClientConfiguration(final int port) {
        this.socketAddress = new InetSocketAddress(port);
    }

    /**
     * Initiating fix client will connect to the host on connect()
     *
     * @param host hostname of server to connect to.
     * @param port tcp port number int between 0 and 65535
     */
    private FixClientConfiguration(final String host, final int port) {
        this.socketAddress = new InetSocketAddress(host, port);
    }

    /**
     * @param stayListening when set to true, Nanofix will continue listening for additional inbound connections
     *                      after an existing connection is dropped.
     */
    public FixClientConfiguration stayListening(final boolean stayListening) {
        this.systemConfig = new SystemConfig(stayListening);
        return this;
    }

    public FixClientConfiguration socketFactory(final SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return this;
    }

    /**
     * @param maxMessageSize Size of the underlying buffer to be used by Nanofix (in bytes)
     */
    public FixClientConfiguration maxMessageSize(final int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public SocketFactory getSocketFactory() {
        return socketFactory;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }
}
