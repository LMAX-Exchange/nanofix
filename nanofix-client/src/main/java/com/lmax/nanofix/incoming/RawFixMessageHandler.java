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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawFixMessageHandler implements MessageParserCallback
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RawFixMessageHandler.class);
    private final FixTagParser fixTagParser;

    public RawFixMessageHandler(final FixTagParser fixTagParser)
    {
        this.fixTagParser = fixTagParser;
    }

    @Override
    public void onMessage(final byte[] buffer, final int offset, final int length)
    {
        fixTagParser.parse(buffer, offset, length, true);
    }

    @Override
    public void onTruncatedMessage()
    {
        LOGGER.warn("Truncated Message received");
    }

    @Override
    public void onParseError(final String error)
    {
        LOGGER.error("Unable to parse data: " + error);
    }
}
