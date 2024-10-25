package com.yunze.common.utils.Email;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @ClassName: SMTPUtil   
 * @Description: TODO 得到smtp
 * @author zhangfeng
 * @date 2018年8月28日
 */
public class SMTPUtil {
	/** 
	 * 方法名: SimpleMailSender 
	 * 功能描述: TODO 简单的smtp生成，大部分是有用的，建议自己建立smtp库....
	 * @param: @param userName
	 * @param: @return  
	 * @return: String 
	 */
	public static String SimpleMailSender(String userName) {
		return "smtp.exmail.qq.com";
		//return  "smtp." + getHost(userName);
	}

	
	/**   
	* @Title: getSMTPAddress    
	* @Description: TODO (这里用一句话描述这个方法的作用)    
	* @param @param userName
	* @param @return    设定文件    
	* @return String    返回类型    
	* @throws    
	*/
	public static String getSMTPAddress(String userName){
		String smtpAddress = null;
		Properties props = new Properties();
		try {
			InputStream in = SMTPUtil.class.getResourceAsStream("/smtp.properties");
			props.load(in);
			//读取properties的内容
			smtpAddress = props.getProperty(getHost(userName).trim());
			//没有获取到
			if(smtpAddress == null){
				//生成简单得
				smtpAddress = SimpleMailSender(userName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return smtpAddress;
	}
	
	
	/**   
	* @Title: getHost    
	* @Description: TODO 得到 邮箱@后面得字符    
	* @param @param userName
	* @param @return    设定文件    
	* @return String    返回类型    
	* @throws    
	*/
	public static String getHost(String userName){
		return userName.split("@")[1];
	}
	
//	public static void main(String[] args) {
//		String s = getSMTPAddress("nie_zw@qq.com");
//		System.out.println(s);
//	}
}
