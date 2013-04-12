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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BeanstalkClientTest {

    private BeanstalkClient client;

    @Before
    public void setUp() throws Exception {
        client = new BeanstalkClient();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void testUseTube() {
        boolean f = client.useTube("batchflow/request");
        Assert.assertTrue(f);
    }

    @Test
    public void testWatchTube() {
        boolean f = client.watchTube("br");
        Assert.assertTrue(f);
    }

    @Test
    public void testPutJob() {
        long id = client.putJob(1, 0, 5000, "this is some data".getBytes());
        Assert.assertTrue(id != 0);
    }

    @Test
    public void testPutJob01() {
        long id = client.putJob(1, 0, 5000, new byte[1024 * 6]);
        Assert.assertTrue(id != 0);
    }

    @Test
    public void testReserveJob() {
        Job job = client.reserveJob(1);
        Assert.assertNotNull(job);

        boolean f = client.releaseJob(job.getId(), 1, 1);
        Assert.assertTrue(f);
    }

    @Test
    public void testDeleteJob() {
        Job job = client.reserveJob(1);
        Assert.assertNotNull(job);

        boolean f = client.deleteJob(job.getId());
        Assert.assertTrue(f);
    }

    @Test
    public void testReleaseJob() {
    }

    @Test
    public void testBury() {
        Job job = client.reserveJob(1);
        Assert.assertNotNull(job);
        boolean f = client.buryJob(job.getId(), 1);
        Assert.assertTrue(f);
    }

    @Test
    public void testPutJob02() {
        long id = client.putJob(1, 0, 5000, "this is some \r\n data".getBytes());
        System.out.println(id);
        Assert.assertTrue(id != 0);

        Job job = client.reserveJob(1);
        Assert.assertNotNull(job);
    }

}
