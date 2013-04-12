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

public class BeanstalkClientStressTest {

    /**
     * Multiple threads share the same client.
     */
    @Test
    public void testStrees() {
        final CountDownLatch doneLatch = new CountDownLatch(6);
        final CountDownLatch startLatch = new CountDownLatch(1);
        BeanstalkClient sameClient = new BeanstalkClient();

        // create thread for test case
        for (int i = 0; i < 6; i++) {
            Thread t = new WorkThread(sameClient, startLatch, doneLatch);
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case(share same client) take " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sameClient.close();
    }

    /**
     * Each thread creates a client
     */
    @Test
    public void testStrees00() {
        final CountDownLatch doneLatch = new CountDownLatch(6);
        final CountDownLatch startLatch = new CountDownLatch(1);
        // create thread for test case
        Configuration config = new Configuration();
        for (int i = 0; i < 6; i++) {
            Thread t = new WorkThread(new BeanstalkClient(config), startLatch, doneLatch);
            t.setName("t-" + i);
            t.start();
        }

        try {
            startLatch.countDown();
            long anyStart = System.currentTimeMillis();
            doneLatch.await();
            long anyEnd = System.currentTimeMillis();
            System.out.println("this case(single client per thread) take " + (anyEnd - anyStart) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testStrees01() {
        BeanstalkClient client = new BeanstalkClient();
        long st = System.currentTimeMillis();

        try {
            client.useTube("streess");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10000; i++) {
            String data = "this is data [" + i + "]";
            try {
                long id = client.putJob(1, 0, 5000, data.getBytes());
                System.out.println("put job id is " + id + ", index is " + i);
            } catch (Exception e) {
                e.printStackTrace();
                // System.out.println("error = " + e.getMessage());
            }
        }

        client.watchTube("streess");
        for (int i = 0; i < 10000; i++) {
            Job job = client.reserveJob(1);
            if (job != null) {
                client.deleteJob(job.getId());
                // System.out.println("deleted job is " + job.getId());
            }

        }

        long et = System.currentTimeMillis();
        System.out.println("cost is " + (et - st) + "ms");
    }

    private static class WorkThread extends Thread {

        private BeanstalkClient client;

        private CountDownLatch startLatch;

        private CountDownLatch doneLatch;

        /**
         * @param client
         * @param startLatch
         * @param doneLatch
         */
        public WorkThread(BeanstalkClient client, CountDownLatch startLatch, CountDownLatch doneLatch) {
            super();
            this.client = client;
            this.startLatch = startLatch;
            this.doneLatch = doneLatch;
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            client.useTube(this.getName());

            client.watchTube(this.getName());

            for (int i = 0; i < 100; i++) {
                try {
                    bc();
                } catch (Exception e) {
                }
            }

            doneLatch.countDown();
        }

        /**
         *
         */
        private void bc() {
            String data = "this is data [" + this.getName() + "]";
            client.putJob(1, 0, 5000, data.getBytes());

            Job job = client.reserveJob(1);

            client.releaseJob(job.getId(), 1, 1);

            job = client.reserveJob(1);

            client.buryJob(job.getId(), 2);

            client.deleteJob(job.getId());
        }

    }

}
