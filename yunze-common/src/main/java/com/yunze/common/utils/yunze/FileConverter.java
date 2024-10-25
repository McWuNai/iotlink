package com.yunze.common.utils.yunze;



import com.csvreader.CsvReader;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.*;


/**
 * @Auther: zhang feng
 * @Date: 2021/07/28/10:21
 * @Description:
 */
@Component
public class FileConverter {

    public static void main(String[] args) {
        FileConverter f = new FileConverter();
        //System.out.println(f.CSVexcel());
    }



    public  List<Map<String,Object>> CSVexcel(String csvFilePath){
        List<Map<String,Object>> rlist = new ArrayList<>();
        try {
            //String csvFilePath = "F:\\谷歌下载\\a4f3b472050746ea8f4035648ded7bc7_CardOut.csv";
                // 创建CSV读对象
                CsvReader csvReader = new CsvReader(csvFilePath,',',Charset.forName("GBK"));
                // 读表头 csvReader.readHeaders() 读行 csvReader.readRecord()
                List<String> headList = new ArrayList<>();
                int i = 0 ;
                while (csvReader.readHeaders()){
                    // 读一整行
                    Map<String,Object> obj = new HashMap<>();
                    String row =   csvReader.getRawRecord();
                    String rows[] = row.split(",");
                    List list = Arrays.asList(rows);
                    if(i==0){
                        headList = list;
                    }
                    i++;
                    for (int j = 0; j < headList.size() ; j++) {
                        Object val = "";
                        try {
                            val =  list.get(j);
                        }catch (Exception e){
                        }
                        obj.put(headList.get(j),val);
                    }
                    rlist.add(obj);
                   // System.out.println(csvReader.get("Link")); // 读这行的某一列
                }

        }catch (Exception e){

        }
        return  rlist;
    }

    /**
     * 写入文件
     * @param list
     * @param savePath
     * @param PageName
     * @param Pagesize 65536
     */
    public void generateExcel(List<Map<String,Object>> list, String savePath,String PageName,int Pagesize) {
        // 创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 建立新的sheet对象（excel的表单）
        Map<String,Object> heard = null;
        Pagesize = Pagesize>65536?65536:Pagesize;//不能超过poi 最大限制

        try {
            int index = 0;
            String PName = PageName +"-"+ (Pagesize*index)+"-"+(Pagesize*(index+1));
            HSSFSheet sheet = workbook.createSheet(PName);
            //sheet.setDefaultRowHeightInPoints(12);// 设置列高
            //sheet.setDefaultColumnWidth(40);// 设置列宽

            for (int i = 0, len = list.size(); i < len; i++) {
                HSSFRow row = sheet.createRow((i) - (index * Pagesize));
                if ((i + 1) % Pagesize == 0) {
                    index+=1;
                    PName = PageName +"-"+ (Pagesize*index)+"-"+(Pagesize*(index+1));
                    sheet = workbook.createSheet(PName);
                    row = sheet.createRow(0);
                    int j = 0;
                    for(String key:heard.keySet()){
                        if(key!=null){
                            Object value = heard.get(key);
                            value = value!=null?value:"";
                            row.createCell(j).setCellValue(value.toString());
                            j++;
                        }
                    }
                }
                if(i==0){
                    heard = list.get(i);//获取表头
                }
                 //row = sheet.createRow(i);

                Map<String,Object> obj = list.get(i);
                if(obj!=null){
                    int j = 0;
                    for(String key:obj.keySet()){
                        if(key!=null){
                            Object value = obj.get(key);
                            value = value!=null?value:"";
                            row.createCell(j).setCellValue(value.toString());
                            j++;
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println("写入数据错误！"+e.getMessage().toString());
        }

        // 输出Excel文件
        try {
            FileOutputStream fos = new FileOutputStream(new File(savePath));
            workbook.write(fos);
            workbook.close();
            fos.close();
        } catch (Exception e) {
            System.out.println("生成excel文档失败"+e.getMessage().toString());
        }
    }


    public String export(List<Map<String,Object>> list, String f) {
        String name = "";
        HSSFWorkbook book = new HSSFWorkbook();
        HSSFSheet sheet = book.createSheet("stud");
// 声明一行
        HSSFRow row = sheet.createRow(0);
        row = sheet.createRow(0);
        row.createCell(0).setCellValue("身份");
        row.createCell(1).setCellValue("信用");
        int index = 0;//记录额外创建的sheet数量
        for (int i = 0; i < list.size(); i++) {
            if ((i + 1) % 65535 == 0) {
                sheet = book.createSheet("stud" + index);
                row = sheet.createRow(0);
                row.createCell(0).setCellValue("身份");
                row.createCell(1).setCellValue("信用");
                index++;
            }
            row = sheet.createRow((i + 1) - (index * 65535));
// 第四步，创建单元格，并设置值
            row.createCell((short) 0).setCellValue(list.get(i).get("info").toString());
            row.createCell((short) 1).setCellValue(list.get(i).get("score").toString());
        }
// 第六步，将文件存到指定位置
        String fileName = "";
        try {
            fileName = f + "/download/导出.xls";
            name = "导出.xls";
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fout = new FileOutputStream(fileName);
            book.write(fout);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;

    }



}
