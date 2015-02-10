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

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.lmax.nanofix.incoming.ByteChannelReader;
import com.lmax.nanofix.outgoing.OutboundMessageHandler;
import com.lmax.nanofix.transport.Transport;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.concurrent.DeterministicExecutor;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ChannelInitializerTest
{

    private Transport transport;
    private Mockery mockery;
    private ByteChannelReader byteChannelReader;
    private OutboundMessageHandler outboundMessageHandler;
    private DeterministicExecutor deterministicExecutor;
    private WritableByteChannel writableByteChannel;
    private ReadableByteChannel readableByteChannel;

    @Before
    public void setUp() throws Exception
    {
        mockery = new Mockery();
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
        transport = mockery.mock(Transport.class);
        byteChannelReader = mockery.mock(ByteChannelReader.class);
        outboundMessageHandler = mockery.mock(OutboundMessageHandler.class);
        writableByteChannel = mockery.mock(WritableByteChannel.class);
        readableByteChannel = mockery.mock(ReadableByteChannel.class);

        mockery.checking(new Expectations()
        {
            {
                ignoring(writableByteChannel);
            }
        });
    }

    @Test
    public void shouldStartByteChannelReaderOnConnectionEstablished() throws Exception
    {
        deterministicExecutor = new DeterministicExecutor();
        final ChannelInitializer channelInitializer = new ChannelInitializer(transport, byteChannelReader, outboundMessageHandler, deterministicExecutor);

        mockery.checking(new Expectations()
        {
            {
                one(transport).getWritableByteChannel();
                will(returnValue(writableByteChannel));

                one(outboundMessageHandler).initialiseOutboundChannel(writableByteChannel);
            }
        });
        channelInitializer.connectionEstablished();

        mockery.checking(new Expectations()
        {
            {
                one(transport).getReadableByteChannel();
                will(returnValue(readableByteChannel));

                one(byteChannelReader).blockingStart(readableByteChannel);
            }
        });
        deterministicExecutor.runPendingCommands();

    }

}
