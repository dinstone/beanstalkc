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

import java.util.List;
import java.util.Map;

/**
 * {@link BeanstalkClient} is a full amount interface, mainly used for
 * management.
 * 
 * @author guojf
 * @version 2.0.0.2013-8-12
 */
public interface BeanstalkClient extends JobConsumer, JobProducer {

    /**
     * The "use" command is for producers. Subsequent put commands will put jobs
     * into the tube specified by this command. If no use command has been
     * issued, jobs will be put into the tube named "default".
     * 
     * @param tube
     *        is a name at most 200 bytes. It specifies the tube to use. If the
     *        tube does not exist, it will be created.
     * @return
     */
    public boolean useTube(String tube);

    /**
     * The "ignore" command is for consumers. It removes the named tube from the
     * watch list for the current connection.
     * 
     * @param tube
     *        is a name at most 200 bytes.
     * @return
     */
    public boolean ignoreTube(String tube);

    /**
     * The "watch" command adds the named tube to the watch list for the current
     * connection. A reserve command will take a job from any of the tubes in
     * the watch list. For each new connection, the watch list initially
     * consists of one tube, named "default".
     * 
     * @param tube
     * @return
     */
    public boolean watchTube(String tube);

    /**
     * The kick command applies only to the currently used tube. It moves jobs
     * into the ready queue. If there are any buried jobs, it will only kick
     * buried jobs. Otherwise it will kick delayed jobs.
     * 
     * @param bound
     * @return
     */
    public long kick(long bound);

    /**
     * The peek commands let the client inspect a job in the system.
     * 
     * @param jobId
     * @return
     */
    public Job peek(long jobId);

    /**
     * The peek commands let the client inspect a job in the system, return the
     * next ready job.
     * 
     * @return
     */
    public Job peekReady();

    /**
     * The peek commands let the client inspect a job in the system, return the
     * delayed job with the shortest delay left.
     * 
     * @return
     */
    public Job peekDelayed();

    /**
     * The peek commands let the client inspect a job in the system, return the
     * next job in the list of buried jobs.
     * 
     * @return
     */
    public Job peekBuried();

    /**
     * The stats-job command gives statistical information about the specified
     * job if it exists.<br>
     * The stats-job data is a YAML file representing a single dictionary of
     * strings to scalars. It contains these keys:<br>
     * - "id" is the job id<br>
     * - "tube" is the name of the tube that contains this job<br>
     * - "state" is "ready" or "delayed" or "reserved" or "buried"<br>
     * - "pri" is the priority value set by the put, release, or bury commands.<br>
     * - "age" is the time in seconds since the put command that created this
     * job.<br>
     * - "time-left" is the number of seconds left until the server puts this
     * job into the ready queue. This number is only meaningful if the job is
     * reserved or delayed. If the job is reserved and this amount of time
     * elapses before its state changes, it is considered to have timed out.<br>
     * - "timeouts" is the number of times this job has timed out during a
     * reservation.<br>
     * - "releases" is the number of times a client has released this job from a
     * reservation.<br>
     * - "buries" is the number of times this job has been buried.<br>
     * - "kicks" is the number of times this job has been kicked.<br>
     * 
     * @param jobId
     * @return a sequence of bytes. It is a YAML file with statistical
     *         information represented a dictionary.
     */
    public Map<String, String> statsJob(long jobId);

    /**
     * The stats-tube command gives statistical information about the specified
     * tube if it exists.<br>
     * The stats-tube data is a YAML file representing a single dictionary of
     * strings to scalars. It contains these keys:<br>
     * - "name" is the tube's name.<br>
     * - "current-jobs-urgent" is the number of ready jobs with priority < 1024
     * in this tube.<br>
     * - "current-jobs-ready" is the number of jobs in the ready queue in this
     * tube.<br>
     * - "current-jobs-reserved" is the number of jobs reserved by all clients
     * in this tube.<br>
     * - "current-jobs-delayed" is the number of delayed jobs in this tube.<br>
     * - "current-jobs-buried" is the number of buried jobs in this tube.<br>
     * - "total-jobs" is the cumulative count of jobs created in this tube.<br>
     * - "current-waiting" is the number of open connections that have issued a
     * reserve command while watching this tube but not yet received a response.<br>
     * 
     * @param tubeName
     * @return
     */
    public Map<String, String> statsTube(String tubeName);

