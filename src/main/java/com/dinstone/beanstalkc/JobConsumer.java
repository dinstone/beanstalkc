/*
 * Copyright (C) 2012~2016 dinstone<dinstone@163.com>
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

/**
 * {@link JobConsumer} is a kind of client beanstalk, that is responsible for
 * the consumer job.
 * 
 * @author guojf
 * @version 1.0.0.2013-4-15
 */
public interface JobConsumer {

    /**
     * beanstalkd will wait to send a response until one becomes available. Once
     * a job is reserved for the client, the client has limited time to run
     * (TTR) the job before the job times out. When the job times out, the
     * server will put the job back into the ready queue. Both the TTR and the
     * actual time left can be found in response to the stats-job command. A
     * timeout value of 0 will cause the server to immediately return either a
     * response or TIMED_OUT. A positive value of timeout will limit the amount
     * of time the client will block on the reserve request until a job becomes
     * available.
     * 
     * @param timeout
     *        if timeout >= 0, then with reserve-with-timeout command.
     * @return
     */
    public Job reserveJob(long timeout);

    /**
     * The delete command removes a job from the server entirely. It is normally
     * used by the client when the job has successfully run to completion. A
     * client can delete jobs that it has reserved, ready jobs, delayed jobs,
     * and jobs that are buried.
     * 
     * @param id
     *        is the job id to delete.
     * @return
     */
    public boolean deleteJob(long id);

    /**
     * The release command puts a reserved job back into the ready queue (and
     * marks its state as "ready") to be run by any client. It is normally used
     * when the job fails because of a transitory error.
     * 
     * @param id
     *        is the job id to release.
     * @param priority
     *        is a new priority to assign to the job.
     * @param delay
     *        is an integer number of seconds to wait before putting the job in
     *        the ready queue. The job will be in the "delayed" state during
     *        this time.
     * @return
     */
    public boolean releaseJob(long id, int priority, int delay);

    /**
     * The bury command puts a job into the "buried" state. Buried jobs are put
     * into a FIFO linked list and will not be touched by the server again until
     * a client kicks them with the "kick" command.
     * 
     * @param id
     *        is the job id to bury.
     * @param priority
     *        is a new priority to assign to the job.
     * @return
     */
    public boolean buryJob(long id, int priority);

    /**
     * The "touch" command allows a worker to request more time to work on a
     * job. This is useful for jobs that potentially take a long time, but you
     * still want the benefits of a TTR pulling a job away from an unresponsive
     * worker. A worker may periodically tell the server that it's still alive
     * and processing a job (e.g. it may do this on DEADLINE_SOON).
     * 
     * @param id
     *        is the ID of a job reserved by the current connection.
     * @return
     */
    public boolean touchJob(long id);

    /**
     * close the current connection and release resources. that's status is
     * closed, and is no longer available.
     */
    public void close();
}
