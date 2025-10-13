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

package com.lmax.nanofix;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.lmax.nanofix.incoming.FixTagParser;

public final class FixUtil {
    public static final Charset ASCII_CHARSET;

    static {
        ASCII_CHARSET = StandardCharsets.US_ASCII;
    }

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    public static Charset getCharset() {
        return ASCII_CHARSET;
    }

    public static byte[] newMessage(String... tags) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = tags.length; i < n; i++) {
            sb.append(tags[i]);
            sb.append(FixTagParser.SOH);
        }
        return sb.toString().getBytes(FixUtil.getCharset());
    }
}

