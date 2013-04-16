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

package com.dinstone.beanstalkc;

import java.util.concurrent.TimeUnit;

import com.dinstone.beanstalkc.internal.Connection;
import com.dinstone.beanstalkc.internal.ConnectionFactory;
import com.dinstone.beanstalkc.internal.ConnectionInitializer;
import com.dinstone.beanstalkc.internal.OperationFuture;
import com.dinstone.beanstalkc.internal.operation.PutOperation;
import com.dinstone.beanstalkc.internal.operation.UseOperation;

/**
 * @author guojf
 * @version 1.0.0.2013-4-15
 */
public class JobProducer implements IJobProducer {

    private Configuration config;

    private long optionTimeout;

    private Connection connection;

    public JobProducer(final Configuration config, final String useTube) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        this.config = config;
        this.optionTimeout = config.getLong(Configuration.OPTION_TIMEOUT, 3);

        this.connection = ConnectionFactory.getInstance().createConnection(config, new ConnectionInitializer() {

            @Override
            public void initConnection(Connection connection) {
                if (useTube != null) {
                    try {
                        connection.handle(new UseOperation(useTube)).get();
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobProducer#putJob(int, int, int, byte[])
     */
    @Override
    public long putJob(int priority, int delay, int ttr, byte[] data) {
        PutOperation operation = new PutOperation(priority, delay, ttr, data);
        OperationFuture<Long> future = connection.handle(operation);
        try {
            return future.get(optionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobProducer#close()
     */
    @Override
    public void close() {
        connection.close();

        ConnectionFactory factory = ConnectionFactory.getInstance();
        factory.releaseConnection(config);
    }

}
