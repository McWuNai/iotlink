package com.huawei.insa2.comm.smpp;

import com.huawei.insa2.comm.PMessage;
import com.huawei.insa2.comm.PReader;
import com.huawei.insa2.comm.smpp.message.*;
import com.huawei.insa2.util.TypeConvert;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SMPP为变长PDU协议
 *
 */
public class SMPPReader extends PReader {

	public SMPPReader(InputStream is) {
		in = new DataInputStream(is);
	}

	public PMessage read() throws IOException {
		int total_Length = in.readInt();
		int command_Id = in.readInt();

		byte buf[] = new byte[total_Length];
		TypeConvert.int2byte(total_Length, buf, 0);
		TypeConvert.int2byte(command_Id, buf, 4);

		in.readFully(buf, 8, total_Length - 8);

		switch (command_Id) {
		case SMPPConstant.Bind_Receiver_Rep_Command_Id:
		case SMPPConstant.Bind_Transmitter_Rep_Command_Id:
			return new SMPPLoginRespMessage(buf);
		case SMPPConstant.Deliver_Command_Id:
			return new SMPPDeliverMessage(buf);
		case SMPPConstant.Submit_Rep_Command_Id:
			return new SMPPSubmitRespMessage(buf);
		case SMPPConstant.Enquire_Link_Command_Id:
			return new SMPPEnquireLinkMessage(buf);
		case SMPPConstant.Deliver_Rep_Command_Id:
			return new SMPPEnquireLinkRespMessage(buf);
		case SMPPConstant.Unbind_Command_Id:
			return new SMPPUnbindMessage(buf);
		case SMPPConstant.Unbind_Rep_Command_Id:
			return new SMPPUnbindRespMessage(buf);
		default:
			return null;
		}
	}

	protected DataInputStream in;
}
