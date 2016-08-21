package com.lmax.nanofix.outgoing;


import com.lmax.nanofix.fields.EncryptMethod;
import com.lmax.nanofix.fields.MsgType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FixMessageBuilderTest
{

    @Test
    public void shouldBuildFix44MessageByDefault() throws Exception
    {
        final FixMessage fixMessage = new FixMessageBuilder().messageType(MsgType.LOGIN).senderCompID("SenderCompID").
                targetCompID("TargetCompId").encryptMethod(EncryptMethod.NONE).heartBtInt(1).msgSeqNum(1).sendingTime(new DateTime(2016, 1, 2, 3, 4, DateTimeZone.UTC)).build();
        final String expectedFixMessage = fixMessage.toFixString();
        assertThat(expectedFixMessage, is("8=FIX.4.4\u00019=78\u000135=A\u0001" +
                                          "49=SenderCompID\u000156=TargetCompId\u000198=0\u0001108=1\u000134=1\u000152=20160102-03:04:00.000\u000110=214\u0001"));
    }

    @Test
    public void shouldBuildFix42MessageWhenSpecified() throws Exception
    {
        final FixMessage fixMessage = new FixMessageBuilder("FIX.4.2").messageType(MsgType.LOGIN).senderCompID("SenderCompID").
                targetCompID("TargetCompId").encryptMethod(EncryptMethod.NONE).heartBtInt(1).msgSeqNum(1).sendingTime(new DateTime(2016, 1, 2, 3, 4, DateTimeZone.UTC)).build();
        final String expectedFixMessage = fixMessage.toFixString();
        assertThat(expectedFixMessage, is("8=FIX.4.2\u00019=78\u000135=A\u0001" +
                                          "49=SenderCompID\u000156=TargetCompId\u000198=0\u0001108=1\u000134=1\u000152=20160102-03:04:00.000\u000110=212\u0001"));
    }

    @Test
    public void shouldBuildFix424MessageWithRawData() throws Exception
    {
        final FixMessage fixMessage = new FixMessageBuilder("FIX.4.2").messageType(MsgType.LOGIN).senderCompID("SenderCompID").
                targetCompID("TargetCompId").encryptMethod(EncryptMethod.NONE).rawData("RawData").heartBtInt(1).msgSeqNum(1).sendingTime(new DateTime(2016, 1, 2, 3, 4, DateTimeZone.UTC)).build();
        final String expectedFixMessage = fixMessage.toFixString();
        assertThat(expectedFixMessage, is("8=FIX.4.2\u00019=94\u000135=A\u000149=SenderCompID\u000156=TargetCompId\u0001" +
                                          "98=0\u000195=7\u000196=RawData\u0001108=1\u000134=1\u000152=20160102-03:04:00.000\u000110=006\u0001"));
    }

    @Test
    public void shouldBeAbleToBuildMessageWithArbitaryTagAndValue() throws Exception
    {
        final FixMessage fixMessage = new FixMessageBuilder("FIX.99").append(666666, "JunkData").build();
        final String expectedFixMessage = fixMessage.toFixString();
        assertThat(expectedFixMessage, is("8=FIX.99\u00019=16\u0001666666=JunkData\u000110=111\u0001"));
    }

    @Test
    public void shouldBuildAnInvalidFixMessageBecauseOfTagOrder()
    {
        FixMessage fixMessage = new FixMessageBuilder("FIX.4.2")
                .msgSeqNum(100)
                .append(123, "Y")
                .append(35, "4")
                .append(36, "10")
                .build();

        assertThat(fixMessage.toFixString(), is("8=FIX.4.2\u00019=24\u000134=100\u0001123=Y\u000135=4\u000136=10\u000110=065\u0001"));
    }
}