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
 * The client sent a command line that was not well-formed. This can happen if the line does not end with \r\n, if
 * non-numeric characters occur where an integer is expected, if the wrong number of arguments are present, or if the
 * command line is mal-formed in any other way.
 * 
 * @author guojinfei
 * @version 1.0.0
 */
public class BadFormatException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public BadFormatException() {
    }

    /**
     * @param message
     */
    public BadFormatException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public BadFormatException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public BadFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public BadFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
