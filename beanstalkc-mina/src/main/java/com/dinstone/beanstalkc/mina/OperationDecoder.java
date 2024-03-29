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
package com.dinstone.beanstalkc.mina;

import java.nio.charset.Charset;
import java.util.Queue;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.dinstone.beanstalkc.UnknownCommandException;
import com.dinstone.beanstalkc.operation.Operation;

public class OperationDecoder extends CumulativeProtocolDecoder {

    private Charset charset;

    private byte[] delimiter;

    public OperationDecoder(Charset charset) {
        this.charset = charset;
        this.delimiter = "\r\n".getBytes(charset);
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        Operation<?> operation = SessionUtil.getOperationQueue(session).peek();
        if (operation == null) {
            throw new UnknownCommandException("unknown command");
        }

        int expect = operation.expectedBytes();
        if (expect == 0) {
            return parseStatusLine(session, in, out);
        } else {
            return parseDataBody(session, in, out, expect);
        }
    }

    /**
     * @param session
     * @param in
     * @param out
     * @param expect
     * 
     * @return
     */
    private boolean parseDataBody(IoSession session, IoBuffer in, ProtocolDecoderOutput out, int expect) {
        int delimLen = delimiter.length;
        int len = expect + delimLen;
        if (in.remaining() >= len) {
            // Remember the current position and limit.
            int position = in.position();
            int limit = in.limit();
            try {
                in.limit(position + len);
                // The bytes between in.position() and in.limit()
                // can't contain a full CRLF terminated line.
                parse(session, getBytes(in.slice()), out);
            } finally {
                // Set the position to point right after the
                // detected line and set the limit to the old
                // one.
                in.limit(limit);
                in.position(position + len);
            }

            return true;
        }

        return false;
    }

    /**
     * @param session
     * @param in
     * @param out
     * 
     * @return
     */
    private boolean parseStatusLine(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
        // Remember the initial position.
        int start = in.position();

        byte previous = 0;
        while (in.hasRemaining()) {
            byte current = in.get();

            if (previous == delimiter[0] && current == delimiter[1]) {
                // Remember the current position and limit.
                int position = in.position();
                int limit = in.limit();
                try {
                    in.position(start);
                    in.limit(position - 2);
                    // The bytes between in.position() and in.limit()
                    // can't contain a full CRLF terminated line.
                    parse(session, getBytes(in.slice()), out);
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

            previous = current;
        }

        // Could not find CRLF in the buffer. Reset the initial
        // position to the one we recorded above.
        in.position(start);

        return false;
    }

    private byte[] getBytes(IoBuffer in) {
        byte[] bs = new byte[in.limit()];
        in.get(bs);
        return bs;
    }

    private void parse(IoSession session, byte[] in, ProtocolDecoderOutput out) {
        Queue<Operation<?>> queue = SessionUtil.getOperationQueue(session);
        Operation<?> operation = queue.peek();
        if (operation.decodeResponse(charset, in)) {
            out.write(operation);
            queue.remove();
        }
    }
}