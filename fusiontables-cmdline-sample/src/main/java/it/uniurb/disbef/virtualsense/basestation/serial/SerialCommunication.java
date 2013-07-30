package it.uniurb.disbef.virtualsense.basestation.serial;

import it.uniurb.disbef.virtualsense.basestation.gui.DebugArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


import gnu.io.*;

public class SerialCommunication {

	public static InputStream in;
	public static OutputStream out;
	private static CommPort commPort;
		
	public static void createCommunication(String port, String bRate, DebugArea debug) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException
    {
		int baudRate = Integer.parseInt(bRate);
               
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            debug.println("Error: Port is currently in use");
        }
        else
        {
            commPort = portIdentifier.open("BaseStation",2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(baudRate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
             
            }
            else
            {
            	debug.println("Error: Port is not a serial port!!!!.");
            }
        }     
        
        
    }  
	
	public static void resetPort(){
		if(commPort != null){
			commPort.close();
			in = null;
			out = null;
		}
	}
   
}
