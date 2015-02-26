/*
 * Copyright (C) 2012~2015 dinstone<dinstone@163.com>
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

package com.dinstone.beanstalkc.internal.operation;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.beanstalkc.internal.OperationFuture;

public class DeleteOperation extends AbstractOperation<Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteOperation.class);

    public DeleteOperation(long id) {
        super(new OperationFuture<Boolean>());
        this.command = "delete " + id;
    }

    @Override
    public boolean parseReply(Charset charset, IoBuffer in) {
        try {
            String line = in.getString(charset.newDecoder());
            LOG.debug("command is [{}], reply is [{}]", command, line);

            if (line.startsWith("DELETED")) {
                future.setResult(true);
                return true;
            } else if (line.startsWith("NOT_FOUND")) {
                future.setResult(false);
                return true;
            }

            exceptionHandler(line);
        } catch (Exception e) {
            future.setException(e);
        }

        return true;
    }

}
