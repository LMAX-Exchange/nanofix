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
import java.util.Collections;
import java.util.List;

import com.lmax.nanofix.fields.EncryptMethod;
import com.lmax.nanofix.fields.MsgType;
import com.lmax.nanofix.transport.ConnectionObserver;
import com.lmax.nanofix.transport.TransportClosedException;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;


import static com.google.common.collect.Lists.newArrayList;
import static com.lmax.nanofix.FixUtil.DATE_TIME_FORMATTER;

public class OutboundMessageHandlerTest
{
    private final WritableByteChannel writableByteChannel = mock(WritableByteChannel.class);
    private final ConnectionObserver connectionObserver = mock(ConnectionObserver.class);
    private final OutboundMessageHandler handler = new OutboundMessageHandler(connectionObserver);


    @Before
    public void setUp()
    {
        handler.initialiseOutboundChannel(writableByteChannel);
    }

    @Test
    public void shouldPlaceMultipleMessagesInSameBuffer() throws Exception
    {
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.parse("19981231-23:58:59.000", DATE_TIME_FORMATTER), ZoneOffset.UTC);
        final FixMessage loginMessage = new FixMessageBuilder().messageType(MsgType.LOGIN).msgSeqNum(1).senderCompID("username").targetCompID("LMXBL")
                .sendingTime(zonedDateTime).username("username").password("password").heartBtInt(100000).encryptMethod(EncryptMethod.NONE).build();
        final FixMessage logoutMessage = new FixMessageBuilder().messageType(MsgType.LOGOUT).msgSeqNum(2).senderCompID("username").targetCompID("LMXBL")
                .sendingTime(zonedDateTime.plusMinutes(1)).build();

        final List<FixMessage> expected = newArrayList(loginMessage, logoutMessage);
        handler.send(expected);

        verify(writableByteChannel).write(argThat(new ByteBufferMatcher(expected)));
    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverIfAClosedChannelExceptionIsThrownWhileWriting() throws Exception
    {
        given(writableByteChannel.write(any(ByteBuffer.class))).willThrow(new ClosedChannelException());

        try
        {
            handler.send(new FixMessageBuilder().build());
        }
        finally
        {
            verify(connectionObserver).connectionClosed();
        }
    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverIfAnClosedChannelExceptionIsThrownWhileWritingACollection() throws Exception
    {
        given(writableByteChannel.write(any(ByteBuffer.class))).willThrow(new ClosedChannelException());

        try
        {
            handler.send(Collections.singletonList(new FixMessageBuilder().build()));
        }
        finally
        {
            verify(connectionObserver).connectionClosed();
        }
    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverWhenAnIOExceptionIsThrownAndCloseTheTransportWhileWritingACollection() throws Exception
    {
        given(writableByteChannel.write(any(ByteBuffer.class))).willThrow(new IOException("Broken pipe"));

        try
        {
            handler.send(Collections.singletonList(new FixMessageBuilder().build()));
        }
        finally
        {
            verify(connectionObserver).connectionClosed();
            verify(writableByteChannel).close();
        }
    }

    @Test(expected = TransportClosedException.class)
    public void shouldNotifyTransportObserverWhenAnIOExceptionIsThrownAndCloseTheTransportWhileWriting() throws Exception
    {
        given(writableByteChannel.write(any(ByteBuffer.class))).willThrow(new IOException("Broken pipe"));

        try
        {
            handler.send(new FixMessageBuilder().build());
        }
        finally
        {
            verify(connectionObserver).connectionClosed();
            verify(writableByteChannel).close();
        }
    }
}
