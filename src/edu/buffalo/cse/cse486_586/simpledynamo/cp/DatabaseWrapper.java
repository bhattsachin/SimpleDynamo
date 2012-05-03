package edu.buffalo.cse.cse486_586.simpledynamo.cp;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseWrapper {
	private static DatabaseWrapper instance = new DatabaseWrapper();

	private DatabaseWrapper() {

	};

	public static DatabaseWrapper getInstance() {
		return instance;
	}

	public void insert(ContentResolver contentResolver, String key, String value) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(Table.KEY, key);
		contentValues.put(Table.VALUE, value);
		contentResolver.insert(Constants.CONTENT_URI, contentValues);

	}

	public Map<String, String> query(ContentResolver contentResolver, String key) {
		Map<String, String> store = new HashMap<String, String>();

		String query = null;
		if (key != null)
			query = Table.KEY + "='" + key + "'";
		Cursor cursor = contentResolver.query(Constants.CONTENT_URI, null,
				query, null, null);
		while (null != cursor && cursor.moveToNext()) {
			store.put(cursor.getString(0), cursor.getString(1));
		}
		return store;

	}

}
