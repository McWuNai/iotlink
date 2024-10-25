package com.yunze.task.yunze.sys;

import com.yunze.common.mapper.yunze.YzOrderMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 系统订单
 * @Auther: zhang feng
 * @Date: 2022/06/15/10:30
 * @Description:
 */
@Component
public class SysOrder {

    @Resource
    private YzOrderMapper yzOrderMapper;


    /**
     * 修改 info LIKE '未找到加%' and add_package = '1'  订单 (未找到加包数据订单修改状态)【可能是未给客户划分资费】【激活生效未获取到激活时间】等！
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_UpdOrderStatus_queue")
    public void UpdOrderStatus( String msg) {
        yzOrderMapper.resetAddPackage(null);


    }

}
