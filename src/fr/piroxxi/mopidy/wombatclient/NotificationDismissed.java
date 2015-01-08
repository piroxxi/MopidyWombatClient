package fr.piroxxi.mopidy.wombatclient;

import android.app.Activity;
import android.os.Bundle;

public class NotificationDismissed extends Activity {
	static boolean dismissed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dismissed = true;
		this.finish();
	}

}
