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

import com.dinstone.beanstalkc.BadFormatException;
import com.dinstone.beanstalkc.DrainingException;
import com.dinstone.beanstalkc.InternalErrorException;
import com.dinstone.beanstalkc.OutOfMemoryException;
import com.dinstone.beanstalkc.UnknownCommandException;
import com.dinstone.beanstalkc.internal.OperationFuture;

/**
 * Beanstalk protocol details please refer to the link:
 * <code> https://github.com/kr/beanstalkd/blob/master/doc/protocol.txt
 * </code>
 * 
 * @author guojf
 * @version 1.0.0.2013-4-11
 */
public class AbstractOperation<R> implements Operation<R> {

    protected final OperationFuture<R> future;

    protected String command;

    protected byte[] data;

    public AbstractOperation(OperationFuture<R> future) {
        this.future = future;
    }

    @Override
    public IoBuffer prepareRequest(Charset charset, String delimiter) {
        IoBuffer buffer = IoBuffer.allocate(64);
        buffer.setAutoExpand(true);

        buffer.put(command.getBytes(charset));
        buffer.put(delimiter.getBytes(charset));

        return buffer;
    }

    @Override
    public boolean parseReply(Charset charset, IoBuffer in) {
        return true;
    }

    /**
     * the command to get
     * 
     * @return the command
     * @see AbstractOperation#command
     */
    public String getCommand() {
        return command;
    }

    @Override
    public OperationFuture<R> getOperationFuture() {
        return future;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.internal.operation.Operation#expect()
     */
    @Override
    public int expect() {
        return 0;
    }

    protected void exceptionHandler(String line) {
        if (line.startsWith("OUT_OF_MEMORY")) {
            future.setException(new OutOfMemoryException());
        } else if (line.startsWith("INTERNAL_ERROR")) {
            future.setException(new InternalErrorException());
        } else if (line.startsWith("DRAINING")) {
            future.setException(new DrainingException());
        } else if (line.startsWith("BAD_FORMAT")) {
            future.setException(new BadFormatException());
        } else {
            future.setException(new UnknownCommandException(line));
        }
    }

}
