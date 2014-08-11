package fr.piroxxi.mopidy.wombatclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
				new CallLoad().execute(values.get(position));
			}
		});

		new CallListPlaylists().execute(/* URL */);
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
				outToServer.writeBytes("listplaylists\r\n");

				Thread.sleep(450);
				while (inFromServer.ready()) {
					String line = inFromServer.readLine();
					if (line.startsWith("playlist: "))
						values.add(line.substring("playlist: ".length()));
				}
				clientSocket.close();

				return (long) 0;
			} catch (Exception e) {
				e.printStackTrace();
				return (long) -1;
			}
		}

		@Override
		public void onPostExecute(Long result) {
			adapter.notifyDataSetChanged();
		}
	}

	private class CallLoad extends AsyncTask<String, Integer, Long> {
		@Override
		protected Long doInBackground(String... playlists) {
			try {
				Socket clientSocket = new Socket("192.168.0.9", 6600);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.writeBytes("clear\r\n");
				
				Thread.sleep(450);
				outToServer.writeBytes("load \"" + playlists[0] + "\"\r\n");

				Thread.sleep(450);
				outToServer.writeBytes("play\r\n");

				Thread.sleep(450);
				clientSocket.close();

				return (long) 0;
			} catch (Exception e) {
				e.printStackTrace();
				return (long) -1;
			}
		}
	}
}
