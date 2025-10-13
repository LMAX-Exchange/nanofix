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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import com.lmax.nanofix.incoming.ByteChannelReader;
import com.lmax.nanofix.outgoing.OutboundMessageHandler;
import com.lmax.nanofix.transport.Transport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ChannelInitializerTest {
    private final Transport transport = mock(Transport.class);
    private final ByteChannelReader byteChannelReader = mock(ByteChannelReader.class);
    private final OutboundMessageHandler outboundMessageHandler = mock(OutboundMessageHandler.class);
    private final WritableByteChannel writableByteChannel = mock(WritableByteChannel.class);
    private final ReadableByteChannel readableByteChannel = mock(ReadableByteChannel.class);

    @Before
    public void setUp() {
        Mockito.ignoreStubs(writableByteChannel);
    }

    @Test
    public void shouldStartByteChannelReaderOnConnectionEstablished() {
        given(transport.getWritableByteChannel()).willReturn(writableByteChannel);
        given(transport.getReadableByteChannel()).willReturn(readableByteChannel);

        final DeterministicExecutor deterministicExecutor = new DeterministicExecutor();
        final ChannelInitializer channelInitializer = new ChannelInitializer(transport, byteChannelReader, outboundMessageHandler, deterministicExecutor);
        channelInitializer.connectionEstablished();
        deterministicExecutor.runPendingCommands();

        verify(outboundMessageHandler).initialiseOutboundChannel(writableByteChannel);
        verify(byteChannelReader).blockingStart(readableByteChannel);
    }

    private static class DeterministicExecutor implements Executor {
        private List<Runnable> commands = new ArrayList<>();

        DeterministicExecutor() {
            super();
        }

        public void runPendingCommands() {
            final List<Runnable> commandsToRun = commands;
            commands = new ArrayList<>();

            for (Runnable command : commandsToRun) {
                command.run();
            }
        }

        public void execute(final Runnable command) {
            commands.add(command);
        }
    }
}
