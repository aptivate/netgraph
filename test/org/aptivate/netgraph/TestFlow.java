package org.aptivate.netgraph;

import nettrack.net.IpAddr;
import nettrack.net.netflow.Flow;
import nettrack.util.ByteUtil;

public class TestFlow extends Flow
{
    public int getLength()
    {
        return 0;
    }
    
    public long getSrcAddr()
    {
        return 0x01020304;
    }
    
    public long getDstAddr()
    {
        return 0x05060708;
    }
    
    public long getNextHop()
    {
        return 0x090a0b0c;
    }
    
    public int getInputIf()
    {
        return 0x0d0e;
    }
    
    public int getOutputIf()
    {
        return 0x0f10;
    }
    
    public long getDPkts()
    {
        return 1;
    }
    
    public long getDOctets()
    {
        return 100;
    }
    
    public long getFirst()
    {
        return 0;
    }
    
    public long getLast()
    {
        return 0;
    }
    
    public int getSrcPort()
    {
        return 1234;
    }
    
    public int getDstPort()
    {
        return 5678;
    }
    
    public String toString()
    {
        return "SrcAddr: " + IpAddr.toString(getSrcAddr()) + "\n" +
        "DstAddr: " + IpAddr.toString(getDstAddr()) + "\n" +
        "NextHop: " + IpAddr.toString(getNextHop()) + "\n" +
        "InputIf: " + getInputIf() + "\n" +
        "OutputIf: " + getOutputIf() + "\n" +
        "DPkts: " + getDPkts() + "\n" +
        "DOctets: " + getDOctets() + "\n" +
        "First: " + getFirst() + "\n" +
        "Last: " + getLast() + "\n" +
        "SrcPort: " + getSrcPort() + "\n" +
        "DstPort: " + getDstPort();
    }
    
    public String toShortString()
    {
        return
        IpAddr.toString(getSrcAddr())+"."+getSrcPort()+" -> "+
        IpAddr.toString(getDstAddr())+"."+getDstPort()+" "+
        getDOctets();
    }
}
