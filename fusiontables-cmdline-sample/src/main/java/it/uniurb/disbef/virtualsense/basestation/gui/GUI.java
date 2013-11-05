package it.uniurb.disbef.virtualsense.basestation.gui;

import com.google.api.services.samples.fusiontables.cmdline.FusionTablesSample;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import it.uniurb.disbef.virtualsense.basestation.BaseStationLogger;
import it.uniurb.disbef.virtualsense.basestation.FusionTableUpdater;
import it.uniurb.disbef.virtualsense.basestation.serial.SerialCommunication;
import it.uniurb.disbef.virtualsense.basestation.serial.SerialReader;
import it.uniurb.disbef.virtualsense.basestation.serial.SerialWriter;
import it.uniurb.disbef.virtualsense.basestation.textparser.TextParser;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
//import net.miginfocom.swing.MigLayout;

public class GUI extends JFrame {

	private JPanel mainPane;
	private ConfigPanel configPanel;
	private String commPort;
	private String baudRate;
	private DebugArea debugArea;
	private SerialReader serialReader;
	private SerialWriter serialWriter;
	public  JCheckBoxMenuItem showsNodeInfoCheckBoxMenuItem;
	public NodesPanel nodesPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
				    FusionTablesSample.connect();
					GUI frame = new GUI();
					frame.setVisible(true);
					FusionTableUpdater updater = new FusionTableUpdater();
					updater.start();
					BaseStationLogger.setFusionTableUpdater(updater);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		this.configPanel = new ConfigPanel();
		this.commPort = this.configPanel.getCommPort();
		this.baudRate = this.configPanel.getBaudRate();
		this.debugArea = new DebugArea();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1027, 695);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitAction();
			}
		});
		
		JMenuItem connectMenuItem = new JMenuItem("Connect");
		connectMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connectAction();
			}
		});
		fileMenu.add(connectMenuItem);
		
		JMenuItem configMItem = new JMenuItem("Config");
		configMItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configAction();
			}

		
		});
		fileMenu.add(configMItem);
		
		JMenuItem loadTraceMenuItem = new JMenuItem("load trace");
		loadTraceMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadAction();
				}

			
		});
		fileMenu.add(loadTraceMenuItem);
		fileMenu.add(exitMenuItem);
		
		JMenu networkMenu = new JMenu("Network");
		menuBar.add(networkMenu);
		
		JMenuItem ommandsMenuItem = new JMenuItem("Commands");
		ommandsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				commandsAction();
			}
		});
		networkMenu.add(ommandsMenuItem);
		
		JMenuItem printNodesInfoMenuItem = new JMenuItem("print nodes info");
		printNodesInfoMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printNodesInfoAction();
			}
		});
		
		JMenuItem loadNodesFileMenuItem = new JMenuItem("Load nodes file");
		loadNodesFileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadNodesFileAction();
			}
		});
		networkMenu.add(loadNodesFileMenuItem);
		
		JSeparator separator = new JSeparator();
		networkMenu.add(separator);
		
		this.showsNodeInfoCheckBoxMenuItem = new JCheckBoxMenuItem("shows node info");
		showsNodeInfoCheckBoxMenuItem.setSelected(true);
		networkMenu.add(showsNodeInfoCheckBoxMenuItem);
		networkMenu.add(printNodesInfoMenuItem);
		mainPane = new JPanel();
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		mainPane.setLayout(new BorderLayout(0, 0));
		
		JScrollPane debugScrollPane = new JScrollPane();
		debugScrollPane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		debugScrollPane.setPreferredSize(new Dimension(500, 200));
		debugScrollPane.setSize(new Dimension(500, 200));
		debugScrollPane.setMinimumSize(new Dimension(500, 200));
		debugScrollPane.setBounds(new Rectangle(0, 0, 500, 200));
		mainPane.add(debugScrollPane, BorderLayout.SOUTH);
		
		
		this.nodesPanel = new NodesPanel(BaseStationLogger.nodes, this);
		nodesPanel.setPreferredSize(new Dimension(500, 700));
		nodesPanel.setSize(new Dimension(500, 700));
		nodesPanel.setMinimumSize(new Dimension(500, 700));
		nodesPanel.setBounds(new Rectangle(0, 0, 500, 700));
		mainPane.add(nodesPanel, BorderLayout.NORTH);
		
		
		
		
		
		debugScrollPane.setViewportView(debugArea);
		
		BaseStationLogger.createNodesFromFile("nodes.txt");
		
		// auto-connect to made software autobootable
		connectAction();
	}
	
	protected void loadNodesFileAction() {
		// TODO Auto-generated method stub
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(this);
		String fileName = "";
		if(returnVal == JFileChooser.APPROVE_OPTION) {
		      fileName = chooser.getSelectedFile().getAbsolutePath();
		}
		BaseStationLogger.createNodesFromFile(fileName);
		
	}

	protected void printNodesInfoAction() {
		// TODO Auto-generated method stub
		BaseStationLogger.printNodesInfo();
		
	}

	private void configAction() {
		// TODO Auto-generated method stub
		String[] buttons = { "OK", "Cancel"};

	    int c = JOptionPane.showOptionDialog(
	            null,
	            this.configPanel,
	            "Config Panel",
	            JOptionPane.DEFAULT_OPTION,
	            JOptionPane.PLAIN_MESSAGE,
	            null,
	            buttons,
	            buttons[0]
	     );

	    if(c ==0){
	    	this.commPort = this.configPanel.getCommPort();
			this.baudRate = this.configPanel.getBaudRate();	
			if(this.serialReader != null)
				this.serialReader.stopReading();
			if(this.serialWriter != null)
				this.serialWriter.stopWriter();
			if (SerialCommunication.in != null)				
					SerialCommunication.resetPort();				
			
	    }
		
	}
	private void connectAction() {
		if(this.serialReader != null)
			this.serialReader.stopReading();
		if(this.serialWriter != null)
			this.serialWriter.stopWriter();
		SerialCommunication.resetPort();
		try {
			SerialCommunication.createCommunication(this.commPort, baudRate, this.debugArea);
			if(SerialCommunication.in != null){				
				this.serialReader = new SerialReader(SerialCommunication.in, this.debugArea, this);
				this.serialReader.start();
				this.serialWriter = new SerialWriter(SerialCommunication.out, this.debugArea);
				this.serialWriter.start();
			}
			
		} catch (NoSuchPortException e) {
			// TODO Auto-generated catch block
			this.debugArea.println("DEBUG "+e);
		} catch (PortInUseException e) {
			// TODO Auto-generated catch block
			this.debugArea.println("DEBUG "+e);
		} catch (UnsupportedCommOperationException e) {
			// TODO Auto-generated catch block
			this.debugArea.println("DEBUG "+e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.debugArea.println("DEBUG "+e);
		}		
		
	}
	private void exitAction() {
		System.exit(0);
		
	}
	private void commandsAction() {
		// TODO Auto-generated method stub
		if(this.serialWriter != null)
			this.serialWriter.writeCommand("APPS LOAD 1");
		
	}
	
	private void loadAction() {
		
		 JFileChooser chooser = new JFileChooser();
		 int returnVal = chooser.showOpenDialog(this);
		 String fileName = "";
		 if(returnVal == JFileChooser.APPROVE_OPTION) {
		        fileName = chooser.getSelectedFile().getAbsolutePath();
		 }
		 final GUI myGui = this;
		 final String name = fileName;
		 new Thread(){
			 public void run(){
				 int lineNumber = 0;
	
		 String tmp = null;
		 try {
			BufferedReader in
			   = new BufferedReader(new FileReader(name));			
			do{
				 tmp = in.readLine();
				 //Thread.sleep(50); // to debug 
				 if(tmp != null)
					 TextParser.parseText(tmp, myGui);
				 lineNumber++;
			}while(tmp != null);
			System.out.println("Log file readed lines: "+lineNumber);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("For input string: "+tmp+" at line number: "+lineNumber);
		}
			 }
		 }.start();
		
	}
}
