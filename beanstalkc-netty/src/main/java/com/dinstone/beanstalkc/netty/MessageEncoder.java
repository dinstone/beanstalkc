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
package com.dinstone.beanstalkc.netty;

import java.nio.charset.Charset;

import com.dinstone.beanstalkc.operation.Operation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Operation<?>> {

    private Charset charset;

    private byte[] delimiter;

    public MessageEncoder(Charset charset) {
        this.charset = charset;
        this.delimiter = "\r\n".getBytes(charset);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Operation<?> operation, ByteBuf out) throws Exception {
        byte[] buffer = operation.encodeRequest(charset, delimiter);
        out.writeBytes(buffer);
    }

}
