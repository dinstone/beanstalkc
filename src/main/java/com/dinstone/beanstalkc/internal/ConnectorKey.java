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

package com.dinstone.beanstalkc.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dinstone.beanstalkc.Configuration;

/**
 * @author guojf
 * @version 1.0.0.2013-4-10
 */
public class ConnectorKey {

    private Map<String, String> properties;

    /**
     * @param config
     */
    public ConnectorKey(Configuration config) {
        Map<String, String> m = new HashMap<String, String>();
        if (config != null) {
            m.put(Configuration.REMOTE_HOST, config.getRemoteHost());
            m.put(Configuration.REMOTE_PORT, config.getRemoteHost());
        }
        this.properties = Collections.unmodifiableMap(m);
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String value = properties.get(Configuration.REMOTE_HOST);
        if (value != null) {
            result = prime * result + value.hashCode();
        }
        value = properties.get(Configuration.REMOTE_PORT);
        if (value != null) {
            result = prime * result + value.hashCode();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConnectorKey other = (ConnectorKey) obj;
        if (this.properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (other.properties == null) {
            return false;
        } else {
            String thisValue = this.properties.get(Configuration.REMOTE_HOST);
            String thatValue = other.properties.get(Configuration.REMOTE_HOST);
            if (thisValue == null || !thisValue.equals(thatValue)) {
                return false;
            }

            thisValue = this.properties.get(Configuration.REMOTE_PORT);
            thatValue = other.properties.get(Configuration.REMOTE_PORT);
            if (thisValue == null || !thisValue.equals(thatValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ConnectorKey [properties=" + properties + "]";
    }

}
