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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author guojf
 * @version 1.0.0.2013-12-31
 */
public abstract class StatsInfo {

    public static AtomicInteger sendCount = new AtomicInteger();

    public static AtomicInteger sendError = new AtomicInteger();

    public static AtomicLong sendTimes = new AtomicLong();

    public static AtomicInteger receiveCount = new AtomicInteger();

    public static AtomicInteger receiveError = new AtomicInteger();

    public static String getView() {
        return "Send[" + sendCount.get() + ":" + sendTimes.get() + ":" + sendError.get() + "]; Receive["
                + receiveCount.get() + ":" + receiveError.get() + "]";
    }
}
