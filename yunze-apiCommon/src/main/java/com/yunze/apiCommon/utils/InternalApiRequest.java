package com.yunze.apiCommon.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 内部请求
 *
 * @Auther: zhang feng
 * @Date: 2021/11/30/11:10
 * @Description:
 */
@Component
public class InternalApiRequest {

    public static String Server_IP = "http://127.0.0.1:9080/route/Api";

    @Resource
    private ApiUtil_NoStatic apiUtil_NoStatic;

    @Resource
    private GetApiParam getApiParam;


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryFlow(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {

                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryFlow", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryFlow" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryFlow(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }

    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryHistoryFlow(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            Rmap = apiUtil_NoStatic.queryHistoryFlow(map, fmap);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }

    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryFlowHis(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {

                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryFlowHis", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryFlowHis" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryFlowHis(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryCardStatus(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {

                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryCardStatus", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryCardStatus" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryCardStatus(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> changeCardStatus(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {

                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/changeCardStatus", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/changeCardStatus" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.changeCardStatus(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> FunctionApnStatus(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {

                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/FunctionApnStatus", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/FunctionApnStatus" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.FunctionApnStatus(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> SpeedLimit(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {

                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/SpeedLimit", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/SpeedLimit" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.SpeedLimit(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryOnlineStatus(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryOnlineStatus", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryOnlineStatus" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryOnlineStatus(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryCardActiveTime(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryCardActiveTime", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryCardActiveTime" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryCardActiveTime(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> unbundling(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/unbundling", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/unbundling" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.unbundling(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryCardImei(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryCardImei", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryCardImei" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryCardImei(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> realNameRemove(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/realNameRemove", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/realNameRemove" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.realNameRemove(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> simStopReason(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/simStopReason", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/simStopReason" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.simStopReason(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> onOffStatus(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/onOffStatus", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/onOffStatus" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.onOffStatus(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> apnInfo(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/apnInfo", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/apnInfo" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.apnInfo(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> cardBindStatus(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/cardBindStatus", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/cardBindStatus" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.cardBindStatus(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> simChangeHistory(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/simChangeHistory", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/simChangeHistory" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.simChangeHistory(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> changeCardStatusFlexible(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/changeCardStatusFlexible", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/changeCardStatusFlexible" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.changeCardStatusFlexible(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryOffering(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryOffering", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryOffering" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryOffering(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryCardInfo(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryCardInfo", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryCardInfo" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryCardInfo(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryGroupInfo(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryGroupInfo", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryGroupInfo" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryGroupInfo(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> queryGroupMember(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/queryGroupMember", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/queryGroupMember" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.queryGroupMember(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 内部请求
     *
     * @param map
     * @param fmap
     * @return
     */
    public Map<String, Object> InternetDisconnection(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            if (IsInternal(fmap)) {
                try {
                    Map<String, Object> apiMapNet = getApiParam.getApiParams(map);
                    String Pardata = AesEncryptUtil.encrypt(JSON.toJSONString(apiMapNet));
                    String Rstr = HttpClientUtil.doPostStr(Server_IP + "/InternetDisconnection", Pardata);// 返回结果字符串
                    Rmap = JSON.parseObject(Rstr);
                    if (Rmap.get("code").equals("200")) {
                        Rmap = JSON.parseObject(AesEncryptUtil.desEncrypt(Rmap.get("Data").toString()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("/InternetDisconnection" + " map " + map + " fmap " + fmap + " Rmap " + Rmap);
                }
            } else {
                Rmap = apiUtil_NoStatic.InternetDisconnection(map, fmap);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }


    /**
     * 是否需要走内部API 【特定情况如ip地址白名单等 API 与 部署服务不在同一服务器】
     *
     * @param fmap
     * @return
     */
    public boolean IsInternal(Map<String, Object> fmap) {
        boolean bool = false;
        String cd_control_type = fmap.get("cd_control_type") != null && fmap.get("cd_control_type").toString().length() > 0 ? fmap.get("cd_control_type").toString() : "0";
        bool = cd_control_type.equals("1") ? true : false;
        return bool;
    }


    public Map<String, Object> autocompleteCard(Map<String, Object> map, Map<String, Object> fmap) {
        Map<String, Object> Rmap = new HashMap<>();
        try {
            Rmap = apiUtil_NoStatic.autocompleteCard(map, fmap);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Rmap;
    }
}

