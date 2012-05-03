package edu.buffalo.cse.cse486_586.simpledynamo.cp;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.os.StrictMode;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.MessageType;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Client;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Configuration;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Membership;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Node;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Row;

public class Helper {

	Client client;
	Thread serverThread;

	public void init() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		// start server
		client = new Client(Configuration.CLIENT_IP);

	}

	/**
	 * 
	 * @param values
	 * @return
	 */
	public Message prepareMessage(ContentValues values) {
		Row rowData = new Row();
		rowData.setKey(values.getAsString(Table.KEY));
		if ((values.getAsString(Table.VALUE)) != null)
			rowData.setValue(values.getAsString(Table.VALUE));
		if (values.getAsString(Table.FOR) != null)
			rowData.setTarget(values.getAsString(Table.FOR));
		if (values.getAsInteger(Table.COUNT) != null)
			rowData.setVstamp(values.getAsInteger(Table.COUNT));
		// if this dude doesn't belong here
		Message message = new Message();
		Node nd = Membership.getRequestRouting(rowData.getKey());
		message.setFrom(Configuration.MY_EMULATOR_PORT);
		rowData.setTarget(String.valueOf(nd.getPort()));
		message.setTo(nd.getPort());
		// message.setType(MessageType.INSERT);
		message.setRow(rowData);
		return message;
	}

	/**
	 * sends message to required coordinator
	 * 
	 * @param msg
	 */

	/**
	 * public List<Integer> dispatchMessageForInsert(Message msg) {
	 * 
	 * int port = msg.getTo(); // check receipient if alive. else send to next
	 * node Node node = Membership.getNode(port); boolean isKingAlive =
	 * client.send(createHeartBeatMessage(port), port * 2); if (isKingAlive) {
	 * // regular stuff msg.setType(MessageType.INSERT);
	 * 
	 * } else { // report this guy is dead Membership.reportDead(port);
	 * List<Node> prefList = node.getPreferenceList(); for (Node guy : prefList)
	 * { if (guy.isAlive()) { node = guy; break; } } msg.setTo(node.getPort());
	 * msg.setType(MessageType.INSERTSLAVED); }
	 * 
	 * return insertWithQuorum(msg);
	 * 
	 * }
	 */

	private Message createHeartBeatMessage(int port) {
		Message msg = new Message();
		msg.setFrom(Configuration.MY_EMULATOR_PORT);
		msg.setTo(port);
		msg.setType(MessageType.HEARTBEAT);

		return msg;
	}

	/**
	 * We insert after creating a quorum here we need to find out if total
	 * number of alive nodes are in majority and if so go ahead with this
	 * transaction
	 * 
	 * @param msg
	 */

	public void sendQueryResponse(Message msg) {
		msg.setType(MessageType.QUERYRESPONSE);
		client.send(msg, msg.getFrom() * 2);
	}

	/**
	 * Finds the correct port to send m
	 * 
	 * @param key
	 * @return
	 */
	public int getPortToSendQuery(String key) {
		Node node = Membership.getRequestRouting(key);
		int port = node.getPort();
		List<Node> list = node.getPreferenceList();

		// check if this dude is alive
		if (client.isAlive(port*2)) {
			return port;
		} else {
			int k = 0;
			node = list.get(k);

			if (node == null) {
				return 0; // this should not have happened
			}
			port = node.getPort();

			while (node != null && !client.isAlive(port*2)) {

				k++;
				port = list.get(k).getPort();
			}

		}

		return port;
	}
	
	public int getPortToInsert(String key) {
		Node node = Membership.getRequestRouting(key);
		int port = node.getPort();
		List<Node> list = node.getPreferenceList();

		// check if this dude is alive
		if (client.isAlive(port*2)) {
			return port;
		} else {
			int k = 0;
			node = list.get(k);

			if (node == null) {
				return 0; // this should not have happened
			}
			port = node.getPort();

			while (node != null && !client.isAlive(port*2)) {

				k++;
				port = list.get(k).getPort();
			}

		}

		return port;
	}
	
	public int getPortToInsertByPort(int ports) {
		Node node = Membership.getNode(ports);
		int port = node.getPort();
		List<Node> list = node.getPreferenceList();

		// check if this dude is alive
		if (client.isAlive(port*2)) {
			return port;
		} else {
			int k = 0;
			node = list.get(k);

			if (node == null) {
				return 0; // this should not have happened
			}
			port = node.getPort();

			while (node != null && !client.isAlive(port*2)) {

				k++;
				port = list.get(k).getPort();
			}

		}

		return port;
	}
	
	

}
