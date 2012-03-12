/**
 * Copyright (C) 2005-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: V5IpAccount.java,v 1.2 2007-04-24 13:24:49 oli Exp $
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

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import nettrack.net.IllegalAddress;
import nettrack.net.IpAddr;
import nettrack.net.Subnet;
import nettrack.net.IllegalSubnet;
import nettrack.net.netflow.*;

public class V5IpAccount
{
  final static int INTERVAL = 5;

  static class SubnetFilter
    implements Filter
  {
    Subnet subnet;

    public SubnetFilter(Subnet s)
    {
      subnet = s;
    }

    public boolean filter(Flow f)
    {
      // Filter if neither the source nor the destination are in our
      // subnet.
      return (!subnet.containsAddr(new IpAddr(f.getSrcAddr())) &&
	      !subnet.containsAddr(new IpAddr(f.getDstAddr())));
    }

    public void destroy()
    {
    }
  }

  static class IpAccountant
    implements Accountant
  {
    long start;
    long stop;

    long[][] in;
    long[][] out;

    int current;

    public IpAccountant(Subnet s)
    {
      start = s.getNetwork().longValue();
      stop = start + (0x100000000L - s.getMask().longValue());

      in = new long[2][];
      in[0] = new long[(int) (stop - start)];
      in[1] = new long[(int) (stop - start)];

      out = new long[2][];
      out[0] = new long[(int) (stop - start)];
      out[1] = new long[(int) (stop - start)];

      current = 0;
    }

    public void account(Flow f)
    {
      long src = f.getSrcAddr();
      long dst = f.getDstAddr();
      if (src >= start && src < stop) {
	out[current][(int) (src - start)] += f.getDOctets();
      }
      if (dst >= start && dst < stop) {
	in[current][(int) (dst - start)] += f.getDOctets();
      }
    }

    public void destroy()
    {
    }

    public void report()
    {
      int cur = current;

      // Swap buffers
      current = 1 - current;

      String s = "";
      for (long i = start; i < stop; i++) {
	int n = (int) (i - start);

	System.out.println((new IpAddr(i)).toString()+" "+
			   in[cur][n]+" bytes in ("+((in[cur][n]*8) / INTERVAL)+" bits/s) "+
			   out[cur][n]+" bytes out ("+((out[cur][n]*8) / INTERVAL)+" bits/s)");

	// Reset counters
	in[cur][n] = 0;
	out[cur][n] = 0;
      }
    }
  }

  public static void main(String[] arg)
    throws SocketException, UnknownHostException, IllegalAddress
  {
    if (arg.length != 2) {
      System.err.println("Usage: java "+
			 V5IpAccount.class.getName()+
			 " sourcehost subnet/mask");
      System.exit(1);
    }

    InetAddress source = InetAddress.getByName(arg[0]);
    Subnet subnet = new Subnet(arg[1]);

    Collector collector = new Collector(2055);
    V5FlowHandler handler = new V5FlowHandler(source, 100);

    IpAccountant ipaccountant = new IpAccountant(subnet);
    handler.addFilter(new SubnetFilter(subnet));
    handler.addAccountant(ipaccountant);

    collector.addFlowHandler(handler);
    collector.start();

    while (true) {
      Thread thread = Thread.currentThread();
      try {
	thread.sleep(INTERVAL*1000);
      } catch (InterruptedException e) {
      }
      ipaccountant.report();
    }
  }
}
