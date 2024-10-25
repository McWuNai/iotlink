package com.huawei.insa2.comm.cmpp30;

import com.huawei.insa2.comm.PMessage;
import com.huawei.insa2.comm.PReader;
import com.huawei.insa2.comm.cmpp.CMPPConstant;
import com.huawei.insa2.comm.cmpp.message.*;
import com.huawei.insa2.comm.cmpp30.message.CMPP30ConnectRepMessage;
import com.huawei.insa2.comm.cmpp30.message.CMPP30DeliverMessage;
import com.huawei.insa2.comm.cmpp30.message.CMPP30SubmitRepMessage;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CMPP30Reader extends PReader {
	protected DataInputStream in;

	public CMPP30Reader(InputStream is) {
		in = new DataInputStream(is);
	}

	public PMessage read() throws IOException {
		int total_Length = in.readInt();
		int command_Id = in.readInt();
		byte buf[] = new byte[total_Length - 8];
		in.readFully(buf);

		switch (command_Id) {
		case CMPPConstant.Connect_Rep_Command_Id:
			return new CMPP30ConnectRepMessage(buf);
		case CMPPConstant.Deliver_Command_Id:
			return new CMPP30DeliverMessage(buf);
		case CMPPConstant.Submit_Rep_Command_Id:
			return new CMPP30SubmitRepMessage(buf);
		case CMPPConstant.Query_Rep_Command_Id:
			return new CMPPQueryRepMessage(buf);
		case CMPPConstant.Cancel_Command_Id:
			return new CMPPCancelRepMessage(buf);
		case CMPPConstant.Active_Test_Rep_Command_Id:
			return new CMPPActiveRepMessage(buf);
		case CMPPConstant.Active_Test_Command_Id:
			return new CMPPActiveMessage(buf);
		case CMPPConstant.Terminate_Command_Id:
			return new CMPPTerminateMessage(buf);
		case CMPPConstant.Terminate_Rep_Command_Id:
			return new CMPPTerminateRepMessage(buf);
		default:
			return null;
		}
	}

}
