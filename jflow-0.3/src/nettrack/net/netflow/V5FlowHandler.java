/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: V5FlowHandler.java,v 1.2 2007-04-24 13:24:54 oli Exp $
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

import java.net.InetAddress;
import java.util.Map;

public class V5FlowHandler
  extends FlowHandler
{
  public V5FlowHandler(InetAddress source, int fifosize)
  {
    super(source, fifosize);
    header = new V5FlowHeader();
    flow = new V5Flow();
  }

  public int getVersion()
  {
    return 5;
  }

  public int getHeaderSize()
  {
    return 24;
  }

  public int getFlowSize()
  {
    return 48;
  }

  public int getMaxFlowsPerPacket()
  {
    return 30;
  }

  private long nextFlow    = -1;

  private long missedFlows = 0;
  private long aheadTimes  = 0;

  public Map getStatistics()
  {
    Map statistics = super.getStatistics();
    statistics.put("missed flows",          new Long(missedFlows));
    statistics.put("number of times ahead", new Long(aheadTimes));
    return statistics;
  }

  public boolean checkHeader(FlowHeader header)
  {
    long flowNumber = ((V5FlowHeader) header).getFlowSequence();

    if (nextFlow != -1 && nextFlow != flowNumber) {
      if (flowNumber > nextFlow) {
	missedFlows += flowNumber - nextFlow;
      } else {
	// We are ahead, the router may have been reset
	aheadTimes++;
      }
    }

    nextFlow = flowNumber + header.getCount();

    return true;
  }

  public String toString()
  {
    return "Version 5 NetFlow Handler, listening to flows from "+getSource();
  }
}
