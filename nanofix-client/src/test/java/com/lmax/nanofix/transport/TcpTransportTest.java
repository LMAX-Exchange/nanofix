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

package com.lmax.nanofix.transport;

import java.net.InetSocketAddress;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class TcpTransportTest {
    private final PublishingConnectionObserver publishingTransportObserver = mock(PublishingConnectionObserver.class);
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final DelegatingServerSocketChannel serverSocketChannel = mock(DelegatingServerSocketChannel.class);
    private final InetSocketAddress socketAddress = new InetSocketAddress("host", 222);

    @Test
    public void shouldBindToSocketOnListen() {
        given(socketFactory.bind(socketAddress)).willReturn(serverSocketChannel);

        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));
        tcpTransport.listen();

        verify(socketFactory).createSocketOnIncomingConnection(eq(serverSocketChannel), any(SocketFactory.SocketEstablishedCallback.class));
    }

    @Test
    public void shouldRemoveBindOnWhenTheConnectionClosesByDefault() throws Exception {
        given(socketFactory.bind(socketAddress)).willReturn(serverSocketChannel);

        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));
        tcpTransport.listen();
        tcpTransport.connectionClosed();

        verify(socketFactory).createSocketOnIncomingConnection(eq(serverSocketChannel), any(SocketFactory.SocketEstablishedCallback.class));
        verify(serverSocketChannel).close();
    }

    @Test
    public void shouldStayBoundOnWhenTheConnectionClosesIfConfiguredToMaintainBind() throws Exception {
        given(socketFactory.bind(socketAddress)).willReturn(serverSocketChannel);

        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(true));
        tcpTransport.listen();
        tcpTransport.connectionClosed();

        verify(socketFactory, times(2)).createSocketOnIncomingConnection(eq(serverSocketChannel), any(SocketFactory.SocketEstablishedCallback.class));
        verify(serverSocketChannel, never()).close();
    }

    @Test
    public void shouldCreateAnOutboundSocketWhenConnecting() {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));
        tcpTransport.connect();

        verify(socketFactory).createSocketOnOutgoingConnection(eq(socketAddress), any(SocketFactory.SocketEstablishedCallback.class));
    }

    @Test
    public void shouldNotAcceptTryAcceptNewConnectionsWhenInitiatingOutboundConnections() {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(true));
        tcpTransport.connect();
        tcpTransport.connectionClosed();

        verify(socketFactory).createSocketOnOutgoingConnection(eq(socketAddress), any(SocketFactory.SocketEstablishedCallback.class));
    }
}
