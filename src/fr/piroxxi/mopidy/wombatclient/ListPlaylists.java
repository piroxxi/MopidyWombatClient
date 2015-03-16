package fr.piroxxi.mopidy.wombatclient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import fr.piroxxi.mopidy.sdk.MopidyConnection;
import fr.piroxxi.mopidy.sdk.MopidyDefaultCommandCallback;
import fr.piroxxi.mopidy.sdk.playlist.tree.PlaylistItem;
import fr.piroxxi.mopidy.sdk.playlist.tree.PlaylistTree;

public class ListPlaylists extends Activity {
	public static final String PREFS_NAME = "PreferencesMopidy";
	
	/*
	 * TODO(rpoittevin) Gérée les cas de texte accentué (UTF8 ?) pour pouvoir
	 * avoir des caracteres spéciaux dans les noms de playlist
	 * 
	 * TODO(rpoittevin) Gérer plusieures vues (Playlist en cours de lecture,
	 * Liste des playlists)
	 * 
	 * TODO(rpoittevin) Faire une vraie page de parametres; avec un meilleure
	 * gestion du stockage (créer le stockage si il n'existe pas, toujours se
	 * baser dessus... (en profiter pour enlever ce "connection" en statique) ).
	 * Egalement, il faudrait ajouter la notion "mise à jour des params"...
	 * qui va également avec la notion de "message d'erreure" lorsqu'il y a pas
	 * de parametres...
	 */
	private ArrayAdapter<PlaylistItem> adapter;
	private static PlaylistTree playlistTree = new PlaylistTree("");
	private ArrayList<PlaylistItem> values = new ArrayList<PlaylistItem>();
	
	private ArrayAdapter<String> menuAdapter;
	private ArrayList<String> menuValues = new ArrayList<String>();
	{
		menuValues.add("Playlist en cours");
		menuValues.add("Toutes les playlists");
		menuValues.add("Préferences");
	}

	public static MopidyConnection connection = new MopidyConnection("192.168.0.9", "6600");

	private Handler repeatedHandler = new Handler();
	private Runnable newRunnable = new Runnable() {
		@Override
		public void run() {
			connection.executeCommand(new MopidyDefaultCommandCallback() {
				@Override
				public void success(List<String> results) {
					postSongNameNotification(results);
					repeatedHandler.postDelayed(newRunnable, 1200);
				}
			}, "currentsong");
		}
	};
	
	private ActionBarDrawerToggle menuToggle;

	private String previousSong = null;

