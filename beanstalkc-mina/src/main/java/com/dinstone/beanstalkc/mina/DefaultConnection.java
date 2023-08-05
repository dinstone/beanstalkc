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
package com.dinstone.beanstalkc.mina;

import java.util.concurrent.CompletableFuture;

import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.ConnectionException;
import com.dinstone.beanstalkc.connection.Connection;
import com.dinstone.beanstalkc.connection.Initializer;
import com.dinstone.beanstalkc.operation.Operation;

public class DefaultConnection implements Connection {

    private Configuration config;
    private Initializer initializer;
    private DefaultConnector connector;

    private IoSession ioSession;
    private boolean closed;

    public DefaultConnection(Configuration config, DefaultConnector connector, Initializer initializer) {
        this.config = config;
        this.connector = connector;
        this.initializer = initializer;
    }

    @Override
    public synchronized <T> CompletableFuture<T> handle(final Operation<T> operation) {
        checkAndReconnect();

        SessionUtil.getOperationQueue(ioSession).add(operation);
        WriteFuture writeFuture = ioSession.write(operation);
        writeFuture.addListener(new IoFutureListener<WriteFuture>() {

            @Override
            public void operationComplete(WriteFuture future) {
                if (!future.isWritten()) {
                    if (ioSession != null) {
                        SessionUtil.getOperationQueue(ioSession).remove(operation);
                    }
                    operation.getFuture().completeExceptionally(future.getException());
                }
            }
        });

        return operation.getFuture();
    }

    @Override
    public synchronized void close() {
        if (ioSession != null) {
            ioSession.closeNow();
        }
        ioSession = null;
    }

    private synchronized void checkAndReconnect() {
        if (closed) {
            throw new ConnectionException("connection is closed");
        }

        if (!isConnected()) {
            ioSession = connector.connect(config.getServiceHost(), config.getServicePort());
            try {
                if (initializer != null) {
                    initializer.initialize(this);
                }
            } catch (Exception e) {
                ioSession.closeNow();
                ioSession = null;

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new ConnectionException("can't init connection", e);
                }
            }
        }
    }

    private boolean isConnected() {
        return ioSession != null && ioSession.isConnected();
    }

}
