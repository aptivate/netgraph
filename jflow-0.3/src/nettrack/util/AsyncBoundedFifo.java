/**
 * Copyright (C) 2000-2007 Oliver Hitz <oliver@net-track.ch>
 *
 * $Id: AsyncBoundedFifo.java,v 1.2 2007-04-24 13:24:55 oli Exp $
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

public abstract class AsyncBoundedFifo
  extends Thread
{
  int nextPut;
  int nextGet;
  boolean empty;
  boolean running = true;
  Object[] queue;

  /** 
   * Constructs a Fifo with a certain size.
   *
   * @param queueSize Size of the queue.
   */
  public AsyncBoundedFifo(int queueSize)
  {
    queue = new Object[queueSize];
    empty = true;
    nextPut = 0;
    nextGet = 0;
    start();
  }

  /** 
   * Returns the length of the queue.
   *
   * @return Length of the queue.
   */
  public int getQueueSize()
  {
    return queue.length;
  }

  /** 
   * Puts an element into the queue.
   *
   * @param element Element to put
   */
  public synchronized void put(Object element)
  {
    if (!empty && (nextPut == nextGet)) {
      try {
	wait();
      } catch(InterruptedException e) {
	System.err.println(e);
      }
    }

    queue[nextPut++] = element;
    nextPut %= queue.length;
    empty = false;

    notify();
  }

  /** 
   * Calls the handle method if there is something in the queue.
   */
  public synchronized void run()
  {
    boolean go = true;

    while (go && running) {
      if (empty) {
        try {
	  wait();
	} catch(InterruptedException e) {
	  System.err.println(e);
	}
      }

      if (running) {
	Object element = queue[nextGet++];
	nextGet %= queue.length;
	empty = (nextGet == nextPut);
	
	notify();
	
	go = handle(element);
      }
    }
  }

  /** 
   * Destroys the FIFO.
   */
  public synchronized void destroy()
  {
    running = false;
    notify();
  }

  /**
   * Handles every element given to the queue.
   *
   * @param element Element to handle.
   * @return False if the thread should be terminated.
   */
  public abstract boolean handle(Object element);
}
