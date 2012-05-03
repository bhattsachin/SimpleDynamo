package edu.buffalo.cse.cse486_586.simpledynamo.util;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node>{
	private int port;
	private boolean alive = true;
	private List<Node> preferenceList = new ArrayList<Node>();
	private String hashValue;
	
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isAlive() {
		return alive;
	}
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	public List<Node> getPreferenceList() {
		return preferenceList;
	}
	public void setPreferenceList(List<Node> preferenceList) {
		this.preferenceList = preferenceList;
	}
	public String getHashValue() {
		return hashValue;
	}
	public void setHashValue(String hashValue) {
		this.hashValue = hashValue;
	}
	public int compareTo(Node another) {
		if(this.hashValue!=null && another!=null && another.getHashValue()!=null)
		return this.hashValue.compareTo(another.hashValue);
		
		//by default returning equal which it should never reach
		return 0;
	}
	@Override
	public boolean equals(Object o) {
		Node node = (Node)o;
		return this.hashValue.equals(node.getHashValue());
	}
	
	
	

}
