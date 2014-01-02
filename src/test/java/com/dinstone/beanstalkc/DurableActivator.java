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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guojf
 * @version 1.0.0.2013-12-30
 */
public class DurableActivator {

    private static final Logger LOG = LoggerFactory.getLogger(DurableActivator.class);

    /**  */
    private static final String TUBE = "DurableTube";

    private SendRunner[] senders;

    private ReceiveRunner[] receivers;

    private StatsRunner stats;

    private JobProducer producer;

    /**
     * @param args
     */
    public static void main(String[] args) {
        DurableActivator activator = new DurableActivator();
        activator.start();

        System.out.println("activator started");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        activator.stop();
        System.out.println("activator stopped");
    }

    public void start() {
        BeanstalkClientFactory factory = new BeanstalkClientFactory(new Configuration());
        clear(factory);

        int scount = 3;
        senders = new SendRunner[scount];
        producer = factory.createJobProducer(TUBE);
        for (int i = 0; i < scount; i++) {
            senders[i] = new SendRunner("Send-" + i, producer);
            senders[i].setName("JobSender-" + i);
            senders[i].start();
        }

        int rcount = 6;
        receivers = new ReceiveRunner[rcount];
        for (int i = 0; i < rcount; i++) {
            JobConsumer consumer = factory.createJobConsumer(TUBE);
            receivers[i] = new ReceiveRunner("Receive-" + i, consumer);
            receivers[i].setName("JobReceiver");
            receivers[i].start();
        }

        stats = new StatsRunner();
        new Thread(stats, "JobStats").start();

        LOG.info("activator started");
    }

    /**
     * @param factory
     */
    private void clear(BeanstalkClientFactory factory) {
        JobConsumer consumer = factory.createJobConsumer(TUBE);
        int clear = 0;
        int tc = 0;
        while (tc < 4) {
            try {
                Job job = consumer.reserveJob(1);
                if (job != null) {
                    consumer.deleteJob(job.getId());
                    clear++;
                } else {
                    tc++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        consumer.close();
        LOG.info("clear job count is " + clear);
    }

    public void stop() {
        for (SendRunner sender : senders) {
            sender.setStop(true);
            try {
                sender.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        producer.close();

        for (ReceiveRunner receiver : receivers) {
            receiver.setStop(true);
            try {
                receiver.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stats.setStop(true);
        try {
            stats.join();
        } catch (InterruptedException e) {
        }

        LOG.info("activator stopped");
    }

}
