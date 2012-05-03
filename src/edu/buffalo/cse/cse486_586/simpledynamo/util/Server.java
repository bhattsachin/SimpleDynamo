package edu.buffalo.cse.cse486_586.simpledynamo.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import android.util.Log;
import edu.buffalo.cse.cse486_586.simpledynamo.cp.DynamoProvider;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.MessageType;
import edu.buffalo.cse.cse486_586.simpledynamo.util.threads.BackupHandler;
import edu.buffalo.cse.cse486_586.simpledynamo.util.threads.CoordinatorHandler;
import edu.buffalo.cse.cse486_586.simpledynamo.util.threads.InsertHandler;
import edu.buffalo.cse.cse486_586.simpledynamo.util.threads.QueryHandler;

public class Server implements Runnable {
	private ServerSocket socket;
	private DynamoProvider provider;

	public Server(DynamoProvider provider, int port) throws IOException {
		this.socket = ServerSocketFactory.getDefault().createServerSocket(port);
		this.provider = provider;
	}

	public void run() {
		while (true) {
			// run this for ever
			Socket client = null;
			try {

				client = socket.accept();
				ObjectInputStream inpstr = new ObjectInputStream(
						client.getInputStream());
				Message obj = (Message) inpstr.readObject(); // this is your
																// object, keep
				ObjectOutputStream os; // it safe
				InsertHandler handler;
				ReplicationHandler repHandler;
				CoordinatorHandler coordinate;
				QueryHandler queryHandler;
				BackupHandler backupHandler;

				// do something with this
				if (obj != null) {
					Log.d("SERVER - object we got", obj.toString());

					switch (obj.getType()) {

					case HEARTBEAT:
						os = new ObjectOutputStream(client.getOutputStream());
						os.writeObject(obj);
						//os.flush();
						//os.close();
						break;
						
					case COORDINATEINSERT:
						coordinate = new CoordinatorHandler(client, obj, MessageType.INSERT);
						coordinate.start();
						break;
						
					case COORDINATEQUERY:
						coordinate = new CoordinatorHandler(client, obj, MessageType.QUERY);
						coordinate.start();
						break;

					case INSERT:
						handler = new InsertHandler(client, this.provider, obj);
						handler.start();
						break;

					case QUERY:
						queryHandler = new QueryHandler(this.provider, client, obj);
						queryHandler.start();
						break;

					case QUERYSLAVED:
						//query for given port and return
						break;
						
					case BACKUP:
						Row row = obj.getRow();
						String target = row.getTarget();
						backupHandler = new BackupHandler(client, Integer.parseInt(target), this.provider);
						backupHandler.start();
						
						break;

					default:
					}

				}

				// write back to acknowledge

				//client.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

}
