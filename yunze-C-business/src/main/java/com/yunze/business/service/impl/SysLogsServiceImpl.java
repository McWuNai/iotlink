package com.yunze.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunze.business.dao.SysLogsMapper;
import com.yunze.business.entity.SysLogs;
import com.yunze.business.service.SysLogsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 系统级日志 服务实现类
 * </p>
 *
 * @author wct
 * @since 2021-12-21
 */
@Service
public class SysLogsServiceImpl extends ServiceImpl<SysLogsMapper, SysLogs> implements SysLogsService {

    @Resource
    private SysLogsMapper sysLogsMapper;




}
