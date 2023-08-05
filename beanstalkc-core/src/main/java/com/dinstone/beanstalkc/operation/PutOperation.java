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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.beanstalkc.BadFormatException;
import com.dinstone.beanstalkc.OutOfMemoryException;

public class PutOperation extends AbstractOperation<Long> {

    private static final Logger LOG = LoggerFactory.getLogger(PutOperation.class);

    public PutOperation(int priority, int delay, int ttr, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        this.command = "put " + priority + " " + delay + " " + ttr + " " + data.length;
        this.data = data;
    }

    @Override
    public byte[] encodeRequest(Charset charset, byte[] delimiter) {
        byte[] bytes = command.getBytes(charset);

        int capacity = bytes.length + 2 * delimiter.length + data.length;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(bytes).put(delimiter).put(data).put(delimiter).flip();
        return buffer.array();
    }

    @Override
    public boolean decodeResponse(Charset charset, byte[] bytes) {
        try {
            String line = new String(bytes, charset);
            LOG.debug("command is [{}], status is [{}]", command, line);

            if (line.startsWith("INSERTED")) {
                long id = Long.parseLong(line.replaceAll("[^0-9]", ""));
                future.complete(id);
                return true;
            }

            if (line.startsWith("BURIED")) {
                future.completeExceptionally(new OutOfMemoryException("this job is buried"));
                return true;
            }

            if (line.startsWith("EXPECTED_CRLF")) {
                future.completeExceptionally(new BadFormatException("the job body must be followed by a CR-LF pair"));
                return true;
            }

            if (line.startsWith("JOB_TOO_BIG")) {
                future.completeExceptionally(
                        new BadFormatException("the job's size is larger than max-job-size bytes."));
                return true;
            }

            exceptionHandler(line);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return true;
    }
}
