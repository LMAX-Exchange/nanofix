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

package com.lmax.nanofix.outgoing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;

import com.lmax.nanofix.transport.ConnectionObserver;
import com.lmax.nanofix.transport.TransportClosedException;

public class OutboundMessageHandler {

    private final ConnectionObserver connectionObserver;
    private volatile WritableByteChannel writableByteChannel;

    public OutboundMessageHandler(final ConnectionObserver connectionObserver) {
        this.connectionObserver = connectionObserver;
    }

    public void send(FixMessage message) {
        final byte[] bytes = message.toFixString().getBytes();
        sendBytes(bytes);
    }

    public void send(Collection<FixMessage> messages) {
        int totalBytes = 0;
        for (final FixMessage message : messages) {
            totalBytes += message.toFixString().getBytes().length;
        }
        byte[] bytes = new byte[totalBytes];

        int nextIndex = 0;
        for (final FixMessage message : messages) {
            byte[] from = message.toFixString().getBytes();
            int bytesToCopy = from.length;
            System.arraycopy(from, 0, bytes, nextIndex, bytesToCopy);
            nextIndex += bytesToCopy;
        }

        sendBytes(bytes);
    }

    public void send(final String message) {
        sendBytes(message.getBytes());
    }

    public void sendBytes(final byte[] bytes) {
        if (writableByteChannel == null) {
            throw new RuntimeException("Writable Byte Channel not initialized. Is the socket open? You can wait for the socket to be open by calling fixClient.awaitConnection");
        }
        try {
            writableByteChannel.write(ByteBuffer.wrap(bytes));
        } catch (ClosedChannelException e) {
            connectionObserver.connectionClosed();
            throw new TransportClosedException("Unable to write to channel", e);
        } catch (IOException e) {
            try {
                writableByteChannel.close();
                connectionObserver.connectionClosed();
                throw new TransportClosedException("Unable to write to channel", e);
            } catch (IOException e1) {
                //Don't care
            }
        }
    }


    public void initialiseOutboundChannel(final WritableByteChannel writableByteChannel) {
        this.writableByteChannel = writableByteChannel;
    }
}
