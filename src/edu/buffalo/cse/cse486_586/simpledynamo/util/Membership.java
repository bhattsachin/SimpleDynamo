package edu.buffalo.cse.cse486_586.simpledynamo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Membership {
	
	public static List<Node> membersList = new ArrayList<Node>();
	
	public static void doMembership(){
		//since multiple threads updating this list, make it thread safe
		Collections.synchronizedList(membersList);
		Integer port;
		Node node;
		for (int i = 0; i < Configuration.NUMBER_OF_NODES; i++) {
			node = new Node();
			port = (Configuration.BASE_PORT + 2 * i);
			node.setPort(port);
			try{
			node.setHashValue(Util.genHash(String.valueOf(port)));
			}catch(Exception ex){
				ex.printStackTrace();
				//You don't want to come here
				node.setHashValue(String.valueOf(port));
			}
			membersList.add(node);
		}
		
		//sort members
		Collections.sort(membersList);
		int i=0;
		List<Node> prefList;
		for(Node nd : membersList){
			prefList = new ArrayList<Node>();
			if(i==Configuration.NUMBER_OF_NODES-1){ //last node will have first two elements has pref list elements
				prefList.add(membersList.get(0)); //first element
				prefList.add(membersList.get(1));
			}else if(i==Configuration.NUMBER_OF_NODES-2){ //second last node
				prefList.add(membersList.get(i+1)); //last element
				prefList.add(membersList.get(0)); //first element
			}else{
				prefList.add(membersList.get(i+1)); //last element
				prefList.add(membersList.get(i+2)); //first element
			}
			nd.setPreferenceList(prefList); //setting preference list
			i=i+1;
		}
		
	}
	
	/**
	 * Returns request routing node
	 * @param key
	 * @return
	 */
	public static Node getRequestRouting(String key){
		String hashvalue = "";
		try{
		hashvalue = Util.genHash(key);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		for(Node node : membersList){
			if(node.getHashValue().compareTo(hashvalue)>=0){
				return node;
			}
		}
		
		//hash value is greater than the hash value of the last node.
		//return first element
		return membersList.get(0);
		
	}
	
	public static boolean belongsHere(String key){
		Node node = getRequestRouting(key);
		if(node.getPort()==Configuration.MY_EMULATOR_PORT){
			return true;
		}
		
		return false;
	}
	
	public static Node getNodeAtRelativePostion(int position){
		
		Node node = new Node();
		node.setPort(Configuration.MY_EMULATOR_PORT);
		node.setHashValue(Util.genHash(String.valueOf(Configuration.MY_EMULATOR_PORT)));
		int i = membersList.indexOf(node);
		
		if(position==0) return membersList.get(i);
		
		if(position==-1){
			if(i!=0){
				return membersList.get(i-1);
			}else{
				return membersList.get(Configuration.NUMBER_OF_NODES-1);
			}
		}
		
		if(position==-2){
			if(i==0) return membersList.get(Configuration.NUMBER_OF_NODES-2);
			if(i==1) return membersList.get(Configuration.NUMBER_OF_NODES-1);
			return membersList.get(i-2);
		}
		
		if(position==1){
			if(i==4) return membersList.get(0);
			return membersList.get(i+1);
		}
		
		if(position==2){
			if(i==4) return membersList.get(1);
			if(i==3) return membersList.get(0);
			return membersList.get(i+2);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param port
	 * @return
	 */
	public static Node getNode(int port){
		for(Node nod : membersList){
			if(nod.getPort()==port) return nod;
		}
		return null;
	}
	
	public static void reportDead(int port){
		for(Node nod : membersList){
			if(nod.getPort()==port){
				nod.setAlive(false);
				break;
			}
			
		}
	}
	
	
	

}
