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

package com.dinstone.beanstalkc;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.dinstone.beanstalkc.internal.OperationConnection;
import com.dinstone.beanstalkc.internal.OperationConnectionHandler;
import com.dinstone.beanstalkc.internal.codec.OperationDecoder;
import com.dinstone.beanstalkc.internal.codec.OperationEncoder;

public class BeanstalkClientFactory {

    private NioSocketConnector ioConnector;

    public BeanstalkClientFactory(String hostname, int port) {
        initIoConnector(new InetSocketAddress(hostname, port));
    }

    public BeanstalkClientFactory(InetSocketAddress socketAddress) {
        initIoConnector(socketAddress);
    }

    public BeanstalkClient createClient() {
        OperationConnection con = createConnection();
        return new BeanstalkClient(con);
    }

    public void dispose() {
        ioConnector.dispose();
    }

    private OperationConnection createConnection() {
        return new OperationConnection(ioConnector);
    }

    /**
     * initialize socket connector
     * 
     * @param socketAddress
     */
    private void initIoConnector(InetSocketAddress socketAddress) {
        // create connector
        ioConnector = new NioSocketConnector();
        // add filter
        DefaultIoFilterChainBuilder chain = ioConnector.getFilterChain();

        final Charset charset = Charset.forName("utf-8");
        final String delimiter = "\r\n";

        chain.addLast("codec", new ProtocolCodecFilter(new ProtocolCodecFactory() {

            @Override
            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
                return new OperationEncoder(charset, delimiter);
            }

            @Override
            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
                return new OperationDecoder(charset, delimiter);
            }
        }));

        // set handler
        ioConnector.setHandler(new OperationConnectionHandler());
        ioConnector.setDefaultRemoteAddress(socketAddress);
    }

}
