/*
 * Copyright (C) 2012~2013 dinstone<dinstone@163.com>
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

import org.junit.After;
import org.junit.Test;

public class ConnectionTest {

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateProducer() {
        JobProducer producer = new BeanstalkClient();
        producer.useTube("jobs");

        long id = producer.putJob(1, 0, 5000, "this is some data".getBytes());
        System.out.println(id);
    }

    @Test
    public void testCreateConsumer() {
        JobConsumer consumer = new BeanstalkClient();
        consumer.watchTube("jobs");
        consumer.ignoreTube("default");

        Job job = consumer.reserveJob(1);
        if (job != null) {
            consumer.touchJob(job.getId());
            consumer.releaseJob(job.getId(), 1, 1);
        }

        job = consumer.reserveJob(1);
        if (job != null) {
            consumer.buryJob(job.getId(), 2);
            consumer.deleteJob(job.getId());
        }
    }

    @Test
    public void testConsumer001() {
        JobConsumer consumer = new BeanstalkClient();
        consumer.watchTube("prepareMessage");
        consumer.ignoreTube("default");
        // consumer.deleteJob(301782);

        for (int i = 0; i < 10; i++) {
            Job job = consumer.reserveJob(1);
            if (job != null) {
                consumer.touchJob(job.getId());
                consumer.releaseJob(job.getId(), 1, 1);
            }
        }
    }

}
