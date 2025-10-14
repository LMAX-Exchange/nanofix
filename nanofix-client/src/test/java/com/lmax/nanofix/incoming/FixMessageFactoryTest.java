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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;


public class FixMessageFactoryTest {
    private static final byte ONE = 1;
    private static final byte TWO = 1;
    private static final byte THREE = 1;

    @Test
    public void shouldConstructFixMessage() throws Exception {
        final byte[] msg1 = {ONE, TWO, THREE};

        final FixMessageStreamFactory fixMessageStreamFactory = new FixMessageStreamFactory(new FixMessageHandler() {
            @Override
            public void onFixMessage(final FixMessage fixMessage) {
                Assert.assertThat(fixMessage.getFirstValue(1), is(new String(msg1, 0, 1)));
                Assert.assertThat(fixMessage.getFirstValue(2), is(new String(msg1, 1, 2)));

            }
        });

        fixMessageStreamFactory.messageStart();
        fixMessageStreamFactory.onTag(1, msg1, 0, 1);
        fixMessageStreamFactory.onTag(2, msg1, 1, 2);
        fixMessageStreamFactory.messageEnd();
    }
}
