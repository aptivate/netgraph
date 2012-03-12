/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
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

import nettrack.util.ByteUtil;

/**
 * Netflow flow header
 *
 * @author $Author: oli $
 * @version $Revision: 1.2 $
 */

public class FlowHeader {

  protected byte[] data;

  public void setData(byte[] data) {
    this.data = data;
  }

  public int getVersion() {
    return ByteUtil.getUShort(data, 0);
  }

  public int getCount() {
    return ByteUtil.getUShort(data, 2);
  }

  public long getSysUptime() {
    return ByteUtil.getULong(data, 4);
  }

  public long getUnixSecs() {
    return ByteUtil.getULong(data, 8);
  }

  public long getUnixNsecs() {
    return ByteUtil.getULong(data, 12);
  }

  public String toShortString() {
    return "Header [v="+getVersion()+",c="+getCount()+"]";
  }
}

/**
 * $Log: FlowHeader.java,v $
 * Revision 1.2  2007-04-24 13:24:54  oli
 * Changed license to LGPL.
 *
 * Revision 1.1  2005/04/21 15:45:04  oli
 * Initial checkin.
 *
 * Revision 1.1  2003/07/16 07:14:45  oli
 * Importing sources.
 *
 * Revision 1.1.1.1  2002/04/08 22:17:33  cvsadmin
 * Imported sources
 *
 * Revision 1.2  2000/11/01 23:35:11  oli
 * Redesign.
 *
 * Revision 1.1  2000/10/26 19:43:04  oli
 * Initial checkin.
 *
 */
