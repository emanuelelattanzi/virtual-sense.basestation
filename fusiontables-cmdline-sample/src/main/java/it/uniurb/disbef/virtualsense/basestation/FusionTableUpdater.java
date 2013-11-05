

package it.uniurb.disbef.virtualsense.basestation;

import com.google.api.services.samples.fusiontables.cmdline.FusionTablesSample;

import java.io.IOException;
import java.util.Date;
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
  int cycleCounter = 0;

  
  public FusionTableUpdater(){
    
  }
  
  @Override
  public void run(){
	  // start resetter thread
	  CounterResetter resetter = new CounterResetter();
	  resetter.start();
	  
    while(true){
      if(cycleCounter == 0)
		try {
			globalRecord = FusionTablesSample.initGlobalRecord(globalRecord);
		} catch (IOException exception1) {
			// TODO Auto-generated catch block
			exception1.printStackTrace();
		}
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
          }else {
        	  this.lastNodeRecords.put(nn,new FusionTableNodeRecord(nn.ID)); //to reset record if no packet have been received
          }

        }// end for each node  
        Enumeration<Node>nodes = this.lastNodeRecords.keys();
        // for each node
        if(this.packetsOnSend.size() > 0){
	        System.out.println("Calculating global values ...");
	        while(nodes.hasMoreElements()){        	
	        	Node n = nodes.nextElement();
	        	System.out.println("Processing node "+n.ID);
	        	FusionTableNodeRecord nodeRecord = this.lastNodeRecords.get(n);
	        	if(n.hasCapability("People") && nodeRecord.checked) {
	        		System.out.println("Processing node "+n.ID);
	        		globalRecord.in+=nodeRecord.in; // sum epoch deltas to global counters
	        		globalRecord.out+=nodeRecord.out;
	        		System.out.println("add to global record: "+nodeRecord.in);
	        		System.out.println("add to global record: "+nodeRecord.out);
	        		nodeRecord.checked = false;
	        	}

	        }
	        globalRecord.inside = globalRecord.in - globalRecord.out;      
	        if(globalRecord.inside < 0){
	        	globalRecord.inside = 0;
	        	globalRecord.out = globalRecord.in;
	        }
	        System.out.println("####   Global values inside: "+globalRecord.inside+
	        		" in: "+globalRecord.in+" out: "+globalRecord.out); 
	        
	                
	        // save to the fusion table the global counter values 
	        if(this.lastNodeRecords.size() > 0)
	        	try {
	        		FusionTablesSample.insertDataToGlobalCounter(System.currentTimeMillis(), ""+globalRecord.in, ""+globalRecord.out, ""+globalRecord.inside);
	        	} catch (IOException exception) {
	        		// TODO Auto-generated catch block
	        		exception.printStackTrace();
	        	}  
        }
        cycleCounter++;
        Thread.sleep(1000*60*5);
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
	        newNodeRecord.checked = true;
	        lastNodeRecord = this.lastNodeRecords.get(nodeID);
	        // lastNodeRecord is null if not present
	        // the node pointer
	        Node nn = BaseStationLogger.nodes.get(nodeID);
	        // for each packet 
	        System.out.println("Process packets of node "+nodeID);
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
	            	System.out.println("\tNode has been rebooted");
	            	System.out.println("\tResetting delta to the packet value in: "+pa.in+" out:"+pa.out);
	            	System.out.println("\tWas in: "+nn.lastInValue+" out:"+nn.lastOutValue);
	            }else {
	            	// calculate delta and sum it to the node record to obtain the epoch delta
	            	deltaIn =  (pa.in - nn.lastInValue );
	            	deltaOut = (pa.out - nn.lastOutValue);
	            	System.out.println("\tCalculated delta in steady sistuation for nodeID "+nn.ID+" in "+
	            	deltaIn+" out "+deltaOut);
	            	System.out.println("\tlastInValue : "+nn.lastInValue+" "
	            					+  "lastOutValue: "+nn.lastOutValue+" "
	            					+  "pa.in: "+pa.in+" pa.out: "+pa.out);
	            }
	            nn.lastInValue = pa.in;
            	nn.lastOutValue = pa.out;
	            newNodeRecord.in += deltaIn;
	            newNodeRecord.out += deltaOut;
	            System.out.println("\tNewNodeRecord.in : "+newNodeRecord.in+"\n"
    					+  "newNodeRecord.out "+newNodeRecord.out);
	            
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
	        System.out.println("\tFinal NodeRecord");
	        if(nn.hasCapability("People"))
	        	newNodeRecord.print();
	        System.out.println("\t ----- end node "+nodeID+" ------");
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
  
  private class CounterResetter extends Thread {
	  private Date lastCheck;
	  
	  public CounterResetter(){
		  lastCheck = new Date();
	  }
	  
	  public void run(){
		  while(true){
			  try {
				Date now = new Date();
				if(now.getDay() != lastCheck.getDay()){
				//if(now.getHours() != lastCheck.getHours()){
					System.out.println("Resetting globalRecord at "+now);
					System.out.println("in was: "+globalRecord.in+" and out was: "+globalRecord.out);
					globalRecord = new FusionTableGlobalPeopleRecord();
				}
				lastCheck = now;
				Thread.sleep(1000*60*10);
			  } catch (InterruptedException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			  }
		  }
		  
	  }
	  
  }

}
