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
        Queue<Operation<?>> optQueue = (Queue<Operation<?>>) session.getAttribute("OPERATION_QUEUE");
        if (optQueue == null) {
            optQueue = new ConcurrentLinkedQueue<Operation<?>>();
            session.setAttribute("OPERATION_QUEUE", optQueue);
        }
        return optQueue;
    }

    public static void setConnection(IoSession session, Connection connection) {
        session.setAttribute(Connection.class.getName(), connection);
    }

    public static Connection getConnection(IoSession session) {
        return (Connection) session.getAttribute(Connection.class.getName());
    }
}
