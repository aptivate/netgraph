// Based on JRobin demo applet

package org.aptivate.netgraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import nettrack.net.IpAddr;
import nettrack.net.netflow.Accountant;
import nettrack.net.netflow.Collector;
import nettrack.net.netflow.Flow;
import nettrack.net.netflow.V5Flow;
import nettrack.net.netflow.V5FlowHandler;

import org.jrobin.core.DsDef;
import org.jrobin.core.FetchData;
import org.jrobin.core.FetchRequest;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.jrobin.core.Util;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.TimeAxisUnit;

public class Applet extends JApplet
{
    private static final Logger m_LOG = Logger.getLogger(Applet.class.getName());
    public static final String RRD_PATH = "random";
    static final long serialVersionUID = 1;
    
    public enum Mode
    {
    	PING("Ping"),
    	NETFLOW("Netflow");
    	
    	public final String label;
    	
    	Mode(String label)
    	{
    		this.label = label;
    	}
    }
    
    public static void main(final String[] args) 
    {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater
        (
                new Runnable() 
                {
                    public void run() 
                    {
                        createAndShowGUI();
                        if (args.length == 1)
                        {
                        	m_Applet.m_TargetAddrBox.setText(args[0]);
                        }
                    }
                }
        );
    }
    
    private static Applet m_Applet;
    
