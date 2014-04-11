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

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.dinstone.beanstalkc.internal.DefaultBeanstalkClient;

public class BeanstalkClientStressTest {

    @Test
    public void testStrees00() {
        DefaultBeanstalkClient client = new DefaultBeanstalkClient(new Configuration());
        client.useTube("someone");
        client.watchTube("someone");

        long st = System.currentTimeMillis();

        for (int i = 0; i < 300; i++) {
            fun(client, i);
        }

        long et = System.currentTimeMillis();
        System.out.println("common case[1thread * 300time=300] takes " + (et - st) + "ms");
    }

    /**
     * Each thread creates a client(30thread * 10time=300)
     */
    @Test
    public void testStrees10() {
        int tc = 30;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        final Configuration config = new Configuration();
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
                    client.useTube("someone");
                    client.watchTube("someone");
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 10; i++) {
                        fun(client, i);
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
            System.out.println("this case[single client per thread(30thread * 10time=300)] take " + (anyEnd - anyStart)
                    + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Each thread creates a client(10thread * 30time=300)
     */
    @Test
    public void testStrees11() {
        int tc = 10;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        final Configuration config = new Configuration();
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
                    client.useTube("someone");
                    client.watchTube("someone");
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 30; i++) {
                        fun(client, i);
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
            System.out.println("this case[single client per thread(10thread * 30time=300)] take " + (anyEnd - anyStart)
                    + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Each thread creates a client(3thread * 100time=300)
     */
    @Test
    public void testStrees12() {
        int tc = 3;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        final Configuration config = new Configuration();
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
                    client.useTube("someone");
                    client.watchTube("someone");
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 100; i++) {
                        fun(client, i);
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
            System.out.println("this case[single client per thread(3thread * 100time=300)] take " + (anyEnd - anyStart)
                    + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Each thread creates a client(1thread * 300time=300)
     */
    @Test
    public void testStrees13() {
        int tc = 1;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        final Configuration config = new Configuration();
        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
                    client.useTube("someone");
                    client.watchTube("someone");
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 300; i++) {
                        fun(client, i);
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
            System.out.println("this case[single client per thread(1thread * 300time=300)] take " + (anyEnd - anyStart)
                    + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * same client(30thread * 10time=300)
     */
    @Test
    public void testStrees01() {
        int tc = 30;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("someone");
        client.watchTube("someone");

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
                        fun(client, i);
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
            System.out.println("this case[same client(30thread * 10time=300)] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * same client(30thread * 10time=300)
     */
    @Test
    public void testStrees02() {
        int tc = 30;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("someone");
        client.watchTube("someone");

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
                        fun(client, i);
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
            System.out.println("this case[same client(30thread * 10time=300)] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * same client(10thread * 30time=300)
     */
    @Test
    public void testStrees03() {
        int tc = 10;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("someone");
        client.watchTube("someone");

        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 30; i++) {
                        fun(client, i);
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
            System.out.println("this case[same client(10thread * 30time=300)] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * same client(1thread * 300time=300)
     */
    @Test
    public void testStrees04() {
        int tc = 1;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("someone");
        client.watchTube("someone");

        for (int i = 0; i < tc; i++) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        return;
                    }

                    for (int i = 0; i < 300; i++) {
                        fun(client, i);
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
            System.out.println("this case[same client(1thread * 300time=300)] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * same client(60thread * 5time=300)
     */
    @Test
    public void testStrees05() {
        int tc = 60;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("someone");
        client.watchTube("someone");

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
                        fun(client, i);
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
            System.out.println("this case[same client(60thread * 5time=300)] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * same client(150thread * 2time=300)
     */
    @Test
    public void testStrees06() {
        int tc = 150;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("someone");
        client.watchTube("someone");

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
                        fun(client, i);
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
            System.out.println("this case[same client(150thread * 2time=300)] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * same client(300thread * 1time=300)
     */
    @Test
    public void testStrees07() {
        int tc = 300;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("someone");
        client.watchTube("someone");

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
                        fun(client, i);
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
            System.out.println("this case[same client(300thread * 1time=300)] takes " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param client
     * @param i
     */
    static void fun(final DefaultBeanstalkClient client, int i) {
        try {
            for (int j = 0; j < 30; j++) {
                String data = "this is data [" + i + "]";
                client.putJob(1, 0, 5000, data.getBytes());
            }

            for (int j = 0; j < 30; j++) {
                Job job = client.reserveJob(1);
                if (job != null) {
                    client.releaseJob(job.getId(), 1, 1);
                }
            }

            for (int j = 0; j < 30; j++) {
                Job job = client.reserveJob(1);
                if (job != null) {
                    client.buryJob(job.getId(), 2);
                    client.deleteJob(job.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStreesPut00() {
        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("stress");

        long anyStart = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            try {
                client.putJob(1, 0, 5000, "this is some data".getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long anyEnd = System.currentTimeMillis();
        long ts = anyEnd - anyStart;
        System.out.println("this case takes " + ts + " ms, the rate is " + (1 * 10000000 / ts) + " p/s");

        client.close();
    }

    @Test
    public void testStreesPut() {
        int tc = 4;
        final CountDownLatch doneLatch = new CountDownLatch(tc);
        final CountDownLatch startLatch = new CountDownLatch(1);

        Configuration config = new Configuration();
        final DefaultBeanstalkClient client = new DefaultBeanstalkClient(config);
        client.useTube("stress");

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

                    for (int i = 0; i < 10000; i++) {
                        try {
                            client.putJob(1, 0, 5000, "this is some data".getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    doneLatch.countDown();
                }

            };
            t.setName("t-" + i);
            t.start();
        }

        try {
            Thread.sleep(1000);
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            long ts = anyEnd - anyStart;
            System.out.println("this case takes " + ts + " ms, the rate is " + (tc * 10000000 / ts) + " p/s");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
