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

public class Job {

    private long id;

    private byte[] data;

    /**
     * the id to get
     * 
     * @return the id
     * @see Job#id
     */
    public long getId() {
        return id;
    }

    /**
     * the id to set
     * 
     * @param id
     * @see Job#id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * the data to get
     * 
     * @return the data
     * @see Job#data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * the data to set
     * 
     * @param data
     * @see Job#data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

}
