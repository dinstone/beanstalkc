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
package com.dinstone.beanstalkc.connection;

import java.util.concurrent.CompletableFuture;

import com.dinstone.beanstalkc.operation.Operation;

/**
 * connect the beanstalkd service and asynchronous handle operation.
 * 
 * @author guojf
 * 
 * @version 1.0.0.2013-4-10
 */
public interface Connection {

    /**
     * asynchronous handle operation.
     * 
     * @param operation
     * 
     * @return
     */
    public <T> CompletableFuture<T> handle(Operation<T> operation);

    /**
     * close the connection,then that status is closed.
     */
    public void close();

}