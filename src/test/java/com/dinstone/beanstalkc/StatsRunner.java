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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author guojf
 * @version 1.0.0.2013-12-31
 */
public class StatsRunner extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(StatsRunner.class);

    private volatile boolean stop;

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            LOG.info("{}", StatsInfo.getView());
        }
    }

    /**
     * the stop to set
     * 
     * @param stop
     * @see StatsRunner#stop
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

}
