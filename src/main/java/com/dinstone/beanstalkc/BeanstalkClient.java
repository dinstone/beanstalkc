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
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.beanstalkc.internal.OperationConnection;
import com.dinstone.beanstalkc.internal.OperationFuture;
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

public class BeanstalkClient implements JobProducer, JobConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(BeanstalkClient.class);

    private OperationConnection connection;

    public BeanstalkClient(OperationConnection connection) {
        this.connection = connection;
    }

    public boolean useTube(String tube) {
        UseOperation operation = new UseOperation(tube);
        connection.handle(operation);
        return getBoolean(operation.getOperationFuture());
    }

    public boolean watchTube(String tube) {
        WatchOperation operation = new WatchOperation(tube);
        connection.handle(operation);
        return getBoolean(operation.getOperationFuture());
    }

    public boolean ignoreTube(String tube) {
        IgnoreOperation operation = new IgnoreOperation(tube);
        connection.handle(operation);
        return getBoolean(operation.getOperationFuture());
    }

    public long putJob(int priority, int delay, int ttr, byte[] data) {
        PutOperation operation = new PutOperation(priority, delay, ttr, data);
        connection.handle(operation);
        try {
            return operation.getOperationFuture().get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteJob(long id) {
        DeleteOperation operation = new DeleteOperation(id);
        connection.handle(operation);
        return getBoolean(operation.getOperationFuture());
    }

    public boolean touchJob(long id) {
        TouchOperation operation = new TouchOperation(id);
        connection.handle(operation);
        return getBoolean(operation.getOperationFuture());
    }

    public Job reserveJob(long timeout) {
        ReserveOperation operation = new ReserveOperation(timeout);
        connection.handle(operation);
        try {
            return operation.getOperationFuture().get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean releaseJob(long id, int priority, int delay) {
        ReleaseOperation operation = new ReleaseOperation(id, priority, delay);
        connection.handle(operation);
        return getBoolean(operation.getOperationFuture());
    }

    public boolean buryJob(long id, int priority) {
        BuryOperation operation = new BuryOperation(id, priority);
        connection.handle(operation);
        return getBoolean(operation.getOperationFuture());
    }

    public void quit() {
        QuitOperation operation = new QuitOperation();
        connection.handle(operation);
        try {
            Boolean f = operation.getOperationFuture().get(1, TimeUnit.SECONDS);
            LOG.debug("reply is {}", f);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getBoolean(OperationFuture<Boolean> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void close() {
        connection.close();
    }
}
