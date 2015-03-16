package fr.piroxxi.mopidy.wombatclient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import fr.piroxxi.mopidy.sdk.MopidyConnection;
import fr.piroxxi.mopidy.sdk.MopidyDefaultCommandCallback;

public class ListSongsInPlaylist extends Activity {
	private ArrayAdapter<String> adapter;
	private ArrayList<String> values = new ArrayList<String>();

	private MopidyConnection connection = new MopidyConnection("192.168.0.9",
			"6600");
	
	private ArrayList<MopidySong> playlistSongs = new ArrayList<MopidySong>();
	public class MopidySong{
		public String file;
		public String title;
		public String artist;
		public String time;
		public String album;
		public String date;
		public String track;
		public String albumArtist;
	}
	
	private String playlistName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_songs_in_playlist);

		SharedPreferences settings = getSharedPreferences(ListPlaylists.PREFS_NAME, 0);
		String addresse = settings.getString("addresse", "192.168.0.9");
		String port = settings.getString("port", "6600");
		connection.setAddress(addresse);
		connection.setPort(port);
		
		// Get ListView object from xml
		ListView listView = (ListView) findViewById(R.id.list);

		Intent intent = getIntent();
		playlistName = intent.getStringExtra("playlist-name");
		setTitle(playlistName);
		
		// new adapter to be updated
		adapter = new ArrayAdapter<String>(this, R.layout.left_drawer_item, values);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				connection.executeCommand(new MopidyDefaultCommandCallback() {
					
					@Override
					public void success(List<String> results) {
						for(String a : results)
							Log.d("piroxxi","=>"+a);
					}
				}, "clear", "load \"" + playlistName + "\"", "play "+position);
			}
		});
		
		/* Mise a jour contenu liste */
		connection.executeCommand(new MopidyDefaultCommandCallback() {
			@Override
			public void success(List<String> results) {
				playlistSongs = new ArrayList<ListSongsInPlaylist.MopidySong>();
				MopidySong currentSong = null;
				for (String result : results) {
					if (result.startsWith("file: ")){
						if( currentSong != null )
							playlistSongs.add(currentSong);
						currentSong = new MopidySong();
						currentSong.file = result.substring("file: ".length());
					} else if (result.startsWith("Time: ")){
						currentSong.time = result.substring("Time: ".length());
					} else if (result.startsWith("Artist: ")){
						currentSong.artist = result.substring("Artist: ".length());
					} else if (result.startsWith("Title: ")){
						currentSong.title = result.substring("Title: ".length());
					} else if (result.startsWith("Album: ")){
						currentSong.album = result.substring("Album: ".length());
					} else if (result.startsWith("Date: ")){
						currentSong.date = result.substring("Date: ".length());
					} else if (result.startsWith("Track: ")){
						currentSong.track = result.substring("Track: ".length());
					} else if (result.startsWith("AlbumArtist: ")){
						currentSong.albumArtist = result.substring("AlbumArtist: ".length());
					}
				}
				playlistSongs.add(currentSong);
				
				for(MopidySong song : playlistSongs)
					adapter.add(song.title+" - "+song.album+" - "+song.artist);
				
				adapter.notifyDataSetChanged();
			}
		}, "listplaylistinfo \""+playlistName+"\"");
	}
}
