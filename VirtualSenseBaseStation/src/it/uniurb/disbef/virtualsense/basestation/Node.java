package it.uniurb.disbef.virtualsense.basestation;

import java.util.LinkedList;

public class Node {
	short ID = -1;
	LinkedList<Packet> myPackets = new LinkedList<Packet>();
	short lastCounter = 0;
	int resetCounter = 0;
	long lastPacketTimeStamp = 0;
	short routedPacket = 0;
	
	public Node(short id){
		this.ID = id;
	}

}
