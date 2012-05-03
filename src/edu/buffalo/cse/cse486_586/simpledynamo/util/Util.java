package edu.buffalo.cse.cse486_586.simpledynamo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Util {

	public synchronized static String genHash(String input) {
		try{
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
		}catch(NoSuchAlgorithmException ex){
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try{
		System.out.println(Util.genHash("5554"));
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
