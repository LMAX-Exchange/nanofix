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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FixMessagePublisher implements FixMessageHandler {
    List<FixMessageHandler> handlers = new CopyOnWriteArrayList<>();

    public void subscribeToAllMessages(final FixMessageHandler fixMessageHandler) {
        handlers.add(fixMessageHandler);
    }

    @Override
    public void onFixMessage(final FixMessage fixMessage) {
        for (FixMessageHandler fixMessageHandler : handlers) {
            fixMessageHandler.onFixMessage(fixMessage);
        }
    }
}
