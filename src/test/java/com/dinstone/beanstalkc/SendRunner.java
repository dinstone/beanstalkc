/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendRunner extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(SendRunner.class);

    private JobProducer producer;

    private String name;

    private volatile boolean stop;

    /**
     * @param string
     * @param producer
     */
    public SendRunner(String name, JobProducer producer) {
        this.name = name;
        this.producer = producer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // Random random = new Random();
        while (!stop) {
            try {
                String data = new Date().toString();
                long s = System.currentTimeMillis();
                long id = producer.putJob(1, 0, 50, data.getBytes());
                long e = System.currentTimeMillis();

                StatsInfo.sendTimes.addAndGet(e - s);
                StatsInfo.sendCount.incrementAndGet();
                LOG.info("{} produce JobId : {}", name, id);

                // float w = 1000 * random.nextFloat();
                // Thread.sleep((long) w);
            } catch (Exception e) {
                StatsInfo.sendError.incrementAndGet();
                LOG.info("{} exception : {}", name, e.getMessage());
            }
        }
        LOG.info("{} closed", name);
    }

    /**
     * the stop to set
     * 
     * @param stop
     * @see SendRunner#stop
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

}