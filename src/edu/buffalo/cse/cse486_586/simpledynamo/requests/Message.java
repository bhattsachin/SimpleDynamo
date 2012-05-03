package edu.buffalo.cse.cse486_586.simpledynamo.requests;

import java.io.Serializable;

import edu.buffalo.cse.cse486_586.simpledynamo.util.Row;

public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Row row;
	private int from;
	private int to;
	private MessageType type;
	private ResponseType status;
	public Row getRow() {
		return row;
	}
	public void setRow(Row row) {
		this.row = row;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public ResponseType getStatus() {
		return status;
	}
	public void setStatus(ResponseType status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "Message [row=" + row + ", from=" + from + ", to=" + to
				+ ", type=" + type + "]";
	}
	
	

}
