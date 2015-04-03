package fr.piroxxi.mopidy.sdk;

import android.content.Context;



/**
 * It's quit complex to call the MPD server. It requires to 
 * create a TCP connection, to launch the command and to wait
 * for the result.
 * 
 * This is a simple SKD to summon a command.
 * 
 * @author PiroXXI
 */
public class MopidyConnection {
	private Context context;
	private String port;
	private String address;
	
	public MopidyConnection(String address, String port) {
		this.address = address;
		this.port = port;
	}

	/**
	 * Executes the "command" on the server side, and get's back to you via the callback.
	 * @param callback
	 * @param command
	 * 
	 * TODO(rpoittevin) May have a "not waiting for answer" flag, to perform asynchronous actions.
	 */
	public void executeCommand(MopidyCommandCallback callback, String... commands){
		new MopidyConnectionAsychronousTask(context,address,port,callback).execute(commands);
	}

	// FIXME
	public void setAddress(String address) {
		this.address = address;
	}

	// FIXME
	public void setPort(String port) {
		try{
			this.port = port;
		}catch(Exception e){
			return;
		}
	}

	public Context getContext() {
		return this.context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
}
