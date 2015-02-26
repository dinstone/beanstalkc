/*
 * Copyright (C) 2012~2015 dinstone<dinstone@163.com>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guojf
 * @version 1.0.0.2013-12-31
 */
public class ReceiveRunner extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ReceiveRunner.class);

    private JobConsumer consumer;

    private String name;

    private volatile boolean stop;

    /**
     * @param string
     * @param consumer
     */
    public ReceiveRunner(String name, JobConsumer consumer) {
        this.name = name;
        this.consumer = consumer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (!stop) {
            try {
                Job job = consumer.reserveJob(1);
                if (job != null) {
                    consumer.deleteJob(job.getId());

                    StatsInfo.receiveCount.incrementAndGet();
                    LOG.info("{} consume JobId : {}", name, job.getId());
                }
            } catch (Exception e) {
                StatsInfo.receiveError.incrementAndGet();
                LOG.info("{} exception", e);
            }
        }

        consumer.close();
        LOG.info("{} closed", name);
    }

    /**
     * the stop to set
     * 
     * @param stop
     * @see ReceiveRunner#stop
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

}
