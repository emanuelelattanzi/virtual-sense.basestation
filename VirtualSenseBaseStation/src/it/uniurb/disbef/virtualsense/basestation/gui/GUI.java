package it.uniurb.disbef.virtualsense.basestation.gui;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import it.uniurb.disbef.virtualsense.basestation.serial.SerialCommunication;
import it.uniurb.disbef.virtualsense.basestation.serial.SerialReader;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
//import net.miginfocom.swing.MigLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.CardLayout;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JMenu;

import sun.security.ssl.Debug;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.awt.Cursor;

public class GUI extends JFrame {

	private JPanel mainPane;
	private ConfigPanel configPanel;
	private String commPort;
	private String baudRate;
	private DebugArea debugArea;
	private SerialReader serialReader;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
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
		
		
		debugScrollPane.setViewportView(debugArea);
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
			if (SerialCommunication.in != null)				
					SerialCommunication.resetPort();				
			
	    }
		
	}
	private void connectAction() {
		if(this.serialReader != null)
			this.serialReader.stopReading();
		SerialCommunication.resetPort();
		try {
			SerialCommunication.createCommunication(this.commPort, baudRate, this.debugArea);
			if(SerialCommunication.in != null){				
				this.serialReader = new SerialReader(SerialCommunication.in, this.debugArea);
				this.serialReader.start();
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
		
	}
}
