package com.huawei.smproxy;

import com.huawei.insa2.comm.cmpp.message.CMPPMessage;
import com.huawei.insa2.comm.cmpp30.CMPP30Connection;
import com.huawei.insa2.comm.cmpp30.CMPP30Transaction;
import com.huawei.insa2.comm.cmpp30.message.CMPP30DeliverMessage;
import com.huawei.insa2.comm.cmpp30.message.CMPP30DeliverRepMessage;
import com.huawei.insa2.util.Args;
import com.yunze.utils.SMSSendPlugin;

import java.io.IOException;
import java.util.Map;


public class SMProxy30 {
	private CMPP30Connection conn;

	// 完成初始化和向ISMG登录等工作
	public SMProxy30(Map<String, Object> args) {
		this(new Args(args));
	}

	public SMProxy30(Args args) {
		conn = new CMPP30Connection(args);
		conn.addEventListener(new CMPP30EventAdapter(this));
		conn.waitAvailable();
		if (!conn.available())
			throw new IllegalStateException(conn.getError());
		else
			return;
	}

	/**
	 * 发送消息，阻塞直到收到响应或超时。 返回为收到的消息
	 *
	 * @exception 超时或通信异常
	 */
	public CMPPMessage send(CMPPMessage message) throws IOException {
		if (message == null)
			return null;
		CMPP30Transaction t = (CMPP30Transaction) conn.createChild();
		try {
			t.send(message);
			t.waitResponse();
			CMPPMessage rsp = t.getResponse();
			CMPPMessage cmppmessage = rsp;
			return cmppmessage;
		} finally {
			t.close();
		}
	}

	/**
	 * 连接终止的处理，由API使用者实现 SMC连接终止后，需要执行动作的接口
	 */
	public void onTerminate() {
	}

	/**
	 * 对收到消息的处理。由API使用者实现。缺省返回成功收到的响应
	 * 
	 * @param msg
	 *            从短消息中心来的消息。
	 * @return 应该回的响应，由API使用者生成。
	 */
	public CMPPMessage onDeliver(CMPP30DeliverMessage msg) {
	
		SMSSendPlugin.threadeceiveQueueAdd(msg);
		return new CMPP30DeliverRepMessage(msg.getMsgId(), 0);
	}

	/**
	 * 终止连接。调用之后连接将永久不可用。
	 */
	public void close() {
		conn.close();
	}

	public CMPP30Connection getConn() {
		return conn;
	}

	/**
	 * 提供给业务层调用的获取连接状态的方法
	 */
	public String getConnState() {
		return conn.getError();
	}

}
