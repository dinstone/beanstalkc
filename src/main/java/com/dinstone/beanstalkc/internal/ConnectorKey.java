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

    private static final String[] CONNECTION_PROPERTIES = { Configuration.SERVICE_HOST, Configuration.SERVICE_PORT };

    private Map<String, String> properties;

    /**
     * @param config
     */
    public ConnectorKey(Configuration config) {
        Map<String, String> m = new HashMap<String, String>();
        if (config != null) {
            for (String property : CONNECTION_PROPERTIES) {
                String value = config.get(property);
                if (value != null) {
                    m.put(property, value);
                }
            }
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
        for (String property : CONNECTION_PROPERTIES) {
            String value = properties.get(property);
            if (value != null) {
                result = prime * result + value.hashCode();
            }
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
            for (String property : CONNECTION_PROPERTIES) {
                String thisValue = this.properties.get(property);
                String thatValue = other.properties.get(property);
                if (thisValue == thatValue) {
                    continue;
                }
                if (thisValue == null || !thisValue.equals(thatValue)) {
                    return false;
                }
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
        return "ConnectorKey{properties=" + properties + "}";
    }

}
