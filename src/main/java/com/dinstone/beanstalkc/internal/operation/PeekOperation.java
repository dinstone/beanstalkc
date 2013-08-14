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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.beanstalkc.Job;
import com.dinstone.beanstalkc.internal.OperationFuture;

public class PeekOperation extends AbstractOperation<Job> {

    private static final Logger LOG = LoggerFactory.getLogger(PeekOperation.class);

    public static enum Type {
        ready, delayed, buried
    }

    private static class Peeked {

        long id;

        int length;
    }

    private Peeked peeked;

    public PeekOperation(long jobId) {
        super(new OperationFuture<Job>());
        this.command = "peek " + jobId;
    }

    public PeekOperation(Type type) {
        super(new OperationFuture<Job>());
        if (type == Type.ready) {
            this.command = "peek-ready";
        } else if (type == Type.delayed) {
            this.command = "peek-delayed";
        } else if (type == Type.buried) {
            this.command = "peek-buried";
        }
    }

    @Override
    public boolean parseReply(Charset charset, IoBuffer in) {
        if (peeked != null) {
            Job job = new Job();
            job.setId(peeked.id);

            byte[] data = new byte[peeked.length];
            in.get(data, 0, peeked.length);
            job.setData(data);

            future.setResult(job);
            return true;
        }

        try {
            String line = in.getString(charset.newDecoder());
            LOG.debug("command is [{}], reply is [{}]", command, line);

            if (line.startsWith("NOT_FOUND")) {
                future.setResult(null);
                return true;
            }

            if (line.startsWith("FOUND")) {
                peeked = new Peeked();

                String[] tmp = line.split("\\s+");
                peeked.id = Long.parseLong(tmp[1]);
                peeked.length = Integer.parseInt(tmp[2]);

                return false;
            }

            future.setException(new RuntimeException(line));
        } catch (Exception e) {
            future.setException(e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.internal.operation.AbstractOperation#expect()
     */
    @Override
    public int expect() {
        if (peeked != null) {
            return peeked.length;
        }

        return 0;
    }

}
