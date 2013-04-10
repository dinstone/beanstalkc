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

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.dinstone.beanstalkc.internal.operation.Operation;

public class OperationConnection {

    private final NioSocketConnector ioConnector;

    private boolean stopped;

    private IoSession ioSession;

    public OperationConnection(NioSocketConnector connector) {
        this.ioConnector = connector;
    }

    public synchronized <T> OperationFuture<T> handle(Operation<T> operation) {
        connect();

        SessionUtil.getOperationQueue(ioSession).add(operation);
        ioSession.write(operation);

        return operation.getOperationFuture();
    }

    public synchronized void connect() {
        if (stopped) {
            throw new RuntimeException("connection is closed");
        }

        if (!isConnected()) {
            createSession();
        }
    }

    public synchronized boolean reconnect() {
        try {
            destroySession(null);

            connect();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void close() {
        destroySession(null);
        stopped = true;
    }

    public synchronized void destroySession(Throwable cause) {
        if (isConnected()) {
            if (cause == null) {
                cause = new RuntimeException("connection is closed");
            }

            Queue<Operation<?>> queue = SessionUtil.getOperationQueue(ioSession);
            while (true) {
                Operation<?> operation = queue.poll();
                if (operation == null) {
                    break;
                }
                operation.getOperationFuture().setException(cause);
            }

            ioSession.close(true);
            ioSession = null;
        }
    }

    private void createSession() {
        // create session
        ConnectFuture cf = ioConnector.connect();
        cf.awaitUninterruptibly();

        ioSession = cf.getSession();

        SessionUtil.createOperationQueue(ioSession);
        SessionUtil.setOperationConnection(ioSession, this);
    }

    private boolean isConnected() {
        return ioSession != null && ioSession.isConnected();
    }

}
