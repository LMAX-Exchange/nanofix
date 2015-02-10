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

package com.lmax.nanofix.integration.fixture;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import com.lmax.nanofix.transport.ConnectionObserver;

public class SignallingConnectionObserver implements ConnectionObserver
{
    private final Lock lock;
    private final Condition connectionEstablishedCondition;
    private final Condition connectionClosedCondition;

    public SignallingConnectionObserver(final Lock lock, final Condition connectionEstablishedCondition, final Condition connectionClosedCondition)
    {
        this.lock = lock;
        this.connectionEstablishedCondition = connectionEstablishedCondition;
        this.connectionClosedCondition = connectionClosedCondition;
    }

    @Override
    public void connectionEstablished()
    {
        lock.lock();
        connectionEstablishedCondition.signalAll();
        lock.unlock();

    }

    @Override
    public void connectionClosed()
    {
        lock.lock();
        connectionClosedCondition.signalAll();
        lock.unlock();
    }
}
