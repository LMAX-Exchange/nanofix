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


import java.util.Collection;

import com.lmax.nanofix.outgoing.FixMessage;
import com.lmax.nanofix.outgoing.OutboundMessageHandler;

public class FixSession
{
    private int outboundSequenceNumber;
    private OutboundMessageHandler outboundMessageSender;

    public FixSession(final OutboundMessageHandler outboundMessageSender)
    {
        this.outboundMessageSender = outboundMessageSender;
        this.outboundSequenceNumber = 1;
    }

    public void send(final Collection<FixMessage> messages)
    {
        outboundMessageSender.send(messages);
    }

    public void send(final FixMessage message)
    {
        outboundMessageSender.send(message);
    }

    public void send(final String message)
    {
        outboundMessageSender.send(message);
    }

    public void send(final byte[] bytes)
    {
        outboundMessageSender.sendBytes(bytes);
    }
}
