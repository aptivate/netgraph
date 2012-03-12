/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: Log.java,v 1.2 2007-04-24 13:24:52 oli Exp $
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

public class Log 
{
  private static Logger logger = new ConsoleLogger();

  public final static void setLogger(Logger l)
  {
    logger = l;
  }

  public final static void emergency(Object o)
  {
    logger.log(Const.LOG_EMERG, o);
  }

  public final static void alert(Object o)
  {
    logger.log(Const.LOG_ALERT, o);
  }

  public final static void critical(Object o)
  {
    logger.log(Const.LOG_CRIT, o);
  }

  public final static void error(Object o)
  {
    logger.log(Const.LOG_ERR, o);
  }

  public final static void warning(Object o)
  {
    logger.log(Const.LOG_WARNING, o);
  }

  public final static void notice(Object o)
  {
    logger.log(Const.LOG_NOTICE, o);
  }

  public final static void info(Object o)
  {
    logger.log(Const.LOG_INFO, o);
  }

  public final static void debug(Object o)
  {
    logger.log(Const.LOG_DEBUG, o);
  }
}
