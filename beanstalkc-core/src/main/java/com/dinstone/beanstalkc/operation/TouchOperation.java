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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TouchOperation extends AbstractOperation<Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(TouchOperation.class);

    public TouchOperation(long id) {
        this.command = "touch " + id;
    }

    @Override
    public boolean decodeResponse(Charset charset, byte[] bytes) {
        try {
            String line = new String(bytes, charset);
            LOG.debug("command is [{}], status is [{}]", command, line);

            if (line.startsWith("TOUCHED")) {
                future.complete(true);
                return true;
            } else if (line.startsWith("NOT_FOUND")) {
                future.complete(false);
                return true;
            }

            exceptionHandler(line);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return true;
    }

}
