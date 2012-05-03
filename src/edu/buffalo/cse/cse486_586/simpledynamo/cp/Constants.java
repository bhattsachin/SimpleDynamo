package edu.buffalo.cse.cse486_586.simpledynamo.cp;

import android.net.Uri;

public class Constants {
	
	public static final String DATABASE_NAME = "DynamoDB";
    
    public static final String TABLE_NAME = "Dynamo2";
    
    public static String AUTHORITY = "edu.buffalo.cse.cse486_586.simpledynamo.provider";
    
   	public static final Uri CONTENT_URI = Uri.parse("content://"
   			+ AUTHORITY + "/messages");


}
