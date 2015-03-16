package fr.piroxxi.mopidy.sdk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import android.os.AsyncTask;
import android.util.Log;

public class MopidyConnectionAsychronousTask extends AsyncTask<String, Integer, Long> {
	
	private String serverAddress;
	private String port;
	private MopidyCommandCallback callback;
	
	private ArrayList<String> lines;

	public MopidyConnectionAsychronousTask(String serverAddress, String port, MopidyCommandCallback callback){
		this.serverAddress = serverAddress;
		this.port = port;
		this.callback = callback;
		this.lines = new ArrayList<String>();
	}
	
	@Override
	protected Long doInBackground(String... actions) {
		if(actions.length < 1){
			callback.error(new Throwable("The actions list is empty"));
			return (long) -1;
		}
		
		try {
			Socket clientSocket = new Socket(serverAddress, Integer.parseInt(port));
			DataOutputStream outToServer = new DataOutputStream(
					clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			checkConnection(inFromServer);
			
			/* Send the command list */
			if(actions.length > 1){
				outToServer.writeBytes("command_list_begin\r\n");
				for (String action : actions) {
					outToServer.writeBytes(action + "\r\n");
				}
				outToServer.writeBytes("command_list_end\r\n");
			}else{
				outToServer.writeBytes(actions[0] + "\r\n");
			}
			

			if(callback == null){
				// Si il y a pas de callback; on attends pas le retour.
				clientSocket.close();
				return (long) 0;
			}

			waitUntilReady(inFromServer);
			String line;
			while ((line = inFromServer.readLine()) != null) {
				lines.add(line);
				if (line.equals("OK"))
					break; // At the end of the command, returns OK.
			}
			clientSocket.close();

			return (long) 0;
		} catch (Exception e) {
			if (callback != null)
				callback.error(e, actions);
			else
				Log.e("Mopidy",
						"An error occured while executing one of the commands "
								+ Arrays.toString(actions), e);

			return (long) -1;
		}
	}

	@Override
	public void onPostExecute(Long result) {
		if(callback != null)
			callback.success(lines);
	}

	private void waitUntilReady(BufferedReader inFromServer)
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

	private void checkConnection(BufferedReader inFromServer)
			throws IOException, InterruptedException {
		waitUntilReady(inFromServer);

		String line = inFromServer.readLine();
		if (line.startsWith("OK MPD"))
			return;

		throw new IOException("received an error from server : " + line);
	}
}
