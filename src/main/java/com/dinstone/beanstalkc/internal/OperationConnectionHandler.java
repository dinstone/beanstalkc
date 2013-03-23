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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OperationConnectionHandler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(OperationConnectionHandler.class);

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        OperationConnection connect = SessionUtil.getOperationConnection(session);
        connect.destroySession(new RuntimeException("connection is closed"));
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOG.error("Unhandled I/O Exception", cause);
        OperationConnection connect = SessionUtil.getOperationConnection(session);
        connect.destroySession(cause);
        connect.reconnect();
    }

}
