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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.log4j.Logger;

public class TcpTransport implements Transport, ConnectionObserver
{

    private static final Logger LOGGER = Logger.getLogger(TcpTransport.class);

    private final PublishingConnectionObserver publishingTransportObserver;
    private final TransportConfig transportConfig;

    private volatile SocketChannel socketChannel;
    private volatile DelegatingServerSocketChannel serverSocketChannel;

    private InetSocketAddress socketAddress;
    private SocketFactory asyncTcpSocketFactory;

    public TcpTransport(final PublishingConnectionObserver publishingTransportObserver, final InetSocketAddress socketAddress,
                        final SocketFactory asyncTcpSocketFactory, final TransportConfig transportConfig)
    {
        this.publishingTransportObserver = publishingTransportObserver;
        this.socketAddress = socketAddress;
        this.asyncTcpSocketFactory = asyncTcpSocketFactory;

        this.transportConfig = transportConfig;
    }

    @Override
    public void connect()
    {
        asyncTcpSocketFactory.createSocketOnOutgoingConnection(socketAddress, new SocketFactory.SocketEstablishedCallback()
        {
            @Override
            public void onSocketEstablished(final SocketChannel socketChannel)
            {
                TcpTransport.this.socketChannel = socketChannel;
                publishingTransportObserver.connectionEstablished();
            }
        });

    }

    @Override
    public void listen()
    {
        serverSocketChannel = asyncTcpSocketFactory.bind(socketAddress);
        acceptNewConnection();
    }

    private void acceptNewConnection()
    {
        asyncTcpSocketFactory.createSocketOnIncomingConnection(serverSocketChannel, new SocketFactory.SocketEstablishedCallback()
        {
            @Override
            public void onSocketEstablished(final SocketChannel socketChannel)
            {
                TcpTransport.this.socketChannel = socketChannel;
                publishingTransportObserver.connectionEstablished();
            }
        });
    }

    @Override
    public void stopListening()
    {
        try
        {
            if (serverSocketChannel != null)
            {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to close listener socket", e);
        }
    }

    @Override
    public ReadableByteChannel getReadableByteChannel()
    {
        return socketChannel;

    }

    @Override
    public WritableByteChannel getWritableByteChannel()
    {
        return socketChannel;
    }

    @Override
    public void killSocket()
    {
        try
        {
            socketChannel.socket().setSoLinger(true, 0);
            socketChannel.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to kill socket", e);

        }
    }

    @Override
    public void close()
    {
        try
        {
            if (socketChannel != null)
            {
                socketChannel.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to close socket", e);
        }
    }

    @Override
    public boolean isConnected()
    {
        return socketChannel != null && socketChannel.isConnected();
    }

    @Override
    public void registerTransportObserver(final ConnectionObserver connectionObserver)
    {
        publishingTransportObserver.addObserver(connectionObserver);
    }

    @Override
    public void unregisterTransportObserver(final ConnectionObserver connectionObserver)
    {
        publishingTransportObserver.removeObserver(connectionObserver);
    }

    @Override
    public void connectionEstablished()
    {
    }

    @Override
    public void connectionClosed()
    {
        if (serverSocketChannel != null)
        {
            if (!transportConfig.shouldStayListening())
            {
                try
                {
                    serverSocketChannel.close();
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to stop listening on port", e);
                }
            }
            else
            {
                acceptNewConnection();
            }
        }
    }
}
