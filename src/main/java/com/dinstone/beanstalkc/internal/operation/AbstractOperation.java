/*
 * Copyright (C) 2012~2013 dinstone<dinstone@163.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dinstone.beanstalkc.internal.operation;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;

import com.dinstone.beanstalkc.internal.OperationFuture;

public class AbstractOperation<R> implements Operation<R> {

    protected final Charset charset;

    protected final OperationFuture<R> future;

    protected String command;

    public AbstractOperation(OperationFuture<R> future) {
        this.charset = Charset.forName("utf-8");
        this.future = future;
    }

    @Override
    public IoBuffer commandBuffer(Charset charset, String delimiter) {
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

}
