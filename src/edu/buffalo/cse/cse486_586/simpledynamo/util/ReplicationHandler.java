package edu.buffalo.cse.cse486_586.simpledynamo.util;

import edu.buffalo.cse.cse486_586.simpledynamo.cp.DynamoProvider;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;

public class ReplicationHandler extends Thread {
	Message msg;
	DynamoProvider provider;

	public ReplicationHandler(DynamoProvider provider, Message msg) {
		this.provider = provider;
		this.msg = msg;
	}

	public void run() {
		if (this.msg != null && this.msg.getRow() != null) {
			Row row = this.msg.getRow();
			this.provider.realInsert(row); //and lets hope it gets saved
		}

	}

}
