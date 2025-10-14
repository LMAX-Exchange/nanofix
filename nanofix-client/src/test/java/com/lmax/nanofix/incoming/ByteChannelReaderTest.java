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

package com.lmax.nanofix.incoming;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

import com.lmax.nanofix.concurrent.ThreadBlocker;
import com.lmax.nanofix.transport.ConnectionObserver;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

public class ByteChannelReaderTest {
    private final ReadableByteChannel readableByteChannel = Mockito.mock(ReadableByteChannel.class);
    private final ByteStreamMessageParser byteStreamMessageParser = Mockito.mock(ByteStreamMessageParser.class);
    private final ConnectionObserver connectionObserver = Mockito.mock(ConnectionObserver.class);
    private final ByteChannelReader inputStreamReader = new ByteChannelReader(byteStreamMessageParser, new ThreadBlocker(), connectionObserver);

    @Test
    public void shouldPassBytesFromInputStreamToParser() throws Exception {
        given(readableByteChannel.isOpen()).willReturn(false);
        given(readableByteChannel.read(any(ByteBuffer.class))).willReturn(1, -1);

        inputStreamReader.blockingStart(readableByteChannel);

        verify(byteStreamMessageParser).parse(any(ByteBuffer.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldCloseInputStreamIfExceptionIsThrownAndInputStreamIsStillOpen() throws Exception {
        given(readableByteChannel.read(any(ByteBuffer.class))).willThrow(new RuntimeException("boom!"));
        given(readableByteChannel.isOpen()).willReturn(true);

        try {
            inputStreamReader.blockingStart(readableByteChannel);
        } finally {
            verify(readableByteChannel).close();
        }
    }

    @Test
    public void shouldNotifyTransportObserverWhenConnectionIsClosed() throws Exception {
        given(readableByteChannel.read(any(ByteBuffer.class))).willThrow(new ClosedChannelException());

        inputStreamReader.blockingStart(readableByteChannel);

        verify(connectionObserver).connectionClosed();
    }

    @Test
    public void shouldNotifyTransportObserverWhenIOExceptionIsThrown() throws Exception {
        given(readableByteChannel.read(any(ByteBuffer.class))).willThrow(new IOException());

        inputStreamReader.blockingStart(readableByteChannel);

        verify(connectionObserver).connectionClosed();
    }

    @Test
    public void shouldCloseChannelWhenEndOfStreamIsReached() throws Exception {
        given(readableByteChannel.read(any(ByteBuffer.class))).willReturn(-1);
        given(readableByteChannel.isOpen()).willReturn(true);

        inputStreamReader.blockingStart(readableByteChannel);

        verify(readableByteChannel).close();
        verify(connectionObserver).connectionClosed();
    }
}
