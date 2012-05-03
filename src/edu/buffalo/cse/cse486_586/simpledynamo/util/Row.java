package edu.buffalo.cse.cse486_586.simpledynamo.util;

import java.io.Serializable;

public class Row implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String key;
	private String value;
	private String target;
	private int vstamp;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public int getVstamp() {
		return vstamp;
	}
	public void setVstamp(int vstamp) {
		this.vstamp = vstamp;
	}
	@Override
	public String toString() {
		return "Row [key=" + key + ", value=" + value + ", target=" + target
				+ ", vstamp=" + vstamp + "]";
	}
	
		
	
}
