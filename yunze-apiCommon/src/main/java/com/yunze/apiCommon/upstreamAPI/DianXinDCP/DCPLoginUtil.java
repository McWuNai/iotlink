package com.yunze.apiCommon.upstreamAPI.DianXinDCP;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.XML;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.concurrent.TimeUnit;

@Component
public class DCPLoginUtil {

    private static String urls = "https://si.global.ct10649.com/api/dcpsiapi/login";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public  String getToken(String username,String password){
        CloseableHttpClient closeableHttpClient=null;
        CloseableHttpResponse closeableHttpResponse=null;

        try {
            closeableHttpClient= HttpClientBuilder.create().build();
            HttpPost httpPost=new HttpPost();
            httpPost.setURI(new URI(urls));
            httpPost.setHeader("Content-Type","application/json;charset=utf-8");

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("password",password);

            StringEntity param=new StringEntity(jsonObject.toJSONString(),"utf-8");
            httpPost.setEntity(param);

            closeableHttpResponse=closeableHttpClient.execute(httpPost);
            HttpEntity httpEntity=closeableHttpResponse.getEntity();
            String enstr= EntityUtils.toString(httpEntity,"utf-8");
            JSONObject data= JSON.parseObject(enstr);
            System.out.println(data);
            String token=data.getJSONObject("data").getString("token");

            //保存到redis
            redisTemplate.opsForValue().set("dcp_token",token,10, TimeUnit.MINUTES);
            return token;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null!=closeableHttpClient){
                    closeableHttpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(null!=closeableHttpResponse){
                    closeableHttpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public HttpURLConnection createConnection(URI uri) throws IOException {
        URL url = uri.toURL();
        URLConnection connection = url.openConnection();
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
        // httpsURLConnection.setSSLSocketFactory(new TLSSocketConnec());
        return httpsURLConnection;
    }

    public static org.json.JSONObject httpSoap(String soap_url , String xml) {
        DCPLoginUtil httpsUrlConnectionMessageSender = new DCPLoginUtil();
        HttpURLConnection connection;
        try {
            connection = httpsUrlConnectionMessageSender.createConnection(new URI(soap_url ));
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");

            connection.connect();
            //POST请求
            OutputStreamWriter os = null;
            String json="";
            os = new OutputStreamWriter(connection.getOutputStream());
            os.write(xml);
            os.flush();
            json=getResponse(connection);

            org.json.JSONObject jsons=XML.toJSONObject(json);

            if (connection != null) {
                connection.disconnect();
                return jsons;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getResponse(HttpURLConnection Conn) throws IOException {
        InputStream is;
        if (Conn.getResponseCode() >= 400) {
            is = Conn.getErrorStream();
        } else {
            is = Conn.getInputStream();
        }


        String response = "";
        byte buff[] = new byte[512];
        int b = 0;
        while ((b = is.read(buff, 0, buff.length)) != -1) {
            response += new String(buff, 0, b);

        }
        is.close();
        return response;
    }

}
