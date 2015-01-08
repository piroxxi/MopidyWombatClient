package fr.piroxxi.mopidy.sdk;

import java.util.Arrays;

import android.util.Log;

public abstract class MopidyDefaultCommandCallback implements
		MopidyCommandCallback {

	@Override
	public void error(Throwable error, String... commands) {
		Log.e("Mopidy", "An error occured while executing one of the commands "+Arrays.toString(commands),
				error);
	}

}
