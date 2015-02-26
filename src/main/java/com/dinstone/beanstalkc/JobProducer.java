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

import java.util.concurrent.TimeUnit;

/**
 * {@link JobProducer} is a kind of client beanstalk, that is responsible for
 * the production job.
 * 
 * @author guojf
 * @version 1.0.0.2013-4-15
 */
public interface JobProducer {

    /**
     * It inserts a job into the client's currently used tube.
     * 
     * @param priority
     *        an integer < 2**32. Jobs with smaller priority values will be
     *        scheduled before jobs with larger priorities. The most urgent
     *        priority is 0; the least urgent priority is 4,294,967,295.
     * @param delay
     *        {@link TimeUnit.SECONDS}
     * @param ttr
     *        {@link TimeUnit.SECONDS} time to run -- is an integer number of
     *        seconds to allow a worker to run this job. This time is counted
     *        from the moment a worker reserves this job. If the worker does not
     *        delete, release, or bury the job within <ttr> seconds, the job
     *        will time out and the server will release the job. The minimum ttr
     *        is 1. If the client sends 0, the server will silently increase the
     *        ttr to 1.
     * @param data
     *        the job body,that length is an integer indicating the size of the
     *        job body, not including the trailing "\r\n". This value must be
     *        less than max-job-size (default: 2**16).
     * @return the integer id of the new job
     */
    public long putJob(int priority, int delay, int ttr, byte[] data);

    /**
     * close the current connection and release resources. that's status is
     * closed, and is no longer available.
     */
    public void close();
}
