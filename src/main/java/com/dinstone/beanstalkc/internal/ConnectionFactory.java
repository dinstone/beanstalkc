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

import java.util.HashMap;
import java.util.Map;

import com.dinstone.beanstalkc.Configuration;

public class ConnectionFactory {

    private static final ConnectionFactory factory = new ConnectionFactory();

    private final Map<ConnectorKey, DefaultConnector> cachedConnectors;

    protected ConnectionFactory() {
        cachedConnectors = new HashMap<ConnectorKey, DefaultConnector>();
    }

    public static ConnectionFactory getInstance() {
        return factory;
    }

    public Connection createConnection(Configuration config) {
        return createConnection(config, null);
    }

    public Connection createConnection(Configuration config, ConnectionInitializer initer) {
        ConnectorKey ckey = new ConnectorKey(config);
        synchronized (cachedConnectors) {
            DefaultConnector connector = cachedConnectors.get(ckey);
            if (connector == null) {
                connector = new DefaultConnector(config);
                cachedConnectors.put(ckey, connector);
            }
            connector.incrementRefCount();
            return new DefaultConnection(connector, initer);
        }
    }

    public void releaseConnection(Configuration config) {
        ConnectorKey ckey = new ConnectorKey(config);
        synchronized (cachedConnectors) {
            DefaultConnector connector = cachedConnectors.get(ckey);
            if (connector != null) {
                connector.decrementRefCount();
                if (connector.isZeroRefCount()) {
                    cachedConnectors.remove(ckey);
                    connector.dispose();
                }
            }
        }
    }

}
