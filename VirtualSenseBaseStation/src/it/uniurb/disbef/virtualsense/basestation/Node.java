package it.uniurb.disbef.virtualsense.basestation;

import java.awt.Point;
import java.util.LinkedList;

public class Node {
	public short ID = -1;
	public int xLocation;
	public int yLocation;
	public LinkedList<Packet> myPackets = new LinkedList<Packet>();
	public short lastCounter = 0;
	public int resetCounter = 0;
	public long lastPacketTimeStamp = 0;
	public short routedPacket = 0;
	
	public Node(short id){
		this.ID = id;
	}
	public Node(short id, int x, int y){
		this.ID = id;
		this.xLocation = x;
		this.yLocation = y;
	}
	
	public double getDistance(Node n){
		return new Point(n.xLocation, n.yLocation).distance(new Point(this.xLocation, this.yLocation));		
	}

}
