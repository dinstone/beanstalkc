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

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.internal.codec.OperationDecoder;
import com.dinstone.beanstalkc.internal.codec.OperationEncoder;

/**
 * @author guojf
 * @version 1.0.0.2013-4-11
 */
public class SocketConnector implements Connector {

    private static final Logger LOG = LoggerFactory.getLogger(SocketConnector.class);

    private NioSocketConnector ioConnector;

    private int refCount;

    /**
     * @param config
     * @param ioConnector
     */
    public SocketConnector(Configuration config) {
        initConnector(config);
    }

    /**
     * @param config
     */
    private void initConnector(Configuration config) {
        // create connector
        ioConnector = new NioSocketConnector();
        SocketSessionConfig sessionConfig = ioConnector.getSessionConfig();
        LOG.debug("KeepAlive is {}", sessionConfig.isKeepAlive());
        LOG.debug("ReadBufferSize is {}", sessionConfig.getReadBufferSize());
        LOG.debug("SendBufferSize is {}", sessionConfig.getSendBufferSize());

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
        ioConnector.setHandler(new ConnectionHandler());

        InetSocketAddress address = new InetSocketAddress(config.getRemoteHost(), config.getRemotePort());
        ioConnector.setDefaultRemoteAddress(address);
    }

    /**
     * @return
     */
    @Override
    public IoSession createSession() {
        // create session
        ConnectFuture cf = ioConnector.connect();
        cf.awaitUninterruptibly();

        return cf.getSession();
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.internal.Connector#dispose()
     */
    @Override
    public void dispose() {
        ioConnector.dispose(false);
    }

    /**
     *
     */
    public void incrementRefCount() {
        ++refCount;
    }

    /**
     *
     */
    public void decrementRefCount() {
        if (refCount > 0) {
            --refCount;
        }
    }

    /**
     * @return
     */
    public boolean isZeroRefCount() {
        return refCount == 0;
    }
}
