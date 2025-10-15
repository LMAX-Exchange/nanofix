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

package com.lmax.nanofix.byteoperations;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BitsTest {

    byte[] data;

    @Before
    public void setUp() {
        data = new byte[20];
    }

    @Test
    public void shouldWriteAndReadLong() {
        long expectedValue = 12893471928734L;
        Bits.writeLong(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readLong(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeLong() {
        long expectedValue = -722393471928734L;
        Bits.writeLong(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readLong(data, 3));
    }

    @Test
    public void shouldWriteAndReadInt() {
        int expectedValue = 51345234;
        Bits.writeInt(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readInt(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeInt() {
        int expectedValue = -34574234;
        Bits.writeInt(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readInt(data, 3));
    }

    @Test
    public void shouldWriteAndReadShort() {
        short expectedValue = 3452;
        Bits.writeShort(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readShort(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeShort() {
        short expectedValue = -8123;
        Bits.writeShort(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readShort(data, 3));
    }

    @Test
    public void shouldWriteAndReadByte() {
        byte expectedValue = 23;
        Bits.writeShort(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readShort(data, 3));
    }

    @Test
    public void shouldWriteAndReadNegativeByte() {
        byte expectedValue = -13;
        Bits.writeByte(data, 3, expectedValue);
        assertEquals(expectedValue, Bits.readByte(data, 3));
    }

    @Test
    public void shouldWriteAndReadBoolean() {
        Bits.writeBoolean(data, 3, true);
        assertTrue(Bits.readBoolean(data, 3));
        Bits.writeBoolean(data, 3, false);
        assertFalse(Bits.readBoolean(data, 3));
    }

}