    public static void createAndShowGUI()
    {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        
        m_Applet = new Applet(); 
        frame.add(m_Applet, BorderLayout.CENTER);
        
        frame.setSize(800, 600);
        frame.pack();
        frame.setVisible(true);
        
        m_Applet.init();
        m_Applet.start();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    static 
    {
        try 
        {
            RrdDb.setDefaultFactory("MEMORY");
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    
    private AutoRefreshThread     m_refresher;
    private GraphUpdaterThread    m_grapher;
    
    private JTable     m_FlowTable;
    private GraphPanel m_Graph;
    private JComboBox  m_ModeCombo;
    private JTextField m_TargetAddrBox;
    private JScrollBar m_TimeWindow;
    private JLabel     m_StatusBar;
    private Vector     m_colHeads;
    private Vector     m_selected = new Vector();
    private boolean    m_initialised = false;
    private DefaultTableModel m_datamodel;
    
    public void init()
    {
    	try
    	{
    		initWithExceptions();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		JOptionPane.showMessageDialog(this, e.toString());
    	}
    }
    
    public void initWithExceptions() throws Exception
    {
        // UI
    	
    	JPanel settings = new JPanel();
    	getContentPane().add(settings, BorderLayout.NORTH);
    	
    	settings.setLayout(new GridBagLayout());
    	
    	Insets spacing = new Insets(2, 2, 2, 2);
    	
    	settings.add(new JLabel("Mode:"), new GridBagConstraints(0, 0, 1, 1,
    			0, 0, GridBagConstraints.LINE_START, 
    			GridBagConstraints.NONE, spacing, 0, 0));
    	m_ModeCombo = new JComboBox(Mode.values());
    	settings.add(m_ModeCombo, new GridBagConstraints(1, 0, 1, 1,
    			0.5, 0.5, GridBagConstraints.LINE_START, 
    			GridBagConstraints.HORIZONTAL, spacing, 0, 0));
    	
    	settings.add(new JLabel("Target:"), new GridBagConstraints(0, 1, 1, 1,
    			0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
    			spacing, 0, 0));
    	m_TargetAddrBox = new JTextField("62.3.84.19");
    	settings.add(m_TargetAddrBox, new GridBagConstraints(1, 1, 1, 1,
    			0.5, 0.5, GridBagConstraints.LINE_START, 
    			GridBagConstraints.HORIZONTAL, spacing, 0, 0));
    	
        m_colHeads = new Vector();
        m_colHeads.add("IP");
        m_colHeads.add("Bytes In");
        m_colHeads.add("Bytes Out");

        // Object[][] data = { };
        
        m_FlowTable = new JTable();
        // getContentPane().add(table, BorderLayout.NORTH);
        
        JScrollPane tableScroll = new JScrollPane(m_FlowTable);
        m_FlowTable.setPreferredScrollableViewportSize(new Dimension(500, 200));
        
        JTableHeader header = m_FlowTable.getTableHeader();
        // getContentPane().add(header, BorderLayout.NORTH);
        
        String [] columnNames = { "IP", "Bytes In", "Bytes Out" };
        m_datamodel = new DefaultTableModel(m_colHeads, 0);
        m_FlowTable.setModel(m_datamodel);
        
        JPanel graphContainer = new JPanel(new BorderLayout());
        
        // Create a split pane with the two scroll panes in it.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            tableScroll, graphContainer); 
        splitPane.setOneTouchExpandable(true);
        // splitPane.setDividerLocation(20);

        // Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(100, 20);
        m_FlowTable.setMinimumSize(m_FlowTable.getPreferredSize());
        graphContainer.setMinimumSize(minimumSize);
        
        getContentPane().add(splitPane, BorderLayout.CENTER);
        
        /*
        JButton restartButton = new JButton("Restart");
        getContentPane().add(restartButton, BorderLayout.SOUTH);
        
        restartButton.addActionListener
        (
                new ActionListener() 
                {
                    public void actionPerformed(ActionEvent e) 
                    {
                        restart();
                    }
                }
        );
        */

        m_Graph = new GraphPanel();
        graphContainer.add(m_Graph, BorderLayout.CENTER);

        m_TimeWindow = new JScrollBar(JScrollBar.HORIZONTAL);
        graphContainer.add(m_TimeWindow, BorderLayout.SOUTH);
        m_TimeWindow.setMinimum(0 - dataStoreSeconds);
        m_TimeWindow.setMaximum(graphWidthSeconds);
        m_TimeWindow.setValue(0);
        m_TimeWindow.setVisibleAmount(graphWidthSeconds);
        m_TimeWindow.setBlockIncrement(graphWidthSeconds / 2);
        m_TimeWindow.setUnitIncrement(graphWidthSeconds / 10);
        m_TimeWindow.addAdjustmentListener(new AdjustmentListener(){
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				m_grapher.m_ScrollOffsetFromNow = e.getValue();
				try
				{
					m_grapher.redrawGraph();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
        
        m_StatusBar = new JLabel(" ");
        getContentPane().add(m_StatusBar, BorderLayout.SOUTH);
        
        // Ask to be notified of selection changes.
        ListSelectionModel rowSM = m_FlowTable.getSelectionModel();
        rowSM.addListSelectionListener
        (
            new ListSelectionListener() 
            {
                public void valueChanged(ListSelectionEvent e) 
                {
                    // Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
                    
                    ListSelectionModel lsm =
                        (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) 
                    {
                        // no rows are selected
                        // don't bother clearing the graph
                        return;
                    } 

                    int min = lsm.getMinSelectionIndex();
                    int max = lsm.getMaxSelectionIndex();
                    m_selected.clear();
                    
                    for (int i = min; i <= max; i++)
                    {
                        if (lsm.isSelectedIndex(i))
                        {
                            m_selected.add(m_datamodel.getValueAt(i, 0));
                        }
                    }

                    m_grapher.restart();
                }
            }
        );

        InetAddress source;
        // Subnet subnet;
        
        try
        {
            source = InetAddress.getByName("127.0.0.1");
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return;
        }
        
        /*
        try
        {
            subnet = new Subnet("10.0.156.0/24");
        }
        catch (IllegalAddress e)
        {
            System.out.println(e);
            return;
        }
        */

        m_refresher = new AutoRefreshThread();
        m_grapher = new GraphUpdaterThread();

        V5FlowHandler handler = new V5FlowHandler(source, 100);
        // handler.addFilter(new SubnetFilter(subnet));

        handler.addAccountant(m_grapher);

        try
        {
            Collector collector = new Collector(2055);
            collector.addFlowHandler(handler);
            collector.start();
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            return;
        }

        m_initialised = true;
    }
    
    private void restart() 
    {
        stop();
        start();
    }
    
    public void start() 
    {
        if (!m_initialised) return;
        m_refresher.start();
        m_grapher.start();
    }
    
    public void stop() 
    {
        m_refresher.terminate();
        m_grapher.terminate();
    }

    class GraphPanel extends JPanel 
    {
        public final long serialVersionUID() { return 1; }
        private RrdGraphDef m_rrdGraphDef;
        
        public void setRrd(RrdGraphDef rrdGraphDef)
        {
            synchronized(this)
            {
                m_rrdGraphDef = rrdGraphDef;
            }
        }
        
        protected void paintComponent(Graphics g) 
        {
            RrdGraphDef initialGraphDef;
            
            synchronized(this)
            {
                initialGraphDef = m_rrdGraphDef;
            }
            
            if (initialGraphDef == null)
            {
                super.paintComponent(g);
                return;
            }
            
            try 
            {
                RrdGraph graph = new RrdGraph(initialGraphDef);
                graph.specifyImageSize(true);
                graph.renderImage((Graphics2D) g, getWidth(), getHeight());
            }
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }
    
    static class ColourIterator
    {
        private int position = 0;
        private static final Color[] wheel = 
        {
            new Color(0xFF, 0x00, 0x00), // red
            new Color(0xFF, 0x80, 0x00), // orange
            new Color(0xFF, 0xFF, 0x00), // yellow
            new Color(0x80, 0xFF, 0x00), // lime
            new Color(0x00, 0xFF, 0x00), // lime
            new Color(0x00, 0xFF, 0x80), // algae
            new Color(0x00, 0xFF, 0xFF), // cyan
            new Color(0x00, 0x80, 0xFF), // sky
            new Color(0x00, 0x00, 0xFF), // blue
            new Color(0x80, 0x00, 0xFF), // magenta
            new Color(0xFF, 0x00, 0x80), // violet
            new Color(0x80, 0x00, 0x00), // dark red
            new Color(0x80, 0x80, 0x00), // dark yellow
            new Color(0x00, 0x80, 0x00), // dark green
            new Color(0x00, 0x80, 0x80), // dark cyan
            new Color(0x00, 0x00, 0x80), // dark blue
            new Color(0x80, 0x00, 0x80), // dark magenta
            Color.BLACK,
        };
        private static int intensitySteps = 2;
        
        public Color next()
        {
            int color = position % wheel.length;
            int intensity = (position / wheel.length) % intensitySteps;
            
            int red = wheel[color].getRed();
            int grn = wheel[color].getGreen();
            int blu = wheel[color].getBlue();
            
            red += ((255 - red) * intensity) / intensitySteps;
            grn += ((255 - grn) * intensity) / intensitySteps;
            blu += ((255 - blu) * intensity) / intensitySteps;
            
            Color c = new Color(red, grn, blu);
            
            position++;
            position %= (wheel.length * intensitySteps);
            
            return c;
        }
    }

    private int graphWidthSeconds = 300;
    private int dataStoreSeconds = 86400;
    
    abstract class RestartableProcess extends Thread
    {
        public static final int DELAY = 30;
        private boolean m_shouldStop = false, m_shouldRestart = false;
        
        protected boolean shouldStop()    { return m_shouldStop; }
        protected boolean shouldRestart() { return m_shouldRestart; }
        
        public void restart()
        {
            m_shouldRestart = true;

            synchronized(this)
            {
                notify();
            }
        }
        
        public final void run() 
        {
            while (!m_shouldStop)
            {
                m_shouldRestart = false;
                doProcess();
                
                // wait for someone to tell us to stop or restart
                
                while (!m_shouldStop && !m_shouldRestart)
                {
                    try
                    {
                        synchronized(this)
                        {
                            wait(1000);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // do nothing
                    }
                }
            }
        }
        
        protected abstract void doProcess();
        
        void terminate() 
        {
            m_shouldStop = true;
            synchronized(this)
            {
                notify();
            }
            
            while (isAlive()) 
            {
                try 
                {
                    Thread.sleep(DELAY);
                }
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    class AutoRefreshThread extends RestartableProcess
    {
        protected void doProcess()
        {
            while (!shouldStop() && !shouldRestart())
            {
                try
                {
                    if (m_grapher != null)
                        m_grapher.restart();
                    
                    synchronized(this)
                    {
                        wait(1000);
                    }
                }
                catch (InterruptedException e)
                {
                    // do nothing
                }
            }
        }
    }
    
    class FlowInfo
    {
        private IpAddr m_srcaddr, m_dstaddr;
        private int    m_srcport, m_dstport, m_protocol;
        private long   m_bytes, m_timestamp;
        
        public FlowInfo(IpAddr src, IpAddr dst, int protocol, int srcport,
            int dstport, long bytes, long timestamp)
        {
            m_srcaddr   = src;
            m_dstaddr   = dst;
            m_protocol  = protocol;
            m_srcport   = srcport;
            m_dstport   = dstport;
            m_bytes     = bytes;
            m_timestamp = timestamp;
        }

        public FlowInfo(V5Flow flow)
        {
            m_srcaddr   = new IpAddr(flow.getSrcAddr());
            m_dstaddr   = new IpAddr(flow.getDstAddr());
            m_protocol  = flow.getProt();
            m_srcport   = flow.getSrcPort();
            m_dstport   = flow.getDstPort();
            m_bytes     = flow.getDOctets();
            m_timestamp = flow.getLast();
        }

        public IpAddr getSrcAddr()   { return m_srcaddr; }
        public IpAddr getDstAddr()   { return m_dstaddr; }
        public int    getProtocol()  { return m_protocol; }
        public int    getSrcPort()   { return m_srcport; }
        public int    getDstPort()   { return m_dstport; }
        public long   getBytes()     { return m_bytes; }
        public long   getTimestamp() { return m_timestamp; }
    }

    class GraphUpdaterThread extends RestartableProcess 
    implements Accountant
    {
        public static final int STEPS_PER_REPAINT = 5;
        private RrdDef        m_rrdDefinition;        
        private RrdDb         m_rrdDatabase;
        private Vector        m_flows = new Vector();
        // private TrafficSum [] m_sums  = null;
        private long          m_time  = 0;
        
        public void destroy() { }

        class DataSource
        {
            int m_outIndex, m_inIndex;
            long m_in, m_out;
            String name;
        }
        
        private DataSource m_otherDataSource;
        private DsDef m_PingDataSourceDef;
        private Map m_separateDataSources = new Hashtable();
        private Double [] m_PingTimes;
        private int m_PingTimesIndex = 0;
        private long m_ScrollOffsetFromNow = 0;

        public GraphUpdaterThread() throws Exception
        {
            m_otherDataSource = new DataSource();
            m_otherDataSource.m_outIndex = 0;
            m_otherDataSource.m_inIndex  = 1;
            m_otherDataSource.name = "Other";
            
            m_PingDataSourceDef = new DsDef("Ping", "GAUGE", 3600,
            		Double.NaN, Double.NaN);
            m_PingTimesIndex = 2;

            rebuildGraph();
        }
        
        class TrafficSum
        {
            long in, out;
        }

        class Entity
        {
            private int        m_index, m_tableRow;
            private TrafficSum m_total, m_temp;
            
            public Entity(int index) 
            { 
                m_index = index;
                m_total = new TrafficSum();
                m_temp  = new TrafficSum();
            }
            
            public int        getIndex()      { return m_index; }
            public int        getTableRow()   { return m_tableRow; }
            public void       setTableRow(int newRow) { m_tableRow = newRow; }
        }
        
        Map m_entities    = new Hashtable();
        Map m_datasources = new Hashtable();
        
        private Entity lookupEntry(Map map, String id)
        {
            Entity source = (Entity)map.get(id);
            
            if (source != null)
            {
                return source;
            }
            
            int newIndex = 0; // map.size() + 1;
            source = new Entity(newIndex);
            map.put(id, source);
            
            return source;
        }

        public void account(Flow f)
        {
            FlowInfo flow = new FlowInfo((V5Flow)f);
            m_flows.add(flow);
            
            {
                Entity source = lookupEntry(m_entities, 
                    flow.getSrcAddr().toString());
                source.m_total.out += f.getDOctets();
                source.m_temp.out  += f.getDOctets();
                // m_sums[source.getIndex()].out += f.getDOctets();
            }

            {
                Entity dest = lookupEntry(m_entities, 
                    flow.getDstAddr().toString());
                dest.m_total.in += f.getDOctets();
                dest.m_temp.in  += f.getDOctets();
                // m_sums[dest.getIndex()].in += f.getDOctets();
            }
            
            if (Util.getTime() < m_time + 1)
            {
                return;
            }
            
            try
            {
                Vector dataSources = new Vector();
                
                for (Iterator i = m_selected.iterator(); i.hasNext();)
                {
                    String host = (String)( i.next() );
                    DataSource ds = (DataSource)m_separateDataSources.get(host);
                    if (ds == null)
                    {
                        m_rrdDefinition.addDatasource("From "+host, 
                            "ABSOLUTE", 3600, Double.NaN, Double.NaN);
                        m_rrdDefinition.addDatasource("To "+host,   
                            "ABSOLUTE", 3600, Double.NaN, Double.NaN);
                        m_rrdDatabase = new RrdDb(m_rrdDefinition);
    
                        ds = new DataSource();
                        ds.name = host;
                        ds.m_outIndex = m_rrdDefinition.getDsCount() - 2;
                        ds.m_inIndex  = m_rrdDefinition.getDsCount() - 1;
                        m_separateDataSources.put(host, ds);
                    }
                    ds.m_in  = 0;
                    ds.m_out = 0;
                    dataSources.add(ds);
                }
                
                dataSources.add(m_otherDataSource);

                for (Iterator i = dataSources.iterator(); i.hasNext();)
                {
                    DataSource ds = (DataSource)( i.next() );   
                    ds.m_in  = 0;
                    ds.m_out = 0;
                }

                for (Iterator i = m_entities.keySet().iterator(); i.hasNext();)
                {
                    String host   = (String)( i.next() );
                    Entity entity = (Entity)( m_entities.get(host) );
                    DataSource ds = (DataSource)( m_separateDataSources.get(host) );
                    if (ds == null) ds = m_otherDataSource;
                    ds.m_in  += entity.m_temp.in;
                    ds.m_out += entity.m_temp.out;
                    entity.m_temp.in  = 0;
                    entity.m_temp.out = 0;
                }
                
                Sample sample = m_rrdDatabase.createSample();

                for (Iterator i = dataSources.iterator(); i.hasNext();)
                {
                    DataSource ds = (DataSource)( i.next() );   
                    sample.setValue(ds.m_inIndex,  ds.m_in);
                    sample.setValue(ds.m_outIndex, - ds.m_out);
                }

                sample.update();
            }
            catch (Exception e)
            {
                m_LOG.fine(e.toString());
            }
            
            m_time = Util.getTime();
        }
        
        private final Color m_otherColour = new Color(0xC0, 0xC0, 0xC0);
        private final Color m_PacketLossColour = new Color(0xFF, 0xCC, 0xAA);

        protected void doProcess()
        {
            Mode mode = (Mode) m_ModeCombo.getSelectedItem();
            
            try 
            {
                switch (mode)
                {
                case PING:
                    long start = System.currentTimeMillis();
                    Sample sample = m_rrdDatabase.createSample(start / 1000);
                    Double value = Double.NaN;

                    try
                    {
                        InetAddress addr = InetAddress.getByName(m_TargetAddrBox.getText());
                        
	                    if (addr.isReachable(1000))
	                    {
	                    	long elapsed = System.currentTimeMillis() - start;
	                    	value = ((double)elapsed) / 1000;
	                    	m_StatusBar.setText("");

	                    	/*
		                    System.out.println("Ping at " + start + 
		                    		" returned in " + value + " seconds");
		                    */
	                    }
	                    else
	                    {
	                    	m_StatusBar.setText("Ping at " + start + " lost");
	                    }
                    }
                    catch (UnknownHostException e)
                    {
                    	m_StatusBar.setText(e.toString());
                    }
                    catch (SocketException e)
                    {
                    	// not terribly interesting
                    	m_StatusBar.setText("Ping at " + start + 
                        		" failed: " + e);
                    }

                	sample.setValue(m_PingDataSourceDef.getDsName(), value);
                    sample.update();
                    
                	break;
                	
                case NETFLOW:
                    m_datamodel.setRowCount(m_entities.keySet().size());
                    int rowIndex = 0;
                    
                    for (Iterator i = m_entities.keySet().iterator(); i.hasNext();)
                    {
                        String key = (String)( i.next() );
                        Entity entity = (Entity)( m_entities.get(key) );

                        m_datamodel.setValueAt(key, rowIndex, 0);
                        m_datamodel.setValueAt("" + entity.m_total.in, 
                            rowIndex, 1);
                        m_datamodel.setValueAt("" + entity.m_total.out, 
                            rowIndex, 2);
                        rowIndex++;
                    }

                	break;
                }
                
                redrawGraph();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        public void redrawGraph() throws RrdException, IOException
        {
            ColourIterator iter = new ColourIterator();
            Mode mode = (Mode) m_ModeCombo.getSelectedItem();

            long graphOriginX = Util.getTime() + m_ScrollOffsetFromNow - 
            		graphWidthSeconds;
            
            RrdGraphDef rrdGraph = new RrdGraphDef(graphOriginX,
            		graphOriginX + graphWidthSeconds);
                
            switch (mode)
            {
            case PING:
            	rrdGraph.setTitle("Round Trip Time (ping)");
            	rrdGraph.setVerticalLabel("seconds");
            	rrdGraph.setLowerLimit(0.0);
            	rrdGraph.setTimeAxis(TimeAxisUnit.SECOND, 10,
            			TimeAxisUnit.MINUTE, 1,
            			"HH:mm:ss", true);

                rrdGraph.datasource(m_PingDataSourceDef.getDsName(), RRD_PATH, 
                		m_PingDataSourceDef.getDsName(), "AVERAGE");
        		rrdGraph.line(m_PingDataSourceDef.getDsName(), iter.next(), 
        				m_PingDataSourceDef.getDsName(), 1);
        		
        		// http://oldwww.jrobin.org/api/coreapi.html
        		FetchRequest request = m_rrdDatabase.createFetchRequest(
        				"AVERAGE", graphOriginX, 
        				graphOriginX + graphWidthSeconds);
        		request.setFilter(m_PingDataSourceDef.getDsName());

        		// execute the request
        		FetchData fetchData = request.fetchData();

        		// From a logical point of view FetchData object is, simply, 
        		// an array of FetchPoint objects. Each FetchPoint object 
        		// represents all datasource values for the specific timestamp.
        		// Here is how you can produce the same output to stdout 
        		// as RRDTool's fetch command
        		
        		long [] times = fetchData.getTimestamps();
        		double [] values = fetchData.getValues(0);
        		boolean haveAddedLossMarker = false;
        		
        		for(int i = 0; i < fetchData.getRowCount(); i++)
        		{
        		    if (Double.isNaN(values[i]))
        		    {
        		    	GregorianCalendar date = (GregorianCalendar)
        		    			GregorianCalendar.getInstance();
        		    	date.setTimeInMillis(times[i] * 1000);
        		    	rrdGraph.vrule(date, m_PacketLossColour,
        		    			haveAddedLossMarker ? null : "Loss");
        		    	haveAddedLossMarker = true;
        		    }
        		}
        		break;
        		
            case NETFLOW:
            	rrdGraph.setTitle("Network Traffic (netflow)");
            	
            	Vector dataSources = new Vector(m_selected);
            	dataSources.add("Other");

            	for (Iterator i = dataSources.iterator(); i.hasNext();)
            	{
            		String host = (String)( i.next() );
            		DataSource ds;
            		Color c;

            		if (host.equals("Other"))
            		{
            			ds = m_otherDataSource;
            			c = m_otherColour;
            		}
            		else
            		{
            			ds = (DataSource)m_separateDataSources.get(host);
            			c = iter.next();
            		}

            		if (ds == null)
            		{
            			continue;
            		}

            		String fromName = "From " + ds.name;
            		String toName   = "To "   + ds.name;

            		rrdGraph.datasource(fromName, RRD_PATH, fromName, "AVERAGE");
            		rrdGraph.datasource(toName,   RRD_PATH, toName,   "AVERAGE");

            		rrdGraph.line(fromName, c, ds.name, 1);
            		rrdGraph.line(toName,   c, null,    1);
            	}

            	break;
            }

            m_Graph.setRrd(rrdGraph);
            m_Graph.repaint();
            
        }

        private void rebuildGraph()
        throws RrdException, IOException
        {
            long endTime   = Util.getTime() - 1;
            long startTime = endTime - graphWidthSeconds;
            
            m_rrdDefinition = new RrdDef(RRD_PATH, startTime - 1, 1);
            m_rrdDefinition.addDatasource("From Other", "ABSOLUTE", 3600, 
                Double.NaN, Double.NaN);
            m_rrdDefinition.addDatasource("To Other",   "ABSOLUTE", 3600, 
                Double.NaN, Double.NaN);
            m_rrdDefinition.addDatasource(m_PingDataSourceDef);
            m_rrdDefinition.addArchive("AVERAGE", 0, 1, dataStoreSeconds);
            m_rrdDatabase = new RrdDb(m_rrdDefinition);

            /*
            for (Iterator i = bucketNames.iterator(); i.hasNext(); )
            {
                String name   = (String)( i.next() );
                Bucket bucket = (Bucket)( m_accountant.getBucket(name) );
                
                myDef.addDatasource("From "+name, "GAUGE", 3600, 
                    Double.NaN, Double.NaN);
                myDef.addDatasource("To "+name, "GAUGE", 3600, 
                    Double.NaN, Double.NaN);
            }
            */

            /*
            RrdGraphDef rrdGraph = new RrdGraphDef(startTime, 
                        startTime + totalTime);
            rrdGraph.setTitle("Network Traffic");
            */
            // myGraphDef.setLowerLimit(0);
            
            TrafficSum [][] sums = new TrafficSum [(int)(graphWidthSeconds + 1)][1];
            
            for (int i = 0; i <= graphWidthSeconds; i++)
            {
                sums[i] = new TrafficSum[1];
            
                for (int j = 0; j < sums[i].length; j++)
                {      
                    if (sums[i][j] == null)
                    {
                        sums[i][j] = new TrafficSum();
                    }
                    else
                    {
                        sums[i][j].in  = 0;
                        sums[i][j].out = 0;
                    }
                }
            }
            
            for (Iterator i = m_flows.iterator(); i.hasNext(); )
            {
                FlowInfo flow = (FlowInfo)( i.next() );
                
                long timestamp = flow.getTimestamp();
                
                if (timestamp < startTime)
                {
                    m_LOG.fine("skipped 1 underflow, "+
                        "("+timestamp+" < "+startTime+"), "+
                        flow.getBytes()+" bytes");
                    continue;
                }
                
                if (timestamp > endTime)
                {
                    m_LOG.fine("skipped 1 overflow, "+
                        "("+timestamp+" > "+endTime+"), "+
                        flow.getBytes()+" bytes");
                    continue;
                }
                
                int index = (int)(timestamp - startTime);
                if (index < 0 || index > graphWidthSeconds)
                    throw new AssertionError("bad index: "+index);
                
                TrafficSum sum = sums[index][0];
                sum.in += flow.getBytes();
                
                m_LOG.fine("added 1 flow, "+flow.getBytes()+" bytes");
            }

            Sample sample = null;
            
            for (int i = 0; i < sums.length; i++)
            {
                if (shouldStop() || shouldRestart())
                    break;

                TrafficSum [] timeSums = sums[i];

                long timestamp = i + startTime;
                sample = m_rrdDatabase.createSample();
                sample.setTime(timestamp);

                for (int j = 0; j < timeSums.length; j++)
                {
                    int dataSourceIndex = j * 2;
                    sample.setValue(dataSourceIndex,     timeSums[j].in);
                    sample.setValue(dataSourceIndex + 1, timeSums[j].out);
                }
                
                sample.update();
            }
            
            /*
            String fromName = "From Other";
            String toName   = "To Other";

            ColourIterator iter = new ColourIterator();
            Color c = iter.next();
            
            rrdGraph.datasource(fromName, RRD_PATH, fromName, "AVERAGE");
            rrdGraph.datasource(toName,   RRD_PATH, toName,   "AVERAGE");
            rrdGraph.line      (fromName, c, "Other", 1);
            rrdGraph.line      (toName,   c, null,    1);
            
            m_panel.setRrd(rrdGraph);
            */
            m_Graph.repaint();
        }
    }
}
