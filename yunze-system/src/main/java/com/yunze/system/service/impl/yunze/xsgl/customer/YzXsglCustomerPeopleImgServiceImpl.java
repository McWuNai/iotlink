package com.yunze.system.service.impl.yunze.xsgl.customer;

import com.yunze.common.mapper.yunze.xsgl.customer.YzXsglCustomerPeopleImgMapper;
import com.yunze.common.utils.yunze.PageUtil;
import com.yunze.common.utils.yunze.Upload;
import com.yunze.system.service.yunze.xsgl.customer.YzXsglCustomerPeopleImgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

@Service
public class YzXsglCustomerPeopleImgServiceImpl implements YzXsglCustomerPeopleImgService {

    @Resource
    private YzXsglCustomerPeopleImgMapper yzXsglCustomerPeopleImgMapper;

    @Override
    @Transactional
    public String Prdedit(MultipartFile[] files, Map<String, Object> map) {
            try {
                List<Map<String,Object>> imgArr = (List<Map<String, Object>>) map.get("imgArr");
                String Vue_is_master = map.get("is_master").toString();
                String cu_id = map.get("cup_id").toString();
                String dept_id = map.get("dept_id").toString();
                List<Map<String,Object>> UpdimgArr = new ArrayList<>();
                //文件写入
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    String filename=file.getOriginalFilename();
                    String ReadName = UUID.randomUUID().toString().replace("-","")+filename;
                    // 获取当前项目的工作路径
                    File file2 = new File("");
                    String filePath = file2.getCanonicalPath();
                    File newFile = new File(filePath+"/mnt/yunze/img/contacts/"+ReadName);
                    if(i==0){
                        File Url=new File(filePath+"/mnt/yunze/img/contacts/1.txt");//tomcat 生成路径
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
                    img.put("cup_id",cu_id);
                    img.put("dept_id",dept_id);
                    img.put("pic_url","/mnt/yunze/img/contacts/"+ReadName);
                    UpdimgArr.add(img);
                }
                map.put("UpdimgArr",UpdimgArr);
                try {
                    if(yzXsglCustomerPeopleImgMapper.saveArr(map)<=0){
                        return ("上传图片 操作失败！");
                    }
                }catch (Exception e) {
                    System.out.println("图片保存   失败 " + e.getMessage());
                    return ("图片保存 操作失败！");
                }
            }catch (Exception e){
                System.out.println(e);
                return "上传图片异常";
            }
            return "图片上传成功";
    }

    @Override
    public List<Map<String, Object>> contactsList(Map<String, Object> map) {
        return yzXsglCustomerPeopleImgMapper.contactsList(map);
    }

    @Override
    public Map<String, Object> liaisonList(Map map) {
        Map<String, Object> omp=new HashMap<String, Object>();
        Integer currenPage=map.get("pageNum")!=null?Integer.parseInt(map.get("pageNum").toString()):0;
        Integer pageSize=map.get("pageSize")!=null?Integer.parseInt(map.get("pageSize").toString()):10;
        Integer rowCount = yzXsglCustomerPeopleImgMapper.selMapCount(map);
        rowCount=rowCount!=null?rowCount:0;
        PageUtil pu=new PageUtil(rowCount,currenPage , pageSize);//初始化分页工具类
        //数据打包
        map.put("StarRow", pu.getStarRow());
        map.put("PageSize", pu.getPageSize());
        omp.put("Pu", pu);
        omp.put("Data", yzXsglCustomerPeopleImgMapper.liaisonList(map));
        return  omp;
    }

    @Override
    public boolean deleteUrl(Map map) {
        return yzXsglCustomerPeopleImgMapper.deleteUrl(map)>0;
    }

    @Override
    public boolean upInvalid(Map map) {
        return yzXsglCustomerPeopleImgMapper.upInvalid(map)>0;
    }

    @Override
    public boolean upEffective(Map map) {
        return yzXsglCustomerPeopleImgMapper.upEffective(map)>0;
    }


}




















