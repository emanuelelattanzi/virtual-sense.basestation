package it.uniurb.disbef.virtualsense.basestation;

import java.util.LinkedList;

public class Packet {
	 long time;
	 short lastRouter;
	 short sender;
	 short counter;
	 short route;
	 short noise;
	 short co2;
	 /*short pressure;
	 short temperature;
	 short luminosity;*/
	 
	 public Packet(long t, short lR, short s, short c, short r, short n, short co2/*, short p, short temp, short lum*/){
		 this.time = t;
		 this.lastRouter = lR;
		 this.sender = s;
		 this.counter = c;
		 this.route = r;
		 this.noise = n;
		 this.co2 = co2;
		 /*this.pressure = p;
		 this.temperature = temp;
		 this.luminosity = lum;*/
		 
	 }
	 
	 public LinkedList<Short> getHospIndexes(){
		 LinkedList<Short> list = null;
		 if(this.route != 0){
			 list = new LinkedList<Short>();
			 for(short i = 1; i < 16; i++){
				 //System.out.println("comparing "+(0x01 << i)+" and "+this.route);
				if(((0x01 << i) == this.route)){
					
					list.add(new Short(i));
				}
			 }
		 }
		 return list;
	 }
}
