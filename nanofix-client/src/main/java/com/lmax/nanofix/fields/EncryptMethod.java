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

public enum EncryptMethod {
    NONE(0),
    PKCS(1),
    DES(2),
    PKCS_DES(3),
    PGP_DES(4),
    PGP_DES_MD5(5),
    PEM_DES_MD5(6);

    private final int code;

    EncryptMethod(final int code) {
        this.code = code;
    }

    public String getCode() {
        return Integer.toString(code);
    }
}
