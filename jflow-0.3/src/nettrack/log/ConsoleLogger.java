/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: ConsoleLogger.java,v 1.2 2007-04-24 13:24:52 oli Exp $
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

package nettrack.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class ConsoleLogger 
  implements Logger
{
  public void log(int severity, Object o)
  {
    StringBuffer msg = new StringBuffer();
    msg.append(new Date());
    msg.append(" ");
    msg.append(Const.SEVERITY_LABEL[severity]);
    msg.append(" from ");
    msg.append(Thread.currentThread().getName());
    msg.append(": ");

    if (o instanceof Throwable && o != null) {
      StringWriter sw = new StringWriter();
      ((Throwable) o).printStackTrace(new PrintWriter(sw));
      msg.append(sw.toString());
    } else {
      msg.append(o);
    }

    System.err.println(msg);
  }
}
