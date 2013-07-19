package it.uniurb.disbef.virtualsense.basestation.serial;

import it.uniurb.disbef.virtualsense.basestation.gui.DebugArea;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Semaphore;

public class SerialWriter extends Thread {
	private OutputStream out;
	private DebugArea debug;
	private boolean running = true;
	private Semaphore waitForCommand = new Semaphore(0);
	private String command = "";
	
	public SerialWriter(OutputStream out, DebugArea debug){
		super("SerialWriter");
		this.out = out;
		this.debug = debug;
	}
	
	public void run (){
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.out));
		debug.println("DEBUG: SerialWriter started ");
		try {
			while(running){
				this.waitForCommand.acquire();
				writer.write(this.command);
				writer.newLine();
				writer.flush();
				debug.println("COMMAND< "+this.command+"\n");
			}
			debug.println("DEBUG: SerialWriter shut down ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			debug.println(""+e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			debug.println(""+e);
		}
		
	}
	public void writeCommand(String command){
		this.command = command;
		this.waitForCommand.release();
	}
	public void stopWriter(){
		this.running = false;
		this.interrupt();
	}
}
