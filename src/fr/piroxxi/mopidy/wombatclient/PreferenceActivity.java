package fr.piroxxi.mopidy.wombatclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PreferenceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference);

	    final SharedPreferences settings = getSharedPreferences(ListPlaylists.PREFS_NAME, 0);
		
		final Button save = ((Button) findViewById(R.id.button_save));
		
		final EditText server_address = ((EditText) findViewById(R.id.server_address));
		server_address.setText(settings.getString("addresse", "192.168.0.9"));
		
		final EditText server_port = ((EditText) findViewById(R.id.server_port));
		server_port.setText(settings.getString("port", "6600"));

		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("addresse", ""+server_address.getText());
				editor.putString("port", ""+server_port.getText());
				editor.commit();
				
				ListPlaylists.connection.setAddress(""+server_address.getText());
				ListPlaylists.connection.setPort(""+server_port.getText());
				
				Intent intent = new Intent(ListPlaylists.EVENT_CONFIGURATION_UPDATED);
		        intent.putExtra("configuration-updated", "updated");
				sendBroadcast(intent);
				
				PreferenceActivity.this.finish();
			}
		});
	}
}
