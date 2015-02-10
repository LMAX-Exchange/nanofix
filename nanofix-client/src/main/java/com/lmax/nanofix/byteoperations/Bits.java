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


import java.lang.reflect.Field;


import sun.misc.Unsafe;

public final class Bits
{
    public static final int SIZEOF_LONG = 8;
    public static final int SIZEOF_DOUBLE = 8;
    public static final int SIZEOF_INT = 4;
    public static final int SIZEOF_FLOAT = 4;
    public static final int SIZEOF_SHORT = 2;
    public static final int SIZEOF_CHAR = 2;
    public static final int SIZEOF_BYTE = 1;
    public static final int SIZEOF_BOOLEAN = 1;

    private static final Unsafe UNSAFE;
    private static final long ARRAY_BASE_OFFSET;
    private static final boolean SHOULD_REVERSE;

    static
    {
        try
        {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);
            ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            SHOULD_REVERSE = shouldReverseBytes();
        }
        catch (Throwable e)
        {
            throw new AssertionError();
        }
    }

    private Bits()
    {
        // Static class
    }

    public static byte readByte(byte[] buffer, int offset)
    {
        return buffer[offset];
    }

    public static boolean readBoolean(byte[] buffer, int offset)
    {
        return buffer[offset] != 0;
    }

    public static short readShort(byte[] buffer, int offset)
    {
        return (short) (((buffer[offset]) << 8) + ((buffer[offset + 1] & 0xFF)));
    }

    public static char readChar(byte[] buffer, int offset)
    {
        return (char) (((buffer[offset]) << 8) + ((buffer[offset + 1] & 0xFF)));
    }

    public static int readInt(byte[] buffer, int offset)
    {
        return toBigEndian(UNSAFE.getInt(buffer, ARRAY_BASE_OFFSET + offset));
    }

    public static long readLong(byte[] buffer, int offset)
    {
        return toBigEndian(UNSAFE.getLong(buffer, ARRAY_BASE_OFFSET + offset));
    }

    public static void writeByte(byte[] buffer, int offset, byte b)
    {
        buffer[offset] = b;
    }

    public static void writeBoolean(byte[] buffer, int offset, boolean b)
    {
        buffer[offset] = (byte) (b ? 1 : 0);
    }

    public static void writeChar(byte[] buffer, int offset, char c)
    {
        buffer[offset] = (byte) (c >>> 8);
        buffer[offset + 1] = (byte) (c);
    }

    public static void writeShort(byte[] buffer, int offset, short s)
    {
        buffer[offset] = (byte) (s >>> 8);
        buffer[offset + 1] = (byte) (s);
    }

    public static void writeInt(byte[] buffer, int offset, int i)
    {
        UNSAFE.putInt(buffer, ARRAY_BASE_OFFSET + offset, toBigEndian(i));
    }

    public static void writeLong(byte[] buffer, int offset, long l)
    {
        UNSAFE.putLong(buffer, ARRAY_BASE_OFFSET + offset, toBigEndian(l));
    }

    public static byte[] toByteArray(long l)
    {
        byte[] result = new byte[8];
        writeLong(result, 0, l);
        return result;
    }

    public static byte[] toByteArray(int i)
    {
        byte[] result = new byte[4];
        writeInt(result, 0, i);
        return result;
    }

    private static int toBigEndian(int i)
    {
        return SHOULD_REVERSE ? Integer.reverseBytes(i) : i;
    }

    private static long toBigEndian(long l)
    {
        return SHOULD_REVERSE ? Long.reverseBytes(l) : l;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final long l)
    {
        return SIZEOF_LONG;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final double d)
    {
        return SIZEOF_DOUBLE;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final int i)
    {
        return SIZEOF_INT;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final float f)
    {
        return SIZEOF_FLOAT;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final char c)
    {
        return SIZEOF_CHAR;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final short s)
    {
        return SIZEOF_SHORT;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final byte b)
    {
        return SIZEOF_BYTE;
    }

    @SuppressWarnings("unused")
    public static int sizeOf(final boolean b)
    {
        return SIZEOF_BOOLEAN;
    }

    private static void writeLongBigEndian(byte[] buffer, int offset, long l)
    {
        buffer[offset] = (byte) (l >>> 56);
        buffer[offset + 1] = (byte) (l >>> 48);
        buffer[offset + 2] = (byte) (l >>> 40);
        buffer[offset + 3] = (byte) (l >>> 32);
        buffer[offset + 4] = (byte) (l >>> 24);
        buffer[offset + 5] = (byte) (l >>> 16);
        buffer[offset + 6] = (byte) (l >>> 8);
        buffer[offset + 7] = (byte) (l);
    }

    private static boolean shouldReverseBytes()
    {
        final byte[] b = new byte[8];
        final long expected = 98237454L;
        writeLongBigEndian(b, 0, expected);
        long actual = UNSAFE.getLong(b, ARRAY_BASE_OFFSET);
        return actual != expected;
    }
}
