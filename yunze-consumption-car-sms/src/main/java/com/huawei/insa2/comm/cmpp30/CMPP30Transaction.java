package com.huawei.insa2.comm.cmpp30;

import com.alibaba.fastjson.JSON;
import com.huawei.insa2.comm.PException;
import com.huawei.insa2.comm.PLayer;
import com.huawei.insa2.comm.PMessage;
import com.huawei.insa2.comm.cmpp.CMPPConstant;
import com.huawei.insa2.comm.cmpp.message.CMPPMessage;

public class CMPP30Transaction extends PLayer {
	private CMPPMessage receive;
	private int sequenceId;

	public CMPP30Transaction(PLayer connection) {
		super(connection);
		sequenceId = super.id;
	}

	public synchronized void onReceive(PMessage msg) {
		receive = (CMPPMessage) msg;
		sequenceId = receive.getSequenceId();
		if (CMPPConstant.debug)
			System.out.println("onReceive:" + JSON.toJSONString(receive));
		notifyAll();
	}
	
	public void send(PMessage message) throws PException {
		CMPPMessage mes = (CMPPMessage) message;
		mes.setSequenceId(sequenceId);
		super.parent.send(message);

		if (CMPPConstant.debug)
			System.out.println("send:" + JSON.toJSONString(mes));
	}

	public CMPPMessage getResponse() {
		return receive;
	}

	public synchronized void waitResponse() {
		if (receive == null)
			try {
				wait(((CMPP30Connection) super.parent).getTransactionTimeout());
			} catch (InterruptedException interruptedexception) {
			}
	}

}
