// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SGIPReader.java

package com.huawei.insa2.comm.sgip;

import com.huawei.insa2.comm.PMessage;
import com.huawei.insa2.comm.PReader;
import com.huawei.insa2.comm.sgip.message.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SGIPReader extends PReader
{

    public SGIPReader(InputStream is)
    {
        in = new DataInputStream(is);
    }

    public PMessage read()
        throws IOException
    {
    	try {
        int total_Length = in.readInt();
        int command_Id = in.readInt();
        byte buf[] = new byte[total_Length - 8];
        in.readFully(buf);
        if(command_Id == 0x80000001)
            return new SGIPBindRepMessage(buf);
        if(command_Id == 1)
            return new SGIPBindMessage(buf);
        if(command_Id == 4)
            return new SGIPDeliverMessage(buf);
        if(command_Id == 0x80000003)
            return new SGIPSubmitRepMessage(buf);
        if(command_Id == 5)
            return new SGIPReportMessage(buf);
        if(command_Id == 17)
            return new SGIPUserReportMessage(buf);
        if(command_Id == 2)
            return new SGIPUnbindMessage(buf);
        if(command_Id == 0x80000002)
            return new SGIPUnbindRepMessage(buf);
        else{
//        	if (SGIPConstant.debug)
//				Debug.dump("解包或读取错误！");
            return null;
        }
        }catch (Exception ex){
//			if (SGIPConstant.debug)
//				Debug.dump("没有数据需要处理！");
    		return null;
    	}
    }

    protected DataInputStream in;
}
