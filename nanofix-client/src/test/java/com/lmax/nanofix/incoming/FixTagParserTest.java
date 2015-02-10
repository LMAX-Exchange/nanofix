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

import java.io.UnsupportedEncodingException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public final class FixTagParserTest
{
    private final Mockery context = new Mockery();

    @Test
    public void shouldCountTagsCorrectlyInMessage()
    {
        byte[] logonMsg = FixMessageUtil.getLogonMessage();
        final FixTagHandler fixTagHandler = context.mock(FixTagHandler.class);

        context.checking(new Expectations()
        {
            {
                oneOf(fixTagHandler).messageStart();

                exactly(13).of(fixTagHandler).onTag(with(any(int.class)), with(any(byte[].class)), with(any(int.class)), with(any(int.class)));

                exactly(13).of(fixTagHandler).isFinished();
                will(returnValue(Boolean.valueOf(false)));

                oneOf(fixTagHandler).messageEnd();
            }
        });

        FixTagParser parser = new FixTagParser(fixTagHandler);

        parser.parse(logonMsg, 0, logonMsg.length, true);
    }

    @Test
    public void shouldCountTagsSeparatedByMultipleSeparatorsMessage() throws UnsupportedEncodingException
    {
        final byte[] msg = "8=FIX.4.2\u00019=105|35=A|34=1|49=marketm155yjtfwicmfe\u000152=20100713-17:04:39.641|56=FIX-API|95=9|96=P4ssword.|98=0\u0001108=2|141=Y|10=191|".getBytes("US-ASCII");
        byte[] logonMsg = new byte[msg.length];
        System.arraycopy(msg, 0, logonMsg, 0, msg.length);
        final FixTagHandler fixTagHandler = context.mock(FixTagHandler.class);

        context.checking(new Expectations()
        {
            {
                oneOf(fixTagHandler).messageStart();

                exactly(13).of(fixTagHandler).onTag(with(any(int.class)), with(any(byte[].class)), with(any(int.class)), with(any(int.class)));

                exactly(13).of(fixTagHandler).isFinished();
                will(returnValue(Boolean.valueOf(false)));

                oneOf(fixTagHandler).messageEnd();
            }
        });

        FixTagParser parser = new FixTagParser(fixTagHandler, new byte[]{1, 124});

        parser.parse(logonMsg, 0, logonMsg.length, true);
    }

    @Test
    public void shouldReportFirstThreeTags()
    {
        final byte[] logonMsg = FixMessageUtil.getLogonMessage();
        final FixTagHandler fixTagHandler = context.mock(FixTagHandler.class);

        context.checking(new Expectations()
        {
            {
                oneOf(fixTagHandler).messageStart();

                oneOf(fixTagHandler).onTag(8, logonMsg, 2, 7);
                oneOf(fixTagHandler).onTag(9, logonMsg, 12, 3);
                oneOf(fixTagHandler).onTag(35, logonMsg, 19, 1);

                exactly(3).of(fixTagHandler).isFinished();
                will(returnValue(Boolean.valueOf(false)));

                oneOf(fixTagHandler).messageEnd();
            }
        });

        FixTagParser parser = new FixTagParser(fixTagHandler);

        parser.parse(logonMsg, 0, 21, true);
    }

    @Test
    public void shouldStopReadingWhenTheMessageIsNotLogon()
    {
        final byte[] newOrderSingleMsg = FixMessageUtil.getNewOrderSingle();
        final FixTagHandler fixTagHandler = context.mock(FixTagHandler.class);
        final States messageType = context.states("messageType").startsAs("unidentified");

        context.checking(new Expectations()
        {
            {
                oneOf(fixTagHandler).messageStart();

                oneOf(fixTagHandler).onTag(8, newOrderSingleMsg, 2, 7);
                oneOf(fixTagHandler).onTag(9, newOrderSingleMsg, 12, 3);
                oneOf(fixTagHandler).onTag(35, newOrderSingleMsg, 19, 1);
                then(messageType.is("known"));

                exactly(2).of(fixTagHandler).isFinished();
                when(messageType.is("unidentified"));
                will(returnValue(Boolean.valueOf(false)));

                oneOf(fixTagHandler).isFinished();
                when(messageType.is("known"));
                will(returnValue(Boolean.valueOf(true)));

                oneOf(fixTagHandler).messageEnd();
            }
        });

        FixTagParser parser = new FixTagParser(fixTagHandler);

        parser.parse(newOrderSingleMsg, 0, newOrderSingleMsg.length, true);
    }
}
