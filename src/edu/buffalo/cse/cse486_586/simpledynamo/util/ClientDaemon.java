package edu.buffalo.cse.cse486_586.simpledynamo.util;

import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;

public class ClientDaemon implements Runnable {
	Message msg;
	Client client;
	
	public ClientDaemon(Message msg){
		this.msg = msg;
		client = new Client(Configuration.CLIENT_IP);
	}
	
	public void run() {
		client.send(this.msg, this.msg.getTo()*2);
	}

}
