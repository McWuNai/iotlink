package com.yunze.utils;

import com.alibaba.fastjson.JSON;
import com.huawei.insa2.comm.cmpp30.message.CMPP30DeliverMessage;
import com.huawei.insa2.comm.cmpp30.message.CMPP30SubmitMessage;
import com.huawei.insa2.comm.cmpp30.message.CMPP30SubmitRepMessage;
import com.huawei.insa2.util.Args;
import com.huawei.insa2.util.TypeConvert;
import com.huawei.smproxy.SMProxy30;
import com.yunze.apiCommon.utils.SMSUtil;
import com.yunze.common.mapper.yunze.bulk.YzBusiCardSmsMapper;
import com.yunze.common.mapper.yunze.bulk.YzSmSBulkBusinessDtailsMapper;
import com.yunze.common.utils.yunze.BulkUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 短信发送监听类
 * 
 * @author Administrator
 *
 */
@Component
public class SMSSendPlugin {

	@Resource
	public BulkUtil bulkUtil;
	@Resource
	public YzBusiCardSmsMapper yzBusiCardSmsMapper;
	@Resource
	public YzSmSBulkBusinessDtailsMapper yzSmSBulkBusinessDtailsMapper;


	public void setBulkUtil(BulkUtil bulkUtil){
		this.bulkUtil = bulkUtil;
	}

	public void setYzBusiCardSmsMapper(YzBusiCardSmsMapper yzBusiCardSmsMapper){
		this.yzBusiCardSmsMapper = yzBusiCardSmsMapper;
	}

	public void setYzSmSBulkBusinessDtailsMapper(YzSmSBulkBusinessDtailsMapper yzSmSBulkBusinessDtailsMapper){
		this.yzSmSBulkBusinessDtailsMapper = yzSmSBulkBusinessDtailsMapper;
	}



	/**
	 * 短信发送队列,先进先出处理充值请求
	 */
	private  Queue<Map<String, Object>> sendQueue = new ConcurrentLinkedQueue<Map<String, Object>>(); // FIFO（先进先出）原则对元素进行排序

	/**
	 * 短信接收队列,先进先出处理充值请求
	 */
	private static   Queue<CMPP30DeliverMessage> receiveQueue = new ConcurrentLinkedQueue<CMPP30DeliverMessage>(); // FIFO（先进先出）原则对元素进行排序

	/**
	 * 短信处理线程控制
	 */
	private  boolean threadRun = true;
	/**s
	 * 短信代理控制器
	 */
	public  SMProxy30 smProxy = null;
	/**
	 * @pk_Total 相同msg_Id消息总条数,短短信这里是1
	 */
	public   int pk_Total = 1;
	/**
	 * @pk_Number 相同msg_Id的消息序号
	 */
	public   int pk_Number = 1;
	/**
	 * @registered_Delivery 是否要求返回状态报告
	 */
	public static final int registered_Delivery = 0;
	/**
	 * @msg_Level 信息级别
	 */
	public   int msg_Level = 1;
	/**
	 * @fee_UserType 0对目的终端计费；1对源终端计费；2对SP计费;
	 */
	public   int fee_UserType = 0;
	/**
	 * @tp_Pid GSM协议类型 一般文本的时候设0,铃声图片设1
	 */
	public   int tp_Pid = 0;
	/**
	 * @tp_Udhi GSM协议类型 0不包含头部信息 1包含头部信息
	 */
	public   int tp_Udhi = 0;
	/**
	 * @ee_Type 资费类别 一般为02：按条计信息费
	 */
	public   String fee_Type = "02";
	/**
	 * @@fee_Code 资费代码(以分为单位)
	 */
	public   String fee_Code = "0";
	/**
	 * @service_Id 业务类型 用户自定义 用来分类查询
	 */
	public   String service_Id = "SDYZBDDH";
	/**
	 * @fee_Terminal_Id 被计费用户的号码
	 */
	public   String fee_TerminalId = "";
	/**
	 * @fee_Terminal_Type 被计费用户的类型
	 */
	public   int fee_Terminal_Type = 0;
	/**
	 * @msg_Src 消息内容来源 6位的企业代码
	 */
	public   String msgSrc = "";
	/**
	 * @valid_Time 存活有效期
	 */
	public   Date valid_Time = null;
	/**
	 * @at_Time 定时发送时间
	 */
	public   Date at_Time = null;
	/**
	 * @src_Terminal_Id 移动所提供的服务代码 此处需修改
	 */
	public   String src_TerminalId = "";

