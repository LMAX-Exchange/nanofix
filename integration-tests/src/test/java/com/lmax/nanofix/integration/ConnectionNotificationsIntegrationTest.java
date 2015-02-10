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

package com.lmax.nanofix.integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.lmax.nanofix.FixClient;
import com.lmax.nanofix.FixClientFactory;
import com.lmax.nanofix.integration.fixture.IntegrationSocketFactory;
import com.lmax.nanofix.integration.fixture.SignallingConnectionObserver;

import org.junit.Assert;
import org.junit.Test;

public class ConnectionNotificationsIntegrationTest
{
    private ReadableByteChannel readableByteChannel;
    private WritableByteChannel writableByteChannel;
    private ByteArrayOutputStream byteArrayOutputStream;

    @Test
    public void shouldNotifyOnConnectionEstablished() throws Exception
    {
        FixClient fixClient;
        final Lock lock = new ReentrantLock();

        final Condition connectionEstablishedCondition = lock.newCondition();
        final Condition connectionClosedCondition = lock.newCondition();

        byteArrayOutputStream = new ByteArrayOutputStream();
        writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(new byte[0]));
        fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(readableByteChannel, writableByteChannel));
        fixClient.registerTransportObserver(new SignallingConnectionObserver(lock, connectionEstablishedCondition, connectionClosedCondition));

        try
        {
            //when
            lock.lock();
            fixClient.connect();
            final boolean connectionEstablished = connectionEstablishedCondition.await(5, TimeUnit.SECONDS);
            Assert.assertTrue(connectionEstablished);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Test
    public void shouldNotifyOnConnectionClosed() throws Exception
    {
        FixClient fixClient;
        final Lock lock = new ReentrantLock();

        final Condition connectionEstablishedCondition = lock.newCondition();
        final Condition connectionClosedCondition = lock.newCondition();

        byteArrayOutputStream = new ByteArrayOutputStream();
        writableByteChannel = Channels.newChannel(byteArrayOutputStream);
        readableByteChannel = Channels.newChannel(new ByteArrayInputStream(new byte[0]));
        fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(readableByteChannel, writableByteChannel));
        fixClient.registerTransportObserver(new SignallingConnectionObserver(lock, connectionEstablishedCondition, connectionClosedCondition));

        try
        {
            //when
            lock.lock();
            fixClient.connect();
            final boolean connectionClosed = connectionClosedCondition.await(5, TimeUnit.SECONDS);
            Assert.assertTrue(connectionClosed);
        }
        finally
        {
            lock.unlock();
        }
    }

}
