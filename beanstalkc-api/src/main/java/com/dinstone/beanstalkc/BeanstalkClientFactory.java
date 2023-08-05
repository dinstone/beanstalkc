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
package com.dinstone.beanstalkc;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * {@link BeanstalkClientFactory} is a factory class, that is responsible for the creation beanstalk client.
 * 
 * @author guojf
 * 
 * @version 2.0.0.2013-4-17
 */
public class BeanstalkClientFactory {

    private static final String BEANSTALK_CLIENT_NETTY = "netty";

    private static final String BEANSTALK_CLIENT_MINA = "mina";

    private Map<String, BeanstalkClientFactory> factorys = new HashMap<>();

    private Configuration configuration = new Configuration();

    private BeanstalkClientFactory beanstalkClientFactory;

    /**
     * for sub class
     */
    protected BeanstalkClientFactory() {
    }

    /**
     * factory construction.
     * 
     * @param config
     *            beanstalk client configuration
     */
    public BeanstalkClientFactory(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        this.configuration.merge(config);

        ServiceLoader<BeanstalkClientFactory> beanstalkClientLoader = ServiceLoader.load(BeanstalkClientFactory.class);
        for (BeanstalkClientFactory factory : beanstalkClientLoader) {
            String name = factory.getClass().getSimpleName().toLowerCase();
            if (name.contains(BEANSTALK_CLIENT_NETTY)) {
                factorys.put(BEANSTALK_CLIENT_NETTY, factory);
            } else if (name.contains(BEANSTALK_CLIENT_MINA)) {
                factorys.put(BEANSTALK_CLIENT_MINA, factory);
            }
        }
        // try netty
        beanstalkClientFactory = factorys.get(BEANSTALK_CLIENT_NETTY);
        if (beanstalkClientFactory == null) {
            // try mina
            beanstalkClientFactory = factorys.get(BEANSTALK_CLIENT_MINA);
        }
        if (beanstalkClientFactory == null) {
            throw new RuntimeException("please add beanstalkc dependency : beanstalk-netty or beanstalkc-mina");
        }
    }

    /**
     * create a beanstalk client.
     * 
     * @return
     */
    public BeanstalkClient createBeanstalkClient() {
        return beanstalkClientFactory.createBeanstalkClient(configuration);
    }

    protected BeanstalkClient createBeanstalkClient(Configuration configuration) {
        throw new RuntimeException("please add beanstalkc dependency : beanstalk-netty or beanstalkc-mina");
    }

    /**
     * create a job consumer.
     * 
     * @param watchTubes
     *            the named tube to the watch list for the current connection
     * 
     * @return a beanstalk client
     */
    public JobConsumer createJobConsumer(final String... watchTubes) {
        return beanstalkClientFactory.createJobConsumer(configuration, watchTubes);
    }

    protected JobConsumer createJobConsumer(Configuration configuration, String[] watchTubes) {
        throw new RuntimeException("please add beanstalkc dependency : beanstalk-netty or beanstalkc-mina");
    }

    /**
     * create a job producer.
     * 
     * @param useTube
     *            the name of the tube now being used
     * 
     * @return a beanstalk client
     */
    public JobProducer createJobProducer(final String useTube) {
        return beanstalkClientFactory.createJobProducer(configuration, useTube);
    }

    protected JobProducer createJobProducer(Configuration configuration, String useTube) {
        throw new RuntimeException("please add beanstalkc dependency : beanstalk-netty or beanstalkc-mina");
    }

}