    /**
     * The stats command gives statistical information about the system as a
     * whole.<br>
     * The stats data for the system is a YAML file representing a single
     * dictionary of strings to scalars. It contains these keys:<br>
     * - "current-jobs-urgent" is the number of ready jobs with priority < 1024.<br>
     * - "current-jobs-ready" is the number of jobs in the ready queue.<br>
     * - "current-jobs-reserved" is the number of jobs reserved by all clients.<br>
     * - "current-jobs-delayed" is the number of delayed jobs.<br>
     * - "current-jobs-buried" is the number of buried jobs.<br>
     * - "cmd-put" is the cumulative number of put commands.<br>
     * - "cmd-peek" is the cumulative number of peek commands.<br>
     * - "cmd-peek-ready" is the cumulative number of peek-ready commands.<br>
     * - "cmd-peek-delayed" is the cumulative number of peek-delayed commands.<br>
     * - "cmd-peek-buried" is the cumulative number of peek-buried commands.<br>
     * - "cmd-reserve" is the cumulative number of reserve commands.<br>
     * - "cmd-use" is the cumulative number of use commands.<br>
     * - "cmd-watch" is the cumulative number of watch commands.<br>
     * - "cmd-ignore" is the cumulative number of ignore commands.<br>
     * - "cmd-delete" is the cumulative number of delete commands.<br>
     * - "cmd-release" is the cumulative number of release commands.<br>
     * - "cmd-bury" is the cumulative number of bury commands.<br>
     * - "cmd-kick" is the cumulative number of kick commands.<br>
     * - "cmd-stats" is the cumulative number of stats commands.<br>
     * - "cmd-stats-job" is the cumulative number of stats-job commands.<br>
     * - "cmd-stats-tube" is the cumulative number of stats-tube commands.<br>
     * - "cmd-list-tubes" is the cumulative number of list-tubes commands.<br>
     * - "cmd-list-tube-used" is the cumulative number of list-tube-used
     * commands.<br>
     * - "cmd-list-tubes-watched" is the cumulative number of list-tubes-watched
     * commands. <br>
     * - "job-timeouts" is the cumulative count of times a job has timed out.<br>
     * - "total-jobs" is the cumulative count of jobs created.<br>
     * - "max-job-size" is the maximum number of bytes in a job.<br>
     * - "current-tubes" is the number of currently-existing tubes.<br>
     * - "current-connections" is the number of currently open connections.<br>
     * - "current-producers" is the number of open connections that have each
     * issued at least one put command.<br>
     * - "current-workers" is the number of open connections that have each
     * issued at least one reserve command.<br>
     * - "current-waiting" is the number of open connections that have issued a
     * reserve command but not yet received a response.<br>
     * - "total-connections" is the cumulative count of connections.<br>
     * - "pid" is the process id of the server.<br>
     * - "version" is the version string of the server. <br>
     * - "rusage-utime" is the accumulated user CPU time of this process in
     * seconds and microseconds.<br>
     * - "rusage-stime" is the accumulated system CPU time of this process in
     * seconds and microseconds.<br>
     * - "uptime" is the number of seconds since this server started running.<br>
     * - "binlog-oldest-index" is the index of the oldest binlog file needed to
     * store the current jobs<br>
     * - "binlog-current-index" is the index of the current binlog file being
     * written to. If binlog is not active this value will be 0<br>
     * - "binlog-max-size" is the maximum size in bytes a binlog file is allowed
     * to get before a new binlog file is opened<br>
     * 
     * @return
     */
    public Map<String, String> stats();

    /**
     * The list-tubes command returns a list of all existing tubes.
     * 
     * @return
     */
    public List<String> listTubes();

    /**
     * The list-tube-used command returns the tube currently being used by the
     * client.
     * 
     * @return the name of the tube being used.
     */
    public String listTubeUsed();

    /**
     * The list-tubes-watched command returns a list tubes currently being
     * watched by the client.
     * 
     * @return
     */
    public List<String> listTubeWatched();
}
