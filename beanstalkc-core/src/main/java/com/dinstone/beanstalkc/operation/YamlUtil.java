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
package com.dinstone.beanstalkc.operation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * yaml parser util
 * 
 * @author guojf
 * 
 * @version 1.0.0.2013-8-12
 */
public class YamlUtil {

    /**
     * @param charset
     * @param in
     * 
     * @return
     * 
     * @throws IOException
     */
    public static Map<String, String> yaml2Map(Charset charset, InputStream in) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] kvs = line.split(":");
            if (kvs.length == 2) {
                map.put(kvs[0].trim(), kvs[1].trim());
            }
        }

        return map;
    }

    /**
     * @param charset
     * @param in
     * 
     * @return
     * 
     * @throws IOException
     */
    public static List<String> yaml2List(Charset charset, InputStream in) throws IOException {
        List<String> list = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] kvs = line.split(" ");
            if (kvs.length == 2) {
                list.add(kvs[1].trim());
            }
        }
        return list;
    }

}