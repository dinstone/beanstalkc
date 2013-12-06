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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.dinstone.beanstalkc.BeanstalkClient;
import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.Job;
import com.dinstone.beanstalkc.internal.operation.BuryOperation;
import com.dinstone.beanstalkc.internal.operation.DeleteOperation;
import com.dinstone.beanstalkc.internal.operation.IgnoreOperation;
import com.dinstone.beanstalkc.internal.operation.KickOperation;
import com.dinstone.beanstalkc.internal.operation.ListTubeOperation;
import com.dinstone.beanstalkc.internal.operation.PeekOperation;
import com.dinstone.beanstalkc.internal.operation.PeekOperation.Type;
import com.dinstone.beanstalkc.internal.operation.PutOperation;
import com.dinstone.beanstalkc.internal.operation.QuitOperation;
import com.dinstone.beanstalkc.internal.operation.ReleaseOperation;
import com.dinstone.beanstalkc.internal.operation.ReserveOperation;
import com.dinstone.beanstalkc.internal.operation.StatsOperation;
import com.dinstone.beanstalkc.internal.operation.TouchOperation;
import com.dinstone.beanstalkc.internal.operation.UseOperation;
import com.dinstone.beanstalkc.internal.operation.WatchOperation;

/**
 * This is the client implementation of the beanstalkd protocol.
 * 
 * @author guojf
 * @version 1.0.0.2013-4-11
 */
public class DefaultBeanstalkClient implements BeanstalkClient {

    private Connection connection;

    private long operationTimeout;

    private Configuration config;

    /**
     * @param config
     */
    public DefaultBeanstalkClient(Configuration config) {
        this(config, null);
    }

    /**
     * @param config
     * @param initer
     */
    public DefaultBeanstalkClient(Configuration config, ConnectionInitializer initer) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        this.config = config;
        this.operationTimeout = config.getLong(Configuration.OPERATION_TIMEOUT, 1);

        ConnectionFactory factory = ConnectionFactory.getInstance();
        this.connection = factory.createConnection(config, initer);
    }

    // ************************************************************************
    // Produce methods
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
    @Override
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
        PutOperation operation = new PutOperation(priority, delay, ttr, data);
        OperationFuture<Long> future = connection.handle(operation);
        try {
            return future.get(operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ************************************************************************
    // Consumer methods
    // ************************************************************************

    @Override
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
    @Override
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
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
            return future.get(operationTimeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    // ************************************************************************
    // Other methods
    // ************************************************************************

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#kick(long)
     */
    @Override
    public long kick(long bound) {
        KickOperation operation = new KickOperation(bound);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#peek(long)
     */
    @Override
    public Job peek(long jobId) {
        PeekOperation operation = new PeekOperation(jobId);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#peekReady()
     */
    @Override
    public Job peekReady() {
        PeekOperation operation = new PeekOperation(Type.ready);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#peekDelayed()
     */
    @Override
    public Job peekDelayed() {
        PeekOperation operation = new PeekOperation(Type.delayed);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#peekBuried()
     */
    @Override
    public Job peekBuried() {
        PeekOperation operation = new PeekOperation(PeekOperation.Type.buried);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#statsJob(long)
     */
    @Override
    public Map<String, String> statsJob(long jobId) {
        StatsOperation operation = new StatsOperation(jobId);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#statsTube(java.lang.String)
     */
    @Override
    public Map<String, String> statsTube(String tubeName) {
        StatsOperation operation = new StatsOperation(tubeName);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#stats()
     */
    @Override
    public Map<String, String> stats() {
        StatsOperation operation = new StatsOperation();
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#listTubes()
     */
    @Override
    public List<String> listTubes() {
        ListTubeOperation operation = new ListTubeOperation(ListTubeOperation.Type.all);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#listTubeUsed()
     */
    @Override
    public String listTubeUsed() {
        ListTubeOperation operation = new ListTubeOperation(ListTubeOperation.Type.used);
        try {
            return connection.handle(operation).get().get(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.BeanstalkClient#listTubeWatched()
     */
    @Override
    public List<String> listTubeWatched() {
        ListTubeOperation operation = new ListTubeOperation(ListTubeOperation.Type.watched);
        try {
            return connection.handle(operation).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
