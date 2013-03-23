
package com.dinstone.beanstalkc;

import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dinstone.beanstalkc.Job;
import com.dinstone.beanstalkc.JobConsumer;
import com.dinstone.beanstalkc.JobProducer;
import com.dinstone.beanstalkc.BeanstalkClientFactory;

public class ConnectionTest {

    private BeanstalkClientFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new BeanstalkClientFactory(new InetSocketAddress("172.17.6.41", 11300));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateProducer() {
        JobProducer producer = factory.createClient();
        producer.useTube("jobs");

        long id = producer.putJob(1, 0, 5000, "this is some data".getBytes());
        System.out.println(id);
    }

    @Test
    public void testCreateConsumer() {
        JobConsumer consumer = factory.createClient();
        consumer.ignoreTube("default");
        consumer.watchTube("jobs");

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

}
