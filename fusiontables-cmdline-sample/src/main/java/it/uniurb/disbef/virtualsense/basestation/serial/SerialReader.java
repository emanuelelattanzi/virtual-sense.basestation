package it.uniurb.disbef.virtualsense.basestation.serial;

import it.uniurb.disbef.virtualsense.basestation.BaseStationLogger;
import it.uniurb.disbef.virtualsense.basestation.gui.DebugArea;
import it.uniurb.disbef.virtualsense.basestation.gui.GUI;
import it.uniurb.disbef.virtualsense.basestation.textparser.TextParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SerialReader extends Thread {
	private InputStream in;
	private DebugArea debug;
	private GUI gui;
	private boolean running = true;
	
	public SerialReader(InputStream in, DebugArea debug, GUI gui){
		super("SerialReader");
		this.in = in;
		this.debug = debug;
		this.gui = gui;
	}
	
	public void run (){
		BufferedReader reader = new BufferedReader(new InputStreamReader(this.in));
		String readed = "";
		debug.println("DEBUG: SerialReader started ");
		try {
			while(running){
				readed = reader.readLine();
				TextParser.parseText(readed, gui);
				BaseStationLogger.log(readed);
				debug.println("S< "+readed);
			}
			debug.println("DEBUG: SerialReader shut down ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			debug.println(""+e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("For input string: "+readed);
		}
		
	}
	public void stopReading(){
		this.running = false;
		this.interrupt();
	}
}
