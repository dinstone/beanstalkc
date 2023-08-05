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

import java.util.HashMap;
import java.util.Map;

import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.client.AbstractBeanstalkClientFactory;
import com.dinstone.beanstalkc.connection.Connection;
import com.dinstone.beanstalkc.connection.Initializer;

public class NettyBeanstalkClientFactory extends AbstractBeanstalkClientFactory {

    private final Map<String, DefaultConnector> cachedConnectors = new HashMap<>();

    public NettyBeanstalkClientFactory() {
    }

    @Override
    protected Connection createConnection(Configuration config, Initializer initer) {
        int servicePort = config.getServicePort();
        String serviceHost = config.getServiceHost();
        synchronized (cachedConnectors) {
            String ckey = serviceHost + ":" + servicePort;
            DefaultConnector connector = cachedConnectors.get(ckey);
            if (connector == null) {
                connector = createConnector(config);
                cachedConnectors.put(ckey, connector);
            }
            return new DefaultConnection(config, connector, initer);
        }
    }

    private DefaultConnector createConnector(Configuration config) {
        return new DefaultConnector(config);
    }

}
