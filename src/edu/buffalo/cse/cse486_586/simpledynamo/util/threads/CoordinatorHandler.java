package edu.buffalo.cse.cse486_586.simpledynamo.util.threads;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.buffalo.cse.cse486_586.simpledynamo.cp.DynamoProvider;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.MessageType;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.ResponseType;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Configuration;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Membership;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Node;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Row;

/**
 * Job of this handler is to act like a coordinator create
 * 
 * @author bhatt
 * 
 */

public class CoordinatorHandler extends Thread implements BaseHandler{

	Socket socket;
	// Queue queue;
	Message msg;
	MessageType type;

	public CoordinatorHandler(Socket socket, Message msg, MessageType type) {
		this.socket = socket;
		this.msg = msg;
		this.type = type;
	}

	public void run() {

		BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
		int i = 0;

		// send to all three
		//change the message type to insert
		this.msg.setType(type);
		//if message type insert. add vector clock
		if(MessageType.INSERT == type){
			DynamoProvider.vectorClock[Configuration.MY_EMULATOR_PORT]++;
			Row row = msg.getRow();
			row.setVstamp(DynamoProvider.vectorClock[Configuration.MY_EMULATOR_PORT]);
			this.msg.setRow(row);
		}
		
		
		// this is main thread
		//find out the nodes to which we need to send response
		int sourcePort = Integer.parseInt(this.msg.getRow().getTarget()); //do null check
		Node node = Membership.getNode(sourcePort);
		int[] listOfPorts = new int[Configuration.QUORUM_SIZE+1];
		listOfPorts[0] = sourcePort;
		for(int k=1;k<=Configuration.QUORUM_SIZE;k++){
			listOfPorts[k] = node.getPreferenceList().get(k-1).getPort(); //check out this statement
		}
		
		
		// create 3 threads and watch for response
		for(int j : listOfPorts){
			PeerHandler peer = new PeerHandler(queue, this.msg, j*2);
			peer.start();
		}
		
		
		int success = 0;
		int failure = 0;
		Message responseMsg = null;
		// count if all messages been received
		Message data=null;
		while (i <= Configuration.QUORUM_SIZE) {
			
			try{
			data = queue.take();
			}catch(InterruptedException ex){
				ex.printStackTrace(); // fix this
			}
			if (data!=null && data.getStatus() == ResponseType.SUCCESS){
				success++;
				responseMsg = data; //we should also check if all passing values have the same value or majority have same
			}
			else
				failure++;
			i++;
		}
		
		//Quorum stuff here
		if(responseMsg==null){
			success=0;
			failure = 5;
			responseMsg = new Message();
			//just to say that this is error condition
		}
		
		
		if(success>=failure){
			responseMsg.setStatus(ResponseType.SUCCESS);
		}else{
			responseMsg.setStatus(ResponseType.FAILURE);
		}
		
		//write to socket and close
		try{
			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			os.writeObject(responseMsg);
			//os.flush();
		}catch(Exception ex){
			ex.printStackTrace(); //happens when requester dies
		}
		
	}

}
