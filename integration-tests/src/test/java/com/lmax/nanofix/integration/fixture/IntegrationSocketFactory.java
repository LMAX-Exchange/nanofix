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

package com.lmax.nanofix.integration.fixture;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.nanofix.transport.DelegatingServerSocketChannel;
import com.lmax.nanofix.transport.SocketFactory;

public class IntegrationSocketFactory implements SocketFactory
{
    private ReadableByteChannel readableByteChannel;
    private WritableByteChannel writableByteChannel;
    final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public IntegrationSocketFactory(final ReadableByteChannel readableByteChannel, final WritableByteChannel writableByteChannel)
    {
        this.readableByteChannel = readableByteChannel;
        this.writableByteChannel = writableByteChannel;
    }

    @Override
    public DelegatingServerSocketChannel bind(final InetSocketAddress socketAddress)
    {
        return new StubServerSocketChannel();
    }

    @Override
    public void createSocketOnIncomingConnection(final DelegatingServerSocketChannel serverSocketChannel, final SocketEstablishedCallback socketEstablishedCallback)
    {
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    socketEstablishedCallback.onSocketEstablished(serverSocketChannel.accept());
                }
                catch (Exception e)
                {
                    System.err.println("Exception thrown: ");
                    e.printStackTrace(System.err);
                }
            }
        });
    }

    @Override
    public void createSocketOnOutgoingConnection(final InetSocketAddress socketAddress, final SocketEstablishedCallback socketEstablishedCallback)
    {
        final ByteChannel byteChannel = createByteChannel(readableByteChannel, writableByteChannel);
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    socketEstablishedCallback.onSocketEstablished(new StubSocketChannel(byteChannel));
                }
                catch (RuntimeException e)
                {
                    System.err.println("Exception thrown: ");
                    e.printStackTrace(System.err);
                }
            }
        });
    }

    private ByteChannel createByteChannel(final ReadableByteChannel readableByteChannel, final WritableByteChannel writableByteChannel)
    {
        return new ByteChannel()
        {
            @Override
            public int read(final ByteBuffer dst) throws IOException
            {
                return readableByteChannel.read(dst);
            }

            @Override
            public int write(final ByteBuffer src) throws IOException
            {
                return writableByteChannel.write(src);
            }

            @Override
            public boolean isOpen()
            {
                return true;
            }

            @Override
            public void close() throws IOException
            {
            }
        };
    }


    private static class StubSocketChannel extends SocketChannel
    {
        private final ByteChannel byteChannel;

        public StubSocketChannel(final ByteChannel byteChannel)
        {
            super(null);
            this.byteChannel = byteChannel;
        }

        @Override
        public SocketChannel bind(SocketAddress local) throws IOException
        {
            return null;
        }

        @Override
        public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException
        {
            return null;
        }

        @Override
        public <T> T getOption(SocketOption<T> name) throws IOException
        {
            return null;
        }

        @Override
        public Set<SocketOption<?>> supportedOptions()
        {
            return null;
        }

        @Override
        public SocketChannel shutdownInput() throws IOException
        {
            return null;
        }

        @Override
        public SocketChannel shutdownOutput() throws IOException
        {
            return null;
        }

        @Override
        public Socket socket()
        {
            return null;
        }

        @Override
        public boolean isConnected()
        {
            return true;
        }

        @Override
        public boolean isConnectionPending()
        {
            return false;
        }

        @Override
        public boolean connect(final SocketAddress remote) throws IOException
        {
            return true;
        }

        @Override
        public boolean finishConnect() throws IOException
        {
            return true;
        }

        @Override
        public SocketAddress getRemoteAddress() throws IOException
        {
            return null;
        }

        @Override
        public int read(final ByteBuffer dst) throws IOException
        {
            return byteChannel.read(dst);
        }

        @Override
        public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException
        {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int write(final ByteBuffer src) throws IOException
        {
            return byteChannel.write(src);
        }

        @Override
        public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException
        {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public SocketAddress getLocalAddress() throws IOException
        {
            return null;
        }

        @Override
        protected void implCloseSelectableChannel() throws IOException
        {
        }

        @Override
        protected void implConfigureBlocking(final boolean block) throws IOException
        {
        }
    }

    private class StubServerSocketChannel extends DelegatingServerSocketChannel
    {

        public StubServerSocketChannel()
        {
            super(null);
        }

        @Override
        public SocketChannel accept() throws IOException
        {
            return new StubSocketChannel(createByteChannel(readableByteChannel, writableByteChannel));
        }

        @Override
        public void close() throws IOException
        {
        }

    }
}
