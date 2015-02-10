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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class FixMessageStreamFactory implements FixTagHandler
{
    private final Multimap<Integer, String> multimap = LinkedListMultimap.create();
    private final FixMessageHandler handler;

    public FixMessageStreamFactory(final FixMessageHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public void messageStart()
    {
        multimap.clear();
    }

    @Override
    public void onTag(final int tagIdentity, final byte[] message, final int tagValueOffset, final int tagValueLength)
    {
        multimap.put(tagIdentity, new String(message, tagValueOffset, tagValueLength));
    }

    @Override
    public boolean isFinished()
    {
        return false;
    }

    @Override
    public void messageEnd()
    {
        handler.onFixMessage(new FixMessage(multimap));
    }


}
