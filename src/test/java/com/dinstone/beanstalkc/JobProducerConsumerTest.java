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

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author guojf
 * @version 1.0.0.2013-4-15
 */
public class JobProducerConsumerTest {

    private JobProducer producer;

    private JobConsumer consumer;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Configuration config = new Configuration();
        BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        producer = factory.createJobProducer("pctube");
        consumer = factory.createJobConsumer("pctube");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        producer.close();
        consumer.close();
    }

    @Test
    public void testStrees000() {
        long st = System.currentTimeMillis();

        while (true) {
            Job job = consumer.reserveJob(1);
            if (job == null) {
                break;
            }
            consumer.deleteJob(job.getId());
        }

        String data = "xxxxxxxxx";
        producer.putJob(1, 1, 5000, data.getBytes());

        Job job = consumer.reserveJob(2);
        if (job != null) {
            System.out.println(new String(job.getData()));
        } else {
            System.out.println("error");
        }

        long et = System.currentTimeMillis();
        System.out.println("common case[produce] takes " + (et - st) + "ms");
    }

    @Test
    public void testStrees001() {
        Configuration config = new Configuration();
        config.setBoolean("IgnoreDefaultTube", true);
        BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        JobConsumer consumer = factory.createJobConsumer((String[]) null);
        Job job = consumer.reserveJob(1);
        Assert.assertNull(job);
    }

    @Test
    public void testStrees002() {
        Configuration config = new Configuration();
        config.setBoolean("IgnoreDefaultTube", true);
        BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        JobProducer producer = factory.createJobProducer(null);
        producer.putJob(1, 1, 5000, "dddd".getBytes());
    }

    @Test
    public void testStrees00() {
        long st = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            funProduce(producer, i);
        }

        long et = System.currentTimeMillis();
        System.out.println("common case[produce] takes " + (et - st) + "ms");
    }

    @Test
    public void testStrees() {
        long st = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            funConsume(consumer, i);
        }

        long et = System.currentTimeMillis();
        System.out.println("common case[consume] takes " + (et - st) + "ms");
    }

    /**
     * NonShare Client(10thread * 1time) P
     */
    @Test
    public void testStrees01() {
        int tc = 10;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final Configuration config = new Configuration();
        final BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    JobProducer producer = factory.createJobProducer("pctube");

                    for (int i = 0; i < 1; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[NonShare Client(10thread * 1time) P] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * NonShare Client(5thread * 2time) P
     */
    @Test
    public void testStrees02() {
        int tc = 5;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final Configuration config = new Configuration();
        final BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    JobProducer producer = factory.createJobProducer("pctube");

                    for (int i = 0; i < 2; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[NonShare Client(5thread * 2time) P] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * NonShare Client(2thread * 5time) P
     */
    @Test
    public void testStrees03() {
        int tc = 2;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final Configuration config = new Configuration();
        final BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    JobProducer producer = factory.createJobProducer("pctube");

                    for (int i = 0; i < 5; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[NonShare Client(2thread * 5time) P] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * NonShare Client(1thread * 10time) P
     */
    @Test
    public void testStrees04() {
        int tc = 1;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final Configuration config = new Configuration();
        final BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    JobProducer producer = factory.createJobProducer("pctube");

                    for (int i = 0; i < 10; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[NonShare Client(1thread * 10time) P] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * share Client(10thread * 1time) P
     */
    @Test
    public void testStrees10() {
        int tc = 10;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 1; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[Shareing Client(10thread * 1time) P] take " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * share Client(5thread * 2time) P
     */
    @Test
    public void testStrees11() {
        int tc = 5;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 2; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[Shareing Client(5thread * 2time) P] take " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * share Client(2thread * 5time) P
     */
    @Test
    public void testStrees12() {
        int tc = 2;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 5; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[Shareing Client(2thread * 5time) P] take " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * share Client(1thread * 10time) P
     */
    @Test
    public void testStrees13() {
        int tc = 1;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 10; i++) {
                        funProduce(producer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[Shareing Client(1thread * 10time) P] take " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * NonShare Client(10thread * 1time)
     */
    @Test
    public void testStrees21() {
        int tc = 10;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final Configuration config = new Configuration();
        final BeanstalkClientFactory factory = new BeanstalkClientFactory(config);
        // create thread for test case
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    consumer = factory.createJobConsumer("pctube");
                    for (int i = 0; i < 1; i++) {
                        funConsume(consumer, i);
                    }

                    doneLatch.countDown();

                }
            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case[NonShare Client(10thread * 1time) C] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    static void funProduce(JobProducer producer, int i) {
        try {
            for (int j = 0; j < 100; j++) {
                String data = "this is data [" + i + "]";
                producer.putJob(1, 0, 5000, data.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void funConsume(JobConsumer consumer, int i) {
        try {
            while (true) {
                Job job = consumer.reserveJob(1);
                if (job != null) {
                    consumer.deleteJob(job.getId());
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
