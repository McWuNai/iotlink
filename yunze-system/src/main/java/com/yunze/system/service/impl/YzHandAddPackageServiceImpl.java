package com.yunze.system.service.impl;

import com.yunze.common.core.domain.AjaxResult;

import com.yunze.common.mapper.yunze.YzCardFlowMapper;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzCardPacketMapper;
import com.yunze.common.mapper.yunze.YzOrderMapper;
import com.yunze.common.utils.StringUtils;
import com.yunze.common.utils.yunze.ExcelConfig;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.common.utils.yunze.VeDate;
import com.yunze.system.service.YzHandAddPackageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class YzHandAddPackageServiceImpl implements YzHandAddPackageService {
    @Resource
    private YzOrderMapper yzOrderMapper;
    @Resource
    private YzCardFlowMapper yzCardFlowMapper;
    @Resource
    private YzCardPacketMapper packetMapper;
    @Resource
    private YzCardMapper cardMapper;

    @Override
    public AjaxResult handAddPackage(Map<String, Object> parammap) {
        //根据不同的生效类型生成不同的Manage类  1、currentMonth||2、nextMonth||3、periodicExtension||4、activateEffect
        try {
            //当生效类型为1的时候是 当月生效
            if (parammap.get("validateType").toString().equals("1")) {
                this.currentMonth(parammap);
            } else if (parammap.get("validateType").toString().equals("2")) {
                this.nextMonth(parammap);
            } else if (parammap.get("validateType").toString().equals("3")) {
                this.periodicExtension(parammap);
            } else if (parammap.get("validateType").toString().equals("4")) {
                this.activateEffect(parammap);
            }
        } catch (Exception e) {
            return AjaxResult.error("");
        }
        return AjaxResult.success();
    }


    private AjaxResult activateEffect(Map<String, Object> parammap) {
        try {
            //根据startNo 和 endNo 去查询订单当中加包或未加包的
            List<Map<String, Object>> notAddCard = yzOrderMapper.activateEffect(parammap);
            if (notAddCard.size() == 0) {
                return AjaxResult.error("该卡无此类型生效订单 ！");
            }
            for (Map<String, Object> resMap : notAddCard) {
                //取出来加包未加包的订单，去过滤掉当月已完成定购的
                Integer aPackage = yzCardFlowMapper.filterIsAddPackage(resMap.get("iccid").toString());
                //如果未订购，走加包流程
                if (aPackage == 0) {
                    Map<String, Object> packageInfo = packetMapper.selPackageInfo(resMap);
                    if (packageInfo.get("packet_valid_name").toString().equals("月")) {
                        int month = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                        //获取到期日期
                        LocalDate localDate = this.setMonthEndTime(resMap.get("activateDate").toString(), month);
                        //修改 卡信息到期日期
                        Map<String, Object> cardMap = new HashMap<>();
                        cardMap.put("endTime", localDate + " 23:59:59");
                        cardMap.put("iccid", resMap.get("iccid"));
                        cardMap.put("remaining", packageInfo.get("error_flow"));
                        cardMapper.updateEndTime(cardMap);
                        resMap.put("endTime", localDate + " 23:59:59");
                        this.insertFlow(packageInfo, resMap);
                        yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                    } else if (packageInfo.get("packet_valid_name").toString().equals("年")) {
                        int year = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                        LocalDate localDate = this.setYearEndTime(resMap.get("activateDate").toString(), year);
                        //修改 卡信息到期日期
                        Map<String, Object> setCardMap = new HashMap<>();
                        setCardMap.put("endTime", localDate + " 23:59:59");
                        setCardMap.put("iccid", resMap.get("iccid"));
                        setCardMap.put("remaining", packageInfo.get("error_flow"));
                        cardMapper.updateEndTime(setCardMap);
                        resMap.put("endTime", localDate + " 23:59:59");
                        this.insertFlow(packageInfo, resMap);
                        yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                    }
                }
            }
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.success();
    }

    private AjaxResult nextMonth(Map<String, Object> parammap) {
        try {
            //根据startNo 和 endNo 去查询订单当中加包或未加包的
            List<Map<String, Object>> notAddCard = yzOrderMapper.nextMonth(parammap);
            if (notAddCard.size() == 0) {
                return AjaxResult.error("该卡无此类型生效订单 ！");
            }
            for (Map<String, Object> resMap : notAddCard) {
                //取出来加包未加包的订单，去过滤掉当月已完成定购的
                Integer aPackage = yzCardFlowMapper.filterIsAddPackage(resMap.get("iccid").toString());
                //如果未订购，走加包流程
                if (aPackage == 0) {
                    Map<String, Object> packageInfo = packetMapper.selPackageInfo(resMap);
                    if (packageInfo.get("packet_valid_name").toString().equals("月")) {
                        int month = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                        //获取到期日期
                        LocalDate localDate = this.setNextMonthEndTime(resMap.get("createTime").toString(), month);
                        //修改 卡信息到期日期
                        Map<String, Object> cardMap = new HashMap<>();
                        cardMap.put("endTime", localDate + " 23:59:59");
                        cardMap.put("iccid", resMap.get("iccid"));
                        cardMap.put("remaining", packageInfo.get("error_flow"));
                        cardMapper.updateEndTime(cardMap);
                        resMap.put("endTime", localDate + " 23:59:59");
                        this.insertFlow(packageInfo, resMap);
                        yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                    } else if (packageInfo.get("packet_valid_name").toString().equals("年")) {
                        int year = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                        LocalDate localDate = this.setYearNextMonthEndTime(resMap.get("createTime").toString(), year);
                        //修改 卡信息到期日期
                        Map<String, Object> setCardMap = new HashMap<>();
                        setCardMap.put("endTime", localDate + " 23:59:59");
                        setCardMap.put("iccid", resMap.get("iccid"));
                        setCardMap.put("remaining", packageInfo.get("error_flow"));
                        cardMapper.updateEndTime(setCardMap);
                        resMap.put("endTime", localDate + " 23:59:59");
                        this.insertFlow(packageInfo, resMap);
                        yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                    }
                }
            }
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.success();
    }

    private AjaxResult periodicExtension(Map<String, Object> parammap) {
        try {
            //根据startNo 和 endNo 去查询订单当中加包或未加包的
            List<Map<String, Object>> notAddCard = yzOrderMapper.periodicExtension(parammap);
            if (notAddCard.size() == 0) {
                return AjaxResult.error("该卡无此类型生效订单 ！");
            }
            for (Map<String, Object> resMap : notAddCard) {
                if (StringUtils.isNotNull(resMap.get("endTime"))) {
                    Map<String, Object> packageInfo = packetMapper.selPackageInfo(resMap);
                    if (packageInfo.get("packet_valid_name").toString().equals("月")) {
                        int month = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                        //获取到期日期
                        LocalDate localDate = this.setOnlyMonthEndTime(resMap.get("endTime").toString(), month);
                        //修改 卡信息到期日期
                        Map<String, Object> cardMap = new HashMap<>();
                        cardMap.put("endTime", localDate + " 23:59:59");
                        cardMap.put("iccid", resMap.get("iccid"));
                        cardMap.put("remaining", packageInfo.get("error_flow"));
                        cardMapper.updateEndTime(cardMap);
                        resMap.put("endTime", localDate + " 23:59:59");
                        this.insertFlow(packageInfo, resMap);
                        yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                    } else if (packageInfo.get("packet_valid_name").toString().equals("年")) {
                        int year = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                        LocalDate localDate = this.setOnlyYearEndTime(resMap.get("endTime").toString(), year);
                        //修改 卡信息到期日期
                        Map<String, Object> setCardMap = new HashMap<>();
                        setCardMap.put("endTime", localDate + " 23:59:59");
                        setCardMap.put("iccid", resMap.get("iccid"));
                        setCardMap.put("remaining", packageInfo.get("error_flow"));
                        cardMapper.updateEndTime(setCardMap);
                        resMap.put("endTime", localDate + " 23:59:59");
                        this.insertFlow(packageInfo, resMap);
                        yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                    }
                }
            }
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.success();
    }

    private AjaxResult currentMonth(Map<String, Object> parammap) {
        try {
            //根据startNo 和 endNo 去查询订单当中加包或未加包的
            List<Map<String, Object>> notAddCard = yzOrderMapper.filterNotAddCard(parammap);
            if (notAddCard.size() == 0) {
                return AjaxResult.error("该卡无此类型生效订单 ！");
            }
                for (Map<String, Object> resMap : notAddCard) {
                    //取出来加包未加包的订单，去过滤掉当月已完成定购的
                    Integer aPackage = yzCardFlowMapper.filterIsAddPackage(resMap.get("iccid").toString());
                    //如果未订购，走加包流程
                    if (aPackage == 0) {
                        Map<String, Object> packageInfo = packetMapper.selPackageInfo(resMap);
                        if (packageInfo.get("packet_valid_name").toString().equals("月")) {
                            int month = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                            //获取到期日期
                            LocalDate localDate = this.setMonthEndTime(resMap.get("createTime").toString(), month);
                            //修改 卡信息到期日期
                            Map<String, Object> cardMap = new HashMap<>();
                            cardMap.put("endTime", localDate + " 23:59:59");
                            cardMap.put("iccid", resMap.get("iccid"));
                            cardMap.put("remaining", packageInfo.get("error_flow"));
                            cardMapper.updateEndTime(cardMap);
                            resMap.put("endTime", localDate + " 23:59:59");
                            this.insertFlow(packageInfo, resMap);
                            yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                        } else if (packageInfo.get("packet_valid_name").toString().equals("年")) {
                            int year = Integer.parseInt(packageInfo.get("packet_valid_time").toString());
                            LocalDate localDate = this.setYearEndTime(resMap.get("createTime").toString(), year);
                            //修改 卡信息到期日期
                            Map<String, Object> setCardMap = new HashMap<>();
                            setCardMap.put("endTime", localDate + " 23:59:59");
                            setCardMap.put("iccid", resMap.get("iccid"));
                            setCardMap.put("remaining", packageInfo.get("error_flow"));
                            cardMapper.updateEndTime(setCardMap);
                            resMap.put("endTime", localDate + " 23:59:59");
                            this.insertFlow(packageInfo, resMap);
                            yzOrderMapper.updateAddPackage(1, resMap.get("ord_no").toString());
                        }
                    }
                }
        } catch (Exception e) {
            return AjaxResult.success(e.getMessage().toString());
        }
        return AjaxResult.success();
    }

    private void insertFlow(Map<String, Object> packageInfo, Map<String, Object> parammap) {
        Map<String, Object> insertMap = new HashMap<>();
        insertMap.put("package_id", packageInfo.get("package_id").toString());
        insertMap.put("packet_id", packageInfo.get("packet_id"));
        insertMap.put("ord_no", parammap.get("ord_no"));
        insertMap.put("true_flow", packageInfo.get("packet_flow"));
        insertMap.put("error_flow", packageInfo.get("error_flow"));
        insertMap.put("start_time", VeDate.getStringDate());
        insertMap.put("end_time", parammap.get("endTime"));
        insertMap.put("ord_type", "1");
        insertMap.put("status", "1");
        insertMap.put("packet_type", "0");
        insertMap.put("use_flow", 0);
        insertMap.put("iccid", parammap.get("iccid").toString());
        insertMap.put("error_time", packageInfo.get("error_so"));
        insertMap.put("validate_type", "0");
        insertMap.put("validate_time", "");
        yzCardFlowMapper.insertFlow(insertMap);
    }

    private LocalDate setYearEndTime(String addTime, int years) {
        String replace = addTime.replace("-", "");
        Integer year = Integer.valueOf(replace.substring(0, 4).toString());
        Integer month = Integer.valueOf(replace.substring(4, 6).toString());
        Integer day = Integer.valueOf(replace.substring(6, 8).toString());
        // 设置基准日期
        LocalDate baseDate = LocalDate.of(year, month, day);
        // 增加X个月
        LocalDate localDate = baseDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate expiryDate = localDate.plusYears(years);
        return expiryDate;
    }

    private LocalDate setMonthEndTime(String addTime, int monthTime) {
        String replace = addTime.replace("-", "");
        Integer year = Integer.valueOf(replace.substring(0, 4).toString());
        Integer month = Integer.valueOf(replace.substring(4, 6).toString());
        Integer day = Integer.valueOf(replace.substring(6, 8).toString());
        // 设置基准日期
        LocalDate baseDate = LocalDate.of(year, month, day);
        // 增加X个月
        LocalDate localDate = baseDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate expiryDate = localDate.plusMonths(monthTime);
        return expiryDate;
    }

    private LocalDate setOnlyYearEndTime(String endTime, int years) {
        String replace = endTime.replace("-", "");
        Integer year = Integer.valueOf(replace.substring(0, 4).toString());
        Integer month = Integer.valueOf(replace.substring(4, 6).toString());
        Integer day = Integer.valueOf(replace.substring(6, 8).toString());
        // 设置基准日期
        LocalDate baseDate = LocalDate.of(year, month, day);
        // 增加X个月
//        LocalDate localDate = baseDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate expiryDate = baseDate.plusYears(years);
        return expiryDate;
    }

    private LocalDate setOnlyMonthEndTime(String endTime, int monthTime) {
        String replace = endTime.replace("-", "");
        Integer year = Integer.valueOf(replace.substring(0, 4).toString());
        Integer month = Integer.valueOf(replace.substring(4, 6).toString());
        Integer day = Integer.valueOf(replace.substring(6, 8).toString());
        // 设置基准日期
        LocalDate baseDate = LocalDate.of(year, month, day);
        // 增加X个月
//        LocalDate localDate = baseDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate expiryDate = baseDate.plusMonths(monthTime);
        return expiryDate;
    }

    private LocalDate setYearNextMonthEndTime(String createTime, int years) {
        String replace = createTime.replace("-", "");
        Integer year = Integer.valueOf(replace.substring(0, 4).toString());
        Integer month = Integer.valueOf(replace.substring(4, 6).toString());
        Integer day = Integer.valueOf(replace.substring(6, 8).toString());
        // 设置基准日期
        LocalDate baseDate = LocalDate.of(year, month, day);
        LocalDate firstDayOfNextMonth = baseDate.withDayOfMonth(1).plusMonths(1);
        LocalDate localDate = firstDayOfNextMonth.plusYears(year);
        LocalDate localDateYear = localDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return localDateYear;
    }

    private LocalDate setNextMonthEndTime(String createTime, int monthTime) {
        String replace = createTime.replace("-", "");
        Integer year = Integer.valueOf(replace.substring(0, 4).toString());
        Integer month = Integer.valueOf(replace.substring(4, 6).toString());
        Integer day = Integer.valueOf(replace.substring(6, 8).toString());
        // 设置基准日期
        LocalDate baseDate = LocalDate.of(year, month, day);
        LocalDate firstDayOfNextMonth = baseDate.withDayOfMonth(1).plusMonths(1);
        LocalDate localDate = firstDayOfNextMonth.plusMonths(month);
        LocalDate expiryDate = localDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return expiryDate;
    }

    @Override
    public AjaxResult addPackage(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String ReadName = UUID.randomUUID().toString().replace("-", "") + filename;
        String flieUrlRx = "/mnt/file/";
        ReadName = flieUrlRx + ReadName;
        try {
            File file2 = new File("");
            String filePath = file2.getCanonicalPath();
            File newFile = new File(filePath + ReadName);
            File Url = new File(filePath + flieUrlRx + "1.txt");//tomcat 生成路径
            Upload.mkdirsmy(Url);
            file.transferTo(newFile);
            String path = filePath + ReadName;
            ExcelConfig excelConfig = new ExcelConfig();
            String columns[] = {"iccid", "validateType"};
            List<Map<String, String>> list = excelConfig.getExcelListMap(path, columns, 0L);
            System.err.println(list);
            if (list.size() > 10000) {
                return AjaxResult.error("加包卡总数大于10000张,请调整后重试 ！");
            }
            if (list.size() > 0) {
                for (Map<String, String> resMap : list) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("startNo", resMap.get("iccid").toString());
                    map.put("endNo", resMap.get("iccid").toString());
                    //根据Excel中的validateType去调用接口
                    if (resMap.get("validateType").toString().equals("1")) {
                        this.currentMonth(map);
                    } else if (resMap.get("validateType").toString().equals("2")) {
                        this.nextMonth(map);
                    } else if (resMap.get("validateType").toString().equals("3")) {
                        this.periodicExtension(map);
                    } else if (resMap.get("validateType").toString().equals("4")) {
                        this.activateEffect(map);
                    }
                }
            }
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
        return AjaxResult.success();
    }

    public static void main(String[] args) {
        String replace = "2023-08-01".replace("-", "");
        Integer year = Integer.valueOf(replace.substring(0, 4).toString());
        Integer month = Integer.valueOf(replace.substring(4, 6).toString());
        Integer day = Integer.valueOf(replace.substring(6, 8).toString());
        // 设置基准日期
        LocalDate baseDate = LocalDate.of(year, month, day);
        // 增加X个月
        LocalDate localDate = baseDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate expiryDate = localDate.plusMonths(6);
        String s = expiryDate + " 23:59:59";
        System.err.println(s);
    }
}