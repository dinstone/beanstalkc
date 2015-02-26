/*
 * Copyright (C) 2012~2015 dinstone<dinstone@163.com>
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
 * This means that the server has been put into "drain mode" and is no longer accepting new jobs. The client should try
 * another server or disconnect and try again later.
 * 
 * @author guojinfei
 * @version 1.0.0
 */
public class DrainingException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = 1L;

    public DrainingException() {
    }

    public DrainingException(String message) {
        super(message);
    }

    public DrainingException(Throwable cause) {
        super(cause);
    }

    public DrainingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DrainingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
