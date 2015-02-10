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

import static junit.framework.Assert.assertFalse;

public class FixClientLifecycleTest
{
    @Test
    public void shouldBeAbleToStartListeningAgainAfterFirstStartingAndThenStopping() throws InterruptedException
    {
        Lock lock = new ReentrantLock();
        final Condition connectionClosedCondition = lock.newCondition();
        final Condition connectionEstablishedCondition = lock.newCondition();

        final FixClient fixClient = FixClientFactory.createFixClient(9990);
        fixClient.registerTransportObserver(new SignallingConnectionObserver(lock, connectionEstablishedCondition, connectionClosedCondition));


        fixClient.listen();

        try
        {
            lock.lock();
            final FixClient fixClient2 = FixClientFactory.createFixClient("localhost", 9990);
            fixClient2.connect();
            final boolean connectionEstablished = connectionEstablishedCondition.await(5, TimeUnit.SECONDS);
            Assert.assertTrue(connectionEstablished);
            fixClient2.killSocket();
            final boolean connectionClosed = connectionClosedCondition.await(5, TimeUnit.SECONDS);
            Assert.assertTrue(connectionClosed);
            fixClient.listen();
        }
        finally
        {
            lock.unlock();
        }
    }

    @Test
    public void shouldBeSafeToCloseAFixClientWhichHasNotLoggedOn()
    {
        // Passes by merit of not throwing an exception

        final FixClient fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(null, null));
        fixClient.close();
    }

    @Test
    public void shouldReportAnUnconnectedFixClientAsNotConnected()
    {
        final FixClient fixClient = FixClientFactory.createFixClient(new IntegrationSocketFactory(null, null));
        assertFalse(fixClient.isConnected());
    }
}
