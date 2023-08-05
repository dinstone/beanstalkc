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
package com.dinstone.beanstalkc.mina;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
import com.dinstone.beanstalkc.ConnectionException;

/**
 * @author guojf
 * 
 * @version 1.0.0.2013-4-11
 */
public class DefaultConnector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnector.class);

    private NioSocketConnector ioConnector;

    private int refCount;

    /**
     * @param config
     * @param ioConnector
     */
    public DefaultConnector(Configuration config) {
        initConnector(config);
    }

    /**
     * @param config
     */
    private void initConnector(Configuration config) {
        // create connector
        ioConnector = new NioSocketConnector();
        ioConnector.setConnectTimeoutMillis(config.getConnectTimeout());

        SocketSessionConfig sessionConfig = ioConnector.getSessionConfig();
        LOG.debug("KeepAlive is {}", sessionConfig.isKeepAlive());
        LOG.debug("ReadBufferSize is {}", sessionConfig.getReadBufferSize());
        LOG.debug("SendBufferSize is {}", sessionConfig.getSendBufferSize());
        sessionConfig.setReaderIdleTime(config.getReadTimeout());

        // add filter
        DefaultIoFilterChainBuilder chainBuilder = ioConnector.getFilterChain();

        String charsetName = config.getProtocolCharset();
        Charset charset = Charset.forName(charsetName == null ? "ASCII" : charsetName);
        LOG.debug("beanstalk.protocol.charset is {}", charset);

        final OperationEncoder encoder = new OperationEncoder(charset);
        final OperationDecoder decoder = new OperationDecoder(charset);
        chainBuilder.addLast("codec", new ProtocolCodecFilter(new ProtocolCodecFactory() {

            @Override
            public ProtocolEncoder getEncoder(IoSession session) throws Exception {
                return encoder;
            }

            @Override
            public ProtocolDecoder getDecoder(IoSession session) throws Exception {
                return decoder;
            }
        }));

        // set handler
        ioConnector.setHandler(new ConnectionHandler());
    }

    public IoSession connect(String host, int port) {
        SocketAddress socketAddress = new InetSocketAddress(host, port);
        ConnectFuture cf = ioConnector.connect(socketAddress);
        try {
            return cf.awaitUninterruptibly().getSession();
        } catch (RuntimeException e) {
            throw new ConnectionException("can't create connect beanstalkd service on " + socketAddress, e);
        } catch (Exception e) {
            throw new ConnectionException("initializing connect beanstalkd service on " + socketAddress, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.beanstalkc.connection.Connector#dispose()
     */
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
