package it.uniurb.disbef.virtualsense.basestation;


import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;
import com.google.api.services.samples.fusiontables.cmdline.FusionTablesSample;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Locale;
import java.util.StringTokenizer;

public class BaseStationLogger {	
	private static LinkedList<Packet> packets = new LinkedList<Packet>(); 
	public static Hashtable<Short,Node> nodes = new Hashtable<Short,Node>(); 
	private static DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ITALY);
	private static TableList listOfTables = null; 
	private static FusionTableUpdater myUpdater;
	
	public static void resetStats(){
		packets = new LinkedList<Packet>(); 
	}
	
	public static void newPacket(Packet p){
		packets.add(p);
		//create a new node if needed (i.e. the sender, or routeIndexes are not known here)
		short senderID = p.sender;
		if(!nodes.containsKey(senderID)){
			Node n = new Node(senderID);			
			updateStatistics(n,p);
			nodes.put(senderID, n);
			System.out.println("DEBUG: added new sender node "+senderID);
		}else {
			Node n = nodes.get(senderID);
			updateStatistics(n,p);
		}
		// update nodes statistics		
		myUpdater.addPacket(p);
	}
	
	private static void updateStatistics(Node n, Packet p){
		n.lastPacketTimeStamp = p.time;
		n.myPackets.add(p);		
		//update routers statistics
		LinkedList<Short> routers = p.getHospIndexes();
		if(routers != null)
			for(int i = 0; i < routers.size(); i++){
				Short nodeID = routers.get(i);
				if(!nodes.containsKey(nodeID)){
					Node nn = new Node(nodeID);
					nn.routedPacket++;
					nodes.put(nodeID, n);
					System.out.println("DEBUG: added new router node");
				}else {
					Node nn = nodes.get(nodeID);
					nn.routedPacket++;
				}
			}
		// update resetCounter
		if(p.counter < n.lastCounter)
			n.resetCounter++;			
		n.lastCounter = p.counter;
	}

	public static void printNodesInfo() {
		// TODO Auto-generated method stub
		Enumeration<Node> e = nodes.elements();
		while(e.hasMoreElements()){
			Node n = e.nextElement();
			System.out.println("************* Node *****************");
			System.out.print("ID: ");
			System.out.println(n.ID);
			System.out.print("reset: ");
			System.out.println(n.resetCounter);
			System.out.print("routed: ");
			System.out.println(n.routedPacket);
			System.out.print("sent packets: ");
			System.out.println(n.myPackets.size());
			System.out.println("***********  END Node ***************");
			
		}
		
		
	}
	
	public static void log(String text){
		try {
			Date startTime = new Date();
			String name = df.format(startTime).replace('/','-').replace(' ','_').trim()+".log";
			FileOutputStream out = new FileOutputStream(name, true);
			PrintStream p = new PrintStream(out);
			if(text.indexOf("<packet>") != -1)
				text = text+" "+System.currentTimeMillis(); // to add  timestamp to the received packet 
			p.println(text);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	  public static void createNodesFromFile(String file){
	    
	    //TODO: create an init method to call listTables()
	        try {
            listOfTables = FusionTablesSample.listTables();
          } catch (IOException exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
          }	    
	    
	        BufferedReader bReader = null;
	        double maxLat = 1270;
	        double minLat = 1270;//90;
	        double maxLongi = 1270;
	        double minLongi = 1270;//90;
	        
	        try {
	            bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	            
	            String temp = bReader.readLine();
	            
	            while (temp != null) {
	                StringTokenizer tok = new StringTokenizer(temp, ";");
	                short nodeId = Short.parseShort(tok.nextToken());
	                int lati = Integer.parseInt(tok.nextToken());
	                int longi = Integer.parseInt(tok.nextToken());
	                String cap = tok.nextToken();
	                
	               
	               if(lati < minLat)
	                    minLat = lati;                    
	                                 
	                if(longi < minLongi)
	                    minLongi = longi;                    
	                                 
	                
	                Node n = new Node(nodeId, lati, longi, cap);
	                nodes.put(nodeId, n);	          
	                //System.out.println("Creato nodo con "+n.getStringID()+" "+n.getShortID());
	                temp = bReader.readLine();
	            }//end while
	            /*this.view.nodesPanel.maxLati = maxLat;
	            this.view.nodesPanel.minLati = minLat;
	            this.view.nodesPanel.maxLongi = maxLongi;
	            this.view.nodesPanel.minLongi = minLongi;
	            this.view.nodesPanel.updateNodes(nodes);*/
	            
	        } catch (Exception ex) {
	            System.out.println("Errore1:" +ex);
	        } finally {
	            //this.view.nodesPanel.updateNodes(nodes);
	            try {
	                bReader.close();
	            } catch (IOException ex) {
	                System.out.println("Errore2:" +ex);
	            }
	        }
	     }
	  
	  public static Table findTableByNodeID(short id){
	    Table tb = null;
	    //listOfTables.getItems();
	    for (Table table : listOfTables.getItems()) {
	        if(table.getName().endsWith("_"+id)){
	          tb = table;
	          break;
	        }
	    }
	    return tb;
	  }

    /**
     * @param updater
     */
    public static void setFusionTableUpdater(FusionTableUpdater updater) {
      myUpdater = updater;
      
    }

}
