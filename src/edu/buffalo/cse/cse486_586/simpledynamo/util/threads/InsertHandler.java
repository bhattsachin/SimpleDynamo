package edu.buffalo.cse.cse486_586.simpledynamo.util.threads;

import java.io.ObjectOutputStream;
import java.net.Socket;

import android.util.Log;
import edu.buffalo.cse.cse486_586.simpledynamo.cp.DynamoProvider;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.ResponseType;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Configuration;

public class InsertHandler extends Thread implements BaseHandler{
	Socket socket;
	DynamoProvider provider;
	Message obj;

	public InsertHandler(Socket socket, DynamoProvider provider, Message msg) {
		this.socket = socket;
		this.provider = provider;
		this.obj = msg;
	}

	public void run() {
		boolean good = true;

		try {

			// save this object in local now.
			provider.realInsert(obj.getRow());
			Log.d("INSERTHANDLER: added-", obj.toString());
		} catch (Exception ex) {
			// there can be so many reasons why this error coming up.
			// let treat this as timeout
			ex.printStackTrace();
			good = false;
		} finally {
			try {

				Message msg = new Message();
				msg.setFrom(Configuration.MY_EMULATOR_PORT);
				msg.setTo(socket.getPort()); // doesn't matter actually

				if (good)
					msg.setStatus(ResponseType.SUCCESS);
				else
					msg.setStatus(ResponseType.FAILURE);

				// if requester node dies during this time. this will result in
				// exception
				try {

					ObjectOutputStream os = new ObjectOutputStream(
							socket.getOutputStream());
					os.writeObject(msg);
					os.flush();
					Log.d("INSERTHANDLER - wrote object", msg.toString());

				} catch (Exception ex) {
					ex.printStackTrace(); // supress it
				}

				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
