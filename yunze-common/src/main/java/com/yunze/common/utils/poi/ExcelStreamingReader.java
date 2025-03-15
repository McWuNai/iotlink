package com.yunze.common.utils.poi;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelStreamingReader {

    @Data
    public static class SimCardData {
        private String iccid;
        private String addDate;
        private String msisdn;
        private String imsi;
        private String cycleUsage;
        private String simStatus;
        private String activationTime;
        private String imei;
        private String deviceAccount;
    }

    public PageResult readExcel(String filePath, int pageNumber, int pageSize, boolean quickMode) {
        try {
            BaseReadListener readListener;
            if (quickMode) {
                readListener = new QuickModeReadListener(pageNumber, pageSize);
                log.info("使用快速返回模式读取Excel: {}", filePath);
            } else {
                readListener = new NormalModeReadListener(pageNumber, pageSize);
                log.info("使用普通模式读取Excel: {}", filePath);
            }

            try {
                // 修改：读取所有sheet
                EasyExcel.read(filePath, SimCardData.class, readListener).doReadAll();
            } catch (RuntimeException e) {
                if (!"PageComplete".equals(e.getMessage())) {
                    throw e;
                }
            }
            return new PageResult(readListener.getPageData(), pageNumber, readListener.getTotalCount());
        } catch (Exception e) {
            log.error("读取Excel文件失败: {}", filePath, e);
            throw new RuntimeException("读取Excel文件失败", e);
        }
    }

    @Slf4j
    private abstract static class BaseReadListener extends AnalysisEventListener<SimCardData> {
        @SuppressWarnings("unused")
        protected final int pageNumber;
        protected final int pageSize;
        protected final int startRow;
        protected final List<List<String>> pageData = new ArrayList<>();
        protected long totalCount = 0;
        protected long currentRow = 0;
        protected List<String> headers;

        public BaseReadListener(int pageNumber, int pageSize) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.startRow = (pageNumber - 1) * pageSize;
        }

        protected List<String> convertToRowData(SimCardData data) {
            List<String> row = new ArrayList<>();
            row.add(formatValue(data.getIccid()));
            row.add(formatValue(data.getAddDate()));
            row.add(formatValue(data.getMsisdn()));
            row.add(formatValue(data.getImsi()));
            row.add(formatValue(data.getCycleUsage()));
            row.add(formatValue(data.getSimStatus()));
            row.add(formatValue(data.getActivationTime()));
            row.add(formatValue(data.getImei()));
            row.add(formatValue(data.getDeviceAccount()));
            return row;
        }

        public List<List<String>> getPageData() {
            List<List<String>> result = new ArrayList<>();
            if (headers != null) {
                result.add(headers);
            }
            result.addAll(pageData);
            return result;
        }

        public long getTotalCount() {
            return totalCount;
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            headers = new ArrayList<>(headMap.values());
            log.info("读取到表头: {}", String.join(", ", headers));
        }

        protected String formatValue(String value) {
            return value == null || value.trim().isEmpty() ? "--" : value.trim();
        }
    }

    @Slf4j
    private static class QuickModeReadListener extends BaseReadListener {
        private boolean hasReachedTargetRow = false;

        public QuickModeReadListener(int pageNumber, int pageSize) {
            super(pageNumber, pageSize);
        }

        @Override
        public void invoke(SimCardData data, AnalysisContext context) {
            currentRow++;
            totalCount++;

            if (currentRow > startRow) {
                hasReachedTargetRow = true;
            }

            if (hasReachedTargetRow) {
                List<String> rowData = convertToRowData(data);
                pageData.add(rowData);
                log.info("快速模式-读取第 {} 行数据: {}", currentRow, String.join(", ", rowData));

                if (pageData.size() >= pageSize) {
                    log.info("快速模式-已收集足够数据，停止读取");
                    throw new RuntimeException("PageComplete");
                }
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            log.info("快速模式-Excel解析完成, 总行数: {}, 收集数据: {}", totalCount, pageData.size());
        }
    }

    @Slf4j
    private static class NormalModeReadListener extends BaseReadListener {
        private final List<List<String>> allData = new ArrayList<>();
        private int currentSheet = 0;

        public NormalModeReadListener(int pageNumber, int pageSize) {
            super(pageNumber, pageSize);
        }

        @Override
        public void invoke(SimCardData data, AnalysisContext context) {
            try {
                if (data == null) {
                    // log.debug("跳过空数据行");
                    return;
                }

                List<String> rowData = convertToRowData(data);
                // 检查是否为空行
                if (isEmptyRow(rowData)) {
                    // log.debug("跳过空行数据");
                    return;
                }

                currentRow++;
                allData.add(rowData);
                totalCount = allData.size();

                // if (currentRow % 10000 == 0) {
                // log.info("普通模式-读取第{}个表格第{}行数据: {}",
                // currentSheet + 1, currentRow, String.join(", ", rowData));
                // }
            } catch (Exception e) {
                log.error("处理行数据时出错: sheet={}, row={}, error={}",
                        currentSheet + 1, currentRow, e.getMessage());
            }
        }

        private boolean isEmptyRow(List<String> rowData) {
            if (rowData == null || rowData.isEmpty()) {
                return true;
            }
            return rowData.stream().allMatch(cell -> cell == null || cell.trim().isEmpty());
        }

        @Override
        protected List<String> convertToRowData(SimCardData data) {
            if (data == null) {
                return new ArrayList<>();
            }

            List<String> row = new ArrayList<>();
            try {
                row.add(formatValue(data.getIccid()));
                row.add(formatValue(data.getAddDate()));
                row.add(formatValue(data.getMsisdn()));
                row.add(formatValue(data.getImsi()));
                row.add(formatValue(data.getCycleUsage()));
                row.add(formatValue(data.getSimStatus()));
                row.add(formatValue(data.getActivationTime()));
                row.add(formatValue(data.getImei()));
                row.add(formatValue(data.getDeviceAccount()));
            } catch (Exception e) {
                log.error("转换行数据时出错: {}", e.getMessage());
            }
            return row;
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // log.info("普通模式-表格{}解析完成, 当前总行数: {}, 有效数据行数: {}",
            // currentSheet + 1, currentRow, allData.size());
            currentSheet++;
        }

        @Override
        public List<List<String>> getPageData() {
            try {
                List<List<String>> result = new ArrayList<>();

                // 确保表头不为空
                if (headers == null || headers.isEmpty()) {
                    // log.warn("表头为空，使用默认表头");
                    headers = getDefaultHeaders();
                }
                result.add(new ArrayList<>(headers));

                // 处理分页
                if (!allData.isEmpty()) {
                    int fromIndex = Math.min(startRow, allData.size());
                    int toIndex = Math.min(fromIndex + pageSize, allData.size());

                    if (fromIndex < toIndex) {
                        result.addAll(allData.subList(fromIndex, toIndex));
                    }
                }

                // 确保至少返回表头
                if (result.size() <= 1) {
                    // log.warn("没有数据行，仅返回表头");
                }

                // log.info("返回数据: 总表格数={}, 总行数={}, 当前页数据行数={}, 页码={}, 每页大小={}",
                // currentSheet,
                // allData.size(),
                // result.size() - 1,
                // pageNumber,
                // pageSize);

                return result;
            } catch (Exception e) {
                log.error("获取页面数据时出错: {}", e.getMessage());
                // 返回至少包含表头的结果
                List<List<String>> fallback = new ArrayList<>();
                fallback.add(getDefaultHeaders());
                return fallback;
            }
        }

        private List<String> getDefaultHeaders() {
            List<String> defaultHeaders = new ArrayList<>();
            defaultHeaders.add("ICCID");
            defaultHeaders.add("添加日期");
            defaultHeaders.add("MSISDN");
            defaultHeaders.add("IMSI");
            defaultHeaders.add("周期累计用量(MB)");
            defaultHeaders.add("SIM卡状态");
            defaultHeaders.add("激活时间");
            defaultHeaders.add("IMEI");
            defaultHeaders.add("设备账户");
            return defaultHeaders;
        }

        @Override
        public long getTotalCount() {
            return Math.max(0, allData.size());
        }
    }
}