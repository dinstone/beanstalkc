
package com.dinstone.beanstalkc;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BeanstalkClientTest {

    private BeanstalkClient client;

    private long s;

    private BeanstalkClientFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new BeanstalkClientFactory(new InetSocketAddress("172.17.6.41", 11300));
        client = factory.createClient();

        s = System.currentTimeMillis();
    }

    @After
    public void tearDown() throws Exception {
        long e = System.currentTimeMillis();
        System.out.println(e - s + " ms");
        factory.dispose();
    }

    @Test
    public void testStrees() {
        final CountDownLatch done = new CountDownLatch(6);

        final CountDownLatch signal = new CountDownLatch(1);
        for (int i = 0; i < 6; i++) {
            Thread t = new Thread("t-" + i) {

                @Override
                public void run() {
                    try {
                        signal.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    client.useTube(this.getName());

                    client.watchTube(this.getName());

                    String data = "this is data [" + this.getName() + "]";
                    long id = client.putJob(1, 0, 5000, data.getBytes());
                    System.out.println("job id is " + id);

                    Job job = client.reserveJob(1);

                    client.releaseJob(job.getId(), 1, 1);

                    job = client.reserveJob(1);

                    client.buryJob(job.getId(), 2);

                    client.deleteJob(job.getId());

                    done.countDown();
                }
            };
            t.start();
        }

        long s = System.currentTimeMillis();
        signal.countDown();

        try {
            done.await();

            long e = System.currentTimeMillis();

            System.out.println("testStrees " + (e - s) + " ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testStrees01() {
        client.useTube("streess");
        for (int i = 0; i < 100; i++) {
            String data = "this is data [" + i + "]";
            long id = client.putJob(1, 0, 5000, data.getBytes());
            System.out.println("put job id is " + id + ", index is " + i);
        }

        client.watchTube("streess");
        for (int i = 0; i < 100; i++) {
            Job job = client.reserveJob(1);
            if (job != null) {
                System.out.println("reserved job is " + job.getId());
            }

        }
    }

    @Test
    public void testUseTube() {
        client.useTube("batchflow/request");
    }

    @Test
    public void testWatchTube() {
        client.watchTube("br");
    }

    @Test
    public void testPutJob() {
        long id = client.putJob(1, 0, 5000, "this is some data".getBytes());
        System.out.println(id);
    }

    @Test
    public void testPutJob01() {
        long id = client.putJob(1, 0, 5000, new byte[1024 * 6]);
        System.out.println(id);
    }

    @Test
    public void testReserveJob() {
        Job job = client.reserveJob(1);
        client.releaseJob(job.getId(), 1, 1);
    }

    @Test
    public void testDeleteJob() {
        Job job = client.reserveJob(1);
        client.deleteJob(job.getId());
    }

    @Test
    public void testReleaseJob() {
    }

    @Test
    public void testBury() {
        Job job = client.reserveJob(1);
        client.buryJob(job.getId(), 1);
    }

}
