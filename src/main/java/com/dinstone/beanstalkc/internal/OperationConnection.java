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

import java.util.Queue;

import org.apache.mina.core.session.IoSession;

import com.dinstone.beanstalkc.internal.operation.Operation;

public class OperationConnection implements Connection {

    private boolean closed;

    private IoSession ioSession;

    private final Connector connector;

    private final ConnectionInitializer initializer;

    public OperationConnection(Connector connector, ConnectionInitializer initializer) {
        this.connector = connector;
        this.initializer = initializer;
    }

    @Override
    public synchronized <T> OperationFuture<T> handle(Operation<T> operation) {
        connect();

        SessionUtil.getOperationQueue(ioSession).add(operation);
        ioSession.write(operation);

        return operation.getOperationFuture();
    }

    @Override
    public synchronized void close() {
        destroy();
        closed = true;
    }

    @Override
    public synchronized void destroy() {
        if (isConnected()) {
            Queue<Operation<?>> queue = SessionUtil.getOperationQueue(ioSession);
            while (true) {
                Operation<?> operation = queue.poll();
                if (operation == null) {
                    break;
                }
                operation.getOperationFuture().setException(new RuntimeException("connection is closed"));
            }

            ioSession.close(true);
        }
        ioSession = null;
    }

    private void connect() {
        if (closed) {
            throw new RuntimeException("connection is closed");
        }

        if (!isConnected()) {
            ioSession = connector.createSession();
            SessionUtil.setConnection(ioSession, this);

            if (initializer != null) {
                initializer.initConnection(this);
            }
        }
    }

    private boolean isConnected() {
        return ioSession != null && ioSession.isConnected();
    }

}
