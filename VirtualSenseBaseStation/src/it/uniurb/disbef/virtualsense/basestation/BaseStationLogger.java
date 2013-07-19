package it.uniurb.disbef.virtualsense.basestation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Hashtable;

public class BaseStationLogger {
	private static Date startTime = new Date();
	private static LinkedList<Packet> packets = new LinkedList<Packet>(); 
	private static Hashtable<Short,Node> nodes = new Hashtable<Short,Node>(); 
	
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
			FileOutputStream out = new FileOutputStream(startTime.toLocaleString().replace('/','-').trim(), true);
			PrintStream p = new PrintStream(out);
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
}
