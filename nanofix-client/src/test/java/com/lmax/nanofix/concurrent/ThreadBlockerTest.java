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

package com.lmax.nanofix.concurrent;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class ThreadBlockerTest {

    private volatile boolean running = true;

    @Test
    public void threadShouldSwitchBetweenWaitingAndRunningStatesWhenRequested() {
        final ThreadBlocker threadBlocker = new ThreadBlocker();
        final Thread thread = new Thread(() -> {
            while (running) {
                threadBlocker.mayWait();
            }
        });
        Assert.assertThat(thread.getState(), is(Thread.State.NEW));
        thread.start();
        waitForThreadState(thread, Thread.State.RUNNABLE, 100);
        threadBlocker.pause();
        waitForThreadState(thread, Thread.State.WAITING, 100);
        threadBlocker.resume();
        waitForThreadState(thread, Thread.State.RUNNABLE, 100);
        threadBlocker.pause();
        waitForThreadState(thread, Thread.State.WAITING, 100);
        threadBlocker.resume();
        waitForThreadState(thread, Thread.State.RUNNABLE, 100);
        running = false;
        waitForThreadState(thread, Thread.State.TERMINATED, 100);

    }

    private void waitForThreadState(final Thread thread, final Thread.State expectedState, final int timeoutMillis) {
        final int numberOfRetries = 10;
        final int waitBetweenRetries = timeoutMillis / 10;
        for (int i = 0; i < numberOfRetries; i++) {
            if (thread.getState() == expectedState) {
                return;
            } else {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(waitBetweenRetries));
            }
        }
        Assert.fail(String.format("Thread : %s did not match expected state %s actual state %s",
                                  thread.getName(), expectedState.name(), thread.getState().name()));
    }
}
