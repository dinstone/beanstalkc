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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.RuntimeIoException;

public class OperationFuture<T> {

    private T result;

    private Throwable exception;

    private Lock lock = new ReentrantLock();

    private Condition ready = lock.newCondition();

    private boolean done;

    public boolean isDone() {
        lock.lock();
        try {
            return done;
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (!done) {
                ready.await();
            }
            return getValue();
        } finally {
            lock.unlock();
        }
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            if (!done) {
                boolean success = ready.await(timeout, unit);
                if (!success) {
                    throw new TimeoutException("operation timeout (" + timeout + " " + unit + ")");
                }
            }
            return getValue();
        } finally {
            lock.unlock();
        }
    }

    private T getValue() {
        if (result == null && exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            if (exception instanceof Error) {
                throw (Error) exception;
            }
            if (exception instanceof IOException || exception instanceof Exception) {
                throw new RuntimeIoException(exception);
            }
        }

        return result;
    }

    /**
     * the result to set
     * 
     * @param result
     * @see OperationFuture#result
     */
    public void setResult(T result) {
        lock.lock();
        try {
            if (!done) {
                this.result = result;
                done = true;
                this.ready.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * the exception to set
     * 
     * @param exception
     * @see OperationFuture#exception
     */
    public void setException(Throwable exception) {
        lock.lock();
        try {
            if (!done) {
                this.exception = exception;
                done = true;
                this.ready.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

}
