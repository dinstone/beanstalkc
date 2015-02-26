/*
 * Copyright (C) 2012~2015 dinstone<dinstone@163.com>
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

import com.dinstone.beanstalkc.Job;
import com.dinstone.beanstalkc.internal.OperationFuture;

public class ReserveOperation extends AbstractOperation<Job> {

    private static final Logger LOG = LoggerFactory.getLogger(ReserveOperation.class);

    private static class Reserved {

        long id;

        int length;
    }

    private Reserved reserved;

    public ReserveOperation(long timeout) {
        super(new OperationFuture<Job>());

        if (timeout > 0) {
            this.command = "reserve-with-timeout " + timeout;
        } else {
            this.command = "reserve";
        }
    }

    @Override
    public boolean parseReply(Charset charset, IoBuffer in) {
        if (reserved != null) {
            Job job = new Job();
            job.setId(reserved.id);

            byte[] data = new byte[reserved.length];
            in.get(data, 0, reserved.length);
            job.setData(data);

            future.setResult(job);
            return true;
        }

        try {
            String line = in.getString(charset.newDecoder());
            LOG.debug("command is [{}], reply is [{}]", command, line);

            if (line.startsWith("TIMED_OUT")) {
                future.setResult(null);
                return true;
            }

            if (line.startsWith("DEADLINE_SOON")) {
                future.setResult(null);
                return true;
            }

            if (line.startsWith("RESERVED")) {
                reserved = new Reserved();

                String[] tmp = line.split("\\s+");
                reserved.id = Long.parseLong(tmp[1]);
                reserved.length = Integer.parseInt(tmp[2]);

                return false;
            }

            exceptionHandler(line);
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
        if (reserved != null) {
            return reserved.length;
        }

        return 0;
    }

}
