/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: Const.java,v 1.2 2007-04-24 13:24:52 oli Exp $
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

public class Const
{
  public final static int LOG_EMERG   = 0;
  public final static int LOG_ALERT   = 1;
  public final static int LOG_CRIT    = 2;
  public final static int LOG_ERR     = 3;
  public final static int LOG_WARNING = 4;
  public final static int LOG_NOTICE  = 5;
  public final static int LOG_INFO    = 6;
  public final static int LOG_DEBUG   = 7;

  public final static int LOG_KERN   = (0<<3);
  public final static int LOG_USER   = (1<<3);
  public final static int LOG_MAIL   = (2<<3);
  public final static int LOG_DAEMON = (3<<3);
  public final static int LOG_AUTH   = (4<<3);
  public final static int LOG_SYSLOG = (5<<3);
  public final static int LOG_LPR    = (6<<3);
  public final static int LOG_NEWS   = (7<<3);
  public final static int LOG_UUCP   = (8<<3);
  public final static int LOG_CRON   = (15<<3);
  public final static int LOG_LOCAL0 = (16<<3);
  public final static int LOG_LOCAL1 = (17<<3);
  public final static int LOG_LOCAL2 = (18<<3);
  public final static int LOG_LOCAL3 = (19<<3);
  public final static int LOG_LOCAL4 = (20<<3);
  public final static int LOG_LOCAL5 = (21<<3);
  public final static int LOG_LOCAL6 = (22<<3);
  public final static int LOG_LOCAL7 = (23<<3);

  public final static String[] SEVERITY_LABEL =
  {
    "emergency",
    "alert",
    "critical",
    "error",
    "warning",
    "notice",
    "info",
    "debug"
  };
}
