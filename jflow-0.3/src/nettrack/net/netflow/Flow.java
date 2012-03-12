/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: Flow.java,v 1.2 2007-04-24 13:24:54 oli Exp $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package nettrack.net.netflow;

import nettrack.net.IpAddr;
import nettrack.util.ByteUtil;

/**
 * Netflow flow.
 */

public abstract class Flow
{
  protected byte[] data;
  protected int offset;

  public void setData(byte[] data, int offset)
  {
    this.data = data;
    this.offset = offset;
  }

  public abstract int getLength();

  public long getSrcAddr()
  {
    return ByteUtil.getULong(data, offset+0);
  }

  public long getDstAddr()
  {
    return ByteUtil.getULong(data, offset+4);
  }

  public long getNextHop()
  {
    return ByteUtil.getULong(data, offset+8);
  }

  public int getInputIf()
  {
    return ByteUtil.getUShort(data, offset+12);
  }

  public int getOutputIf()
  {
    return ByteUtil.getUShort(data, offset+14);
  }

  public long getDPkts()
  {
    return ByteUtil.getULong(data, offset+16);
  }

  public long getDOctets()
  {
    return ByteUtil.getULong(data, offset+20);
  }

  public long getFirst()
  {
    return ByteUtil.getULong(data, offset+24);
  }

  public long getLast()
  {
    return ByteUtil.getULong(data, offset+28);
  }

  public int getSrcPort()
  {
    return ByteUtil.getUShort(data, offset+32);
  }

  public int getDstPort()
  {
    return ByteUtil.getUShort(data, offset+34);
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
