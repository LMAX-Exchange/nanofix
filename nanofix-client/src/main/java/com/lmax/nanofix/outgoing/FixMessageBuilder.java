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

package com.lmax.nanofix.outgoing;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import com.lmax.nanofix.fields.BusinessRejectionReason;
import com.lmax.nanofix.fields.EncryptMethod;
import com.lmax.nanofix.fields.MsgType;
import com.lmax.nanofix.fields.SessionRejectReason;
import com.lmax.nanofix.fields.Tags;

import org.joda.time.DateTime;


import static com.lmax.nanofix.fields.Tags.BeginSeqNo;
import static com.lmax.nanofix.fields.Tags.BusinessRejectReason;
import static com.lmax.nanofix.fields.Tags.ClOrdID;
import static com.lmax.nanofix.fields.Tags.EncryptMethod;
import static com.lmax.nanofix.fields.Tags.EndSeqNo;
import static com.lmax.nanofix.fields.Tags.HeartBtInt;
import static com.lmax.nanofix.fields.Tags.MsgSeqNum;
import static com.lmax.nanofix.fields.Tags.OrdType;
import static com.lmax.nanofix.fields.Tags.OrderQty;
import static com.lmax.nanofix.fields.Tags.OrigSendingTime;
import static com.lmax.nanofix.fields.Tags.Password;
import static com.lmax.nanofix.fields.Tags.Price;
import static com.lmax.nanofix.fields.Tags.RefMsgType;
import static com.lmax.nanofix.fields.Tags.RefSeqNum;
import static com.lmax.nanofix.fields.Tags.ResetSeqNumFlag;
import static com.lmax.nanofix.fields.Tags.SecurityID;
import static com.lmax.nanofix.fields.Tags.SecurityIDSource;
import static com.lmax.nanofix.fields.Tags.SenderCompID;
import static com.lmax.nanofix.fields.Tags.SendingTime;
import static com.lmax.nanofix.fields.Tags.Side;
import static com.lmax.nanofix.fields.Tags.Symbol;
import static com.lmax.nanofix.fields.Tags.TargetCompID;
import static com.lmax.nanofix.fields.Tags.TestReqID;
import static com.lmax.nanofix.fields.Tags.TransactTime;
import static com.lmax.nanofix.fields.Tags.Username;


public class FixMessageBuilder
{
    private static final String SOH = "\u0001";
    private final StringBuilder msg = new StringBuilder();
    private final String version;

    private String messageLengthOverride;
    private String checksumOverride;

    public FixMessageBuilder()
    {
        this("FIX.4.4");
    }

    public FixMessageBuilder(final String version)
    {
        this.version = version;
    }

    public FixMessageBuilder messageType(final String type)
    {
        if (MsgType.knownMsgType(type))
        {
            throw new RuntimeException("Please use the MsgType enumeration");
        }
        return addTag(Tags.MsgType.getTag(), type);
    }

    public FixMessageBuilder overrideMessageLength(final String messageLengthOverride)
    {
        this.messageLengthOverride = messageLengthOverride;
        return this;
    }

    public FixMessageBuilder overrideChecksum(final String checksumOverride)
    {
        this.checksumOverride = checksumOverride;
        return this;
    }

    public FixMessageBuilder account(final String account)
    {
        return addTag(Tags.Account.getTag(), account);
    }

    public FixMessageBuilder messageType(final MsgType type)
    {
        return addTag(Tags.MsgType.getTag(), type.getCode());
    }

    public FixMessageBuilder senderCompID(final String senderCompID)
    {
        return addTag(SenderCompID.getTag(), senderCompID);
    }

    public FixMessageBuilder targetCompID(final String targetCompID)
    {
        return addTag(TargetCompID.getTag(), targetCompID);
    }

    public FixMessageBuilder beginSeqNo(final int beginSeqNo)
    {
        return addTag(BeginSeqNo.getTag(), Integer.toString(beginSeqNo));
    }

    public FixMessageBuilder endSeqNo(final int endSeqNo)
    {
        return addTag(EndSeqNo.getTag(), Integer.toString(endSeqNo));
    }

    public FixMessageBuilder refSeqNum(final int refSeqNum)
    {
        return addTag(RefSeqNum.getTag(), Integer.toString(refSeqNum));
    }

    public FixMessageBuilder testReqId(final String testReqId)
    {
        return addTag(TestReqID.getTag(), testReqId);
    }

    public FixMessageBuilder resetSeqNumFlag(final String resetSeqNumFlag)
    {
        return addTag(ResetSeqNumFlag.getTag(), resetSeqNumFlag);
    }

    public FixMessageBuilder msgSeqNum(final int msgSeqNum)
    {
        return addTag(MsgSeqNum.getTag(), Integer.toString(msgSeqNum));
    }

