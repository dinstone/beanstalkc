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

public interface JobConsumer {

    public Job reserveJob(long timeout);

    public boolean deleteJob(long id);

    public boolean releaseJob(long id, int priority, int delay);

    public boolean buryJob(long id, int priority);

    public boolean touchJob(long id);

    public boolean watchTube(String tube);

    public boolean ignoreTube(String tube);

    public void close();
}
