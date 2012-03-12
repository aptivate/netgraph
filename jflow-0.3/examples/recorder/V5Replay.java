/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: V5Replay.java,v 1.2 2007-04-24 13:24:51 oli Exp $
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

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

import nettrack.net.netflow.*;
import nettrack.util.ByteUtil;

public class V5Replay
{
  public V5Replay(ArrayList flows, InetAddress host, int port, int timing, boolean resequence)
    throws Exception
  {
    // Send data to host
    DatagramSocket socket = new DatagramSocket();
    int sequence = 0;
    
    while (true) {
      Iterator i = flows.iterator();
      while (i.hasNext()) {
	byte[] b = (byte[]) i.next();

	if (resequence) {
	  ByteUtil.setULong(b, 16, sequence);
	  sequence += ByteUtil.getUShort(b, 2);
	}

	DatagramPacket p = new DatagramPacket(b, b.length, host, port);
	socket.send(p);

	try {
	  Thread.currentThread().sleep(0, timing);
	} catch (InterruptedException e) {
	}
      }
    }
  }

  public static void main(String[] arg) 
    throws Exception
  {
    if (arg.length != 4) {
      System.err.println("Usage: java "+
			 V5Replay.class.getName()+
			 " <filename> <host> <port> <timing (ms)>");
      System.exit(1);
    }

    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arg[0]));

    // Read in all the flows
    ArrayList flows = new ArrayList();
    try {
      while (true) {
	int length = ois.readInt();
	byte[] buf = new byte[length];
	ois.readFully(buf);
	flows.add(buf);
      }
    } catch (EOFException e) {
    }

    InetAddress host = InetAddress.getByName(arg[1]);
    int port = Integer.parseInt(arg[2]);
    int timing = Integer.parseInt(arg[3]);

    new V5Replay(flows, host, port, timing, true);
  }
}
