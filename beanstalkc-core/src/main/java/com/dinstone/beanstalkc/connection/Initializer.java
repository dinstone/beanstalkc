/*
 * Copyright (C) 2012~2023 dinstone<dinstone@163.com>
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
package com.dinstone.beanstalkc.connection;

/**
 * init watch or use tube for connection.
 * 
 * @author guojf
 * 
 * @version 1.0.0.2013-4-15
 */
public interface Initializer {

    /**
     * init watch or use tube for connection.
     * 
     * The "watch" command adds the named tube to the watch list for the current connection. A reserve command will take
     * a job from any of the tubes in the watch list. For each new connection, the watch list initially consists of one
     * tube, named "default".
     * 
     * When a client connects, its watch list is initially just the tube named "default". If it submits jobs without
     * having sent a "use" command, they will live in the tube named "default".
     * 
     * The "use" command is for producers. Subsequent put commands will put jobs into the tube specified by this
     * command. If no use command has been issued, jobs will be put into the tube named "default".
     * 
     * @param connection
     * 
     * @throws Exception
     */
    public void initialize(Connection connection) throws Exception;
}
