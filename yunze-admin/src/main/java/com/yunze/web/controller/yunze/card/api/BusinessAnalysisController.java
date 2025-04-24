package com.yunze.web.controller.yunze.card.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.yunze.common.core.domain.AjaxResult;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务分析 控制器
 */
@RestController
@RequestMapping("/api/business/analysis")
public class BusinessAnalysisController extends MyBaseController {
    private final String projectRoot = new File("").getCanonicalPath();
    private final String reportPath = "/mnt/file/reports";

    public BusinessAnalysisController() throws IOException {
    }

    /**
     * 获取经营分析数据
     */
    @GetMapping("/data")
    public AjaxResult getAnalysisData() {
        // 实际项目中，这里需要查询数据库获取经营分析数据
        // 这里仅返回示例数据
        return AjaxResult.success();
    }

    /**
     * 导出经营分析报表
     */
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportAnalysisReport() {
        // 实际项目中，这里需要生成经营分析报表并导出
        // 这里仅返回示例
        return null;
    }

    /**
     * 获取运营周期报告文件列表
     */
    @GetMapping("/reports")
    public AjaxResult getReportFiles() {
        try {
            if (ensureDirectoryWithPlaceholder()) {
                return AjaxResult.error("报告目录不存在");
            }
            File reportsDir = new File(projectRoot+reportPath);
            // 获取目录中的DOCX文件
            List<String> fileList = Arrays.stream(Objects.requireNonNull(reportsDir.listFiles()))
                    .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".docx"))
                    .map(File::getName)
                    .collect(Collectors.toList());

            return AjaxResult.success(fileList);
        } catch (Exception e) {
            logger.error("获取报告文件列表失败", e);
            return AjaxResult.error("获取报告文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 下载报告文件
     */
    @GetMapping("/reports/download")
    public ResponseEntity<InputStreamResource> downloadReportFile(@RequestParam String fileName) {
        try {
            // 构建文件路径
            File file = new File(projectRoot+reportPath, fileName);

            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            // 返回文件流
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(file.toPath()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (IOException e) {
            logger.error("下载报告文件失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean ensureDirectoryWithPlaceholder() {
        try {
            File placeholderFile = new File(projectRoot + "/mnt/file/reports/" + "1.txt");
            File parentDir = placeholderFile.getParentFile();

            // 如果父目录不存在，先创建父目录
            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    System.out.println("无法创建目录: " + "/mnt/file/reports");
                    return true; // 返回true表示创建失败
                }
            }

            // 创建或更新1.txt文件
            if (!placeholderFile.exists()) {
                try (FileOutputStream fos = new FileOutputStream(placeholderFile)) {
                    fos.write(("placeholder " + System.currentTimeMillis()).getBytes());
                }
                logger.info("已创建目录和占位文件: {}", "/mnt/file/reports");
            }
            return false; // 返回false表示创建成功
        } catch (IOException e) {
            logger.error("创建目录失败: {}", "/mnt/file/reports", e);
            return true; // 返回true表示创建失败
        }
    }
}
