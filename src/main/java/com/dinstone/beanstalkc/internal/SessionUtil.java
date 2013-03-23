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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.core.session.IoSession;

import com.dinstone.beanstalkc.internal.operation.Operation;

public class SessionUtil {

    @SuppressWarnings("unchecked")
    public static Queue<Operation<?>> getOperationQueue(IoSession session) {
        return (Queue<Operation<?>>) session.getAttribute("OPERATION_QUEUE");
    }

    public static Queue<Operation<?>> createOperationQueue(IoSession session) {
        Queue<Operation<?>> queue = new ConcurrentLinkedQueue<Operation<?>>();
        session.setAttribute("OPERATION_QUEUE", queue);
        return queue;
    }

    public static void setOperationConnection(IoSession session, OperationConnection connection) {
        session.setAttribute(OperationConnection.class.getName(), connection);
    }

    public static OperationConnection getOperationConnection(IoSession session) {
        return (OperationConnection) session.getAttribute(OperationConnection.class.getName());
    }
}
