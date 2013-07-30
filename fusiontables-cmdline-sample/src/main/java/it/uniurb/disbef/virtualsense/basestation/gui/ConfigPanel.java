package it.uniurb.disbef.virtualsense.basestation.gui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConfigPanel extends JPanel{

	private String  commPort = "/dev/ttyUSB0";
	private int 	baudRate = 57600;	
	private JTextField portField = new JTextField(commPort);
	private JTextField baudField = new JTextField(""+baudRate);
	   
	
	public ConfigPanel(){
		this.setLayout(new GridLayout(0, 1));
        this.setBorder(BorderFactory.createTitledBorder("Receiver"));
        this.add(new JLabel("Device"));
        this.add(portField);
        this.add(new JLabel("Baud rate"));
        this.add(baudField);     

	}	
	
	public String getCommPort(){
		return this.portField.getText();
	}
	public String getBaudRate(){
		return this.baudField.getText();
	}
	
}
