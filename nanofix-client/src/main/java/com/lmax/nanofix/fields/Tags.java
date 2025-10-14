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

public enum Tags {
    Account(1),
    BeginSeqNo(7),
    BusinessRejectReason(380),
    ClOrdID(11),
    EncryptMethod(98),
    EndSeqNo(16),
    HeartBtInt(108),
    MsgSeqNum(34),
    MsgType(35),
    OrderQty(38),
    OrdType(40),
    OrigSendingTime(122),
    Password(554),
    PossDupFlag(43),
    Price(44),
    RefMsgType(372),
    RefSeqNum(45),
    ResetSeqNumFlag(141),
    SenderCompID(49),
    SecurityID(48),
    SecurityIDSource(22),
    SendingTime(52),
    SessionRejectReason(373),
    Side(54),
    Symbol(55),
    TargetCompID(56),
    TestReqID(112),
    TransactTime(60),
    RawDataLength(95),
    RawData(96),
    Username(553);

    private final int tag;

    Tags(final int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public static boolean knownTag(final int possibleTag) {
        for (Tags tag : Tags.values()) {
            if (tag.getTag() == possibleTag) {
                return true;
            }
        }
        return false;
    }
}
