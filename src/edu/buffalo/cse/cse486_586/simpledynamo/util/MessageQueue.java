package edu.buffalo.cse.cse486_586.simpledynamo.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;

public class MessageQueue {
	
	public static BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<Message>();
	//public static BlockingQueue<Message> responseQueue = new LinkedBlockingQueue<Message>();

}
