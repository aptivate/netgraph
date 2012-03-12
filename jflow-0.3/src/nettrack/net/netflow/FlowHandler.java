/**
 * Copyright (C) 2001-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: FlowHandler.java,v 1.2 2007-04-24 13:24:54 oli Exp $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import nettrack.util.AsyncBoundedFifo;

/**
 * A FlowHandler is a thread that accepts flow packets from a certain
 * destination and of a certain version. No two destinations may
 * deliver packets to the same FlowHandler.
 */

public abstract class FlowHandler
  extends AsyncBoundedFifo
{
  protected FlowHeader   header;
  protected Flow         flow;

  private   Filter[]     filters       = new Filter[0];
  private   Accountant[] accountants   = new Accountant[0];

  private   long         totalPackets  = 0;
  private   long         validPackets  = 0;
  private   long         totalFlows    = 0;
  private   long         filteredFlows = 0;

  private   InetAddress  sourceAddr;

  /**
   * Constructs a FlowHandler object.
   *
   * @param source Source of the netflow packets.
   * @param fifoSize Size of the FIFO.
   */
  public FlowHandler(InetAddress source, int fifosize)
  {
    super(fifosize);
    sourceAddr = source;
  }

  /**
   * Constructs a FlowHandler object by taking the parameters from a
   * Map object.
   *
   * @param configuration Map containing the configuration parameters.
   */
  public FlowHandler(Map configuration)
    throws Exception
  {
    this(InetAddress.getByName((String) configuration.get("source")),
	 Integer.parseInt((String) configuration.get("fifosize")));
  }

  /**
   * Destroys this handler as well as all filters and accountants.
   */
  public void destroy()
  {
    super.destroy();

    for (int i = 0; i < filters.length; i++) {
      filters[i].destroy();
    }
    for (int i = 0; i < accountants.length; i++) {
      accountants[i].destroy();
    }
  }

  /**
   * Adds a Filter.
   *
   * @param f Filter to add.
   */
  public void addFilter(Filter f)
  {
    Filter[] nf = new Filter[filters.length+1];
    int i;
    for (i = 0; i < filters.length; i++) {
      nf[i] = filters[i];
    }
    nf[i] = f;
    filters = nf;
  }

  /**
   * Returns the installed filters
   *
   * @return Installed filters
   */
  public Iterator getFilters()
  {
    return new Iterator() {
	int pos = 0;
	public boolean hasNext() {
	  return pos < filters.length;
	}
	public Object next() {
	  return filters[pos++];
	}
	public void remove() {
	  throw new UnsupportedOperationException("remove not supported");
	}
      };
  }

  /**
   * Adds an Accountant.
   *
   * @param a Accountant to add.
   */
  public void addAccountant(Accountant a)
  {
    Accountant[] na = new Accountant[accountants.length+1];
    int i;
    for (i = 0; i < accountants.length; i++) {
      na[i] = accountants[i];
    }
    na[i] = a;
    accountants = na;
  }

  /**
   * Returns the installed acountants.
   *
   * @return Installed accountants
   */
  public Iterator getAccountants()
  {
    return new Iterator() {
	int pos = 0;
	public boolean hasNext() {
	  return pos < accountants.length;
	}
	public Object next() {
	  return accountants[pos++];
	}
	public void remove() {
	  throw new UnsupportedOperationException("remove not supported");
	}
      };
  }

  public boolean handle(Object element)
  {
    byte[] data = (byte[]) element;
    header.setData(data);

    totalPackets++;

    if (!checkHeader(header)) {
      return true;
    }

    validPackets++;

    for (int i = 0; i < header.getCount(); i++) {
      flow.setData(data, getHeaderSize() + i*getFlowSize());
      totalFlows++;

      // Try to filter the packet
      boolean filtered = false;
      for (int j = 0; !filtered && j < filters.length; j++) {
	filtered = filters[j].filter(flow);
      }

      if (filtered) {
	filteredFlows++;
	continue;
      }

      // Run the packet through the accountants
      for (int j = 0; j < accountants.length; j++) {
	accountants[j].account(flow);
      }
    }

    return true;
  }

  /**
   * Returns the address this handler accepts flows from.
   *
   * @return Address.
   */
  public InetAddress getSource()
  {
    return sourceAddr;
  }

  /**
   * Returns a map containing several statistics.
   *
   * @return Map containing statistics.
   */
  public Map getStatistics()
  {
    Map statistics = new HashMap();
    statistics.put("total packets",  new Long(totalPackets));
    statistics.put("valid packets",  new Long(validPackets));
    statistics.put("total flows",    new Long(totalFlows));
    statistics.put("filtered flows", new Long(filteredFlows));
    return statistics;
  }

  /**
   * Returns the version of the flows this handler handles.
   *
   * @return Flow version.
   */
  public abstract int getVersion();

  /**
   * Returns the header size.   
   *
   * @return Header size.
   */
  public abstract int getHeaderSize();

  /**
   * Returns the size of one flow.
   *
   * @return Flow size.
   */
  public abstract int getFlowSize();

  /**
   * Returns the max. number of flows per packet.
   *
   * @return Flow number per packet.
   */
  public abstract int getMaxFlowsPerPacket();

  /**
   * Returns the max. packet size.
   *
   * @return Max. packet size.
   */
  public int getMaxPacketSize()
  {
    return getHeaderSize() + getFlowSize()*getMaxFlowsPerPacket();
  }

  /**
   * Checks if the header is valid.
   *
   * @param header Header to check.
   * @return False if the header is invalid.
   */
  public abstract boolean checkHeader(FlowHeader header);
}
