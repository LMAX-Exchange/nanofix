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

import javax.annotation.concurrent.Immutable;

@Immutable
public class FixMessage {
    private final String messageWithChecksum;

    FixMessage(final String messageWithChecksum) {
        this.messageWithChecksum = messageWithChecksum;
    }

    public String toFixString() {
        return messageWithChecksum;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FixMessage");
        sb.append("{messageWithChecksum='").append(messageWithChecksum).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
