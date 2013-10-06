package in.co.praveenkumar.groupchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Messaging extends Activity {
	// Settings
	public final String sendDataUrl = "http://home.iitb.ac.in/~praveendath92/groupChat/putData.php?value=";
	public final String fetchDataUrl = "http://home.iitb.ac.in/~praveendath92/groupChat/messages.txt";
	public final int updateFrequency = 500; // In milli seconds

	public static String nick = "default";
	public static String groupMessage = "No messages as of yet !";
	public static String userMessage = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messaging);

		// Receive the Nick from previous activity
		Bundle extras = getIntent().getExtras();
		nick = extras.getString("nick");

		// Setting up onClickListeners for send button
		Button loginButton = (Button) findViewById(R.id.sendButton);
		loginButton.setOnClickListener(sendButtonListener);

		// start group message sync
		new tryFetchingData().execute(fetchDataUrl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private OnClickListener sendButtonListener = new OnClickListener() {
		public void onClick(View v) {
			EditText userTypedMessage = (EditText) findViewById(R.id.userMessageBox);
			userMessage = userTypedMessage.getText().toString();

			// send user message to server
			new trySendingData().execute(sendDataUrl);

			// Clear the field..
			userTypedMessage.setText("");

		}
	};

	// Asynchronous thread for message updation..
	private class tryFetchingData extends AsyncTask<String, Integer, Long> {

		protected Long doInBackground(String... url) {
			getDataFromServer(url[0]);
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected void onPostExecute(Long result) {
			// Update content to the UI..
			updateMessages();

			// Wait before trying for next update..
			Handler myHandler = new Handler();
			myHandler.postDelayed(delayedUpdateLooper, updateFrequency);
		}
	}

	// Asynchronous thread for user message sending..
	private class trySendingData extends AsyncTask<String, Integer, Long> {

		protected Long doInBackground(String... url) {
			sendDataToServer(url[0]);
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			disableSendButton();
		}

		protected void onPostExecute(Long result) {
			enableSendButton();
		}
	}

	private Runnable delayedUpdateLooper = new Runnable() {
		@Override
		public void run() {
			new tryFetchingData().execute(fetchDataUrl);
		}
	};

	public void disableSendButton() {
		Button sendButton = (Button) this.findViewById(R.id.sendButton);
		sendButton.setEnabled(false);
		sendButton.setText("wait!");
	}

	public void enableSendButton() {
		Button sendButton = (Button) this.findViewById(R.id.sendButton);
		sendButton.setEnabled(true);
		sendButton.setText("send");
	}

	public void updateMessages() {
		TextView groupMessageBox = (TextView) this
				.findViewById(R.id.groupMessageBox);
		groupMessageBox.setText(groupMessage);

	}

	// Send data to server
	public void sendDataToServer(String url) {
		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String effectiveMessage = getEffectiveMessage(nick + " : "
					+ userMessage);
			HttpPost httpPost = new HttpPost(url + effectiveMessage);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				httpEntity.consumeContent();
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Fetching data from server
	public void getDataFromServer(String url) {
		String serverResponse = null;
		InputStream is = null;
		// Making HTTP request
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			serverResponse = sb.toString();
			if (serverResponse != null)
				groupMessage = serverResponse;
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

	}

	public String getEffectiveMessage(String data) {
		data = data.replaceAll("%", "%25");
		data = data.replaceAll("\\s", "%20");
		data = data.replaceAll("#", "%23");
		data = data.replaceAll("\\{", "%7B");
		data = data.replaceAll("\\|", "%7C");
		data = data.replaceAll("\\}", "%7D");
		data = data.replaceAll("<", "%3C");
		data = data.replaceAll(">", "%3E");
		data = data.replaceAll("\"", "%22");
		data = data.replaceAll("-", "%2D");
		data = data.replaceAll("&", "%26");
		data = data.replaceAll("\\\\", "%5C");
		return data;

	}
}
