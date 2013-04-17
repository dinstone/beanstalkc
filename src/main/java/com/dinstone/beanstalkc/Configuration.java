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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beanstalk client configuration.
 * 
 * @author guojf
 * @version 1.0.0.2013-4-10
 */
public final class Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private static String configFileName = "beanstalkc.properties";

    private static Properties defProperties = new Properties();

    /** beanstalk server host name */
    public static final String REMOTE_HOST = "RemoteHost";

    /** beanstalk server listen port */
    public static final String REMOTE_PORT = "RemotePort";

    /** option timeout (s) */
    public static final String OPTION_TIMEOUT = "OptionTimeout";

    static {
        initDefault();
    }

    private static void initDefault() {
        InputStream in = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = Configuration.class.getClassLoader();
            }

            in = classLoader.getResourceAsStream(configFileName);
            if (in != null) {
                defProperties.load(in);
            }
        } catch (IOException e) {
            LOG.warn("can't load default configuration file [" + configFileName + "] from classpath.", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private final Properties properties;

    /**
     * loading default properties from classpath
     */
    public Configuration() {
        this(null);
    }

    /**
     * loading default properties from classpath, then overriding default
     * properties with <code>properties</code>
     * 
     * @param properties
     */
    public Configuration(Properties properties) {
        this.properties = new Properties();
        this.properties.putAll(defProperties);

        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    /**
     * Get the value of the <code>name</code> property, <code>null</code> if no
     * such property exists.
     * 
     * @param name
     * @return
     */
    public String get(String name) {
        return properties.getProperty(name);
    }

    /**
     * Set the <code>value</code> of the <code>name</code> property.
     * 
     * @param name
     *        property name.
     * @param value
     *        property value.
     */
    public void set(String name, String value) {
        properties.setProperty(name, value);
    }

    /**
     * Get the value of the <code>name</code> property as an <code>int</code>.
     * If no such property exists, or if the specified value is not a valid
     * <code>int</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    public int getInt(String name, int defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            String hexString = getHexDigits(valueString);
            if (hexString != null) {
                return Integer.parseInt(hexString, 16);
            }
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Set the value of the <code>name</code> property to an <code>int</code>.
     * 
     * @param name
     *        property name.
     * @param value
     *        <code>int</code> value of the property.
     */
    public void setInt(String name, int value) {
        set(name, Integer.toString(value));
    }

    /**
     * Get the value of the <code>name</code> property as a <code>long</code>.
     * If no such property is specified, or if the specified value is not a
     * valid <code>long</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name
     *        property name.
     * @param defaultValue
     *        default value.
     * @return property value as a <code>long</code>, or
     *         <code>defaultValue</code>.
     */
    public long getLong(String name, long defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            String hexString = getHexDigits(valueString);
            if (hexString != null) {
                return Long.parseLong(hexString, 16);
            }
            return Long.parseLong(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Set the value of the <code>name</code> property to a <code>long</code>.
     * 
     * @param name
     *        property name.
     * @param value
     *        <code>long</code> value of the property.
     */
    public void setLong(String name, long value) {
        set(name, Long.toString(value));
    }

    private String getHexDigits(String value) {
        boolean negative = false;
        String str = value;
        String hexString = null;
        if (value.startsWith("-")) {
            negative = true;
            str = value.substring(1);
        }
        if (str.startsWith("0x") || str.startsWith("0X")) {
            hexString = str.substring(2);
            if (negative) {
                hexString = "-" + hexString;
            }
            return hexString;
        }
        return null;
    }

    /**
     * Get the value of the <code>name</code> property as a <code>float</code>.
     * If no such property is specified, or if the specified value is not a
     * valid <code>float</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name
     *        property name.
     * @param defaultValue
     *        default value.
     * @return property value as a <code>float</code>, or
     *         <code>defaultValue</code>.
     */
    public float getFloat(String name, float defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Set the value of the <code>name</code> property to a <code>float</code>.
     * 
     * @param name
     *        property name.
     * @param value
     *        property value.
     */
    public void setFloat(String name, float value) {
        set(name, Float.toString(value));
    }

    /**
     * Get the value of the <code>name</code> property as a <code>boolean</code>
     * . If no such property is specified, or if the specified value is not a
     * valid <code>boolean</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name
     *        property name.
     * @param defaultValue
     *        default value.
     * @return property value as a <code>boolean</code>, or
     *         <code>defaultValue</code>.
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String valueString = get(name);
        if ("true".equals(valueString)) {
            return true;
        } else if ("false".equals(valueString)) {
            return false;
        } else {
            return defaultValue;
        }
    }

    /**
     * Set the value of the <code>name</code> property to a <code>boolean</code>
     * .
     * 
     * @param name
     *        property name.
     * @param value
     *        <code>boolean</code> value of the property.
     */
    public void setBoolean(String name, boolean value) {
        set(name, Boolean.toString(value));
    }

    /**
     * get remote host
     * 
     * @return
     */
    public String getRemoteHost() {
        return get(REMOTE_HOST);
    }

    /**
     * set remote host
     * 
     * @param host
     */
    public void setRemoteHost(String host) {
        set(REMOTE_HOST, host);
    }

    /**
     * get remote port
     * 
     * @return
     */
    public int getRemotePort() {
        return getInt(REMOTE_PORT, 11300);
    }

    /**
     * set remote port
     * 
     * @param port
     */
    public void setRemotePort(int port) {
        setInt(REMOTE_PORT, port);
    }

}
