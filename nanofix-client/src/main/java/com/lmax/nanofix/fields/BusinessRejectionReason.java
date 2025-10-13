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

public enum BusinessRejectionReason {
    Other(0),
    UnknownId(1),
    UnknownSecurity(2),
    UnsupportedMessageType(3),
    ApplicationNotAvailable(4),
    ConditionallyRequiredFieldMissing(5);

    private final int code;

    BusinessRejectionReason(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
