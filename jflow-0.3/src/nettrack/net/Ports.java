/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: Ports.java,v 1.2 2007-04-24 13:24:53 oli Exp $
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

public interface Ports
{
  public final static int HTTP     = 80;
  public final static int FTP      = 21;
  public final static int SSH      = 22;
  public final static int TELNET   = 23;
  public final static int SMTP     = 25;
  public final static int TIME     = 37;
  public final static int DOMAIN   = 53;
  public final static int TFTP     = 69;
  public final static int POP      = 110;
  public final static int BOOTPS   = 67;
  public final static int BOOTPC   = 68;
  public final static int IDENT    = 113;
  public final static int SNMP     = 161;
  public final static int SNMPTRAP = 162;
  public final static int HTTPS    = 443;
  public final static int SYSLOG   = 514;
}
