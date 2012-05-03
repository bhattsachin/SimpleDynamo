package edu.buffalo.cse.cse486_586.simpledynamo;

import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.buffalo.cse.cse486_586.simpledynamo.cp.DatabaseWrapper;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Client;
import edu.buffalo.cse.cse486_586.simpledynamo.util.Configuration;

public class SimpleDynamoActivity extends Activity {
	ContentResolver cr;
	TextView responseObject;
	DatabaseWrapper wrap = DatabaseWrapper.getInstance();
	public static String uitext;
	Handler uiHandler = new UIHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		cr = getContentResolver();

		setPutHandle(R.id.put1, "put1");
		setPutHandle(R.id.put2, "put2");
		setPutHandle(R.id.put3, "put3");
		setGetHandle(R.id.get);
		setDumpHandle(R.id.dump);
		
		responseObject = (TextView) findViewById(R.id.response);

		Button test = (Button) findViewById(R.id.testOtherGuy);
		if (test != null) {
			test.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					//Client client = new Client(Configuration.CLIENT_IP);
					//client.send(null, 11112);
					responseObject.setText("");

				}
			});
		}

		

	}

	private void setPutHandle(final int id, final String prefix) {
		Button test = (Button) findViewById(id);
		if (test != null) {
			test.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						for (int i = 0; i < 10; i++) {
							send(i + "", prefix + "" + i);
							Log.d(SimpleDynamoActivity.class.getSimpleName(),
									"Message sent:" + i);
							try {
								Thread.sleep(3000);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					} finally {
					}
				}
			});
		}
	}

	private void setGetHandle(final int id) {
		Button test = (Button) findViewById(id);
		if (test != null) {
			test.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						String val;
						TextView dump = (TextView) findViewById(R.id.response);
						// dump.setText(dumpText);
						// dump.append(dumpText);
						UpdateResponse resp;
						Thread thread;
						for (int i = 0; i < 10; i++) {
							val = fetch(i + "");
							uitext = "<" + i +":" +  val + ">\n";
							thread = new Thread(new UIHandler());
							thread.start();
							//uiHandler.dispatchMessage(uiHandler.obtainMessage());
							// resp = new UpdateResponse(dump, val);
							// resp.start();
							// dump.append(i + " : " + val);
							try {
								Thread.sleep(3000);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					} finally {
					}
				}
			});
		}
	}

	private void setDumpHandle(final int id) {
		Button test = (Button) findViewById(id);
		if (test != null) {
			test.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						dump();

					} finally {
					}
				}
			});
		}
	}

	private void send(String key, String value) {
		wrap.insert(cr, key, value);
	}

	private String fetch(String key) {
		Map<String, String> response;
		response = wrap.query(cr, key);

		if (response != null && !response.isEmpty()) {
			return (String) response.values().toArray()[0];
		}

		return null;
	}

	private void dump() {
		StringBuffer dumpText = new StringBuffer("");

		// get data from local database
		Map<String, String> localValues = wrap.query(cr, "ALL");
		int i = 0;
		String hash = null;

		for (String key : localValues.keySet()) {
			hash = (key != null && key.length() > 5) ? key.substring(0, 5)
					: key;
			dumpText.append("\n< " + key + " : " + localValues.get(key) + " >");
			i++;
		}
		TextView dump = (TextView) findViewById(R.id.response);
		dump.setText(dumpText);
	}

	class UpdateResponse extends Thread {
		String txt;
		TextView view;

		public UpdateResponse(TextView view, String txt) {
			this.view = view;
			this.txt = txt;
		}

		public void run() {
			this.view.append(txt);
		}
	}

	class UpdateUI extends AsyncTask<String, String, String> {

		protected void onPostExecute(String result) {
			String val = result;

			//responseObject.append(val);
		}
		
		protected void onProgressUpdate(String... progress) {
	         responseObject.append(progress[0]);
	     }

		@Override
		protected String doInBackground(String... arg0) {
			publishProgress(arg0);
			return arg0[0];
		}
	}
	
	class UIHandler extends Handler implements Runnable{

		@Override
		public void handleMessage(Message msg) {
			new UpdateUI().execute(uitext);
			//super.handleMessage(msg);
		}
		
		public void run(){
			new UpdateUI().execute(uitext);
		}
		
		
	}
	
	class UIThread extends Thread{
		
		public void run(){
			new UpdateUI().execute(uitext);
		}
	}

}