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
import java.util.Arrays;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.beanstalkc.internal.OperationFuture;
import com.dinstone.beanstalkc.internal.YamlUtil;

public class ListTubeOperation extends AbstractOperation<List<String>> {

    private static final Logger LOG = LoggerFactory.getLogger(ListTubeOperation.class);

    public static enum Type {
        all, used, watched
    }

    private int length;

    public ListTubeOperation(Type type) {
        super(new OperationFuture<List<String>>());
        if (type == Type.all) {
            this.command = "list-tubes";
        } else if (type == Type.used) {
            this.command = "list-tube-used";
        } else if (type == Type.watched) {
            this.command = "list-tubes-watched";
        }
    }

    @Override
    public boolean parseReply(Charset charset, IoBuffer in) {

        try {
            if (length > 0) {
                future.setResult(YamlUtil.yaml2List(charset, in));
                return true;
            }

            String line = in.getString(charset.newDecoder());
            LOG.debug("command is [{}], reply is [{}]", command, line);

            if (line.startsWith("USING")) {
                String[] tmp = line.split("\\s+");
                future.setResult(Arrays.asList(tmp[1]));
                return true;
            }

            if (line.startsWith("OK")) {
                String[] tmp = line.split("\\s+");
                length = Integer.parseInt(tmp[1]);
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
        return length;
    }

}
