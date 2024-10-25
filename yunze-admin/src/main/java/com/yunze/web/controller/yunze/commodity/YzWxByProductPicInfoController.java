package com.yunze.web.controller.yunze.commodity;


import com.alibaba.fastjson.JSON;
import com.yunze.common.annotation.Log;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.enums.BusinessType;
import com.yunze.common.utils.ServletUtils;
import com.yunze.common.utils.ip.IpUtils;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import com.yunze.system.service.impl.yunze.commodity.YzWxByProductPicInfoServiceImpl;
import com.yunze.web.core.config.MyBaseController;
import io.swagger.annotations.Api;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品图片
 */
@Api("商品图片")
@RestController
@RequestMapping("/yunze/productPic")
public class YzWxByProductPicInfoController extends MyBaseController {

    @Resource
    private YzWxByProductPicInfoServiceImpl yzWxByProductPicInfoServiceImpl;








    /**
     * 商品图片 编辑
     * @param file
     * @return
     */
    @Log(title = "商品图片编辑", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('yunze:productPic:Prdedit')")
    @PostMapping(value = "/Prdedit", produces = { "application/json;charset=utf-8" })
    public AjaxResult Prdedit(MultipartFile file[], @RequestParam Map<String,String> map)
    {
        String Pstr = map.get("Pstr").toString();
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject((String) Pstr));
            return AjaxResult.success(yzWxByProductPicInfoServiceImpl.Prdedit(file,Parammap));
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productPic/Prdedit  <br/> ip =  " + ip + " <br/> ",e.getCause().toString());
        }
        return AjaxResult.error("商品图片编辑 操作失败！");
    }




    /**
     * 获取商品 图片
     */
    @PreAuthorize("@ss.hasPermi('yunze:productPic:Prdedit')")
    @PostMapping(value = "/findList", produces = {"application/json;charset=UTF-8"})
    public String findConcise(@RequestBody String Pstr) {
        HashMap<String, Object> Parammap = new HashMap<String, Object>();
        if (Pstr != null) {
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr = AesEncryptUtil.desEncrypt(Pstr);
            Parammap.putAll(JSON.parseObject(Pstr));
            return MyRetunSuccess(yzWxByProductPicInfoServiceImpl.findList(Parammap), null);
        } catch (Exception e) {
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productPic/findList  <br/> Pstr = " + Pstr + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
        return Myerr("获取商品 图片 操作失败！");
    }



    /**
     * 下载图片
     */
    @RequestMapping(value = "/readImage")
    @ResponseBody
    public void readImage(HttpServletRequest request, HttpServletResponse response){
        String map = request.getParameter("map");
        Map<String, Object> ParamMap = new HashMap<String, Object>();
        if(map!=null){
            map = map.replace("%2F", "/");//转义 /
            map = map.replace(" ", "+");//转义 ' '
        }
        try {
            map =  AesEncryptUtil.desEncrypt(map);
            ParamMap.putAll(JSON.parseObject((String) map));
            String fileName = ParamMap.get("imagePath").toString();
            File cat = new File("");
            String filePath = cat.getCanonicalPath();
            String imagePath =  filePath + fileName;
            if(imagePath != null){
                File file = new File(imagePath);
                if (file.exists()) {
                    DataOutputStream temps = new DataOutputStream(response
                            .getOutputStream());
                    DataInputStream in = new DataInputStream(
                            new FileInputStream(imagePath));
                    byte[] b = new byte[2048];
                    while ((in.read(b)) != -1) {
                        temps.write(b);
                    }
                    temps.flush();
                    in.close();
                    temps.close();
                }
            }
        }catch (Exception e){
            String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
            logger.error("<br/> yunze/productPic/readImage  <br/> Pstr = " + map + " <br/> ip =  " + ip + " <br/> ", e.getCause().toString());
        }
    }



}
















































































