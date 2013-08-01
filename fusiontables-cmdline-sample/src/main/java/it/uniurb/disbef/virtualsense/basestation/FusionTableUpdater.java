

package it.uniurb.disbef.virtualsense.basestation;

import com.google.api.services.samples.fusiontables.cmdline.FusionTablesSample;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lattanzi
 *
 */
public class FusionTableUpdater extends Thread{
  private Hashtable<Short,LinkedList<Packet>> packetsOnSend = new Hashtable<Short,LinkedList<Packet>>(); 
  private Hashtable<Short,LinkedList<Packet>> packetsOnFill = new Hashtable<Short,LinkedList<Packet>>(); 
  private Hashtable<Node,FusionTableNodeRecord> lastNodeRecords = new Hashtable<Node,FusionTableNodeRecord>(); 
  private FusionTableGlobalPeopleRecord globalRecord = new FusionTableGlobalPeopleRecord();
  
  private ReentrantLock lock = new ReentrantLock();
  

  
  public FusionTableUpdater(){
    
  }
  
  @Override
  public void run(){
    while(true){
      this.lock.lock();
      try {
    	  // switch packet buffer pointers
        this.packetsOnSend = this.packetsOnFill;
        this.packetsOnFill = new Hashtable<Short,LinkedList<Packet>>();
        this.lock.unlock();
        
        // process nodesOnSend
        Enumeration<Short> e = this.packetsOnSend.keys();
         // for each node
        while(e.hasMoreElements()){
          short nodeID = e.nextElement().shortValue();
          Node nn = BaseStationLogger.nodes.get(nodeID);
          LinkedList<Packet> p = this.packetsOnSend.get(nodeID);
          FusionTableNodeRecord nr = processPacketList(p,nodeID);
          if(nr != null){
        	  this.lastNodeRecords.put(nn, nr);
        	  // save to the fusion table
        	  try {
                	System.out.println("Inserting record for node "+nn.ID);
                	FusionTablesSample.insertData(nr);          	
              } catch (IOException exception) {
                  // TODO Auto-generated catch block
                  exception.printStackTrace();
              }
          }
        }// end for each node  
        Enumeration<Node>nodes = this.lastNodeRecords.keys();
        // for each node
        while(e.hasMoreElements()){
        	Node n = nodes.nextElement();
        	FusionTableNodeRecord nodeRecord = this.lastNodeRecords.get(n);
        	globalRecord.in+=nodeRecord.in; // sum epoch deltas to global counters
        	globalRecord.out+=nodeRecord.out;
        }
        globalRecord.inside = globalRecord.in - globalRecord.out;      
                
        // save to the fusion table the global counter values 
        if(this.lastNodeRecords.size() > 0)
        	try {
        		FusionTablesSample.insertDataToGlobalCounter(System.currentTimeMillis(), ""+globalRecord.in, ""+globalRecord.out, ""+globalRecord.inside);
        	} catch (IOException exception) {
        		// TODO Auto-generated catch block
        		exception.printStackTrace();
        	}     
        
        Thread.sleep(1000*60*2);
      } catch (InterruptedException exception) {
        // TODO Auto-generated catch block
        exception.printStackTrace();
      } 
    }
  }
  
  public void addPacket(Packet p){
    this.lock.lock();
    try{
      if(!this.packetsOnFill.containsKey(p.sender)){
        this.packetsOnFill.put(p.sender, new LinkedList<Packet>());
      }    
      this.packetsOnFill.get(p.sender).add(p);
    }finally {
      this.lock.unlock();
    }    
  }
  
