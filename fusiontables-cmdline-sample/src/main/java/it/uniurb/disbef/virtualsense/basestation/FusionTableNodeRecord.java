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
  public double pressure;
  public int pressureCounter;
  public double temperature;
  public int temperatureCounter;
  public double luminosity;
  public int luminosityCounter;
  
  public FusionTableNodeRecord(short id){
	  this.nodeID = id;
  }
}
