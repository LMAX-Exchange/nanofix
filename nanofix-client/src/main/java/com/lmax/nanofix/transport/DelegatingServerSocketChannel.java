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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class DelegatingServerSocketChannel
{
    final ServerSocketChannel serverSocketChannel;

    public DelegatingServerSocketChannel(final ServerSocketChannel serverSocketChannel)
    {
        this.serverSocketChannel = serverSocketChannel;
    }

    public SocketChannel accept() throws IOException
    {
        return serverSocketChannel.accept();
    }

    public void close() throws IOException
    {
        serverSocketChannel.close();
    }

}
