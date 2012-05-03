package edu.buffalo.cse.cse486_586.simpledynamo.util.threads;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import android.util.Log;

import edu.buffalo.cse.cse486_586.simpledynamo.cp.DynamoProvider;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Row;

public class BackupHandler extends Thread implements BaseHandler{

	Socket socket;
	int port;
	DynamoProvider provider;
	
	public BackupHandler(Socket socket, int port, DynamoProvider provider){
		this.socket = socket;
		this.port = port;
		this.provider = provider;
	}
	
	
	public void run(){
		
		List<Row> result = provider.backupQuery(this.port);
		
		try {

			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			os.writeObject(result);
			os.flush();
			socket.close();

		} catch (Exception ex) {
			ex.printStackTrace(); // supress it
			Log.d("BACKUPHANDLER - backup",
					"ERROR WRITING TO SOCKET" + this.port);
		}
		
		
		
	}
	
}
