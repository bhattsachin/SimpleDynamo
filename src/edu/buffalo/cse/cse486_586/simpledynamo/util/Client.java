package edu.buffalo.cse.cse486_586.simpledynamo.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import android.util.Log;

import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.MessageType;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.ResponseType;

public class Client {

	private Socket socket;
	private String host;

	public Client(String host) {
		this.host = host;
	}

	public Message send(Message message, int port) {
		boolean good = true;
		Message obj = null;
		try {
			socket = new Socket(host, port);
			socket.setSoTimeout(Configuration.TIMEOUT);
			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			os.writeObject(message);
			// os.flush();
			// os.close();

			// read the response back
			ObjectInputStream inpstr = new ObjectInputStream(
					socket.getInputStream());
			obj = (Message) inpstr.readObject(); // this is your object, keep it
													// safe

		} catch (ClassNotFoundException cx) {
			//cx.printStackTrace();
			// why this kolaveri?
			good = false;

		} catch (InterruptedIOException timeout) {
			//timeout.printStackTrace();
			good = false;
		} catch (IOException ex) {
			// there can be so many reasons why this error coming up.
			// let treat this as timeout
			//ex.printStackTrace();
			good = false;
		} finally {

			if (obj == null) {
				obj = new Message();
				obj.setStatus(ResponseType.FAILURE);
				Log.e("CLIENT", "error in send query:" + port);
			} else {
				obj.setStatus(ResponseType.SUCCESS);
			}

			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return obj;
	}

	public boolean isAlive(int port) {
		Message msg = new Message();
		msg.setType(MessageType.HEARTBEAT);
		Message returnObj = send(msg, port);
		if (returnObj.getStatus() == ResponseType.SUCCESS)
			return true;
		else
			return false;
	}

	public List<Row> sendBackupQuery(Message message, int port) {
		boolean good = true;
		List<Row> result = null;
		try {
			socket = new Socket(host, port);
			socket.setSoTimeout(Configuration.TIMEOUT);
			ObjectOutputStream os = new ObjectOutputStream(
					socket.getOutputStream());
			os.writeObject(message);

			// read the response back
			ObjectInputStream inpstr = new ObjectInputStream(
					socket.getInputStream());
			result = (List<Row>) inpstr.readObject(); // this is your object,
														// keep it
														// safe

		} catch (ClassNotFoundException cx) {
			//cx.printStackTrace();
			good = false;

		} catch (InterruptedIOException timeout) {
			//timeout.printStackTrace();
			good = false;
		} catch (IOException ex) {
			// there can be so many reasons why this error coming up.
			// let treat this as timeout
			//ex.printStackTrace();
			good = false;
		} finally {
			
			if(!good) Log.e("CLIENT", "error in send backup query:" + port);

			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

}
