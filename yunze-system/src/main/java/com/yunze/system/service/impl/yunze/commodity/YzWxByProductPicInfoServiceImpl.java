package com.yunze.system.service.impl.yunze.commodity;

import com.yunze.common.mapper.yunze.commodity.YzWxByProductPicInfoMapper;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.system.service.yunze.commodity.IYzWxByProductPicInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

@Service
public class YzWxByProductPicInfoServiceImpl implements IYzWxByProductPicInfoService {

    @Resource
    private YzWxByProductPicInfoMapper yzWxByProductPicInfoMapper;


    @Override
    public boolean save(Map<String, Object> map) {
        return yzWxByProductPicInfoMapper.save(map)>0;
    }

    @Override
    public boolean edit(Map<String, Object> map) {
        return yzWxByProductPicInfoMapper.edit(map)>0;
    }

    @Override
    public List<Map<String, Object>> findList(Map<String, Object> map) {
        return yzWxByProductPicInfoMapper.findList(map);
    }

    @Override
    @Transactional
    public String Prdedit(MultipartFile files[],Map<String, Object> map) {
        try {
            List<Map<String,Object>> imgArr = (List<Map<String, Object>>) map.get("imgArr");
            String Vue_is_master = map.get("is_master").toString();
            String product_id = map.get("product_id").toString();
            List<Map<String,Object>> UpdimgArr = new ArrayList<>();
            //文件写入
            for (int i = 0; i < files.length; i++) {

                MultipartFile file = files[i];
                String filename=file.getOriginalFilename();
                String ReadName = UUID.randomUUID().toString().replace("-","")+filename;
                // 获取当前项目的工作路径
                File file2 = new File("");
                String filePath = file2.getCanonicalPath();
                File newFile = new File(filePath+"/mnt/yunze/img/shop/"+ReadName);
                if(i==0){
                    File Url=new File(filePath+"/mnt/yunze/img/shop/1.txt");//tomcat 生成路径
                    Upload.mkdirsmy(Url);
                }
                file.transferTo(newFile);
                Map<String,Object> img = null;
                String is_master = "0";
                //获取匹对文件名的 img
                for (int j = 0; j < imgArr.size(); j++) {
                    Map<String,Object> imgObje = imgArr.get(j);
                    String imgName =  imgObje.get("fileName").toString();
                    String pic_order =  imgObje.get("pic_order").toString();
                    if(imgName.equals(filename)){
                        img = imgArr.get(j);
                        if(Vue_is_master.equals("1")){
                            if(pic_order.equals("0")){
                                is_master = "1";
                            }
                        }
                        break;
                    }
                }
                img.put("is_master",is_master);
                img.put("product_id",product_id);
                img.put("pic_url","/mnt/yunze/img/shop/"+ReadName);
                UpdimgArr.add(img);
            }
            map.put("UpdimgArr",UpdimgArr);
            try {
                /*//变更图片状态 失效
                map.put("pic_status","0");
                yzWxByProductPicInfoMapper.updPicStatus(map);*/
               if(yzWxByProductPicInfoMapper.saveArr(map)<=0){
                   return ("上传图片 操作失败！");
               }
            } catch (Exception e) {
                System.out.println("图片保存   失败 " + e.getMessage());
                return ("图片保存 操作失败！");
            }
        }catch (Exception e){
            System.out.println(e);
            return "上传图片异常";
        }
        return "图片上传成功！";
    }
}
