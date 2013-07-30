package it.uniurb.disbef.virtualsense.basestation.textparser;

import it.uniurb.disbef.virtualsense.basestation.BaseStationLogger;
import it.uniurb.disbef.virtualsense.basestation.Packet;
import it.uniurb.disbef.virtualsense.basestation.gui.GUI;

import java.util.StringTokenizer;

public class TextParser {
	static boolean intPacket = false;
	static boolean savedTrace  = false;
	static long time;
	static short lastRouter;
	static short sender;
	static short counter;
	static short route;
	static short noise;
	static short co2;
	static short in;
	static short out;
	static short pressure;
	static short temperature;
    static short luminosity;
	public static void parseText(String text, GUI gui) throws Exception{
		//System.out.println("parsing: "+text);
		if(text.indexOf("<packet>") != -1){
			intPacket = true;
			StringTokenizer tokenizer = new StringTokenizer(text, " ");
			if(tokenizer.countTokens() > 1){
				tokenizer.nextToken();
				time = Long.parseLong(tokenizer.nextToken()); // we are loading a saved trace so we need to take the saved timestamp
				savedTrace = true;
			}else {
				savedTrace = false;
			}
			return;
		}
		if(text.indexOf("</packet>") != -1){
			intPacket = false;
			// finalize the packet
			Packet p = new Packet(time,lastRouter,sender,counter,route,noise,co2, in, out, pressure,temperature,luminosity);
			BaseStationLogger.newPacket(p);
			gui.nodesPanel.updatePacket(p);			
			return;
		}
		
		if(intPacket && (text.indexOf(">") != -1)){
			StringTokenizer tokenizer = new StringTokenizer(text, ": ");
			String tag = tokenizer.nextToken();				
			//System.out.println("working on "+tag);
			if(tag.equals(">time") && !savedTrace){				
				time = System.currentTimeMillis();
			}
			if(tag.equals(">router") && tokenizer.hasMoreTokens()){	
				short value = Short.parseShort(tokenizer.nextToken());	
				lastRouter = value;
			}
			if(tag.equals(">sender") && tokenizer.hasMoreTokens()){	
				short value = Short.parseShort(tokenizer.nextToken());	
				sender = value;
			}
			if(tag.equals(">counter") && tokenizer.hasMoreTokens()){	
				short value = Short.parseShort(tokenizer.nextToken());	
				counter = value;
			}			
			if(tag.equals(">route") && tokenizer.hasMoreTokens()){
				short value = Short.parseShort(tokenizer.nextToken());	
				route = value;
			}
			if(tag.equals(">noise")&& tokenizer.hasMoreTokens()){	
				short value = Short.parseShort(tokenizer.nextToken());	
				noise = value;
			}
			if(tag.equals(">co2") && tokenizer.hasMoreTokens()){		
				short value = Short.parseShort(tokenizer.nextToken());	
				co2 = value;
			}if(tag.equals(">in") && tokenizer.hasMoreTokens()){		
				short value = Short.parseShort(tokenizer.nextToken());	
				in = value;
			}if(tag.equals(">out") && tokenizer.hasMoreTokens()){		
				short value = Short.parseShort(tokenizer.nextToken());	
				out = value;
			}if(tag.equals(">pressure") && tokenizer.hasMoreTokens()){		
				short value = Short.parseShort(tokenizer.nextToken());	
				pressure = value;
			}
			if(tag.equals(">temp") && tokenizer.hasMoreTokens()){		
				short value = Short.parseShort(tokenizer.nextToken());	
				temperature = value;
			}
			if(tag.equals(">light") && tokenizer.hasMoreTokens()){
				short value = Short.parseShort(tokenizer.nextToken());	
				luminosity = value;
			}
		}
	}

}
