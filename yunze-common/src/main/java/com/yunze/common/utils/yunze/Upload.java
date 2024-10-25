package com.yunze.common.utils.yunze;

import java.io.File;
import java.io.IOException;

/**
 * 上传文件帮助类
 * @Auther: zhang feng
 * @Date: 2021/03/05/14:48
 * @Description:
 */
public class Upload {

    /**
     * 判断路径是否存在，否：创建此路径
     * @param file
     * @return
     * @throws IOException
     */
    public static File mkdirsmy(File file) throws IOException{
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        return file;
    }




}