	public SMSSendPlugin(){};


	/**
	 * Cmmp3.0 短信网关参数配置
	 */
	public SMSSendPlugin(Args args) {
		msgSrc = args.get("source-addr","");
		System.out.println("【"+msgSrc+"】短信监控处理程序初始化中...");
		args.get("shared-secret","");
		// 初始化短信代理控制器
		smProxy = new SMProxy30(args);
	}

	public boolean start() {
		System.out.println("【"+msgSrc+"】短信监控处理程序启动中...");
		String state = smProxy.getConnState();
		// 初始化短信上行下行处理程序
		if (state == null) {
			startSendDealThread();
			System.out.println("【"+msgSrc+"】短信下行处理队列启动成功，正在处理...");

			startReceiveDealThread();
			System.out.println("【"+msgSrc+"】短信上行处理队列启动成功，正在处理...");

		}
		return true;
	}




	public boolean stop() {
		System.out.println("短信监控处理程序停止中...");
		smProxy.close();
		return true;
	}

	/**
	 * 控制队列线程是否继续
	 * 
	 * @param
	 */
	public  void setThreadRun(boolean isRun) {
		threadRun = isRun;
	}

	/**
	 * 向队列中添加发送记录，基于ConcurrentLinkedQueue
	 * 
	 * @param
	 */
	public  void threadSendQueueAdd(Map<String, Object> map) {
		sendQueue.offer(map); // 将指定元素插入此队列的尾部
	}

	/**
	 * 获取短信发送记录，基于ConcurrentLinkedQueue
	 * 
	 * @return order
	 */
	public  Map<String, Object> getThreadSendQueueObj() {

		return sendQueue.poll(); // 获取并移除此队列的头，如果此队列为空，则返回 null
	}