	protected void postSongNameNotification(List<String> results) {
		String title = "";
		String album = "";
		String artist = "";
		for (String l : results) {
			if (l.startsWith("Title: "))
				title = l.substring("Title: ".length());
			if (l.startsWith("Artist: "))
				artist = l.substring("Artist: ".length());
			if (l.startsWith("Album: "))
				album = l.substring("Album: ".length());
		}

        PendingIntent onClick =
        		PendingIntent.getActivity(this, 0, new Intent(this, ListPlaylists.class), 0);
        PendingIntent onDismiss =
        		PendingIntent.getActivity(this, 0, new Intent(this, NotificationDismissed.class), 0);
		
		NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (!title.equals("") || !album.equals("") || !artist.equals("")) {
			if( previousSong == null || !previousSong.equals(title+album+artist) ){
				previousSong = title+album+artist;
				NotificationDismissed.dismissed = false; //a.k.a song has changed
			}
			if( !NotificationDismissed.dismissed ){
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ListPlaylists.this)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle(title)
						.setContentText(album + " - " + artist)
		                .setContentIntent(onClick)
		                .setDeleteIntent(onDismiss);
				mNotifyMgr.notify("mopidy", 001, mBuilder.build());
			}
		} else {
			mNotifyMgr.cancel("mopidy", 001);
		}
	}

	/**
	 * When the Activity is created, this method populated all the
	 * components defined in the xml layout file in :
	 * * /res/layout/activity_list_playlists.xml
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_playlists);

		Intent intent = getIntent();
		final String folderName = intent.getStringExtra("folder-name");
		
		SharedPreferences settings = getSharedPreferences(ListPlaylists.PREFS_NAME, 0);
		String addresse = settings.getString("addresse", "192.168.0.9");
		String port = settings.getString("port", "6600");
		connection.setAddress(addresse);
		connection.setPort(port);

//		sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE);
//		ListPlaylists.connection.setAddress(sharedPref.getString(getString(R.string.server_address), "192.168.0.9"));
//		ListPlaylists.connection.setPort(sharedPref.getString(getString(R.string.server_port), "6600"));
		
		// Get ListView object from xml
		ListView listView = (ListView) findViewById(R.id.list);
		final ListView menuListView = (ListView) findViewById(R.id.left_drawer);

		if( folderName != null ){
			/* Si le folderName est setted, on a pas besoin de recharger les playlists */
			String[] levels = folderName.split("\\|");
			PlaylistTree treeLevel = ListPlaylists.playlistTree;
			for(int i=0 ; i < levels.length ; i++ ){
				for( PlaylistItem item : treeLevel.getSubTrees() ){
					if(item.getPlaylistName().equals(levels[i]) ){
						if( i == (levels.length - 1) ){
							values.clear();
							for( PlaylistItem subItem : ((PlaylistTree)item).getSubTrees() ){
								values.add(subItem);
							}
						}else{
							treeLevel = ((PlaylistTree)item);
						}
					}
				}
			}
		}
		
		// new adapter to be updated
		adapter = new ArrayAdapter<PlaylistItem>(this, 0, values) {
			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view;
				
				// Si l'element values.get(position) est un arbre (subtree), on affiche un bouton "+"
				if( values.get(position) instanceof PlaylistTree ){
					view = inflater.inflate(R.layout.playlist_folder, parent, false);
					TextView text = ((TextView) view.findViewById(R.id.folderName));
					text.setText(this.getItem(position).getPlaylistName());

					text.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(ListPlaylists.this, ListPlaylists.class);
							if( folderName != null ){
								intent.putExtra("folder-name", folderName+"|"+values.get(position).getPlaylistName());
							}else{
								intent.putExtra("folder-name", values.get(position).getPlaylistName());
							}
							startActivity(intent);
						}
					});
				}else{
					view = inflater.inflate(R.layout.playlist_item, parent, false);
					TextView text = ((TextView) view.findViewById(R.id.playlistName));
					text.setText(this.getItem(position).getPlaylistName());

					text.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							connection.executeCommand(new MopidyDefaultCommandCallback() {
								@Override
								public void success(List<String> results) {
									postSongNameNotification(results);
									repeatedHandler.postDelayed(newRunnable, 1200);
								}
							}, "clear", "load \"" + values.get(position).getPlaylistFullName() + "\"", "play","currentsong");
						}
					});
					text.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							showPlaylistItemPopup(ListPlaylists.this, values.get(position).getPlaylistFullName());
							return true;
						}
					});
					((ImageButton) view.findViewById(R.id.show_more))
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								showPlaylistItemPopup(ListPlaylists.this, values.get(position).getPlaylistFullName());
							}
						});
				}
				return view;
			}
		};
		listView.setAdapter(adapter);

		menuAdapter = new ArrayAdapter<String>(this,
				R.layout.left_drawer_item, menuValues);
		menuListView.setAdapter(menuAdapter);

		final DrawerLayout menuLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		menuToggle = new ActionBarDrawerToggle(this, /* host Activity */
		menuLayout, /* DrawerLayout object */
		R.drawable.ic_launcher, /* nav drawer icon to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description */
		R.string.drawer_close /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				/*
				 * FIXME(rpoittevin) comment colorier le menu selectionné au bon
				 * moment ?
				 */
				View firstChild = menuListView.getChildAt(1);
				firstChild.setBackgroundColor(getResources().getColor(
						R.color.selected_menu_item));
				menuListView.setOnItemClickListener(new OnItemClickListener() {
				    public void onItemClick(AdapterView<?> parent, View v, int position, long id){
				        if( "Préferences".equals(menuListView.getItemAtPosition(position)) ){
							Intent intent = new Intent(ListPlaylists.this, PreferenceActivity.class);
							startActivity(intent);
							menuLayout.closeDrawers();
				        }
				    }
				});
			}
		};

		menuLayout.setDrawerListener(menuToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		if( folderName == null ){
			/* Mise a jour contenu liste */
			connection.executeCommand(new MopidyDefaultCommandCallback() {
				@Override
				public void success(List<String> results) {
					ListPlaylists.playlistTree.getSubTrees().clear();
					values.clear();
					
					/* Creating the tree */
					for (String result : results) {
						if (result.startsWith("playlist: ")) {
							String[] words = result.substring("playlist: ".length())
									.split("\\| ");
							ListPlaylists.playlistTree.addPlaylist(words, result.substring("playlist: ".length()));
						}
						if (result.equals("OK"))
							break;
					}
					
					for( PlaylistItem item : ListPlaylists.playlistTree.getSubTrees() ){
						values.add(item);
	//					adapter.add(item);
					}
					
					adapter.notifyDataSetChanged();
				}
			}, "listplaylists");
		}
		
		/*
		 * Each 500ms, a the message with the current title will be updated;
		 */
		// repeatedHandler.post(newRunnable);
		repeatedHandler.postDelayed(newRunnable, 500);
	}

	// @see :
	// http://stackoverflow.com/questions/19889436/open-navigation-drawer-by-clicking-the-app-icon
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (menuToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showPlaylistItemPopup(final Activity context, final String playlist) {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		// Inflate the popup_playlist_item.xml
		LinearLayout viewGroup = (LinearLayout) context
				.findViewById(R.id.playlistItemPopupLayout);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_playlist_item,
				viewGroup);

		// Creating the PopupWindow
		final PopupWindow popup = new PopupWindow(context);
		popup.setContentView(layout);
		popup.setWidth( (int)(size.x * 0.8) );
		popup.setHeight( (int)(size.y * 0.5) );
		popup.setFocusable(true);

		((TextView) layout.findViewById(R.id.playlist_title)).setText(playlist);
		((Button) layout.findViewById(R.id.inspect_button))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ListPlaylists.this, ListSongsInPlaylist.class);
					intent.putExtra("playlist-name", playlist);
					startActivity(intent);
					popup.dismiss();
				}
			});
		((Button) layout.findViewById(R.id.add_button))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					connection.executeCommand(new MopidyDefaultCommandCallback() {
						@Override
						public void success(List<String> results) {
							postSongNameNotification(results);
							repeatedHandler.postDelayed(newRunnable, 1200);
						}
					}, "load \"" + playlist + "\"");
					popup.dismiss();
				}
			});
		((Button) layout.findViewById(R.id.play_button))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					connection.executeCommand(new MopidyDefaultCommandCallback() {
						@Override
						public void success(List<String> results) {
							postSongNameNotification(results);
							repeatedHandler.postDelayed(newRunnable, 1200);
						}
					}, "clear", "load \"" + playlist + "\"", "play","currentsong");
					popup.dismiss();
				}
			});

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(layout, Gravity.NO_GRAVITY, (int)(size.x * 0.1), (int)(size.y * 0.25));
	}
}
