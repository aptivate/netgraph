/**
 * Copyright (C) 2005-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: V5Printer.java,v 1.2 2007-04-24 13:24:50 oli Exp $
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

import nettrack.net.netflow.*;

public class V5Printer
{
  static class PrintAccountant
    implements Accountant
  {
    public void account(Flow f)
    {
      System.out.println(f.toShortString());
    }

    public void destroy()
    {
    }
  }

  public static void main(String[] arg)
    throws SocketException, UnknownHostException
  {
    if (arg.length != 1) {
      System.err.println("Usage: java "+
			 V5Printer.class.getName()+
			 " sourcehost");
      System.exit(1);
    }

    InetAddress source = InetAddress.getByName(arg[0]);

    Collector collector = new Collector(2055);
    V5FlowHandler handler = new V5FlowHandler(source, 100);
    handler.addAccountant(new PrintAccountant());
    collector.addFlowHandler(handler);
    collector.start();
  }
}
