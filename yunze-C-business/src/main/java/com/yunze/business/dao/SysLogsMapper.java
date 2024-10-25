package com.yunze.business.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunze.business.entity.SysLogs;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统级日志 Mapper 接口
 */
@Mapper
public interface SysLogsMapper extends BaseMapper<SysLogs> {

}
