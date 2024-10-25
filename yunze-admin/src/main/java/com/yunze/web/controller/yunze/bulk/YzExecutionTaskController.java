package com.yunze.web.controller.yunze.bulk;

import com.alibaba.fastjson.JSON;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.poi.ExcelWrite;
import com.yunze.common.utils.spring.SpringUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.common.utils.yunze.CsvToXlsxUtil;
import com.yunze.common.utils.yunze.FileConverter;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.yunze.bulk.IYzExecutionTaskService;
import com.yunze.web.core.config.MyBaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 执行日志信息
 *  2021-06-21
 * @author root
 */
@RestController
@RequestMapping("/yunze/ExecutionTask")
public class YzExecutionTaskController extends MyBaseController
{

    @Resource
    private IYzExecutionTaskService iYzExecutionTaskService;
    @Resource
    private ExcelWrite excelWrite;
    @Resource
    private FileConverter fileConverter;
    @Resource
    private CsvToXlsxUtil csvToXlsxUtil;



    /**
     * 执行日志列表
     */
    @PreAuthorize("@ss.hasPermi('yunze:ExecutionTask:list')")
    @PostMapping(value = "/list", produces = { "application/json;charset=UTF-8" })
    public String list(@RequestBody String Pstr)
    {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            Object agent_id = Parammap.get("agent_id");
            if(agent_id==null ){
                LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
                SysUser currentUser = loginUser.getUser();
                List<String> agent_idArr = new ArrayList<String>();
                agent_idArr.add(""+currentUser.getDeptId());
                Parammap.put("agent_id",agent_idArr);
            }
            return MyRetunSuccess(iYzExecutionTaskService.sel_Map(Parammap),null);
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze:ExecutionTask:list  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return Myerr("获取API通道信息列表 操作失败！");
    }


    /**
     * 下载执行日志
     * @param Pstr
     * @param response
     * @return
     */
    @GetMapping(value = "/download" )
    public void  download(@RequestParam String Pstr, HttpServletResponse response, HttpServletRequest request)   {
        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        try {
            HashMap<String, Object> Parammap = new HashMap<String, Object>();
            if(Pstr!=null){
                Pstr = Pstr.replace("%2F", "/");//转义 /
                Pstr = Pstr.replace(" ", "+");//转义 [ ]  》 +
            }
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            //  System.out.println(map);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            if(Parammap.get("path")!=null && Parammap.get("token")!=null ){
                String path = Parammap.get("path").toString();
                String token = Parammap.get("token").toString();
                LoginUser loginUser = null;

                 try {
                     loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(token);
                 }catch (Exception e){
                     logger.error("<br/> yunze:ExecutionTask:download token获取用户操作失败 <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
                 }
                 if(loginUser!=null){
                     SysUser User = loginUser.getUser();
                     logger.info("<br/> "+" 公司 "+User.getDept().getDeptName()+"  用户名 "+User.getUserId()+" "+User.getUserName()+" <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ");
                     //获取当前文件下载 地址
                     File file2 = new File("");
                     String filePath = file2.getCanonicalPath();
                     filePath +="/mnt/yunze/download/csv/";
                     //切割出下载的地址请求头
                     String Prefix = path.split("/")[1];
                     path = path.substring(Prefix.length()+2,path.length());
                     //拼接下载地址
                     path = filePath+path;
                     excelWrite.download(path,response,"UTF-8",System.currentTimeMillis() + "_csv_");
                 }

            }else{
                logger.info("<br/> Pstr = " + Pstr + " ip =  " + ip + " 下载文件参数缺失");
            }

        } catch (IOException ex) {
            logger.error("<br/> yunze:ExecutionTask:download IOException <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",ex.getCause().toString());
        }catch (Exception e){
            logger.error("<br/> yunze:ExecutionTask:download Exception <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
    }



    /**
     * 下载转换
     */
    @GetMapping(value = "/downloadConversion" )
    public void  downloadConversion(@RequestParam String Pstr, HttpServletResponse response, HttpServletRequest request) {
        {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            try {
                HashMap<String, Object> Parammap = new HashMap<String, Object>();
                if (Pstr != null) {
                    Pstr = Pstr.replace("%2F", "/");//转义 /
                    Pstr = Pstr.replace(" ", "+");//转义 [ ]  》 +
                }
                Pstr = AesEncryptUtil.desEncrypt(Pstr);
                //  System.out.println(map);
                Parammap.putAll(JSON.parseObject((String) Pstr));
                if (Parammap.get("path") != null && Parammap.get("token") != null) {
                    String path = Parammap.get("path").toString();
                    String token = Parammap.get("token").toString();
                    LoginUser loginUser = null;
                    try {
                        loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(token);
                    } catch (Exception e) {
                        logger.error("<br/> yunze:ExecutionTask:download token获取用户操作失败 <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
                    }
                    if (loginUser != null) {
                        SysUser User = loginUser.getUser();
                        logger.info("<br/> " + " 公司 " + User.getDept().getDeptName() + "  用户名 " + User.getUserId() + " " + User.getUserName() + " <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ");



                        CsvOrExcle(path,response);
                    }
                } else {
                    logger.info("<br/> Pstr = " + Pstr + " ip =  " + ip + " 下载文件参数缺失");
                }
            } catch (Exception e) {
                logger.error("<br/> yunze:ExecutionTask:downloadConversion  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
            }
        }
    }


    /**
     * 下载 Csv Or Excle
     * @param path
     * @param response
     * @throws IOException
     */
    private void CsvOrExcle(String path,HttpServletResponse response) throws IOException {
        //获取当前文件下载 地址
        File file2 = new File("");

        String filePath = file2.getCanonicalPath();
        filePath = filePath.replaceAll("\\\\","/"); //xlsx file address


        //切割出下载的地址请求头
        String Prefix = path.split("/")[1];
        if(Prefix.equals("getOriginal")){

        }else{
            filePath += "/mnt/yunze/download/csv/";
        }


        path = path.substring(Prefix.length() + 2, path.length());
        //拼接下载地址
        path = filePath + path;

        // path是指欲下载的文件的路径。
        String  pathArr[] = path.split("\\.");
        if(pathArr!=null && pathArr.length>0){
            String downloadUrl = pathArr[0] + ".xls";
            File file = new File(downloadUrl);
            if (file!=null && file.exists()) {
                // xls 文件 存在直接下载
                excelWrite.download(downloadUrl, response, "UTF-8",System.currentTimeMillis() + "_xls_");
            } else {
                //cvs 文件 转换
                //fileConverter.generateExcel(fileConverter.CSVexcel(path), downloadUrl, "card",50000);
                csvToXlsxUtil.csvToXLSX(pathArr[0] + ".csv");
                // 下载
                excelWrite.download(downloadUrl, response, "UTF-8",System.currentTimeMillis() + "_xls_");
            }
        }
    }


}
