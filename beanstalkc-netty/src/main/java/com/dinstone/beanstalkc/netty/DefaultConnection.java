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
package com.dinstone.beanstalkc.netty;

import java.util.concurrent.CompletableFuture;

import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.ConnectionException;
import com.dinstone.beanstalkc.connection.Connection;
import com.dinstone.beanstalkc.connection.Initializer;
import com.dinstone.beanstalkc.operation.Operation;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

public class DefaultConnection implements Connection {

    private Configuration config;
    private Initializer initializer;
    private DefaultConnector connector;

    private Channel channel;
    private boolean closed;

    public DefaultConnection(Configuration config, DefaultConnector connector, Initializer initializer) {
        this.config = config;
        this.connector = connector;
        this.initializer = initializer;
    }

    @Override
    public synchronized <T> CompletableFuture<T> handle(final Operation<T> operation) {
        checkAndReconnect();

        ChannelUtil.getOperationQueue(channel).add(operation);
        ChannelFuture writeFuture = channel.writeAndFlush(operation);
        writeFuture.addListener(new GenericFutureListener<ChannelFuture>() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    if (channel != null) {
                        ChannelUtil.getOperationQueue(channel).remove(operation);
                    }
                    operation.getFuture().completeExceptionally(future.cause());
                }
            }
        });

        return operation.getFuture();
    }

    @Override
    public synchronized void close() {
        if (channel != null) {
            channel.close();
        }
        channel = null;
    }

    private synchronized void checkAndReconnect() {
        if (closed) {
            throw new ConnectionException("connection is closed");
        }

        if (!isConnected()) {
            try {
                channel = connector.connect(config.getServiceHost(), config.getServicePort());
                ChannelUtil.setOperationQueue(channel);
                if (initializer != null) {
                    initializer.initialize(this);
                }
            } catch (Exception e) {
                if (channel != null) {
                    channel.close();
                    channel = null;
                }

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new ConnectionException("can't init connection", e);
                }
            }
        }
    }

    private boolean isConnected() {
        return channel != null && channel.isActive();
    }

}
