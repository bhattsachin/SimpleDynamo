package edu.buffalo.cse.cse486_586.simpledynamo.util.threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.ResponseType;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Configuration;

/**
 * Called by Coordinator, this class spawns sockets to remote machines and puts
 * the response back to queue
 * 
 * @author bhatt
 * 
 */
public class PeerHandler extends Thread implements BaseHandler{
	BlockingQueue<Message> queue;
	Message msg;
	int port;

	public PeerHandler(BlockingQueue<Message> queue, Message msg, int port) {
		this.queue = queue;
		this.msg = msg;
		this.port = port;
	}

	public void run() {
		// create a socket for the remote node
		Socket socket = null;
		Message response = null;
		try {
			socket = new Socket(Configuration.CLIENT_IP, this.port);
			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			os.writeObject(msg);
			//os.flush();
			ObjectInputStream inpstr = new ObjectInputStream(
					socket.getInputStream());
			response = (Message)inpstr.readObject();
		} catch (Exception ex) {
			// there can be so many reasons why this error coming up.
			// let treat this as timeout
			ex.printStackTrace();
		} finally {	
			if(response==null){
				Log.d("PEERHANDLER - this dude is dead", port + " ");
				response = new Message();
				response.setStatus(ResponseType.FAILURE);
				response.setFrom(port);
			}
			try{
			queue.put(response);
			socket.close();
			Log.d("PEERHANDLER - returned", response.toString());
			}catch(InterruptedException ex){
				ex.printStackTrace(); //check this out
				Log.d("PEERHANDLER - exception block", ex.getMessage());
			}catch(IOException ex){
				ex.printStackTrace(); //check this out
				Log.d("PEERHANDLER - exception block", ex.getMessage());
			}
		}

	}
}
