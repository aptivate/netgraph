/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: Collector.java,v 1.3 2007-04-24 13:24:54 oli Exp $
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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import nettrack.log.Log;

public class Collector
  extends Thread
{
  protected InetAddress      localAddr;
  protected int              localPort;
  protected DatagramSocket   socket;

  protected FlowHandler[]    handlers  = new FlowHandler[0];
  HashMap handlersIndex = new HashMap();

  protected int              maxPacketSize;
  protected DatagramPacket[] packets;
  protected int              maxPackets;
  protected int              nextPacket;

  boolean running = true;

  /**
   * Constructs a flow collector listening on a certain port.
   *
   * @param port Local port to listen on.
   */
  public Collector(int port) 
    throws SocketException
  {
    this(null, port);
  }

  /**
   * Constructs a flow collector listening on a certain local address
   * and port.
   *
   * @param addr Local address to listen on.
   * @param port Local port to listen on.
   */
  public Collector(InetAddress addr, int port) 
    throws SocketException
  {
    setName("NetFlow Collector");

    localAddr = addr;
    localPort = port;

    if (localAddr == null) {
      socket = new DatagramSocket(localPort);
    } else {
      socket = new DatagramSocket(localPort, localAddr);
    }
    socket.setSoTimeout(1000);
  }

  /**
   * Adds a flow handler to this netflow collector.
   *
   * @param handler Flow handler to add.
   */
  public void addFlowHandler(FlowHandler handler)
  {
    FlowHandler[] nh = new FlowHandler[handlers.length+1];
    int i;
    for (i = 0; i < handlers.length; i++) {
      nh[i] = handlers[i];
    }
    nh[i] = handler;
    handlers = nh;

    handlersIndex.put(handler.getSource(), handler);

    maxPacketSize = Math.max(maxPacketSize, handler.getMaxPacketSize());
    maxPackets += handler.getQueueSize();

    // Create enough maxPackets so we can fill up every queue
    packets = new DatagramPacket[maxPackets+1];

    for (i = 0; i < maxPackets+1; i++) {
      byte[] data = new byte[maxPacketSize];
      packets[i] = new DatagramPacket(data, maxPacketSize);
    }
  }

  /**
   * Returns an Iterator over the installed flow handlers.
   *
   * @return Iterator over flow handlers.
   */
  public Iterator getFlowHandlers()
  {
    return new Iterator() {
	int pos = 0;
	public boolean hasNext() {
	  return pos < handlers.length;
	}
	public Object next() {
	  return handlers[pos++];
	}
	public void remove() {
	  throw new UnsupportedOperationException("remove not supported");
	}
      };
  }

  /**
   * Main loop. Gets packets and forwards them to the appropriate
   * handler.
   */
  public void run()
  {
    DatagramPacket packet;
    InetAddress source;
    HashSet warned = new HashSet();

    while (running) {
      try {
	// Get the next packet
	packet = packets[nextPacket++];
	nextPacket %= packets.length;

	packet.setLength(maxPacketSize);
	socket.receive(packet);

	if (running) {
	  // Find the correct handler and pass the packet along
	  source = packet.getAddress();

	  FlowHandler h = (FlowHandler) handlersIndex.get(source);
	  if (h != null) {
	    h.put(packet.getData());
	  } else {
	    if (!warned.contains(source)) {
	      warned.add(source);
	      Log.warning("Packet from unknown source: "+source.getHostAddress());
	    }
	  }
	}
      } catch (SocketTimeoutException e) {
	// Ignore, just retry.
      } catch (IOException e) {
	// Thrown e.g. when the destroy() method is called.
      }
    }
  }

  /**
   * Terminates the collector and all attached flow handlers.
   */
  public void destroy()
  {
    // Close the socket to end a receive call currently in progress.
    running = false;
    socket.close();

    // Destroy all handlers.
    for (int i = 0; i < handlers.length; i++) {
      handlers[i].destroy();
    }
  }

  /**
   * Test program.
   */
  public static void main(String[] arg)
    throws Exception
  {
    if (arg.length != 2) {
      System.err.println("Usage: java "+
			 Collector.class.getName()+
			 " <host> <port>");
      System.exit(1);
    }

    InetAddress source = InetAddress.getByName(arg[0]);
    int         port = Integer.parseInt(arg[1]);

    // Create a version 5 flow handler with an input queue of 10 packets
    FlowHandler handler = new V5FlowHandler(source, 10);

    // Add a dummy filter
    handler.addFilter(new Filter() {
	public boolean filter(Flow f) {
	  return true;
	}
	public void destroy() {
	}
	public Map getStatistics() {
	  return new HashMap();
	}
      });

    // Set up the flow collector on the specified port
    Collector   collector = new Collector(port);
    collector.addFlowHandler(handler);

    // Start it!
    collector.start();
  }
}
