/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: Subnet.java,v 1.2 2007-04-24 13:24:53 oli Exp $
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

package nettrack.net;

import java.util.HashMap;
import java.util.Map;

public class Subnet
{
  static Map mask2length;
  static Map length2mask;

  static {
    mask2length = new HashMap();
    length2mask = new HashMap();
    for (int i = 0; i <= 32; i++) {
      mask2length.put(new Long((~0L << i) & 0xffffffffL), new Integer(32-i));
    }
    for (int i = 0; i <= 32; i++) {
      length2mask.put(new Integer(32-i), new Long((~0L << i) & 0xffffffffL));
    }
  }

  IpAddr  network;
  Integer length;

  public static void main(String[] a)
    throws Exception
  {
    System.out.println(new Subnet(a[0]));
  }

  /**
   * Creates a new Subnet object.
   *
   * @param network_ Network address
   * @param mask Netmask
   */
  public Subnet(IpAddr network_, IpAddr mask)
    throws IllegalSubnet
  {
    length = (Integer) mask2length.get(new Long(mask.longValue()));
    if (length == null) {
      throw new IllegalSubnet(mask);
    }
    network = network_;
  }

  /**
   * Creates a new Subnet object.
   *
   * @param network_ Network address
   * @param length_ Netmask length
   */
  public Subnet(IpAddr network_, int length_)
    throws IllegalSubnet
  {
    network  = network_;
    if (length_ < 0 || length_ > 32) {
      throw new IllegalSubnet(length_);
    }
    length = new Integer(length_);
  }

  /**
   * Creates a new Subnet object from a string representation
   * (either a.b.c.d/nn or a.b.c.d/e.f.g.h format).
   *
   * @param net String representation
   */
  public Subnet(String net)
    throws IllegalAddress
  {
    if (net == null) {
      throw new IllegalSubnet(net);
    }
    int s = net.indexOf('/');
    if (s == net.length()) {
      throw new IllegalSubnet(net);
    }
    String n = net.substring(0, s);
    String m = net.substring(s+1, net.length());
    network = new IpAddr(n);
    try {
      length = new Integer(m);
      if (length.intValue() < 0 || length.intValue() > 32) {
	throw new IllegalSubnet(length.intValue());
      }
    } catch (NumberFormatException e) {
      IpAddr mask = new IpAddr(m);
      length = (Integer) mask2length.get(new Long(mask.longValue()));
      if (length == null) {
	throw new IllegalSubnet(mask);
      }
    }
  }

  /**
   * Checks if a given address is in this subnet.
   *
   * @param addr Address to check.
   * @return true if the address is in the subnet, false otherwise.
   */
  public boolean containsAddr(IpAddr addr)
  {
    return (network.longValue() == (addr.longValue() & ((Long) length2mask.get(length)).longValue()));
  }

  /**
   * Returns the length of the netmask.
   *
   * @return netmask length
   */
  public int getLength()
  {
    return length.intValue();
  }

  /**
   * Returns the netmask.
   *
   * @return netmask.
   */
  public IpAddr getMask()
  {
    return new IpAddr(((Long) length2mask.get(length)).longValue());
  }

  /**
   * Returns the network address.
   *
   * @return network address
   */
  public IpAddr getNetwork()
  {
    return network;
  }

  /**
   * Returns a string representation of this subnet.
   *
   * @return string representation
   */
  public String toString()
  {
    return network.toString()+"/"+length.toString();
  }

  public int hashCode()
  {
    return network.hashCode();
  }
}
