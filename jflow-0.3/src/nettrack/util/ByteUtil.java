/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: ByteUtil.java,v 1.2 2007-04-24 13:24:55 oli Exp $
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

package nettrack.util;

public class ByteUtil
{
  public final static int getUByte(byte[] data, int offset)
  {
    return data[offset] & 0xff;
  }

  public final static int getUShort(byte[] data, int offset)
  {
    int b0 = getUByte(data, offset + 0);
    int b1 = getUByte(data, offset + 1);
    return (b0 << 8) + b1;
  }

  public final static long getULong(byte[] data, int offset)
  {
    long b0 = getUByte(data, offset + 0);
    long b1 = getUByte(data, offset + 1);
    long b2 = getUByte(data, offset + 2);
    long b3 = getUByte(data, offset + 3);
    return (((((b0 << 8) + b1) << 8) + b2) << 8) + b3;
  }

  public final static void setUByte(byte[] data, int offset, int value)
  {
    data[offset] = (byte) (value & 0xff);
  }

  public final static void setUShort(byte[] data, int offset, int value)
  {
    data[offset+1] = (byte) (value & 0xff);
    value >>= 8;
    data[offset] = (byte) (value & 0xff);
  }

  public final static void setULong(byte[] data, int offset, long value)
  {
    data[offset+3] = (byte) (value & 0xff);
    value >>= 8;
    data[offset+2] = (byte) (value & 0xff);
    value >>= 8;
    data[offset+1] = (byte) (value & 0xff);
    value >>= 8;
    data[offset] = (byte) (value & 0xff);
  }
}
