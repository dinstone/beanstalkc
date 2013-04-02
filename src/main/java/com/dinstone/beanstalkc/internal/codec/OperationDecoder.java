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

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Queue;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.dinstone.beanstalkc.internal.SessionUtil;
import com.dinstone.beanstalkc.internal.operation.Operation;

public class OperationDecoder extends CumulativeProtocolDecoder {

    private Charset charset;

    private String delimiter;

    /** An IoBuffer containing the delimiter */
    private IoBuffer delimBuf;

    public OperationDecoder(Charset charset) {
        this(charset, "\r\n");
    }

    public OperationDecoder(Charset charset, String delimiter) {
        this.charset = charset;
        this.delimiter = delimiter;

        IoBuffer tmp = IoBuffer.allocate(2).setAutoExpand(true);
        try {
            tmp.putString(this.delimiter, charset.newEncoder());
            tmp.flip();
        } catch (CharacterCodingException cce) {
        }
        delimBuf = tmp;
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        // Remember the initial position.
        int start = in.position();

        int matchCount = 0;
        int delimLen = delimBuf.limit();
        while (in.hasRemaining()) {
            byte current = in.get();

            if (current == delimBuf.get(matchCount)) {
                matchCount++;

                if (delimLen == matchCount) {
                    // Remember the current position and limit.
                    int position = in.position();
                    int limit = in.limit();
                    try {
                        in.position(start);
                        in.limit(position - delimLen);
                        // The bytes between in.position() and in.limit()
                        // can't contain a full CRLF terminated line.
                        parse(session, in.slice(), out);
                    } finally {
                        // Set the position to point right after the
                        // detected line and set the limit to the old
                        // one.
                        in.limit(limit);
                        in.position(position);
                    }
                    // Decoded one line; CumulativeProtocolDecoder will
                    // call me again until I return false. So just
                    // return true until there are no more lines in the
                    // buffer.
                    return true;
                }
            } else {
                // fix for DIRMINA-506 & DIRMINA-536
                in.position(Math.max(0, in.position() - matchCount));
                matchCount = 0;
            }

        }

        // Could not find CRLF in the buffer. Reset the initial
        // position to the one we recorded above.
        in.position(start);

        return false;
    }

    private void parse(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
        Queue<Operation<?>> queue = SessionUtil.getOperationQueue(session);
        Operation<?> operation = queue.peek();

        if (operation == null) {
            return;
        }

        boolean finish = operation.parseReply(charset, in);
        if (finish) {
            out.write(operation);
            queue.remove();
        }
    }
}