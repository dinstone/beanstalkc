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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsOperation extends AbstractOperation<Map<String, String>> {

    private static final Logger LOG = LoggerFactory.getLogger(StatsOperation.class);

    private int length;

    public StatsOperation() {
        this.command = "stats";
    }

    public StatsOperation(long jobId) {
        this.command = "stats-job " + jobId;
    }

    public StatsOperation(String tubeName) {
        this.command = "stats-tube " + tubeName;
    }

    @Override
    public boolean decodeResponse(Charset charset, byte[] bytes) {
        try {
            if (length > 0) {
                future.complete(YamlUtil.yaml2Map(charset, new ByteArrayInputStream(bytes)));
                return true;
            }

            String line = new String(bytes, charset);
            LOG.debug("command is [{}], status is [{}]", command, line);

            if (line.startsWith("NOT_FOUND")) {
                future.complete(null);
                return true;
            }

            if (line.startsWith("OK")) {
                String[] tmp = line.split("\\s+");
                length = Integer.parseInt(tmp[1]);

                return false;
            }

            exceptionHandler(line);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.operation.AbstractOperation#expectedBytes()
     */
    @Override
    public int expectedBytes() {
        return length;
    }

}
