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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class FixMessageTest {

    private static final char SOH_CHAR = '\u0001';
    private static final char PIPE_CHAR = '|';

    @Test
    public void shouldReturnFixMessage() throws Exception {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + SOH_CHAR + "2=secondKey" + SOH_CHAR + "3=thirdKey" + SOH_CHAR;
        Assert.assertThat(fixMessage.toFixString(), is(expectedString));

    }

    @Test
    public void shouldReturnFixMessageWithDuplicateKeys() throws Exception {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(2, "anotherSecondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + SOH_CHAR + "2=secondKey" + SOH_CHAR + "2=anotherSecondKey" + SOH_CHAR + "3=thirdKey" + SOH_CHAR;
        Assert.assertThat(fixMessage.toFixString(), is(expectedString));

    }

    @Test
    public void shouldReturnFixMessageWithPipeSeparator() throws Exception {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + PIPE_CHAR + "2=secondKey" + PIPE_CHAR + "3=thirdKey" + PIPE_CHAR;
        Assert.assertThat(fixMessage.toHumanString(), is(expectedString));

    }

    @Test
    public void shouldReturnFalseIfFixMessageDoesNotHaveTag() throws Exception {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();

        final FixMessage fixMessage = new FixMessage(multimap);
        Assert.assertThat(fixMessage.hasValue(23), is(false));
    }

    @Test
    public void shouldReturnFixMessageWithPipeSeparatorWithDuplicateKeys() throws Exception {
        final Multimap<Integer, String> multimap = ArrayListMultimap.create();
        multimap.put(1, "firstKey");
        multimap.put(2, "secondKey");
        multimap.put(2, "anotherSecondKey");
        multimap.put(3, "thirdKey");

        final FixMessage fixMessage = new FixMessage(multimap);
        final String expectedString = "1=firstKey" + PIPE_CHAR + "2=secondKey" + PIPE_CHAR + "2=anotherSecondKey" + PIPE_CHAR + "3=thirdKey" + PIPE_CHAR;
        Assert.assertThat(fixMessage.toHumanString(), is(expectedString));

    }

}
