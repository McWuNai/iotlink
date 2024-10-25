package com.yunze.task.yunze.sys;

import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.mapper.yunze.YzSysLogsMapper;
import com.yunze.common.mapper.yunze.commodity.YzWxByProductAgentMapper;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorGroupMapper;
import com.yunze.common.mapper.yunze.polling.YzCardPollingErrorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志清理
 */
@Slf4j
@Component
public class SysLogClearMQ {

    @Resource
    private YzSysLogsMapper yzSysLogsMapper;
    @Resource
    private YzCardPollingErrorMapper yzCardPollingErrorMapper;
    @Resource
    private YzCardPollingErrorGroupMapper yzCardPollingErrorGroupMapper;
    @Resource
    private YzWxByProductAgentMapper yzWxByProductAgentMapper;

    /**
     * 删除日志
     * */
    @RabbitHandler
    @RabbitListener(queues = "admin_LogClear_queue")
    public void updStatusShowId(String msg) {
        Map<String,Object> findConfigMap = new HashMap<>();
        Map<String,Object> delMap = new HashMap<>();

        findConfigMap.put("config_key","logMaxDay");//日志信息保留最大天数
        String logMaxDay = yzWxByProductAgentMapper.findConfig(findConfigMap);
        logMaxDay = logMaxDay!=null?logMaxDay:"40";
        String delTime = VeDate.getNextDay(VeDate.getStringDateShort(),"-"+logMaxDay);
        delMap.put("delTime",delTime);
        int sysLogsCount = yzSysLogsMapper.del(delMap);
        int jobLogsCount = yzSysLogsMapper.delJobLog(delMap);
        int operLogsCount = yzSysLogsMapper.delOperLog(delMap);
        int errLogsCount = yzCardPollingErrorMapper.delTime(delMap);
        int errtableLogsCount = yzCardPollingErrorGroupMapper.delTableData(null);

        log.info(">>> 日志清理 yz_sys_logs {}, sys_job_log {}, sys_oper_log {},yz_card_polling_error {},yz_card_polling_error_group {} <<<",sysLogsCount,jobLogsCount,operLogsCount,errLogsCount,errtableLogsCount);

    }

}
