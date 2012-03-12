package org.aptivate.netgraph;

import org.aptivate.netgraph.Applet;

import junit.framework.TestCase;

public class MonitorTest extends TestCase
{
    private Applet m_monitor;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_monitor = new Applet();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MonitorTest.class);
    }
}
