/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: IpAddr.java,v 1.2 2007-04-24 13:24:53 oli Exp $
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

public class IpAddr
{
  protected int[] ip = new int[4];

  public IpAddr(String ip_)
    throws IllegalAddress
  {
    if (ip_.length() == 4) {
      char[] i = ip_.toCharArray();
      ip[0] = i[0]; ip[1] = i[1];
      ip[2] = i[2]; ip[3] = i[3];
    } else {
      StringTokenizer tok = new StringTokenizer(ip_, ".");
      
      try{
	ip[0] = Integer.parseInt(tok.nextToken());
	ip[1] = Integer.parseInt(tok.nextToken());
	ip[2] = Integer.parseInt(tok.nextToken());
	ip[3] = Integer.parseInt(tok.nextToken());
      } catch (NumberFormatException e) {
      } catch (NoSuchElementException e) {
      }
    }
  }

  public IpAddr(int[] ip_)
  {
    ip[0] = ip_[0];
    ip[1] = ip_[1];
    ip[2] = ip_[2];
    ip[3] = ip_[3];
  }

  public IpAddr(int a, int b, int c, int d)
  {
    ip[0] = a;
    ip[1] = b;
    ip[2] = c;
    ip[3] = d;
  }

  public IpAddr(int ip_)
  {
    ip[0] = (ip_ >> 24) & 0xff;
    ip[1] = (ip_ >> 16) & 0xff;
    ip[2] = (ip_ >>  8) & 0xff;
    ip[3] =  ip_        & 0xff;
  }

  public IpAddr(long ip_)
  {
    long2ip(ip_, ip);
  }

  protected static void long2ip(long ip_, int[] ip)
  {
    ip[0] = (int) (ip_ >> 24) & 0xff;
    ip[1] = (int) (ip_ >> 16) & 0xff;
    ip[2] = (int) (ip_ >>  8) & 0xff;
    ip[3] = (int)  ip_        & 0xff;
  }

  public int[] getIntArray()
  {
    return ip;
  }

  public int hashCode()
  {
    return (int) longValue();
  }

  public boolean equals(Object o)
  {
    if (o instanceof IpAddr) {
      IpAddr ip_ = (IpAddr) o;

      return
	(ip[0] == ip_.ip[0]) &&
	(ip[1] == ip_.ip[1]) &&
	(ip[2] == ip_.ip[2]) &&
	(ip[3] == ip_.ip[3]);
    }
    return false;
  }

  public long longValue()
  {
    return  ((long) ((((ip[0] << 8) | ip[1]) << 8) | ip[2]) << 8) | ip[3];
  }

  public String toString()
  {
    return ""+ip[0]+"."+ip[1]+"."+ip[2]+"."+ip[3];
  }

  public InetAddress inetAddr()
  {
    try {
      return InetAddress.getByName(toString());
    } catch (UnknownHostException e) {
      return null;
    }
  }

  public static String toString(long ip_)
  {
    int ip[] = new int[4];
    long2ip(ip_, ip);
    return ip[0]+"."+ip[1]+"."+ip[2]+"."+ip[3];
  }
}
