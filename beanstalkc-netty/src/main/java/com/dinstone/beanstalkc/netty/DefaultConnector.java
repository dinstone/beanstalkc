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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.ConnectionException;
import com.dinstone.beanstalkc.operation.Operation;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class DefaultConnector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultConnector.class);

    private final NioEventLoopGroup workGroup;

    private final Bootstrap bootstrap;

    public DefaultConnector(Configuration config) {
        String charsetName = config.getProtocolCharset();
        Charset charset = Charset.forName(charsetName);
        LOG.info("beanstalk.protocol.charset is {}", charset);

        workGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("PCT-Worker"));
        bootstrap = new Bootstrap().group(workGroup).channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("MessageDecoder", new MessageDecoder(charset));
                ch.pipeline().addLast("MessageEncoder", new MessageEncoder(charset));
                ch.pipeline().addLast("ClientHandler", new ClientHandler());
            }
        });
        applyNetworkOptions(bootstrap, config);
    }

    private void applyNetworkOptions(Bootstrap bootstrap, Configuration options) {
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.getConnectTimeout());
    }

    public void destroy() {
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
    }

    public Channel connect(String host, int port) {
        SocketAddress sa = new InetSocketAddress(host, port);
        // wait connect to peer
        ChannelFuture channelFuture = bootstrap.connect(sa).awaitUninterruptibly();
        if (!channelFuture.isDone()) {
            throw new ConnectionException("Connect timeout: " + sa);
        }
        if (channelFuture.isCancelled()) {
            throw new ConnectionException("Connect cancelled: " + sa);
        }
        if (!channelFuture.isSuccess()) {
            throw new ConnectionException("Connect failure: " + sa, channelFuture.cause());
        }

        return channelFuture.channel();
    }

    private class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LOG.info("Channel[{}] is closed", ctx.channel().id());

            Queue<Operation<?>> queue = ChannelUtil.getOperationQueue(ctx.channel());
            Exception ex = new ConnectionException("connection is closed");
            while (true) {
                Operation<?> operation = queue.poll();
                if (operation == null) {
                    break;
                }
                operation.getFuture().completeExceptionally(ex);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Unhandled Exception", cause);
            ctx.close();
        }

    }

}
