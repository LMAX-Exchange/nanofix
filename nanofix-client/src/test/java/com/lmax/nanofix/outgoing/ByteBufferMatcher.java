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

import java.nio.ByteBuffer;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ByteBufferMatcher extends TypeSafeMatcher<ByteBuffer>
{

    private final Collection<FixMessage> expected;

    ByteBufferMatcher(final Collection<FixMessage> expected)
    {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(final ByteBuffer byteBuffer)
    {
        int matchingAt = 0;
        final byte[] array = byteBuffer.array();
        for (FixMessage message : expected)
        {
            final byte[] bytes = message.toFixString().getBytes();

            for (int j = 0; j < bytes.length; j++)
            {
                if (bytes[j] != array[j + matchingAt])
                {
                    return false;
                }
            }

            matchingAt += bytes.length;
        }
        return true;
    }

    @Override
    public void describeTo(final Description description)
    {
        final StringBuilder humanlyExpected = new StringBuilder();

        for (FixMessage message : expected)
        {
            humanlyExpected.append(message.toFixString());
        }

        description.appendText(humanlyExpected.toString());
    }
}
