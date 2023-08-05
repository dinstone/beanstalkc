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

public class KickOperation extends AbstractOperation<Long> {

    private static final Logger LOG = LoggerFactory.getLogger(KickOperation.class);

    public KickOperation(long bound) {
        this.command = "kick " + bound;
    }

    @Override
    public boolean decodeResponse(Charset charset, byte[] bytes) {
        try {
            String line = new String(bytes, charset);
            LOG.debug("command is [{}], status is [{}]", command, line);

            if (line.startsWith("KICKED")) {
                long id = Long.parseLong(line.replaceAll("[^0-9]", ""));
                future.complete(id);
                return true;
            }

            exceptionHandler(line);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return true;
    }

}