    public FixMessageBuilder msgSeqNum(final String msgSeqNum)
    {
        return addTag(MsgSeqNum.getTag(), msgSeqNum);
    }

    public FixMessageBuilder sendingTime(final DateTime sendingTime)
    {
        return addTag(SendingTime.getTag(), com.lmax.nanofix.FixUtil.DATE_TIME_FORMATTER.print(sendingTime));
    }

    public FixMessageBuilder username(final String username)
    {
        return addTag(Username.getTag(), username);
    }

    public FixMessageBuilder password(final String password)
    {
        return addTag(Password.getTag(), password);
    }

    public FixMessageBuilder heartBtInt(final int heartbeatInterval)
    {
        return addTag(HeartBtInt.getTag(), Integer.toString(heartbeatInterval));
    }

    public FixMessageBuilder encryptMethod(final EncryptMethod encryptMethod)
    {
        return addTag(EncryptMethod.getTag(), encryptMethod.getCode());
    }

    public FixMessageBuilder clOrdID(final String clOrdId)
    {
        return addTag(ClOrdID.getTag(), clOrdId);
    }

    public FixMessageBuilder symbol(final String symbol)
    {
        return addTag(Symbol.getTag(), symbol);
    }

    public FixMessageBuilder side(final com.lmax.nanofix.fields.Side side)
    {
        return addTag(Side.getTag(), Integer.toString(side.getCode()));
    }

    public FixMessageBuilder transactionTime(final DateTime transactionTime)
    {
        return addTag(TransactTime.getTag(), com.lmax.nanofix.FixUtil.DATE_TIME_FORMATTER.print(transactionTime));
    }

    public FixMessageBuilder orderQty(final BigDecimal orderQty)
    {
        return addTag(OrderQty.getTag(), orderQty.toPlainString());
    }

    public FixMessageBuilder orderType(final com.lmax.nanofix.fields.OrdType ordType)
    {
        return addTag(OrdType.getTag(), Integer.toString(ordType.getCode()));
    }

    public FixMessageBuilder price(final BigDecimal price)
    {
        return addTag(Price.getTag(), price.toPlainString());
    }

    public FixMessageBuilder securityID(final String symbol)
    {
        return addTag(SecurityID.getTag(), symbol);
    }

    public FixMessageBuilder securityIDSource(final String symbol)
    {
        return addTag(SecurityIDSource.getTag(), symbol);
    }

    public FixMessageBuilder refMsgType(final MsgType refMsgType)
    {
        return addTag(RefMsgType.getTag(), refMsgType.getCode());
    }

    public FixMessageBuilder businessRejectReason(final BusinessRejectionReason rejectionReason)
    {
        return addTag(BusinessRejectReason.getTag(), Integer.toString(rejectionReason.getCode()));
    }

    public FixMessageBuilder sessionRejectReason(final SessionRejectReason rejectReason)
    {
        return addTag(Tags.SessionRejectReason.getTag(), Integer.toString(rejectReason.getCode()));
    }

    public FixMessageBuilder possDuplicate(final boolean possDup)
    {
        return addTag(Tags.PossDupFlag.getTag(), possDup ? "Y" : "N");
    }

    public FixMessageBuilder origSendingTime(final DateTime origSendingTime)
    {
        return addTag(OrigSendingTime.getTag(), com.lmax.nanofix.FixUtil.DATE_TIME_FORMATTER.print(origSendingTime));
    }


    public FixMessageBuilder append(final int tag, final String value)
    {
        if (Tags.knownTag(tag))
        {
            throw new RuntimeException("Don't use append if there is a builder method, tag: " + tag + " value: " + value);
        }
        return addTag(tag, value);
    }

    private FixMessageBuilder addTag(final int tag, final String value)
    {
        msg.append(tag);
        msg.append("=");
        msg.append(value);
        msg.append(SOH);
        return this;
    }

    public FixMessage build()
    {
        final String body = msg.toString();
        final String messageWithHeader = "8=" + version + SOH + "9=" + messageLength(body) + SOH + body;
        final String messageWithChecksum = messageWithHeader + "10=" + checksum(messageWithHeader) + SOH;
        return new FixMessage(messageWithChecksum);
    }

    private String checksum(final String bodyWithLength)
    {
        if (checksumOverride != null)
        {
            return checksumOverride;
        }

        return String.format("%03d", checksum(bodyWithLength.getBytes(Charset.forName("UTF-8"))));
    }

    private int checksum(final byte[] bytes)
    {
        int result = 0;
        for (final byte b : bytes)
        {
            result += b;
        }
        return result % 256;
    }

    private String messageLength(final String body)
    {
        if (messageLengthOverride != null)
        {
            return messageLengthOverride;
        }

        return String.valueOf(body.length());
    }
}