  private FusionTableNodeRecord processPacketList(LinkedList<Packet>  packetsOfNode, short nodeID){
	  FusionTableNodeRecord newNodeRecord = null;   
	  FusionTableNodeRecord lastNodeRecord = null;
      
      //FusionTableGlobalPeopleRecord np = new FusionTableGlobalPeopleRecord();
        // make average on this packet and send to the fusion table
        if(!packetsOfNode.isEmpty()){            
	        newNodeRecord = new FusionTableNodeRecord(nodeID);   
	        lastNodeRecord = this.lastNodeRecords.get(nodeID);
	        // lastNodeRecord is null if not present
	        // the node pointer
	        Node nn = BaseStationLogger.nodes.get(nodeID);
	        // for each packet 
	        for(int i = 0; i < packetsOfNode.size(); i++){
	          Packet pa = packetsOfNode.get(i);
	          if(nn.hasCapability("Counter"))
	            newNodeRecord.counter = pa.counter;
	          if(nn.hasCapability("Noise")  && pa.noise != 0){
	            newNodeRecord.noise += pa.noise;
	            newNodeRecord.noiseCounter++;
	          }
	          if(nn.hasCapability("CO2") && pa.co2 > 100){
	            newNodeRecord.co2 += pa.co2;
	            newNodeRecord.co2Counter++;
	          }
	          if(nn.hasCapability("People")){ // TODO: try with delta
	        	  int deltaIn = 0;
	        	  int deltaOut =0;
	            if(nn.lastInValue > pa.in || nn.lastOutValue > pa.out){ // shut down has been detected 
	            	deltaIn =  pa.in;
	            	deltaOut = pa.out;
	            }else {
	            	// calculate delta and sum it to the node record to obtain the epoch delta
	            	deltaIn =  (nn.lastInValue - pa.in);
	            	deltaOut = (nn.lastOutValue - pa.out);
	            	System.out.println("Calculated delta in steady sistuation for nodeID "+nn.ID+" in "+
	            	deltaIn+" out "+deltaOut);
	            }
	            nn.lastInValue = pa.in;
            	nn.lastOutValue = pa.out;
	            newNodeRecord.in += deltaIn;
	            newNodeRecord.out += deltaOut;
	            
	          }
	          if(nn.hasCapability("Pressure")  && (pa.pressure < 1400) && (pa.pressure > 700)){
	            newNodeRecord.pressure += pa.pressure;
	            newNodeRecord.pressureCounter++;
	          }
	          if(nn.hasCapability("Temp") &&(pa.temperature < 10000)  &&(pa.temperature > 0)){
	            newNodeRecord.temperature += (((double)pa.temperature/100));
	            newNodeRecord.temperatureCounter++;
	          }
	          if(nn.hasCapability("Light") && (pa.luminosity > 15)){
	            newNodeRecord.luminosity += pa.luminosity;
	            newNodeRecord.luminosityCounter++;
	          }            
	        } // end for each packet 
	        // control if some value has not been calculated due to spurious noise
	        newNodeRecord = purgeNodeRecord(newNodeRecord, lastNodeRecord);
	        // newNodeRecord is ready to be saved in the fusion table
        }// end is not empty
      return newNodeRecord;
  }
  
  private FusionTableNodeRecord purgeNodeRecord(
			FusionTableNodeRecord newNodeRecord,
			FusionTableNodeRecord lastNodeRecord) {
		
		// calculate averages
		newNodeRecord.co2 = newNodeRecord.co2Counter>0?newNodeRecord.co2/newNodeRecord.co2Counter:0;
		if(newNodeRecord.co2 == 0 && lastNodeRecord != null)
			newNodeRecord.co2 = lastNodeRecord.co2;
		
		newNodeRecord.noise = newNodeRecord.noiseCounter>0?newNodeRecord.noise/newNodeRecord.noiseCounter:0;
		if(newNodeRecord.noise == 0 && lastNodeRecord != null)
			newNodeRecord.noise = lastNodeRecord.noise;
		
		newNodeRecord.luminosity = newNodeRecord.luminosityCounter>0?newNodeRecord.luminosity/newNodeRecord.luminosityCounter:0;
		if(newNodeRecord.luminosity == 0 && lastNodeRecord != null)
			newNodeRecord.luminosity = lastNodeRecord.luminosity;
		
		newNodeRecord.pressure = newNodeRecord.pressureCounter>0?newNodeRecord.pressure/newNodeRecord.pressureCounter:0;
		if(newNodeRecord.pressure == 0 && lastNodeRecord != null)
			newNodeRecord.pressure = lastNodeRecord.pressure;
		
		newNodeRecord.temperature = newNodeRecord.temperatureCounter>0?newNodeRecord.temperature/newNodeRecord.temperatureCounter:0;
		if(newNodeRecord.temperature == 0 && lastNodeRecord != null)
			newNodeRecord.temperature = lastNodeRecord.temperature;
		
		
		return newNodeRecord;
	}

}
