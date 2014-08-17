package fr.piroxxi.mopidy.wombatclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ListPlaylists extends Activity {
	private ArrayAdapter<String> adapter;
	private ArrayList<String> values = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_list_playlists);

		// Get ListView object from xml
		ListView listView = (ListView) findViewById(R.id.list);

		// new adapter to be updated
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				new CallAction().execute("clear",
						"load \"" + values.get(position) + "\"",
						"play");
			}
		});

		new CallListPlaylists().execute(/* URL */);

		((Button) findViewById(R.id.prev))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new CallAction().execute("previous");
						AlertDialog alertDialog = new AlertDialog.Builder(
								ListPlaylists.this).create();
						alertDialog.setTitle("Title");
						alertDialog.setMessage("Message");
					}
				});
		((Button) findViewById(R.id.play))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new CallAction().execute("play");
					}
				});
		((Button) findViewById(R.id.stop))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new CallAction().execute("stop");
					}
				});
		((Button) findViewById(R.id.next))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new CallAction().execute("next");
					}
				});
	}

	private class CallListPlaylists extends AsyncTask<URL, Integer, Long> {
		@Override
		protected Long doInBackground(URL... urls) {
			try {
				Socket clientSocket = new Socket("192.168.0.9", 6600);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));

				ListPlaylists.checkConnection(inFromServer);

				Log.d("Msg", "> listplaylists\r\n");
				outToServer.writeBytes("listplaylists\r\n");

				ListPlaylists.waitUntilReady(inFromServer);
				String line;
				while ((line = inFromServer.readLine()) != null) {
					Log.d("Msg", "> line : " + line + "\r\n");
					if (line.startsWith("playlist: "))
						values.add(line.substring("playlist: ".length()));
					if (line.equals("OK"))
						break; // At the end of the command, returns OK.
				}
				Log.d("Msg", "> finished\r\n");
				clientSocket.close();

				return (long) 0;
			} catch (Exception e) {
				e.printStackTrace();
				return (long) -1;
			}
		}

		@Override
		public void onPostExecute(Long result) {
			Log.d("Msg", "> result is " + result + "... refreshing\r\n");
			adapter.notifyDataSetChanged();
		}
	}

	private class CallAction extends AsyncTask<String, Integer, Long> {
		@Override
		protected Long doInBackground(String... actions) {
			try {
				Socket clientSocket = new Socket("192.168.0.9", 6600);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));

				ListPlaylists.checkConnection(inFromServer);

				Log.d("Msg", "> command_list_begin\r\n");
				outToServer.writeBytes("command_list_begin\r\n");
				for (String action : actions) {
					Log.d("Msg", "> " + action + "\r\n");
					outToServer.writeBytes(action + "\r\n");
				}
				Log.d("Msg", "> command_list_end\r\n");
				outToServer.writeBytes("command_list_end\r\n");

				ListPlaylists.waitUntilReady(inFromServer);
				String line;
				while ((line = inFromServer.readLine()) != null) {
					Log.d("Msg", "> line : " + line + "\r\n");
					if (line.equals("OK"))
						break; // At the end of the command, returns OK.
				}
				Log.d("Msg", "> finished\r\n");
				clientSocket.close();

				return (long) 0;
			} catch (Exception e) {
				e.printStackTrace();
				return (long) -1;
			}
		}

		@Override
		public void onPostExecute(Long result) {
			Log.d("Msg", "> result is " + result + "... cool heh?\r\n");
		}
	}

	public static void waitUntilReady(BufferedReader inFromServer)
			throws IOException, InterruptedException {
		int attempts = 0;
		while (!inFromServer.ready() && attempts < 1000) {
			attempts++;
			Thread.sleep(10);
		}
		if (!inFromServer.ready()) {
			throw new IOException("Timeout: Server hasn't answered in 10sec.");
		}
	}

	public static void checkConnection(BufferedReader inFromServer)
			throws IOException, InterruptedException {
		waitUntilReady(inFromServer);

		String line = inFromServer.readLine();
		if (line.startsWith("OK MPD"))
			return;

		throw new IOException("received an error from server : " + line);
	}
}
