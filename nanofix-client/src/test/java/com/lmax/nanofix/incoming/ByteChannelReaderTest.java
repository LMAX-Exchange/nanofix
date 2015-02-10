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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ByteChannelReaderTest
{


    private ByteChannelReader inputStreamReader;
    private Mockery mockery = new Mockery();
    private ReadableByteChannel readableByteChannel;
    private ByteStreamMessageParser byteStreamMessageParser;
    private ConnectionObserver connectionObserver;

    @Before
    public void setUp() throws Exception
    {
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
        readableByteChannel = mockery.mock(ReadableByteChannel.class);
        byteStreamMessageParser = mockery.mock(ByteStreamMessageParser.class);
        connectionObserver = mockery.mock(ConnectionObserver.class);
        inputStreamReader = new ByteChannelReader(byteStreamMessageParser, new ThreadBlocker(), connectionObserver);
    }

    @Test
    public void shouldPassBytesFromInputStreamToParser() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                ignoring(connectionObserver).connectionClosed();
                allowing(readableByteChannel).isOpen();
                will(returnValue(false));

                one(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(returnValue(1));
                one(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(returnValue(-1));

                one(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    @Test(expected = RuntimeException.class)
    public void shouldCloseInputStreamIfExceptionIsThrownAndInputStreamIsStillOpen() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                ignoring(connectionObserver).connectionClosed();
                allowing(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));

                //when
                one(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(throwException(new RuntimeException("boom!")));


                //then
                one(readableByteChannel).isOpen();
                will(returnValue(true));
                one(readableByteChannel).close();

            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    @Test
    public void shouldNotifyTransportObserverWhenConnectionIsClosed() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                ignoring(readableByteChannel).isOpen();
                allowing(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));

                //when
                allowing(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(throwException(new ClosedChannelException()));
                allowing(readableByteChannel).close();


                //then
                one(connectionObserver).connectionClosed();
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    @Test
    public void shouldNotifyTransportObserverWhenIOExceptionIsThrown() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                ignoring(readableByteChannel).isOpen();
                allowing(byteStreamMessageParser).parse(with(any(ByteBuffer.class)));

                //when
                allowing(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(throwException(new IOException()));
                allowing(readableByteChannel).close();


                //then
                one(connectionObserver).connectionClosed();
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    @Test
    public void shouldCloseChannelWhenEndOfStreamIsReached() throws Exception
    {
        expectEndOfInputStream();

        mockery.checking(new Expectations()
        {
            {
                one(readableByteChannel).isOpen();
                will(returnValue(true));

                one(readableByteChannel).close();
                one(connectionObserver).connectionClosed();
            }
        });

        //when
        inputStreamReader.blockingStart(readableByteChannel);
    }

    private void expectEndOfInputStream() throws IOException
    {
        mockery.checking(new Expectations()
        {
            {
                one(readableByteChannel).read(with(any(ByteBuffer.class)));
                will(returnValue(-1));
            }
        });

    }


}
