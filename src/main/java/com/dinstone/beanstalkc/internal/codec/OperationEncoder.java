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

package com.dinstone.beanstalkc.internal.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.dinstone.beanstalkc.internal.operation.Operation;

public class OperationEncoder extends ProtocolEncoderAdapter {

    private Charset charset;

    private String delimiter;

    public OperationEncoder(Charset charset, String delimiter) {
        this.charset = charset;
        this.delimiter = delimiter;
    }

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (message instanceof Operation) {
            Operation<?> operation = (Operation<?>) message;

            IoBuffer buffer = operation.commandBuffer(charset, delimiter);
            buffer.flip();

            out.write(buffer);
        }
    }
}
