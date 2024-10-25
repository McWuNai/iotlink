package com.yunze.common.utils.yunze;

import java.util.ArrayList;
import java.util.List;


/**
 * 
* @ClassName: ID_Generate   
* @Description: ID生成器(这里用一句话描述这个类的作用)   
* @author zhangfeng   
* @date 2018年12月14日 上午9:59:42   
*
 */
public class ID_Generate {
	
	/**
	 *  生成单个id编号
	 * @param t_name 表名
	 * @param ct 当天数据数量
	 * @param leng 生成编号位数
	 * @return
	 */
	public static String G_id(String t_name,Integer ct,int leng) {
		String Times[]=VeDate.getYyyyAndMm();//获取当前日期YYYYMMDD
		String TIme_p=Times[0]+Times[1]+Times[2];//日期部分
		ct=ct+1;
		String ID="";
		for (int i = 0; i < (leng-(ct.toString()).length()); i++) {
			ID+="0";
		}
		ID=ID+ct;//编号
		return t_name+TIme_p+ID;		
	}
	
	/**
	 *  生成单个id编号
	 * @param t_name 表名
	 * @param ct 当天数据数量
	 * @param leng 生成编号位数
	 * @param ID_COUNT 生成多少个编号
	 * @return
	 */
	public static List G_id(String t_name,Integer ct,int leng,int ID_COUNT) {
		List<String> olist=new ArrayList<String>();
		String Times[]=VeDate.getYyyyAndMm();//获取当前日期YYYYMMDD
		String TIme_p=Times[0]+Times[1]+Times[2];//日期部分
		for (int j = 0; j < ID_COUNT; j++) {
			ct=ct+1;
			String ID="";
			for (int i = 0; i < (leng-(ct.toString()).length()); i++) {
				ID+="0";
			}
			ID=ID+ct;//编号
			olist.add(t_name+TIme_p+ID);
		}
		
		return olist;		
	}
	
	public static void main(String[] args) {
		List<String> olist=ID_Generate.G_id("GSCN", 0, 4, 101);
		for (int i = 0; i < olist.size(); i++) {
			System.out.println(olist.get(i));
		}
	}
}
