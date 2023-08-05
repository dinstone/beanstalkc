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
package com.dinstone.beanstalkc.client;

import com.dinstone.beanstalkc.BeanstalkClient;
import com.dinstone.beanstalkc.BeanstalkClientFactory;
import com.dinstone.beanstalkc.Configuration;
import com.dinstone.beanstalkc.JobConsumer;
import com.dinstone.beanstalkc.JobProducer;
import com.dinstone.beanstalkc.connection.Connection;
import com.dinstone.beanstalkc.connection.Initializer;
import com.dinstone.beanstalkc.operation.IgnoreOperation;
import com.dinstone.beanstalkc.operation.UseOperation;
import com.dinstone.beanstalkc.operation.WatchOperation;

public abstract class AbstractBeanstalkClientFactory extends BeanstalkClientFactory {

    public AbstractBeanstalkClientFactory() {
    }

    protected abstract Connection createConnection(Configuration config, Initializer initer);

    @Override
    protected BeanstalkClient createBeanstalkClient(Configuration configuration) {
        Connection conn = createConnection(configuration, null);
        return new DefaultBeanstalkClient(configuration, conn);
    }

    @Override
    protected JobProducer createJobProducer(Configuration configuration, String useTube) {
        Initializer initer = new Initializer() {

            @Override
            public void initialize(Connection connection) throws Exception {
                if (useTube != null) {
                    connection.handle(new UseOperation(useTube));
                }
            }
        };
        Connection conn = createConnection(configuration, initer);
        return new DefaultBeanstalkClient(configuration, conn);
    }

    @Override
    protected JobConsumer createJobConsumer(Configuration configuration, String[] watchTubes) {
        final boolean ignoreDefault = configuration.getBoolean("IgnoreDefaultTube", false);
        Initializer initer = new Initializer() {

            @Override
            public void initialize(Connection connection) throws Exception {
                if (watchTubes != null && watchTubes.length > 0) {
                    for (int i = 0; i < watchTubes.length; i++) {
                        connection.handle(new WatchOperation(watchTubes[i])).get();
                    }
                }

                if (ignoreDefault) {
                    connection.handle(new IgnoreOperation("default")).get();
                }
            }
        };
        Connection conn = createConnection(configuration, initer);
        return new DefaultBeanstalkClient(configuration, conn);
    }

}