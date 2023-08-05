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
import java.util.List;
import java.util.Queue;

import com.dinstone.beanstalkc.UnknownCommandException;
import com.dinstone.beanstalkc.operation.Operation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageDecoder extends ByteToMessageDecoder {

    private Charset charset;

    private byte[] delimiter;

    public MessageDecoder(Charset charset) {
        this.charset = charset;
        this.delimiter = "\r\n".getBytes(charset);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Operation<?> operation = ChannelUtil.getOperationQueue(ctx.channel()).peek();
        if (operation == null) {
            throw new UnknownCommandException("unknown command");
        }

        int expect = operation.expectedBytes();
        if (expect == 0) {
            parseStatusLine(ctx.channel(), in, out);
        } else {
            parseDataBody(ctx.channel(), in, out, expect);
        }
    }

    private boolean parseDataBody(Channel channel, ByteBuf in, List<Object> out, int expect) {
        int delimLen = delimiter.length;
        int len = expect + delimLen;
        if (in.readableBytes() >= len) {
            // Remember the current position and limit.
            try {
                // The bytes between in.position() and in.limit()
                // can't contain a full CRLF terminated line.
                byte[] bs = new byte[expect];
                in.readBytes(bs);
                parse(channel, bs, out);
            } finally {
                in.readByte();
                in.readByte();
            }

            return true;
        }

        return false;
    }

    private boolean parseStatusLine(Channel session, ByteBuf in, List<Object> out) {
        // Remember the initial position.
        int start = in.readerIndex();
        in.markReaderIndex();

        byte previous = 0;
        while (in.isReadable()) {
            byte current = in.readByte();
            if (previous == delimiter[0] && current == delimiter[1]) {
                // Remember the current position and limit.
                int position = in.readerIndex();
                int length = position - start;
                // The bytes between in.position() and in.limit()
                // can't contain a full CRLF terminated line.
                parse(session, getBytes(in.slice(start, length)), out);

                // Decoded one line; CumulativeProtocolDecoder will
                // call me again until I return false. So just
                // return true until there are no more lines in the
                // buffer.
                return true;
            }
            previous = current;
        }

        // Could not find CRLF in the buffer. Reset the initial
        // position to the one we recorded above.
        in.resetReaderIndex();

        return false;
    }

    private byte[] getBytes(ByteBuf in) {
        byte[] bs = new byte[in.readableBytes()];
        in.readBytes(bs);
        return bs;
    }

    private void parse(Channel session, byte[] in, List<Object> out) {
        Queue<Operation<?>> queue = ChannelUtil.getOperationQueue(session);
        Operation<?> operation = queue.peek();
        if (operation.decodeResponse(charset, in)) {
            out.add(operation);
            queue.remove();
        }
    }

}
