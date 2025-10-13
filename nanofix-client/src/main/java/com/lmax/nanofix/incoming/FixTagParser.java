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

import com.lmax.nanofix.FixUtil;
import com.lmax.nanofix.byteoperations.ByteUtil;

public final class FixTagParser {
    public static final byte SOH = 1;
    private static final byte ASCII_EQUALS = (byte)61;

    private final FixTagHandler fixTagHandler;
    private final byte[] lineSeparators;

    public FixTagParser(final FixTagHandler fixTagHandler) {
        this.fixTagHandler = fixTagHandler;
        lineSeparators = new byte[1];
        lineSeparators[0] = SOH;
    }

    public FixTagParser(final FixTagHandler fixTagHandler, final byte[] lineSeparators) {
        this.fixTagHandler = fixTagHandler;
        this.lineSeparators = lineSeparators;
    }

    public boolean parse(final byte[] message, final int offset, final int length, final boolean throwExceptionOnParseFailure) {
        fixTagHandler.messageStart();

        int tagStart = offset;
        int equalsIndex = -1;

        for (int i = 0; i < length; i++) {
            int index = i + offset;
            if (-1 == equalsIndex && ASCII_EQUALS == message[index]) {
                equalsIndex = index;
            }

            if (isLineSeparator(message[index])) {
                if (-1 != equalsIndex) {
                    if (!ByteUtil.isInteger(message, tagStart, equalsIndex - tagStart)) {
                        if (throwExceptionOnParseFailure) {
                            raiseException("Invalid tag id", message, offset, length, tagStart, equalsIndex);
                        }
                        return false;
                    }

                    int tagIdentity = ByteUtil.readIntFromAscii(message, tagStart, equalsIndex - tagStart);

                    final int tagValueOffset = equalsIndex + 1;
                    final int tagValueLength = index - tagValueOffset;

                    fixTagHandler.onTag(tagIdentity, message, tagValueOffset, tagValueLength);

                    if (fixTagHandler.isFinished()) {
                        break;
                    }
                }

                tagStart = index + 1;
                equalsIndex = -1;
            }
        }

        fixTagHandler.messageEnd();

        return true;
    }

    private boolean isLineSeparator(final byte charByte) {
        for (final byte lineSeparator : lineSeparators) {
            if (lineSeparator == charByte) {
                return true;
            }
        }
        return false;
    }

    private static void raiseException(final String reason,
                                       final byte[] message,
                                       final int offset,
                                       final int length,
                                       final int tagStart,
                                       final int equalsIndex) {
        String msg = String.format("Parse Failed: %s at tagStart=%d equalsIndex=%d message=%s",
                                   reason,
                                   Integer.valueOf(tagStart),
                                   Integer.valueOf(equalsIndex),
                                   new String(message, offset, length, FixUtil.getCharset()).replace('\u0001', '|'));

        throw new FixParseException(msg);
    }
}

