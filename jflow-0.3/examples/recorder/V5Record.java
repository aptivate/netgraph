/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: V5Record.java,v 1.2 2007-04-24 13:24:51 oli Exp $
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import nettrack.net.netflow.*;

public class V5Record
{
  public V5Record(int port, ObjectOutputStream os)
    throws IOException
  {
    FlowHandler handler = new V5FlowHandler(InetAddress.getLocalHost(), 1);
    byte[] data = new byte[handler.getMaxPacketSize()];

    DatagramSocket socket = new DatagramSocket(port);
    DatagramPacket packet = new DatagramPacket(data, data.length);

    while (true) {
      packet.setLength(data.length);
      socket.receive(packet);
      
      os.writeInt(data.length);
      os.write(data);
      os.flush();
      System.out.print(".");
      System.out.flush();
    }
  }

  public static void main(String[] arg)
       throws IOException
  {
    if (arg.length != 1) {
      System.err.println("Usage: java "+
			 V5Record.class.getName()+
			 " <filename>");
      System.exit(1);
    }

    FileOutputStream ostream = new FileOutputStream(arg[0]);
    new V5Record(2055, new ObjectOutputStream(ostream));
  }
}
