/*
 * Copyright (C) 2012~2023 dinstone<dinstone@163.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dinstone.beanstalkc.operation;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public interface Operation<R> {

    public byte[] encodeRequest(Charset charset, byte[] delimiter);

    public boolean decodeResponse(Charset charset, byte[] bytes);

    public CompletableFuture<R> getFuture();

    /**
     * expect byte size.
     * 
     * @return
     */
    public int expectedBytes();
}
