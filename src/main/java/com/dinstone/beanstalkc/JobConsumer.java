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
import com.dinstone.beanstalkc.internal.operation.BuryOperation;
import com.dinstone.beanstalkc.internal.operation.DeleteOperation;
import com.dinstone.beanstalkc.internal.operation.IgnoreOperation;
import com.dinstone.beanstalkc.internal.operation.ReleaseOperation;
import com.dinstone.beanstalkc.internal.operation.ReserveOperation;
import com.dinstone.beanstalkc.internal.operation.TouchOperation;
import com.dinstone.beanstalkc.internal.operation.WatchOperation;

/**
 * @author guojf
 * @version 1.0.0.2013-4-15
 */
public class JobConsumer implements IJobConsumer {

    private final boolean ignoreDefault;

    private final long optionTimeout;

    private Connection connection;

    private Configuration config;

    public JobConsumer(final Configuration config, final String... watchTubes) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        this.config = config;
        this.ignoreDefault = config.getBoolean("IgnoreDefaultTube", true);
        this.optionTimeout = config.getLong(Configuration.OPTION_TIMEOUT, 3);

        this.connection = ConnectionFactory.getInstance().createConnection(config, new ConnectionInitializer() {

            @Override
            public void initConnection(Connection connection) {
                if (watchTubes != null && watchTubes.length > 0) {
                    for (int i = 0; i < watchTubes.length; i++) {
                        try {
                            connection.handle(new WatchOperation(watchTubes[i])).get();
                        } catch (Exception e) {
                        }
                    }
                }

                if (ignoreDefault) {
                    try {
                        connection.handle(new IgnoreOperation("default")).get();
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobConsumer#reserveJob(long)
     */
    @Override
    public Job reserveJob(long timeout) {
        ReserveOperation operation = new ReserveOperation(timeout);
        OperationFuture<Job> future = connection.handle(operation);
        try {
            return future.get(optionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobConsumer#deleteJob(long)
     */
    @Override
    public boolean deleteJob(long id) {
        return getBoolean(connection.handle(new DeleteOperation(id)));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobConsumer#releaseJob(long, int, int)
     */
    @Override
    public boolean releaseJob(long id, int priority, int delay) {
        return getBoolean(connection.handle(new ReleaseOperation(id, priority, delay)));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobConsumer#buryJob(long, int)
     */
    @Override
    public boolean buryJob(long id, int priority) {
        return getBoolean(connection.handle(new BuryOperation(id, priority)));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobConsumer#touchJob(long)
     */
    @Override
    public boolean touchJob(long id) {
        return getBoolean(connection.handle(new TouchOperation(id)));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.IJobConsumer#close()
     */
    @Override
    public void close() {
        connection.close();

        ConnectionFactory factory = ConnectionFactory.getInstance();
        factory.releaseConnection(config);
    }

    private boolean getBoolean(OperationFuture<Boolean> future) {
        try {
            return future.get(optionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

}
