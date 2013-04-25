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

package com.dinstone.beanstalkc.internal;

import java.util.concurrent.TimeUnit;

import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.Job;
import com.dinstone.beanstalkc.JobConsumer;
import com.dinstone.beanstalkc.JobProducer;
import com.dinstone.beanstalkc.internal.operation.AbstractOperation;
import com.dinstone.beanstalkc.internal.operation.BuryOperation;
import com.dinstone.beanstalkc.internal.operation.DeleteOperation;
import com.dinstone.beanstalkc.internal.operation.IgnoreOperation;
import com.dinstone.beanstalkc.internal.operation.PutOperation;
import com.dinstone.beanstalkc.internal.operation.QuitOperation;
import com.dinstone.beanstalkc.internal.operation.ReleaseOperation;
import com.dinstone.beanstalkc.internal.operation.ReserveOperation;
import com.dinstone.beanstalkc.internal.operation.TouchOperation;
import com.dinstone.beanstalkc.internal.operation.UseOperation;
import com.dinstone.beanstalkc.internal.operation.WatchOperation;

/**
 * This is the client implementation of the beanstalkd protocol.
 * 
 * @author guojf
 * @version 1.0.0.2013-4-11
 */
public class BeanstalkClient implements JobProducer, JobConsumer {

    private Connection connection;

    private long optionTimeout;

    private Configuration config;

    /**
     * @param config
     */
    public BeanstalkClient(Configuration config) {
        this(config, null);
    }

    /**
     * @param config
     * @param initer
     */
    public BeanstalkClient(Configuration config, ConnectionInitializer initer) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        this.config = config;
        this.optionTimeout = config.getLong(Configuration.OPTION_TIMEOUT, 1);

        ConnectionFactory factory = ConnectionFactory.getInstance();
        this.connection = factory.createConnection(config, initer);
    }

    // ************************************************************************
    // Consumer methods
    // ************************************************************************

    /**
     * The "use" command is for producers. Subsequent put commands will put jobs
     * into the tube specified by this command. If no use command has been
     * issued, jobs will be put into the tube named "default".
     * 
     * @param tube
     *        is a name at most 200 bytes. It specifies the tube to use. If the
     *        tube does not exist, it will be created.
     * @return
     */
    public boolean useTube(String tube) {
        UseOperation operation = new UseOperation(tube);
        return getBoolean(connection.handle(operation));
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.JobProducer#putJob(int, int, int, byte[])
     */
    @Override
    public long putJob(int priority, int delay, int ttr, byte[] data) {
        AbstractOperation<Long> operation = new PutOperation(priority, delay, ttr, data);
        OperationFuture<Long> future = connection.handle(operation);
        try {
            return future.get(optionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ************************************************************************
    // Consumer methods
    // ************************************************************************

    public boolean ignoreTube(String tube) {
        IgnoreOperation operation = new IgnoreOperation(tube);
        return getBoolean(connection.handle(operation));
    }

    /**
     * The "watch" command adds the named tube to the watch list for the current
     * connection. A reserve command will take a job from any of the tubes in
     * the watch list. For each new connection, the watch list initially
     * consists of one tube, named "default".
     * 
     * @param tube
     * @return
     */
    public boolean watchTube(String tube) {
        WatchOperation operation = new WatchOperation(tube);
        return getBoolean(connection.handle(operation));
    }

    @Override
    public boolean deleteJob(long id) {
        DeleteOperation operation = new DeleteOperation(id);
        return getBoolean(connection.handle(operation));
    }

    /**
     * The "touch" command allows a worker to request more time to work on a
     * job. This is useful for jobs that potentially take a long time, but you
     * still want the benefits of a TTR pulling a job away from an unresponsive
     * worker. A worker may periodically tell the server that it's still alive
     * and processing a job (e.g. it may do this on DEADLINE_SOON).
     * 
     * @param id
     *        is the ID of a job reserved by the current connection.
     * @return
     */
    @Override
    public boolean touchJob(long id) {
        TouchOperation operation = new TouchOperation(id);
        return getBoolean(connection.handle(operation));
    }

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

    @Override
    public boolean releaseJob(long id, int priority, int delay) {
        ReleaseOperation operation = new ReleaseOperation(id, priority, delay);
        return getBoolean(connection.handle(operation));
    }

    @Override
    public boolean buryJob(long id, int priority) {
        BuryOperation operation = new BuryOperation(id, priority);
        return getBoolean(connection.handle(operation));
    }

    @Override
    public void close() {
        connection.close();

        ConnectionFactory factory = ConnectionFactory.getInstance();
        factory.releaseConnection(config);
    }

    public void quit() {
        QuitOperation operation = new QuitOperation();
        getBoolean(connection.handle(operation));
    }

    private boolean getBoolean(OperationFuture<Boolean> future) {
        try {
            return future.get(optionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }
}
