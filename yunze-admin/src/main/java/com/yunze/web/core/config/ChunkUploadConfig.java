package com.yunze.web.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ChunkUploadConfig {
    @Value("${upload.chunk.temp-dir:/tmp/chunks}")
    private String chunkTempDir;

    @Value("${upload.chunk.size:2097152}") // 2MB
    private long chunkSize;

    @Value("${upload.chunk.timeout:30000}") // 30秒
    private long chunkTimeout;

    @Bean
    public File chunkTempDir() {
        File dir = new File(chunkTempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
