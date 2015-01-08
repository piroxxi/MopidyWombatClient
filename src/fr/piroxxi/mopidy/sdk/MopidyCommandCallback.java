package fr.piroxxi.mopidy.sdk;

import java.util.List;

/**
 * As the calls to MPD are asynchronous, you need to use a callback.
 */
public interface MopidyCommandCallback{
	/**
	 * Called when your command completed, with the result you're looking for.
	 * @param results The results lines. Will at least contain 'OK'.
	 */
	public void success(List<String> results);
	
	/**
	 * Called in case of emergency. Argument Error should give you more infos.
	 * @param error The reason of all your troubles.
	 */
	public void error(Throwable error, String... commands);
}