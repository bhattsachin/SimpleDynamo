package edu.buffalo.cse.cse486_586.simpledynamo.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.Message;
import edu.buffalo.cse.cse486_586.simpledynamo.requests.MessageType;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Bootstrap;
import edu.buffalo.cse.cse486_586.simpledynamo.util.ClientDaemon;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Configuration;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Membership;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Node;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Row;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Server;

public class DynamoProvider extends ContentProvider {
	private static final String ALL = "ALL";
	private Database database;
	private SQLiteDatabase db;
	public Helper helper;
	public ClientDaemon clientDaemon;
	public Thread daemonThread = new Thread();
	public Thread serverThread;
	Server server;
	/**
	 * This is out of my laziness and easy accessibility of this array
	 */
	public static int[] vectorClock = new int[9999];

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		Message msg = helper.prepareMessage(values);
		int targetPort;
		Node node = Membership.getNode(msg.getTo());

		targetPort = helper.getPortToInsertByPort(msg.getTo());

		msg.setType(MessageType.COORDINATEINSERT);
		// it doesn't belong here, send it to appropriate place
		// what if this node dies by the time i reach here. it can be a
		// problem
		Message returnmsg = helper.client.send(msg, targetPort * 2);

		return null;

		// if he does.
		// create a record

		// send to peers and do a quorum which is unnecessary in this case

		// if success in quorum
		// save to all and reply

		// if i am the dude
		// ping other two guys and if anyone alive tell them to persist
		// List<Integer> successList = helper.dispatchMessageForInsert(msg);

