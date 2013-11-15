package it.uniurb.disbef.virtualsense.basestation;

/**
 * @author lattanzi
 *
 */
public class FusionTableNodeRecord {
  public short nodeID;
  public short counter;
  public double noise;
  public int noiseCounter;
  public double co2;
  public int co2Counter;
  public short out;
  public short in; 
  public short deltaOut;
  public short deltaIn; 
  public double pressure;
  public int pressureCounter;
  public double temperature;
  public int temperatureCounter;
  public double luminosity;
  public int luminosityCounter;
  public boolean checked;
  
  public FusionTableNodeRecord(short id){
	  this.nodeID = id;
  }
  public void print() {
		System.out.println("\t\t nodeID: "+nodeID);
		System.out.println("\t\t counter: "+counter);
		System.out.println("\t\t nosie: "+noise);
		System.out.println("\t\t noiseCounter: "+noiseCounter);
		System.out.println("\t\t co2: "+co2);
		System.out.println("\t\t co2Counter: "+co2Counter);
		System.out.println("\t\t out: "+out);
		System.out.println("\t\t in: "+in);
		System.out.println("\t\t deltaOut: "+deltaOut);
		System.out.println("\t\t deltaIn: "+deltaIn);
		System.out.println("\t\t pressure: "+pressure);
		System.out.println("\t\t pressureCounter: "+pressureCounter);
		System.out.println("\t\t temperature: "+temperature);
		System.out.println("\t\t temperatureCounter: "+temperatureCounter);
		System.out.println("\t\t luminosity: "+luminosity);
		System.out.println("\t\t luminosityCounter: "+luminosityCounter);
		System.out.println("\t\t checked: "+checked);
		
	}

}
