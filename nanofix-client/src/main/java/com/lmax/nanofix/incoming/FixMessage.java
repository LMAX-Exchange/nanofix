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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class FixMessage {
    private final Multimap<Integer, String> multimap;

    public FixMessage(final Multimap<Integer, String> multimap) {
        this.multimap = LinkedListMultimap.create(multimap);
    }

    public Collection<String> getValues(int tagId) {
        return multimap.get(tagId);
    }

    public String getFirstValue(int tagId) {
        return multimap.get(tagId).iterator().next();
    }

    public boolean hasValue(int tagId) {
        return !multimap.get(tagId).isEmpty();
    }

    public void replace(int tagId, String value) {
        final LinkedList<String> values = new LinkedList<>();
        values.add(value);
        multimap.replaceValues(tagId, values);
    }

    public String toFixString() {
        final char tagSeparator = '\u0001';

        return buildString(tagSeparator);
    }

    public String toHumanString() {
        final char tagSeparator = '|';
        return buildString(tagSeparator);
    }

    @Override
    public String toString() {
        return toHumanString();
    }

    private String buildString(final char tagSeparator) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Collection<Map.Entry<Integer, String>> entries = multimap.entries();
        for (Map.Entry<Integer, String> entry : entries) {
            stringBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append(tagSeparator);
        }
        return stringBuilder.toString();
    }
}
