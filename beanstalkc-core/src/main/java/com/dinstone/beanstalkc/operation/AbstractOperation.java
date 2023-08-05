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
import java.util.concurrent.CompletableFuture;

import com.dinstone.beanstalkc.BadFormatException;
import com.dinstone.beanstalkc.DrainingException;
import com.dinstone.beanstalkc.InternalErrorException;
import com.dinstone.beanstalkc.OutOfMemoryException;
import com.dinstone.beanstalkc.UnknownCommandException;

/**
 * Beanstalk protocol details please refer to the link:
 * <code> https://github.com/kr/beanstalkd/blob/master/doc/protocol.txt
 * </code>
 * 
 * @author guojf
 * 
 * @version 1.0.0.2013-4-11
 */
public class AbstractOperation<R> implements Operation<R> {

    protected final CompletableFuture<R> future = new CompletableFuture<>();

    protected String command;

    protected byte[] data;

    @Override
    public byte[] encodeRequest(Charset charset, byte[] delimiter) {
        byte[] bytes = command.getBytes(charset);

        int capacity = bytes.length + delimiter.length;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(bytes).put(delimiter).flip();
        return buffer.array();
    }

    @Override
    public boolean decodeResponse(Charset charset, byte[] bytes) {
        return true;
    }

    /**
     * the command to get
     * 
     * @return the command
     * 
     * @see AbstractOperation#command
     */
    public String getCommand() {
        return command;
    }

    @Override
    public CompletableFuture<R> getFuture() {
        return future;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.operation.Operation#expectedBytes()
     */
    @Override
    public int expectedBytes() {
        return 0;
    }

    protected void exceptionHandler(String line) {
        if (line.startsWith("OUT_OF_MEMORY")) {
            future.completeExceptionally(new OutOfMemoryException());
        } else if (line.startsWith("INTERNAL_ERROR")) {
            future.completeExceptionally(new InternalErrorException());
        } else if (line.startsWith("DRAINING")) {
            future.completeExceptionally(new DrainingException());
        } else if (line.startsWith("BAD_FORMAT")) {
            future.completeExceptionally(new BadFormatException());
        } else {
            future.completeExceptionally(new UnknownCommandException(line));
        }
    }

}
