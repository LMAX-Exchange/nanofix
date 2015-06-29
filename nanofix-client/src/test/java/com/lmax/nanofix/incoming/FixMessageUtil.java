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
import java.util.Arrays;


import com.lmax.nanofix.byteoperations.ByteUtil;

import org.junit.Test;

public final class FixMessageUtil
{
    public static final byte[] LOGON_MESSAGE;

    public static final byte[] NEW_ORDER_SINGLE;
    public static final byte[] EXECUTION_REPORT;
    public static final byte[] TRUNCATED_EXECUTION_REPORT;
    public static final byte[] LOGON_44_MESSAGE;

    static
    {
        try
        {
            LOGON_MESSAGE = "8=FIX.4.2|9=105|35=A|34=1|49=marketm155yjtfwicmfe|52=20100713-17:04:39.641|56=FIX-API|95=9|96=P4ssword.|98=0|108=2|141=Y|10=191|".getBytes("ASCII");
            LOGON_44_MESSAGE = "8=FIX.4.2|9=105|35=A|34=1|49=marketm155yjtfwicmfe|52=20100713-17:04:39.641|56=FIX-API|553=foobar|554=P4ssword.|98=0|108=2|141=Y|10=191|".getBytes("ASCII");

            NEW_ORDER_SINGLE = ("8=FIX.4.4|9=191|35=D|34=14|49=PERS|52=20080108-19:41:12.859|56=PHLX|57=MINIDEMO|1=00217844|11=xxxxxxxxxxxxxxxxxxx|38=10000|40=3|54=1|55=EUR/USD|59=1|" +
                                "60=20080108-19:41:12|99=1.46909|9041=7670249|10=171|").getBytes("ASCII");
            EXECUTION_REPORT = ("8=FIX.4.4|9=178|35=8|49=PHLX|56=PERS|11=xxxxxxxxxxxxxxxxxxx|52=20071123-05:30:00.000|20=3|150=0|39=E|55=MSFT|167=CS|54=1|38=15|40=2|44=15|58=PHLX EQUITY TESTING|" +
                                "59=0|47=C|32=0|31=0|151=15|14=0|6=0|10=128|").getBytes("ASCII");
            TRUNCATED_EXECUTION_REPORT = ("8=FIX.4.2|9=254|35=8|34=1005699|49=FIX-API|52=20110216-14:14:46.354|56=optfx|1=8000|6=0|11=8000001f0fcbd139|14=0|17=AAAPrQAAAAADi0g0|20=0|22=8|" +
                                          "37=AAAPrQAAAAA").getBytes("ASCII");
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static final byte SOH = (byte)1;
    private static final byte BAR = (byte)124;

    public static byte[] getLogonMessage()
    {
        byte[] buffer = new byte[LOGON_MESSAGE.length];
        System.arraycopy(LOGON_MESSAGE, 0, buffer, 0, LOGON_MESSAGE.length);
        prepareBuffer(buffer);
        return buffer;
    }

    public static byte[] getNewOrderSingle()
    {
        byte[] buffer = new byte[NEW_ORDER_SINGLE.length];
        System.arraycopy(NEW_ORDER_SINGLE, 0, buffer, 0, NEW_ORDER_SINGLE.length);
        prepareBuffer(buffer);
        return buffer;
    }

    public static byte[] getExecutionReport()
    {
        byte[] buffer = new byte[EXECUTION_REPORT.length];
        System.arraycopy(EXECUTION_REPORT, 0, buffer, 0, EXECUTION_REPORT.length);
        prepareBuffer(buffer);
        return buffer;
    }

    public static byte[] getTruncatedExecutionReport()
    {
        byte[] buffer = new byte[TRUNCATED_EXECUTION_REPORT.length];
        System.arraycopy(TRUNCATED_EXECUTION_REPORT, 0, buffer, 0, TRUNCATED_EXECUTION_REPORT.length);
        prepareBuffer(buffer);
        return buffer;
    }

    private static void prepareBuffer(final byte[] buffer)
    {
        ByteUtil.replace(buffer, 0, buffer.length, BAR, SOH);
    }

    public static void setOrderIdOnNewOrderSingle(final long orderId, final byte[] newOrderSingle)
        throws Exception
    {
        int orderIdOffset = 94;
        ByteUtil.writeLongAsAscii(newOrderSingle, orderIdOffset, orderId);
    }

    public static String convertFixControlCharacters(byte[] message)
    {
        try
        {
            byte[] buffer = new byte[message.length];
            System.arraycopy(message, 0, buffer, 0, message.length);
            prepareBufferForStringForm(buffer);
            return new String(buffer, "ASCII");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("ASCII not supported by your runtime environment");
        }
    }

    public static byte[] convertFixControlCharacters(String message)
    {
        try
        {
            byte[] asByteArray = message.getBytes("ASCII");
            byte[] buffer = new byte[asByteArray.length];
            System.arraycopy(asByteArray, 0, buffer, 0, asByteArray.length);
            prepareBuffer(buffer);
            return buffer;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("ASCII not supported by your runtime environment");
        }
    }

    private static void prepareBufferForStringForm(final byte[] buffer)
    {
        final byte soh = (byte)1;
        final byte bar = (byte)124;

        ByteUtil.replace(buffer, 0, buffer.length, soh, bar);
    }

    @Test
    public void shouldSetOrderId() throws Exception
    {
        byte[] buffer = Arrays.copyOf(NEW_ORDER_SINGLE, NEW_ORDER_SINGLE.length);
        FixMessageUtil.setOrderIdOnNewOrderSingle(15, buffer);
    }
}