		//return null;
	}

	public long realInsert(Row rowData) {

		// if the guy who called you is not one of your two prev nodes
		// (coordinator) then he might be dead
		ContentValues values = new ContentValues();
		values.put(Table.KEY, rowData.getKey());
		values.put(Table.VALUE, rowData.getValue());
		values.put(Table.FOR, rowData.getTarget());
		values.put(Table.COUNT, rowData.getVstamp());
		long row = 0;
		try {
			String[] where = new String[1];
			where[0] = rowData.getKey();
			String filter = Table.KEY + "=" + rowData.getKey();

			// update if need be
			row = db.update(Constants.TABLE_NAME, values, filter, null);

			if (row <= 0) {

				row = db.insert(Constants.TABLE_NAME, "", values);
			}

		} catch (RuntimeException ex) {
			ex.printStackTrace();
			Log.d("DYNAMOPROVIDER", "error in insert");

			if (row > 0)
				return row;
		}
		if (row > 0) {
			Uri _uri = ContentUris.withAppendedId(Constants.CONTENT_URI, row);
			getContext().getContentResolver().notifyChange(_uri, null);
			return row;
		}

		Log.d(DynamoProvider.class.getSimpleName(), "ERROR ADDING ROW");
		throw new SQLException("Failed to insert row");

	}

	@Override
	public boolean onCreate() {
		database = new Database(getContext(), Constants.DATABASE_NAME);
		db = database.getWritableDatabase();
		database.onCreate(db);

		helper = new Helper();
		helper.init();
		getTelephony();

		startServer();
		Bootstrap bootstrap = new Bootstrap();
		// Bootstrap.

		// recover in case it was failed or whatever
		backup();

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// if this belongs here
		String key = null;
		boolean skip = false;
		MatrixCursor pcursor = null;

		if (selection != null && !selection.isEmpty()) {
			key = selection.substring(6, selection.length() - 1);
		}
		// First element is always key
		if (ALL.equalsIgnoreCase(key)) {
			selection = null;
			skip = true;
		}

		if (!skip) {

			Node node = Membership.getRequestRouting(key);
			// so we don't have response present here.
			// send it to other dude and
			ContentValues cv = new ContentValues();
			cv.put(Table.KEY, key);
			Message msg = helper.prepareMessage(cv);

			msg.setType(MessageType.COORDINATEQUERY);
			int targetPort = helper.getPortToSendQuery(key);
			// it doesn't belong here, send it to appropriate place
			// what if this node dies by the time i reach here. it can be a
			// problem
			Message returnmsg = helper.client.send(msg, targetPort * 2);

			// now just wait on the queue for someone to send me something

			pcursor = new MatrixCursor(new String[] { Table.KEY, Table.VALUE });
			Row tuple;
			tuple = returnmsg.getRow();
			if (tuple != null && tuple.getKey() != null
					&& tuple.getValue() != null)
				pcursor.addRow(new String[] { tuple.getKey(), tuple.getValue() });

			Log.d("PROVIDER", "testblock");

		} else {

			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			builder.setTables(Constants.TABLE_NAME);

			Cursor cursor = builder.query(database.getReadableDatabase(),
					projection, selection, selectionArgs, null, null, null);
			if (cursor == null) {
				return null;
			} else if (!cursor.moveToFirst()) {
				cursor.close();
				return null;
			}
			String[] columns = new String[2];
			columns[0] = Table.KEY;
			columns[1] = Table.VALUE;
			pcursor = new MatrixCursor(columns);
			String[] obj;
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				obj = new String[2];
				obj[0] = cursor.getString(0);
				obj[1] = cursor.getString(1);
				pcursor.addRow(obj);
				cursor.moveToNext();
			}

		}

		return pcursor;
	}

	public Message realQuery(Message msg) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(Constants.TABLE_NAME);

		Row tuple = msg.getRow();

		String selection = Table.KEY + "=\'" + tuple.getKey() + "\'";

		Cursor cursor = builder.query(database.getReadableDatabase(), null,
				selection, null, null, null, null);

		if (cursor == null) {
			tuple.setValue("NOT FOUND");
			msg.setRow(tuple);
			// helper.sendQueryResponse(msg);
			// return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			tuple.setValue("NOT FOUND");
			msg.setRow(tuple);
			// helper.sendQueryResponse(msg);
			// return null;
		} else {
			tuple.setKey(tuple.getKey());
			tuple.setValue(cursor.getString(1));
		}

		cursor.moveToFirst();
		cursor.close();
		msg.setRow(tuple);
		msg.setFrom(msg.getFrom());
		// msg.setStatus(Res.Success);

		return msg;

	}

	public List<Row> backupQuery(int port) {

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(Constants.TABLE_NAME);
		List<Row> recoveryList = new ArrayList<Row>();

		String selection = Table.FOR + "=\'" + port + "\'";

		Cursor cursor = builder.query(database.getReadableDatabase(), null,
				selection, null, null, null, null);

		if (cursor == null) {
			return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		cursor.moveToFirst();
		Row row;
		while (cursor.isAfterLast() == false) {
			row = new Row();
			row.setKey(cursor.getString(0));
			row.setValue(cursor.getString(1));
			row.setTarget(cursor.getString(2));
			row.setVstamp(Integer.parseInt(cursor.getString(3)));
			recoveryList.add(row);
			cursor.moveToNext();
		}

		return recoveryList;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void getTelephony() {
		// get my port
		TelephonyManager telMgr = (TelephonyManager) getContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		Configuration.MY_EMULATOR_PORT = Integer.valueOf(telMgr
				.getLine1Number().substring(
						telMgr.getLine1Number().length() - 4));

	}

	private void startServer() {
		// start server
		try {
			server = new Server(this, Configuration.SERVER_PORT);
			serverThread = new Thread(server);
			serverThread.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void backup() {

		List<Row> updateList = new ArrayList<Row>();
		List<Row> tmpList = null;

		// query successors for my own messages
		Node myNode = Membership.getNode(Configuration.MY_EMULATOR_PORT);
		List<Node> successor = myNode.getPreferenceList();
		Message msg = new Message();
		Row row = new Row();
		row.setTarget(String.valueOf(Configuration.MY_EMULATOR_PORT));
		msg.setRow(row);
		msg.setType(MessageType.BACKUP);
		for (Node node : successor) {
			tmpList = helper.client.sendBackupQuery(msg, node.getPort() * 2);

			if (tmpList != null && tmpList.size() > 0) {
				for (Row trow : tmpList) {
					updateList.add(trow);
				}
			}
		}

		// for the node one behind
		myNode = Membership.getNodeAtRelativePostion(-1);
		successor = myNode.getPreferenceList();
		row.setTarget(String.valueOf(myNode.getPort()));
		msg.setRow(row);
		for (Node node1 : successor) {

			if (node1.getPort() == Configuration.MY_EMULATOR_PORT)
				continue; // I am still recovering no point querying self
			tmpList = helper.client.sendBackupQuery(msg, node1.getPort() * 2);

			if (tmpList != null && tmpList.size() > 0) {
				for (Row trow : tmpList) {
					updateList.add(trow);
				}
			}
		}

		// for the node two step behind
		myNode = Membership.getNodeAtRelativePostion(-2);
		successor = myNode.getPreferenceList();
		row.setTarget(String.valueOf(myNode.getPort()));
		msg.setRow(row);
		for (Node node2 : successor) {

			if (node2.getPort() == Configuration.MY_EMULATOR_PORT)
				continue; // I am still recovering no point querying self

			tmpList = helper.client.sendBackupQuery(msg, node2.getPort() * 2);

			if (tmpList != null && tmpList.size() > 0) {
				for (Row trow : tmpList) {
					updateList.add(trow);
				}
			}
		}

		// Now persist all these record to self
		for (Row row1 : updateList) {
			realInsert(row1); // hopefully this will succeed
		}

	}

}
