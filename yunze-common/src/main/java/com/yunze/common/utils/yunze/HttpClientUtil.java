package com.yunze.common.utils.yunze;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class HttpClientUtil {

    /**
     * 传递string参数
     */
    public static String doPostStr(String url, String json){
        String result = null;

        HttpPost post = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            // 构建消息实体
            StringEntity entity = new StringEntity(json, Charset.forName("UTF-8"));
            post.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(post);

            // 检验返回码
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode == HttpStatus.SC_OK){
                result= EntityUtils.toString(response.getEntity(),"utf-8");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(post != null){
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(result);
        return result;
    }

    /**
     * post 请求
     */
    public static String doPostJson(String url, JSONObject json){
        String result = null;

        HttpPost post = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            // 构建消息实体
            StringEntity entity = new StringEntity(json.toString(), Charset.forName("UTF-8"));
            post.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(post);

            // 检验返回码
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode == HttpStatus.SC_OK){
                result= EntityUtils.toString(response.getEntity(),"utf-8");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(post != null){
                try {
                    post.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(result);
        return result;
    }

    /**
     *get 请求
     */
    public static String doGet(String url) throws ClientProtocolException, IOException {
        String result = null;
        DefaultHttpClient client = new DefaultHttpClient();
        org.apache.http.client.methods.HttpGet getHttp = new org.apache.http.client.methods.HttpGet(url);
        HttpResponse res =client.execute(getHttp);
        HttpEntity entity =res.getEntity();
        if(null!=entity){
            result = EntityUtils.toString(entity,"utf-8");
            //System.out.println(result);
        }
        System.out.println(result);
        return result;
    }
}
