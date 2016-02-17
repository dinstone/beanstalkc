/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

import com.dinstone.beanstalkc.BadFormatException;
import com.dinstone.beanstalkc.OutOfMemoryException;
import com.dinstone.beanstalkc.internal.OperationFuture;

public class PutOperation extends AbstractOperation<Long> {

    private static final Logger LOG = LoggerFactory.getLogger(PutOperation.class);

    public PutOperation(int priority, int delay, int ttr, byte[] data) {
        super(new OperationFuture<Long>());
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        this.command = "put " + priority + " " + delay + " " + ttr + " " + data.length;
        this.data = data;
    }

    @Override
    public IoBuffer prepareRequest(Charset charset, String delimiter) {
        byte[] delBytes = delimiter.getBytes(charset);

        IoBuffer buffer = IoBuffer.allocate(64);
        buffer.setAutoExpand(true);

        buffer.put(command.getBytes(charset));
        buffer.put(delBytes);
        buffer.put(data);
        buffer.put(delBytes);

        return buffer;
    }

    @Override
    public boolean parseReply(Charset charset, IoBuffer in) {
        try {
            String line = in.getString(charset.newDecoder());
            LOG.debug("command is [{}], reply is [{}]", command, line);

            if (line.startsWith("INSERTED")) {
                long id = Long.parseLong(line.replaceAll("[^0-9]", ""));
                future.setResult(id);
                return true;
            }

            if (line.startsWith("BURIED")) {
                future.setException(new OutOfMemoryException("this job is buried"));
                return true;
            }

            if (line.startsWith("EXPECTED_CRLF")) {
                future.setException(new BadFormatException("the job body must be followed by a CR-LF pair"));
                return true;
            }

            if (line.startsWith("JOB_TOO_BIG")) {
                future.setException(new BadFormatException("the job's size is larger than max-job-size bytes."));
                return true;
            }

            exceptionHandler(line);
        } catch (Exception e) {
            future.setException(e);
        }
        return true;
    }
}
