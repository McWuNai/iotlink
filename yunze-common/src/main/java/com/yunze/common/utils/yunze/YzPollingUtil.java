package com.yunze.common.utils.yunze;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 轮询 公共代码部分
 * @Auther: zhang feng
 * @Date: 2021/11/24/16:51
 * @Description:
 */
@Component
public class YzPollingUtil {


    /**
     * 过滤不需要循环的 通道 id
     * @param Ex_ChannelArr
     * @param ChannelArr
     * @param bool
     * @return
     */
    public List<Map<String, Object>> filterNoPollin(List<String> Ex_ChannelArr, List<Map<String, Object>> ChannelArr, boolean bool){
        if(bool){
            List<Map<String, Object>> Run_ChannelArr = new ArrayList<>();
            //过滤 没有划卡的通道不需要监听
            for (int i = 0; i < ChannelArr.size(); i++) {
                Map<String,Object> ChannelObj = ChannelArr.get(i);
                String cd_id = ChannelObj.get("cd_id").toString();
                for (int j = 0; j < Ex_ChannelArr.size(); j++) {
                    if(cd_id.equals(Ex_ChannelArr.get(j))){
                        Run_ChannelArr.add(ChannelObj);
                    }
                }
            }
            ChannelArr = Run_ChannelArr;//刷新赋值
        }
        return ChannelArr;
    }






}