	/**
	 * 启动短信下行线程
	 */
	public  void startSendDealThread() {
		try {
			Thread sendThread = new Thread(new Runnable() {
				public void run() {
					while (threadRun) {
						try {
							// 取队列数据
							Map<String, Object> map = getThreadSendQueueObj();
							if (null == map) {
								System.out.println("【"+msgSrc+"】短信下行队列为空，监控程序休眠10秒！");
								// 队列为空，等待10秒
								Thread.sleep(10000);
							} else {
								threadSend(map);
							}
						} catch (Exception e) {
							System.out.println("【"+msgSrc+"】短信下发异常：" + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});
			sendThread.start();
		} catch (Exception e) {
			throw new RuntimeException("Thread moneyThread new Thread Exception");
		}
	}

	/**
	 * 短信发送队列处理程序
	 * 
	 * @param map
	 */
	public  void threadSend(Map<String, Object> map) {
		System.out.println("【"+msgSrc+"】下行队列处理中：" + JSON.toJSONString(map));
		String b_id = map.get("b_id")!=null?map.get("b_id").toString():"";
		String card_number = map.get("card_number")!=null?map.get("card_number").toString():"";
		Map<String,Object> updMap = null;
		// @dest_Terminal_Id 接收业务的MSISDN号码,就是接收短信的手机号,String数组
		String[] simId = { map.get("sms_number").toString() };
		// @msg_Content 消息内容 ,发送的消息内容
		String content = map.get("content").toString();
		try {
			byte[] messageUCS2 = content.getBytes("UnicodeBigUnmarked");// 转换为byte[]
			int messageUCS2Len = messageUCS2.length;// 长短信长度
			int maxMessageLen = 140;
			if (messageUCS2Len > maxMessageLen){
				updMap = sendLongSMS( simId, content);
			}else{
				updMap = sendSMS( simId, content);
			}
		}catch (Exception e){
			System.out.println("【"+msgSrc+"】CMPP短信下发异常 "+e.getMessage());
		}
		if(updMap!=null){
			updMap.put("b_id",b_id);
			updMap.put("card_number",card_number);
			updMap.put("send_number",simId[0]);
 			String state_id = "2";
			boolean bool = (boolean) updMap.get("bool");
			if(bool){
				state_id = "1";
			}
			updMap.put("state_id",state_id);
			if(bulkUtil!=null){
				bulkUtil.smsDupdate(updMap);
			}
			if(yzSmSBulkBusinessDtailsMapper!=null){
				yzSmSBulkBusinessDtailsMapper.update(updMap);
			}
		}else{
			System.out.println("【"+msgSrc+"】updMap == null "+JSON.toJSONString(map));
		}
	}





	/**
	 * 短短信消息发送接口
	 * @param simId
	 *            接收者
	 * @param content
	 *            短信内容
	 * @return
	 */
	public  Map<String,Object> sendSMS(String[] simId, String content) {
		System.out.println("sendSMS : registered_Delivery = "+registered_Delivery+" ,msg_Level = "+msg_Level+" ,service_Id = "+service_Id+" ,fee_UserType = "+fee_UserType+" ,fee_TerminalId = "+fee_TerminalId
				+" ,fee_Terminal_Type = "+fee_Terminal_Type+" ,tp_Pid = "+tp_Pid+" ,msgSrc = "+msgSrc+" ,fee_Code = "+fee_Code+" ,valid_Time = "+valid_Time+" ,at_Time = "+at_Time+" ,src_TerminalId = "+src_TerminalId+" ,simId = "+simId);
		Map<String,Object> rMap = new HashMap<>();
		boolean bool = false;
		String result = "";
		String msgId = "";
		String mydescribe = "";
		// @msg_Fmt 消息格式 0：ASCII串；8：UCS2编码；其他非法
		int msg_Fmt = 0;
		// @msg_Content 消息内容 byte[],发送的消息内容,需要转化为byte[]
		byte[] msgContent = null;
		// 判断是否中文
		if (SMSUtil.isChinese(content)) {
			msg_Fmt = 8;
			try {
				msgContent = content.getBytes("UTF-16BE");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				msgContent = "".getBytes();
			}
		} else {
			msgContent = content.getBytes();
		}
		CMPP30SubmitMessage message = new CMPP30SubmitMessage(pk_Total, pk_Number, registered_Delivery, msg_Level,
				service_Id, fee_UserType, fee_TerminalId, fee_Terminal_Type, tp_Pid, tp_Udhi, msg_Fmt, msgSrc, fee_Type,
				fee_Code, valid_Time, at_Time, src_TerminalId, simId, 0, msgContent, "");
		CMPP30SubmitRepMessage subRM = null;
		try {
			subRM = (CMPP30SubmitRepMessage) smProxy.send(message);
			//content = content.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
			System.out.println("短信下发结果：" + subRM);

			if (subRM.getResult() == 0) {
				bool = true;
				try {
					msgId = subRM.getMsgId()!=null?""+TypeConvert.byte2long(subRM.getMsgId()):"";
					result = ""+subRM.getResult();
				}catch (Exception e) {
					System.out.println("msgId 转化失败……");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			mydescribe = e.getMessage();
			mydescribe = mydescribe.length()>240?mydescribe.substring(0,240):mydescribe;

		}
		rMap.put("bool",bool);
		rMap.put("result",result);
		rMap.put("msgId",msgId);
		rMap.put("mydescribe",mydescribe);
		return rMap;
	}

	/**
	 * 长短信消息发送接口
	 * @param simId
	 *            接收者
	 * @param content
	 *            短信内容
	 * @return
	 */
	public  Map<String,Object> sendLongSMS( String[] simId, String content) {
		System.out.println("sendLongSMS : registered_Delivery = "+registered_Delivery+" ,msg_Level = "+msg_Level+" ,service_Id = "+service_Id+" ,fee_UserType = "+fee_UserType+" ,fee_TerminalId = "+fee_TerminalId
				+" ,fee_Terminal_Type = "+fee_Terminal_Type+" ,tp_Pid = "+tp_Pid+" ,msgSrc = "+msgSrc+" ,fee_Code = "+fee_Code+" ,valid_Time = "+valid_Time+" ,at_Time = "+at_Time+" ,src_TerminalId = "+src_TerminalId+" ,simId = "+simId);
		Map<String,Object> rMap = new HashMap<>();
		boolean bool = false;
		String result = "";
		String msgId = "";
		String mydescribe = "";
		// @msg_Fmt 消息格式 8：UCS2编码；其他非法
		int msg_Fmt = 8;
		List<byte[]> list = SMSUtil.getLongByte(content);// 截取短信内容并配置参数,这个内容要大于70汉字
		for (int i = 0; i < list.size(); i++) {
			CMPP30SubmitMessage message = new CMPP30SubmitMessage(list.size(), i + 1, registered_Delivery, msg_Level,
					service_Id, fee_UserType, fee_TerminalId, fee_Terminal_Type, tp_Pid, 1, msg_Fmt, msgSrc, fee_Type,
					fee_Code, valid_Time, at_Time, src_TerminalId, simId, 0, list.get(i), "");
			CMPP30SubmitRepMessage subRM = null;
			try {
				subRM = (CMPP30SubmitRepMessage) smProxy.send(message);
				System.out.println("短信下发结果：" + subRM);
				if (subRM.getResult() == 0) {
					bool = true;
					try {
						msgId = subRM.getMsgId()!=null?""+TypeConvert.byte2long(subRM.getMsgId()):"";
						result = ""+subRM.getResult();
					}catch (Exception e) {
						System.out.println("msgId 转化失败……");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		rMap.put("bool",bool);
		rMap.put("result",result);
		rMap.put("msgId",msgId);
		rMap.put("mydescribe",mydescribe);
		return rMap;
	}
	/**
	 * 向队列中添加发送记录，基于ConcurrentLinkedQueue
	 * 
	 * @param
	 */
	public static void threadeceiveQueueAdd(CMPP30DeliverMessage msg) {
		receiveQueue.offer(msg); // 将指定元素插入此队列的尾部
	}

	/**
	 * 获取短信发送记录，基于ConcurrentLinkedQueue
	 * 
	 * @return order
	 */
	public  CMPP30DeliverMessage getThreadeceiveQueueObj() {
		return receiveQueue.poll(); // 获取并移除此队列的头，如果此队列为空，则返回 null
	}

	/**
	 * 启动短信下行线程
	 */
	public  void startReceiveDealThread() {
		try {
			Thread receiveThread = new Thread(new Runnable() {
				public void run() {
					while (threadRun) {
						try {
							// 取队列数据
							CMPP30DeliverMessage msg = getThreadeceiveQueueObj();
							if (null == msg) {
								System.out.println("【"+msgSrc+"】短信上行队列为空，监控程序休眠10秒！");
								// 队列为空，等待10秒
								Thread.sleep(10000);
							} else {
								threadReceive(msg);
							}
						} catch (Exception e) {
							System.out.println("【"+msgSrc+"】短信上行异常：" + e.getMessage());
						}
					}
				}
			});
			receiveThread.start();
		} catch (Exception e) {
			throw new RuntimeException("Thread moneyThread new Thread Exception");
		}
	}

	/**
	 * 短信发送队列处理程序
	 * @param
	 */
	public  void threadReceive(CMPP30DeliverMessage msg) {
		System.out.println("【"+msgSrc+"】上行队列处理中：" + JSON.toJSONString(msg));
		// 上行队列处理中：{"bytes":[1,10,28,54,20,-60,5,-64,0,13,81,-89,49,48,54,52,56,57,57,49,53,48,49,51,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,49,48,54,52,55,51,49,48,53,49,53,56,50,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,65,97,97,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],"destnationId":"1064899150130","statusMsgId":null,"submitTime":null,"sMSCSequence":-1,"registeredDeliver":0,"commandId":5,"tpPid":0,"srcterminalId":"1064731051582","tpUdhi":0,"stat":null,"linkID":"","doneTime":null,"serviceId":"","msgFmt":0,"destTerminalId":null,"msgLength":3,"msgId":[20,-60,5,-64,0,13,81,-89],"msgContent":[65,97,97],"sequenceId":17439798,"srcterminalType":0}
		int msgFmt = msg.getMsgFmt();
		String msgContent = "";
		if (8 == msgFmt) {
			try {
				msgContent = new String(msg.getMsgContent(), "UTF-16BE");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				msgContent = "";
			}//estnationId
		} else {
			msgContent = new String(msg.getMsgContent());
		}
		msgContent = msgContent.replaceAll(">", "&gt;").replaceAll("<", "&lt;");

		Map<String, Object> addMap = new HashMap<>();
		try {
			String msgId = msg.getMsgId()!=null?""+ TypeConvert.byte2long(msg.getMsgId()):"";
			addMap.put("msgid",msgId);
			addMap.put("msisdn",msg.getSrcterminalId());
			addMap.put("content",msgContent);
			addMap.put("state_id","1");
			addMap.put("type","receive");
			addMap.put("sms_src_TerminalId",msg.getDestnationId());
			yzBusiCardSmsMapper.add(addMap);
		}catch (Exception e){
			System.out.println("【"+msgSrc+"】yzBusiCardSmsMapper.add(addMap) 异常 "+JSON.toJSONString(addMap));
		}


	}

}
