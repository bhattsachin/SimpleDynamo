package edu.buffalo.cse.cse486_586.simpledynamo.cp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper{
	
	private static final String SQL_CREATE_DB = "CREATE TABLE "
			+ Constants.TABLE_NAME
			+ " "
			+ 
			"(" + Table.KEY
			+ " text primary key, "
			+ Table.VALUE
			+ " text," + " " + Table.FOR + " text," + " " + Table.COUNT + " integer)";

	public Database(Context context, String dbname) {
		super(context, dbname, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(SQL_CREATE_DB);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.d("Table", "Table already exists");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//not supported
	}

}
