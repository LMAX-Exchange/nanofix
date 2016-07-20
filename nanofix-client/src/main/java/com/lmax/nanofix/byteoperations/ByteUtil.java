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

import java.nio.charset.StandardCharsets;

public final class ByteUtil
{
    public static final int MAX_UNSIGNED_SHORT = (1 << 16) - 1;

    public static void writeLongAsAscii(final byte[] outputBuffer, final int offset, final long valueParam)
    {
        long value = valueParam;
        if (value < 0 || value > 9000000000000000000L)
        {
            throw new IllegalArgumentException("Value out of range: value=" + value);
        }

        int outputBufferIndex = offset + 18;

        long factor = 1;
        while (value >= factor)
        {
            long remainder = value % (factor * 10);

            outputBuffer[outputBufferIndex--] = (byte) ((remainder / factor) + 48);

            value -= remainder;
            factor *= 10;
        }

        final byte zero = (byte) 48;
        while (outputBufferIndex >= offset)
        {
            outputBuffer[outputBufferIndex--] = zero;
        }
    }

    public static boolean isAsciiDigit(final byte asciiCharacter)
    {
        return asciiCharacter > 47 && asciiCharacter < 58;
    }

    public static String formatIntAsIpAddress(final int address)
    {
        StringBuilder buffer = new StringBuilder();

        formatIntAsIpAddress(buffer, address);

        return buffer.toString();
    }

    public static void formatIntAsIpAddress(final StringBuilder buffer, final int address)
    {
        buffer.append((address >>> 24) & 0xFF);
        buffer.append('.');
        buffer.append((address >>> 16) & 0xFF);
        buffer.append('.');
        buffer.append((address >>> 8) & 0xFF);
        buffer.append('.');
        buffer.append(address & 0xFF);
    }

    public static long packLongWithUnsignedShortInts(final int int1, final int int2, final int int3, final int int4)
    {
        checkUnsignedShort(int1);
        checkUnsignedShort(int2);
        checkUnsignedShort(int3);
        checkUnsignedShort(int4);

        return ((int1 & 0xFFFFL) << 48) +
                ((int2 & 0xFFFFL) << 32) +
                ((int3 & 0xFFFFL) << 16) +
                (int4 & 0xFFFFL);
    }

    public static boolean isEqual(final byte[] lhs, final int lhsOffset, final byte[] rhs, final int rhsOffset, final int length)
    {
        for (int i = 0; i < length; i++)
        {
            if (lhs[lhsOffset + i] != rhs[rhsOffset + i])
            {
                return false;
            }
        }

        return true;
    }

    public static int readIntFromAscii(final byte[] asciiBuffer, final int offset, final int length)
    {
        int value = 0;
        int power = length;
        for (int i = offset; power > 0; i++, power--)
        {
            byte b = asciiBuffer[i];
            if (!isAsciiDigit(b))
            {
                throw new IllegalArgumentException(b + " is not a digit");
            }

            int digitValue = b - 48;
            for (int p = power; p > 1; p--)
            {
                digitValue *= 10;
            }

            value += digitValue;
        }

        return value;
    }

    public static long readLongFromAscii(final byte[] asciiBuffer, final int offset, final int length)
    {
        long value = 0;
        long power = length;
        for (int i = offset; power > 0; i++, power--)
        {
            byte b = asciiBuffer[i];
            if (!isAsciiDigit(b))
            {
                throw new IllegalArgumentException(b + " is not a digit");
            }

            long digitValue = b - 48;
            for (long p = power; p > 1; p--)
            {
                digitValue *= 10L;
            }

            value += digitValue;
        }

        return value;
    }

    public static boolean isInteger(final byte[] asciiBuffer, final int offset, final int length)
    {
        for (int i = 0; i < length; i++)
        {
            if (!isAsciiDigit(asciiBuffer[offset + i]))
            {
                return false;
            }
        }

        return true;
    }

    public static void replace(final byte[] buffer, final int offset, final int length,
                               final byte target, final byte replacement)
    {
        for (int i = 0; i < length; i++)
        {
            int location = i + offset;
            if (target == buffer[location])
            {
                buffer[location] = replacement;
            }
        }
    }

    public static void asciiEncode(final CharSequence chars, final int charOffset,
                                   final byte[] buffer, final int bufferOffset, final int length)
    {
        for (int i = 0; i < length; i++)
        {
            int charValue = chars.charAt(i + charOffset);

            if (charValue > 127)
            {
                throw new IllegalArgumentException("Character " + chars.charAt(i + charOffset) + " is not " + StandardCharsets.US_ASCII);
            }

            buffer[i + bufferOffset] = (byte) charValue;
        }
    }

    private static void checkUnsignedShort(final int value)
    {
        if (value < 0 || value > MAX_UNSIGNED_SHORT)
        {
            throw new IllegalArgumentException("Value outside range for unsigned short integer [0-65535]: " + value);
        }
    }

}

