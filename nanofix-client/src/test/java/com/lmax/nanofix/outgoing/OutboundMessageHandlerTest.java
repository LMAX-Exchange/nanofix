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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import com.lmax.nanofix.fields.EncryptMethod;
import com.lmax.nanofix.fields.MsgType;
import com.lmax.nanofix.transport.ConnectionObserver;
import com.lmax.nanofix.transport.TransportClosedException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static com.google.common.collect.Lists.newArrayList;
import static com.lmax.nanofix.FixUtil.DATE_TIME_FORMATTER;

@RunWith(JMock.class)
public class OutboundMessageHandlerTest
{
    private Mockery mockery;
    private WritableByteChannel writableByteChannel;

    private OutboundMessageHandler handler;
    private ConnectionObserver connectionObserver;

    @Before
    public void setUp()
    {
        mockery = new Mockery();
        writableByteChannel = mockery.mock(WritableByteChannel.class);
        connectionObserver = mockery.mock(ConnectionObserver.class);
        handler = new OutboundMessageHandler(connectionObserver);
        handler.initialiseOutboundChannel(writableByteChannel);
    }

    @Test
    public void shouldPlaceMultipleMessagesInSameBuffer() throws Exception
    {
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.parse("19981231-23:58:59.000", DATE_TIME_FORMATTER), ZoneOffset.UTC);
        final FixMessage loginMessage = new FixMessageBuilder().messageType(MsgType.LOGIN).msgSeqNum(1).senderCompID("username").targetCompID("LMXBL").
                sendingTime(zonedDateTime).username("username").password("password").heartBtInt(100000).encryptMethod(EncryptMethod.NONE).build();
        final FixMessage logoutMessage = new FixMessageBuilder().messageType(MsgType.LOGOUT).msgSeqNum(2).senderCompID("username").targetCompID("LMXBL").
                sendingTime(zonedDateTime.plusMinutes(1)).build();
        final List<FixMessage> expected = newArrayList(loginMessage, logoutMessage);

        mockery.checking(new Expectations()
        {
            {
                one(writableByteChannel).write(with(new ByteBufferMatcher(expected)));
            }
        });

        handler.send(expected);
    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverIfAClosedChannelExceptionIsThrownWhileWriting() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                //when
                one(writableByteChannel).write(with(any(ByteBuffer.class)));
                will(throwException(new ClosedChannelException()));

                //then
                one(connectionObserver).connectionClosed();
            }
        });

        handler.send(new FixMessageBuilder().build());

    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverIfAnClosedChannelExceptionIsThrownWhileWritingACollection() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                //when
                one(writableByteChannel).write(with(any(ByteBuffer.class)));
                will(throwException(new ClosedChannelException()));

                //then
                one(connectionObserver).connectionClosed();
            }
        });

        handler.send(Arrays.asList(new FixMessageBuilder().build()));

    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverWhenAnIOExceptionIsThrownAndCloseTheTransportWhileWritingACollection() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                //when
                one(writableByteChannel).write(with(any(ByteBuffer.class)));
                will(throwException(new IOException("Broken pipe")));

                //then
                one(connectionObserver).connectionClosed();
                one(writableByteChannel).close();
            }
        });

        handler.send(Arrays.asList(new FixMessageBuilder().build()));

    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverWhenAnIOExceptionIsThrownAndCloseTheTransportWhileWriting() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                //when
                one(writableByteChannel).write(with(any(ByteBuffer.class)));
                will(throwException(new IOException("Broken pipe")));

                //then
                one(connectionObserver).connectionClosed();
                one(writableByteChannel).close();
            }
        });

        handler.send(new FixMessageBuilder().build());

    }

}
