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

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * @author guojinfei
 * @version 1.0.0.2015-2-2
 * @param <E>
 */
public class BenchmarkTest {

    final int count = 100;

    final byte[] data = new byte[512];

    @Test
    public void test() {
        for (int i = 0; i < 3; i++) {
            fun();
            System.out.println("========================================");
        }
    }

    private void fun() {
        Configuration config = new Configuration("test-configuration.xml");
        BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        JobProducer producer = factory.createJobProducer("Benchmark");
        JobConsumer consumer = factory.createJobConsumer("Benchmark");

        clear(producer, consumer);

        long start = System.currentTimeMillis();

        put(producer);

        long end = System.currentTimeMillis();
        System.out.println("Benchmark put operation " + count * 1000 / (end - start) + " tps");

        start = System.currentTimeMillis();

        List<Long> ids = reserve(consumer);

        end = System.currentTimeMillis();
        System.out.println("Benchmark reserve operation " + count * 1000 / (end - start) + " tps");

        start = System.currentTimeMillis();

        delete(consumer, ids);
        end = System.currentTimeMillis();
        System.out.println("Benchmark delete operation " + count * 1000 / (end - start) + " tps");

        producer.close();
        consumer.close();
    }

    /**
     * @param producer
     */
    private void put(JobProducer producer) {
        for (int i = 0; i < count; i++) {
            producer.putJob(1, 0, 5000, data);
        }
    }

    /**
     * @param consumer
     * @return
     */
    private List<Long> reserve(JobConsumer consumer) {
        LinkedList<Long> ids = new LinkedList<Long>();
        for (int i = 0; i < count; i++) {
            Job job = consumer.reserveJob(0);
            if (job != null) {
                ids.add(job.getId());
            } else {
                break;
            }
        }
        return ids;
    }

    /**
     * @param consumer
     * @param ids
     */
    private void delete(JobConsumer consumer, List<Long> ids) {
        for (Long id : ids) {
            consumer.deleteJob(id);
        }
    }

    private void clear(JobProducer producer, JobConsumer consumer) {
        producer.putJob(1, 0, 5000, data);

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
        System.out.println("clear job count is " + clear);
    }
}
