/**
 * Copyright (C) 2001-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: Filter.java,v 1.2 2007-04-24 13:24:54 oli Exp $
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

public interface Filter
{
  /**
   * Filters a flow.
   *
   * @param flow Flow to filter
   * @return true if the flow can be filtered, false otherwise
   */
  public boolean filter(Flow flow);

  /**
   * Destroys this filter.
   */
  public void destroy();
}
