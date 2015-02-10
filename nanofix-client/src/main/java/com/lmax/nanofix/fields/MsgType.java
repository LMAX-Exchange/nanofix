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

package com.lmax.nanofix.fields;

public enum MsgType
{
    BUSINESS_MESSAGE_REJECT("j"),
    EXECUTION_REPORT("8"),
    LOGIN("A"),
    LOGOUT("5"),
    MARKET_DATA_SNAPSHOT("W"),
    NEW_ORDER_SINGLE("D"),
    REJECT("3"),
    RESEND_REQUEST("2"),
    TEST_REQUEST("1");

    private final String code;

    MsgType(final String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    public static boolean knownMsgType(final String possibleMessageType)
    {
       for (MsgType msgType : MsgType.values())
       {
           if (msgType.getCode().equals(possibleMessageType))
           {
               return true;
           }
       }
       return false;
    }

}
