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

package com.lmax.nanofix.transport;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class PublishingConnectionObserver implements ConnectionObserver {

    private final Set<ConnectionObserver> observers = new CopyOnWriteArraySet<ConnectionObserver>();

    public void addObserver(final ConnectionObserver connectionObserver) {
        observers.add(connectionObserver);
    }

    public void removeObserver(final ConnectionObserver connectionObserver) {
        observers.remove(connectionObserver);
    }

    @Override
    public void connectionEstablished() {
        for (ConnectionObserver observer : observers) {
            observer.connectionEstablished();
        }
    }

    @Override
    public void connectionClosed() {
        for (ConnectionObserver observer : observers) {
            observer.connectionClosed();
        }
    }
}
