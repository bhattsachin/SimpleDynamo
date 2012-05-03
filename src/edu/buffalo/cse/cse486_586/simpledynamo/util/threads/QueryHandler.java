package edu.buffalo.cse.cse486_586.simpledynamo.util.threads;

import java.io.ObjectOutputStream;
import java.net.Socket;

import android.util.Log;
import edu.buffalo.cse.cse486_586.simpledynamo.cp.DynamoProvider;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;

/**
 * fetch asked value from local store and
 * 
 * @author bhatt
 * 
 */
public class QueryHandler extends Thread implements BaseHandler {
	DynamoProvider provider;
	Socket socket;
	Message msg;

	public QueryHandler(DynamoProvider provider, Socket socket, Message msg) {
		this.provider = provider;
		this.socket = socket;
		this.msg = msg;

	}

	public void run() {
		Message response = provider.realQuery(this.msg);
		response.setStatus(edu.buffalo.cse.cse486_586.simpledynamo.requests.ResponseType.SUCCESS);

		try {

			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			os.writeObject(response);
			os.flush();
			socket.close();

		} catch (Exception ex) {
			ex.printStackTrace(); // supress it
			Log.d("QUERYHANDLER - query",
					"ERROR WRITING TO SOCKET" + msg.getFrom());
		}

	}

}
