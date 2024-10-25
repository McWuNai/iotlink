package com.yunze.apiCommon.upstreamAPI.DianXinDCP;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询类接口
 */
@Component
public class DcpDxService {

    //接口
    private static String soap_url = "https://global.ct10649.com/dcpapi/SubscriptionManagement?WSDL=";

    @Resource
    private DCPLoginUtil dcpLoginUtil;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cd_username", "");
        map.put("cd_pwd","");
        map.put("cd_key", "");

        DcpDxService ds = new DcpDxService();

        System.out.println(ds.changeCardStatus("8986","3",map));


    }



    /**
     * auther:
     * return:
     * 描述： 更新限速
     * 上行速率：1 1kbps
     * 2 64kbps
     * 3 128kbps
     * 4 512kbps
     * 5 1Mbps
     * 6 3Mbps
     * 7 5Mbps
     * 8 7Mbps
     * 9 10Mbps
     * 10 20Mbps
     * 11 30Mbps
     * 12 40Mbps
     * 13 50Mbps
     * 14 60Mbps
     * 15 70Mbps
     * 下行速率：
     * 16 80Mbps
     * 17 90Mbps
     * 18 100Mbps
     * 19 110Mbps
     * 20 120Mbps
     * 21 130Mbps
     * 22 140Mbps
     * 23 150Mbps
     */
    public Map<String,Object> changeLimit(String tel,
                                          String upValue,String dwValue,Map<String,Object> infoMap){
        JSONObject jsonMap = new JSONObject();
        JSONObject json = new JSONObject();
        jsonMap.put("state", "false");
        jsonMap.put("info", "api调用失败");
        jsonMap.put("data","0");
        try {
            //获取流量地址
            String url = "https://si.global.ct10649.com/api/dcpsiapi/ue/ambr/update";
            json.put("msisdn", tel);
            json.put("UE_AMBR_UL", upValue);
            json.put("UE_AMBR_DL", dwValue);
            String js = getDocking(url,json,infoMap);

            JSONObject jsonObject= JSONObject.parseObject(js);
            Object data = jsonObject.get("msg");
            if(data.toString().equals("success")){
                jsonMap.put("state", "true");
                jsonMap.put("info", "成功");
                jsonMap.put("data",data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(jsonMap);
        return jsonMap;
    }

    /**
     * return:
     * 描述： 查询是否限速
     */
    public  Map<String,Object> queryLimit(String tel,Map<String,Object> infoMap){
        JSONObject jsonMap = new JSONObject();
        JSONObject json = new JSONObject();
        jsonMap.put("state", "false");
        jsonMap.put("info", "api调用失败");
        jsonMap.put("data","0");
        try {
            //获取流量地址
            String url = "https://si.global.ct10649.com/api/dcpsiapi/ue/" +
                    "ambr/query?msisdn="+tel;
            String js = doGet(url,infoMap);

            JSONObject jsonObject= JSONObject.parseObject(js);
            Object code = jsonObject.get("status");
            Object data = jsonObject.get("data");
            if(code.toString().equals("200")){
                jsonMap.put("state", "true");
                jsonMap.put("info", "成功");
                jsonMap.put("data",data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(jsonMap);
        return jsonMap;
    }

    /**
     * return:
     * 描述： 查询实名
     */
    public  Map<String,Object> queryAuth(String iccid,String tel,Map<String,Object> infoMap){
        JSONObject jsonMap = new JSONObject();
        jsonMap.put("state", "false");
        jsonMap.put("info", "api调用失败");
        jsonMap.put("data","0");
        try {
            //获取流量地址
            String url = "https://si.global.ct10649.com/api/dcpsiapi/ct" +
                    "/subscriptionQuery?iccid="+iccid+"&msisdn="+tel+"&isAuth=1";
            jsonMap.put("iccid",iccid);
            jsonMap.put("msisdn",tel);
            jsonMap.put("isAuth","1");
            String js = doGet(url, infoMap);

            JSONObject jsonObject= JSONObject.parseObject(js);
            Object code = jsonObject.get("status");
            Object data = jsonObject.get("data");
            if(code.toString().equals("200")){
                jsonMap.put("state", "true");
                jsonMap.put("info", "成功");
                jsonMap.put("data",data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(jsonMap);
        return jsonMap;
    }






    /**
     * return:
     * 描述： 流量查询
     */
    public  Map<String,Object> queryFlow(String tel,Map<String,Object> infoMap){
        JSONObject jsonMap = new JSONObject();
        JSONObject jsonMaps = new JSONObject();
        jsonMap.put("state", "false");
        jsonMap.put("info", "api调用失败");
        jsonMap.put("data","0");

        String sjyyyy = getMonthLastDayyyyy(0); //获取本年
        String sjmm = getMonthLastDaymm(0); //获取本月
        String sjdd = getMonthLastDaydd(); //获取本日

        try {
            //获取流量地址
            String url = "https://si.global.ct10649.com/api/dcpsiapi/iccid/"+tel+"/trafficUsageHistory";
            String month = sjyyyy+"-"+sjmm;
            JSONArray jsonArray = new JSONArray();
            jsonArray.add("ALL");
            //j String[] strs = new String[]{"GPRS"};
            jsonMaps.put("type",jsonArray);
            // jsonMaps.put("type",strs);
            jsonMaps.put("month", month);			//年月份
            jsonMaps.put("from_date", "01");	//开始日
            jsonMaps.put("to_date", sjdd);		//结束日
            //System.out.println(jsonMaps);
            String js = getDocking(url,jsonMaps,infoMap);

            JSONObject j0= JSONObject.parseObject(js);
            Object data = j0.get("data");
            Object state=j0.get("status");
            if(state.toString().equals("200")){
                jsonMap.put("state","true");
                JSONObject j1= JSONObject.parseObject(data.toString());
                Object totalVolumnGPRS = j1.get("totalVolumnGPRS");
                //MB
                Double toGPRS = Double.valueOf(totalVolumnGPRS.toString())/1024/1024;
                DecimalFormat df=new DecimalFormat("#0.00");
                toGPRS=Double.parseDouble(df.format(toGPRS));
                jsonMap.put("data", data);
                jsonMap.put("info", "成功");
            }

            return jsonMap;
//			//System.out.println(jsonMap.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(jsonMap);
        return jsonMap;
    }

    /**
     *  TODO  停开机
     *
     *  •Activate（激活）---------1
     *  •Pause（停机）------------2
     *  •Deactivate（使失效） -----3
     *  •Reactivate（重新激活）----4
     * @return
     */
    public  Map<String,Object> changeCardStatus(String tel,String setup,Map<String,Object> infoMap){
        JSONObject jsonMap = new JSONObject();
        jsonMap.put("state", "false");
        jsonMap.put("info", "api调用失败");
        jsonMap.put("data","0");
        String env = "\"http://schemas.xmlsoap.org/soap/envelope/\"";
        String sub = "\"http://api.dcp.ericsson.net/SubscriptionManagement\"";
        String wsse = "\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"";

        String state = null;
        switch (setup) {
            case "2":
                state = "Activate";
                break;
            case "3":
                state = "Pause";
                break;
            default:
                break;
        }
        StringBuffer sb = new StringBuffer();
        String name=(String)infoMap.get("cd_username");
        String password= (String)infoMap.get("cd_pwd");
        String[] names=name.replace("，",",").split(",");
        String[] passwords=password.replace("，",",").split(",");

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<soapenv:Envelope xmlns:soapenv="+env+" xmlns:sub="+sub+">");
        sb.append("<soapenv:Header>");
        sb.append("<wsse:Security xmlns:wsse="+wsse+" wsse:mustUnderstand=\"1\">");
        sb.append("<wsse:UsernameToken>");
        sb.append("<wsse:Username>"+names[0]+"</wsse:Username>"); //用户名
        sb.append("<wsse:Password>"+passwords[0]+"</wsse:Password>");  //密码
        sb.append("</wsse:UsernameToken>");
        sb.append("</wsse:Security>");
        sb.append("</soapenv:Header>");
        sb.append("<soapenv:Body>");
        sb.append("<sub:RequestSubscriptionStatusChange>");
        sb.append("<resource>");
        sb.append("<id>"+tel+"</id>");
        sb.append("<type>icc</type>");
        sb.append("</resource>");
        sb.append("<subscriptionStatus>"+state+"</subscriptionStatus>");
        sb.append("<range>1</range>");
        sb.append("</sub:RequestSubscriptionStatusChange>");
        sb.append("</soapenv:Body>");
        sb.append("</soapenv:Envelope>");
        String xml = sb.toString();
        System.out.println(soap_url);
        System.out.println(xml);
        try {
            org.json.JSONObject json = dcpLoginUtil.httpSoap(soap_url,xml);
            JSONObject j0= JSONObject.parseObject(json.toString());
            Object Envelope =j0.get("env:Envelope");

            JSONObject j1= JSONObject.parseObject(Envelope.toString());
            Object envBody = j1.get("env:Body");
            if((envBody.toString()).contains("env:Fault")){
                jsonMap.put("info", "操作失败");
                return jsonMap;
            }
            JSONObject j2= JSONObject.parseObject(envBody.toString());
            Object RequestSubscriptionStatusChangeResponse = j2.get("ns2:RequestSubscriptionStatusChangeResponse");

            JSONObject j3= JSONObject.parseObject(RequestSubscriptionStatusChangeResponse.toString());
            Object serviceRequestId  = j3.get("serviceRequestId").toString();
            //提示变更结果
            StringBuffer sbs = new StringBuffer();
            sbs.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sbs.append("<soapenv:Envelope xmlns:soapenv="+env+" xmlns:sub="+sub+">");
            sbs.append("<soapenv:Header>");
            sbs.append("<wsse:Security xmlns:wsse="+wsse+" wsse:mustUnderstand=\"1\">");
            sbs.append("<wsse:UsernameToken>");
            sbs.append("<wsse:Username>"+names[0]+"</wsse:Username>"); //用户名
            sbs.append("<wsse:Password>"+passwords[0]+"</wsse:Password>");  //密码
            sbs.append("</wsse:UsernameToken>");
            sbs.append("</wsse:Security>");
            sbs.append("</soapenv:Header>");
            sbs.append("<soapenv:Body>");
            sbs.append("<sub:QuerySubscriptionStatusChange>");
            sbs.append("<serviceRequestId>"+serviceRequestId+"</serviceRequestId>");
            sbs.append("</sub:QuerySubscriptionStatusChange>");
            sbs.append("</soapenv:Body>");
            sbs.append("</soapenv:Envelope>");
            String xmls = sbs.toString();
            json = dcpLoginUtil.httpSoap(soap_url,xmls);
            JSONObject jss= JSONObject.parseObject(json.toString());
            Object envEnvelope =jss.get("env:Envelope");

            JSONObject jss1= JSONObject.parseObject(envEnvelope.toString());
            Object envBodys =jss1.get("env:Body");

            JSONObject jss2= JSONObject.parseObject(envBodys.toString());
            Object ns2QuerySubscriptionStatusChangeResponse =jss2.get("ns2:QuerySubscriptionStatusChangeResponse");

            JSONObject jss3= JSONObject.parseObject(ns2QuerySubscriptionStatusChangeResponse.toString());
            Object statusRequestResponse =jss3.get("statusRequestResponse");
            String zt = "查询失败";
            String state_id = "-1";
            switch (statusRequestResponse.toString()) {
                case "Completed":
                    zt = "修改成功";
                    state_id = "0000";
                    break;
                case "In Progress":
                    zt = "修改中";
                    break;
                case "Pending":
                    zt = "待处理";
                    break;
                case "Canceled":
                    zt = "已 取 消";
                    break;
                case "Rejected":
                    zt = "已拒绝";
                    break;
                default:
                    zt = "修改失败！";
                    break;
            }
            jsonMap.put("state", "true");
            jsonMap.put("data", jss);
            jsonMap.put("info", "成功");
        } catch (Exception  e) {
            e.printStackTrace();
        }
        return jsonMap;
    }



    /**
     *  TODO 卡状态查询
     * @return
     */
    public  Map<String,Object> querySimStatus(String tel,Map<String,Object> infoMap){
        Map<String,Object> retMap = new HashMap<>();
        retMap.put("state", "false");
        retMap.put("info", "api调用失败");
        retMap.put("data", "0");
        String env = "\"http://schemas.xmlsoap.org/soap/envelope/\"";
        String sub = "\"http://api.dcp.ericsson.net/SubscriptionManagement\"";
        String wsse = "\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"";

        StringBuffer sb = new StringBuffer();
        String name=(String)infoMap.get("cd_username");
        String password= (String)infoMap.get("cd_pwd");
        String[] names=name.replace("，",",").split(",");
        String[] passwords=password.replace("，",",").split(",");

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<soapenv:Envelope xmlns:soapenv="+env+" xmlns:sub="+sub+">");
        sb.append("<soapenv:Header>");
        sb.append("<wsse:Security xmlns:wsse="+wsse+" wsse:mustUnderstand=\"1\">");
        sb.append("<wsse:UsernameToken>");
        sb.append("<wsse:Username>"+names[0]+"</wsse:Username>"); //用户名
        sb.append("<wsse:Password>"+passwords[0]+"</wsse:Password>");  //密码


        sb.append("</wsse:UsernameToken>");
        sb.append("</wsse:Security>");
        sb.append("</soapenv:Header>");
        sb.append("<soapenv:Body>");
        sb.append("<sub:QuerySimResource>");
        sb.append("<resource>");
        sb.append("<id>"+tel+"</id>"); //查询号码
        sb.append("<type>icc</type>");
        sb.append("</resource>");
        sb.append("</sub:QuerySimResource>");
        sb.append("</soapenv:Body>");
        sb.append("</soapenv:Envelope>");
        String xml = sb.toString();
        try {
            org.json.JSONObject json = DCPLoginUtil.httpSoap(soap_url,xml);
            JSONObject j0= JSONObject.parseObject(json.toString());
            Object Envelope =j0.get("env:Envelope");

            JSONObject j1= JSONObject.parseObject(Envelope.toString());
            Object Body = j1.get("env:Body");

            JSONObject j2= JSONObject.parseObject(Body.toString());
            Object qsr = j2.get("ns2:QuerySimResourceResponse");

            JSONObject j3= JSONObject.parseObject(qsr.toString());
            Object SimResource  = j3.get("SimResource");

            JSONObject j4= JSONObject.parseObject(SimResource.toString());
            Object simSubscriptionStatus  = j4.get("simSubscriptionStatus");

//			//System.out.println(simSubscriptionStatus);

            String zt = "查询失败";
            String state_id = "-1";
            switch (simSubscriptionStatus.toString()) {
                case "Active":
                    retMap.put("state", "true");
                    retMap.put("info", "成功");
                    break;
                case "Pause":
                    retMap.put("state", "true");
                    retMap.put("info", "成功");
                    break;
                case "Deactivated":
                    retMap.put("state", "true");
                    retMap.put("info", "成功");
                    break;
                case "Terminated":
                    retMap.put("state", "true");
                    retMap.put("info", "成功");
                    break;
                default:
                    retMap.put("state", "true");
                    retMap.put("info", "成功");
                    break;
            }
            retMap.put("data", j0);
            retMap.put("state", "true");
            retMap.put("info", "成功");
        } catch (Exception  e) {
            e.printStackTrace();
        }
        //System.out.println(retMap);
        return retMap;
    }





    /**
     * 发送GET请求
     * @return
     * @throws IOException
     */
    public  String doGet(String httpUrl,Map<String,Object> infoMap) {

        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            String name=(String)infoMap.get("cd_username");
            String password= (String)infoMap.get("cd_pwd");
            String[] names=name.replace("，",",").split(",");
            String[] passwords=password.replace("，",",").split(",");

            //Object tokenO=redisTemplate.opsForValue().get("dcp_token");
            String huoqutoken=null;
            /*if(null!=tokenO){
                huoqutoken=tokenO.toString();
            }else{*/
                huoqutoken = dcpLoginUtil.getToken(names[1],
                        passwords[1]);
           // }
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Connection", "Kepp-Alive");
            connection.setRequestProperty("X-Access-Token", huoqutoken);
            connection.connect();
            // 通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r");
                }
                result = sbf.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 断开与远程地址url的连接
            connection.disconnect();
        }
        return result;
    }

    /**
     * TODO
     * 发送POST请求

     * @return
     */
    public  String getDocking (String urls, JSONObject map,  Map<String,Object> infoMap){
        OutputStreamWriter out=null;
        InputStream is=null;
        BufferedReader br=null;
        try {
            String name=(String)infoMap.get("cd_username");
            String password= (String)infoMap.get("cd_pwd");
            String[] names=name.replace("，",",").split(",");
            String[] passwords=password.replace("，",",").split(",");

            String huoqutoken=null;
                huoqutoken = dcpLoginUtil.getToken(names[1],passwords[1]);
            URL url = new URL(urls);
            URLConnection connection = url.openConnection();
            HttpsURLConnection conn = (HttpsURLConnection) connection;
            //请求方式
            conn.setRequestMethod("POST");
            //设置通用的请求属性
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setDefaultUseCaches(false);
            conn.setConnectTimeout(30000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "Kepp-Alive");
            conn.setRequestProperty("X-Access-Token", huoqutoken);
            //断开连接
            conn.connect();
            //获取URLconnection对象对应的输出流
            out = new OutputStreamWriter(conn.getOutputStream(),"utf-8");
            //发送请求参数
            out.write(map.toString());
            //缓冲数据
            out.flush();
            //获取对应的输入流
            is = conn.getInputStream();
            //字符流缓存
            br = new BufferedReader(new InputStreamReader(is));
            String str = null;
            String huoqu = null;
            while ((str=br.readLine())!=null) {
                huoqu = str + "\t";
            }
            //System.out.println(huoqu);
            return huoqu;
        } catch (Exception e) {
//			//System.out.println("请求数据失败");
            e.printStackTrace();
        } finally{
            try {
                br.close();
                is.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 使用SOAP1.1发送消息
     *
     * @param url
     * @param xml
     * @return
     */
    public static String doPostSoap1_1(String url, String xml){
        String retStr = null;
        // 创建HttpClientBuilder
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        // HttpClient
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setHeader("Content-Type",  "text/xml;charset=UTF-8");
            StringEntity data = new StringEntity(xml, Charset.forName("UTF-8"));
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
            httpPost.setEntity(data);
//            //System.out.println("httpPost:"+httpPost);
            CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                // 打印响应内容
                retStr = EntityUtils.toString(httpEntity, "UTF-8");
            }
            // 释放资源
            closeableHttpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retStr;
    }
    /**
     * 获取上月月份
     */
    public static String getYearMonthss(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 设置为当前时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
        date = calendar.getTime();

        return dateFormat.format(date);
    }

    /**
     * 获取指定月份的最后一天
     * @param year
     * @param month
     * @return
     */
    public static String getLastDayOfMonth(int year,int month){
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR,year);
        //设置月份
        cal.set(Calendar.MONTH, month-1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
        return lastDayOfMonth;
    }

    /**
     * 获取本年
     * @return
     */
    public static String getMonthLastDayyyyy(int sj) {
        Date date =  new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, sj);
        int day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat sim = new SimpleDateFormat("yyyy");
        date = cal.getTime();
        return sim.format(date);
    }
    /**
     * 获取本月
     * @return
     */
    public static String getMonthLastDaymm(int sj) {
        Date date =  new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, sj);
        int day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat sim = new SimpleDateFormat("MM");
        date = cal.getTime();
        return sim.format(date);
    }
    /**
     * 获取本日
     * @return
     */
    public static String getMonthLastDaydd() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");//可以方便地修改日期格式
        String hehe = dateFormat.format( now );
        return hehe;
    }


}
