/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: SyslogLogger.java,v 1.2 2007-04-24 13:24:52 oli Exp $
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

package nettrack.log;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import nettrack.net.Ports;

public class SyslogLogger 
  implements Logger
{
  private InetAddress      destAddr;
  private String           identity    = "Java";
  private int              facility    = Const.LOG_LOCAL0;

  private DatagramSocket   socket;
  private DatagramPacket   packet;

  private final static int PACKET_SIZE = 1024;

  public SyslogLogger(InetAddress dest) 
    throws SocketException
  {
    destAddr = dest;

    socket = new DatagramSocket();
    packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
  }

  public SyslogLogger(InetAddress dest, String id, int fac) 
    throws SocketException
  {
    this(dest);

    identity = id;
    facility = fac;
  }

  public synchronized void log(int severity, Object o)
  {
    try {
      byte[] data = packet.getData();
      int dataLength = 0;
      int i;
      
      byte[] pri = ((String) "<"+(facility+severity)+">").getBytes();
      i = 0;
      while (i < pri.length && dataLength < PACKET_SIZE) {
	data[dataLength++] = pri[i++];
      }

      byte[] id = (identity+"["+Thread.currentThread().getName()+"]: ").getBytes();
      i = 0;
      while (i < id.length && dataLength < PACKET_SIZE) {
	data[dataLength++] = id[i++];
      }
      
      byte[] message = o.toString().getBytes();
      i = 0;
      while (i < message.length && dataLength < PACKET_SIZE) {
	data[dataLength++] = message[i++];
      }
      
      packet.setLength(dataLength);
      packet.setAddress(destAddr);
      packet.setPort(Ports.SYSLOG);
      socket.send(packet);
    } catch (IOException e) {
      e.printStackTrace(new PrintStream(System.err));
    }
  }

  public static void main(String[] arg) 
    throws Exception
  {
    if (arg.length != 2) {
      System.err.println("Usage: java "+
			 SyslogLogger.class.getName()+
			 " <host> <message>");
      System.exit(1);
    }

    InetAddress host    = InetAddress.getByName(arg[0]);
    String      message = arg[1];

    Log.setLogger(new SyslogLogger(host));
    Log.info(message);
  }
}
