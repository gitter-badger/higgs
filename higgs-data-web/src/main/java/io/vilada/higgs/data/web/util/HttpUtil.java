/*
 * Copyright 2018 The Higgs Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vilada.higgs.data.web.util;


import com.google.common.base.Joiner;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static ResponseEntity get(String url, Map<String, Object> params, int timeoutMils) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        String parmstr = "";
        if(params != null) {
            parmstr = "?".concat(Joiner.on("&").withKeyValueSeparator("=").join(params));
        }
        //log.info("httpclient url={}", url.concat(parmstr));


        HttpGet get = new HttpGet(url + parmstr);
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(timeoutMils)
                .setConnectTimeout(timeoutMils).setRedirectsEnabled(true)
                .setSocketTimeout(timeoutMils).build();
        get.setConfig(config);

        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            ResponseEntity responseEntity = new ResponseEntity(JSONObject.fromObject(result), HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
            //log.info("get result :" + result);
            return responseEntity;
        } catch (Exception e) {
            //log.error("get is error {}", e);
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                //log.error("post is error {}", e);
                e.printStackTrace();
            }
        }
        return new ResponseEntity("", HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
    }

    public static ResponseEntity post(String url, Map<String, String> params, int timeoutMils) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = setHttpPostConfig(url, timeoutMils);
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        CloseableHttpResponse response = null;
        try {
            List formParams = new ArrayList();
            for (String key : params.keySet()) {
                formParams.add(new BasicNameValuePair(key, params.get(key)));
            }
            post.setEntity(new UrlEncodedFormEntity(formParams, "UTF-8"));
            response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            ResponseEntity responseEntity = new ResponseEntity(JSONObject.fromObject(result), HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
            return responseEntity;
        } catch (Exception e) {
            //log.error("post is error {}", e);
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                //log.error("post is error {}", e);
                e.printStackTrace();
            }
        }
        return new ResponseEntity("", HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
    }


    public static ResponseEntity post(String url, String json, int timeout) {
        //log.info("url=" + url + ",json=" + json);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = setHttpPostConfig(url, timeout);

        CloseableHttpResponse response = null;
        JSONObject jsonObject = new JSONObject();
        try {
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            post.setEntity(new StringEntity(json, "UTF-8"));
            response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            ResponseEntity responseEntity = new ResponseEntity(JSONObject.fromObject(result), HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
            //log.info("post result :" + result);
            return responseEntity;
        } catch (Exception e) {
            //log.error("post is error {}", e);
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                //log.error("post is error {}", e);
                e.printStackTrace();
            }
        }
        return new ResponseEntity(jsonObject, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
    }

    private static HttpPost setHttpPostConfig(String url, int timeout) {
        HttpPost post = new HttpPost(url);
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout).setRedirectsEnabled(true)
                .setSocketTimeout(timeout).build();
        post.setConfig(config);
        return post;
    }



}
