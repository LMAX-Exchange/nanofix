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

import com.lmax.nanofix.concurrent.Blocker;
import com.lmax.nanofix.transport.ConnectionObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteChannelReader
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteChannelReader.class);
    private static final int BUFFER_SIZE = 1024;
    private final ByteStreamMessageParser byteStreamMessageParser;
    private final Blocker blocker;
    private final ConnectionObserver connectionObserver;
    private final ByteBuffer buffer;

    public ByteChannelReader(final ByteStreamMessageParser byteStreamMessageParser, final Blocker blocker, final ConnectionObserver connectionObserver)
    {
        this.byteStreamMessageParser = byteStreamMessageParser;
        this.blocker = blocker;
        this.connectionObserver = connectionObserver;
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public void blockingStart(final ReadableByteChannel readableByteChannel)
    {
        try
        {
            while ((readableByteChannel.read(buffer)) != -1)
            {
                blocker.mayWait();
                buffer.flip();
                byteStreamMessageParser.parse(buffer);
                buffer.clear();
            }

        }
        catch (final ClosedChannelException e)
        {
            //Yes closed.
        }
        catch (final IOException e)
        {
            LOGGER.error("An error occurred trying to read from the socket", e);
        }
        finally
        {
            if (readableByteChannel != null)
            {
                try
                {
                    if (readableByteChannel.isOpen())
                    {
                        readableByteChannel.close();
                    }
                }
                catch (IOException e)
                {
                    //I don't care
                }
                connectionObserver.connectionClosed();
            }
        }
    }
}
