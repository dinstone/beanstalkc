/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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
package com.dinstone.beanstalkc.internal;

import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

import com.dinstone.beanstalkc.ConnectionException;
import com.dinstone.beanstalkc.internal.operation.Operation;

public class DefaultConnection implements Connection {

    private boolean closed;

    private IoSession ioSession;

    private final Connector connector;

    private final ConnectionInitializer initializer;

    public DefaultConnection(Connector connector, ConnectionInitializer initializer) {
        this.connector = connector;
        this.initializer = initializer;
    }

    @Override
    public synchronized <T> OperationFuture<T> handle(final Operation<T> operation) {
        connect();

        SessionUtil.getOperationQueue(ioSession).add(operation);
        WriteFuture writeFuture = ioSession.write(operation);
        writeFuture.addListener(new IoFutureListener<WriteFuture>() {

            @Override
            public void operationComplete(WriteFuture future) {
                if (!future.isWritten()) {
                    if (ioSession != null) {
                        SessionUtil.getOperationQueue(ioSession).remove(operation);
                    }
                    operation.getOperationFuture().setException(future.getException());
                }
            }
        });

        return operation.getOperationFuture();
    }

    @Override
    public synchronized void close() {
        destroy();
        closed = true;
    }

    public synchronized void destroy() {
        if (isConnected()) {
            ioSession.close(true);
        }
        ioSession = null;
    }

    private synchronized void connect() {
        if (closed) {
            throw new ConnectionException("connection is closed");
        }

        if (!isConnected()) {
            ioSession = connector.createSession();
            try {
                if (initializer != null) {
                    initializer.initConnection(this);
                }
            } catch (Exception e) {
                ioSession.close(true);
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
