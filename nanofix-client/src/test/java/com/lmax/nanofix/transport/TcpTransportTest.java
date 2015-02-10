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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JMock.class)
public class TcpTransportTest
{

    private Mockery mockery;
    private PublishingConnectionObserver publishingTransportObserver;
    private SocketFactory socketFactory;
    private DelegatingServerSocketChannel serverSocketChannel;
    private InetSocketAddress socketAddress;

    @Before
    public void setUp() throws Exception
    {
        mockery = new Mockery();
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
        publishingTransportObserver = mockery.mock(PublishingConnectionObserver.class);
        serverSocketChannel = mockery.mock(DelegatingServerSocketChannel.class);
        socketFactory = mockery.mock(SocketFactory.class);
        socketAddress = new InetSocketAddress("host", 222);
    }

    @Test
    public void shouldBindToSocketOnListen() throws Exception
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));

        mockery.checking(new Expectations()
        {
            {
                one(socketFactory).bind(socketAddress);
                will(returnValue(serverSocketChannel));
                one(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));
            }
        });
        tcpTransport.listen();
    }

    @Test
    public void shouldRemoveBindOnWhenTheConnectionClosesByDefault() throws Exception
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));

        mockery.checking(new Expectations()
        {
            {

                allowing(socketFactory).bind(socketAddress);
                will(returnValue(serverSocketChannel));
                allowing(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));

                one(serverSocketChannel).close();
            }
        });

        tcpTransport.listen();
        tcpTransport.connectionClosed();
    }

    @Test
    public void shouldStayBoundOnWhenTheConnectionClosesIfConfiguredToMaintainBind() throws Exception
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(true));

        mockery.checking(new Expectations()
        {
            {

                one(socketFactory).bind(socketAddress);
                will(returnValue(serverSocketChannel));
                one(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));

                never(serverSocketChannel).close();
                one(socketFactory).createSocketOnIncomingConnection(with(serverSocketChannel), with(any(SocketFactory.SocketEstablishedCallback.class)));

            }
        });

        tcpTransport.listen();
        tcpTransport.connectionClosed();
    }

    @Test
    public void shouldCreateAnOutboundSocketWhenConnecting() throws Exception
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(false));

        mockery.checking(new Expectations()
        {
            {
                one(socketFactory).createSocketOnOutgoingConnection(with(socketAddress), with(any(SocketFactory.SocketEstablishedCallback.class)));
            }
        });

        tcpTransport.connect();
    }

    @Test
    public void shouldNotAcceptTryAcceptNewConnectionsWhenInitiatingOutboundConnections() throws Exception
    {
        final TcpTransport tcpTransport = new TcpTransport(publishingTransportObserver, socketAddress, socketFactory, new TransportConfigImpl(true));

        mockery.checking(new Expectations()
        {
            {
                one(socketFactory).createSocketOnOutgoingConnection(with(socketAddress), with(any(SocketFactory.SocketEstablishedCallback.class)));
            }
        });

        tcpTransport.connect();
        tcpTransport.connectionClosed();
    }

}
