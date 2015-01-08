package fr.piroxxi.mopidy.wombatclient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import fr.piroxxi.mopidy.sdk.MopidyConnection;
import fr.piroxxi.mopidy.sdk.MopidyDefaultCommandCallback;

public class ListPlaylists extends Activity {
	/*
	 * TODO(rpoittevin) Gérée les cas de texte accentué (UTF8 ?) pour pouvoir
	 * avoir des caracteres spéciaux dans les noms de playlist
	 * 
	 * TODO(rpoittevin) Gérer plusieures vues (Playlist en cours de lecture,
	 * Liste des playlists)
	 */
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> menuAdapter;
	private ArrayList<String> values = new ArrayList<String>();
	private ArrayList<String> menuValues = new ArrayList<String>();
	{
		menuValues.add("Playlist en cours");
		menuValues.add("Toutes les playlists");
		menuValues.add("Préferences");
	}

	private MopidyConnection connection = new MopidyConnection("192.168.0.9",
			6600);

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

	private int volume;
	private int repeat;
	private ImageButton repeatButton;
	private int random;
	private ImageButton randomButton;

	// The "x" and "y" position of the "Show Button" on screen.
	Point p;
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

		// Get ListView object from xml
		ListView listView = (ListView) findViewById(R.id.list);
		final ListView menuListView = (ListView) findViewById(R.id.left_drawer);

		// new adapter to be updated
		adapter = new ArrayAdapter<String>(this, 0, values) {
			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View view = inflater.inflate(R.layout.playlist_item, parent, false);
				TextView text = ((TextView) view.findViewById(R.id.playlistName));
				text.setText(this.getItem(position));
				text.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						connection.executeCommand(new MopidyDefaultCommandCallback() {
							@Override
							public void success(List<String> results) {
								postSongNameNotification(results);
								repeatedHandler.postDelayed(newRunnable, 1200);
							}
						}, "clear", "load \"" + values.get(position) + "\"", "play","currentsong");
					}
				});
				text.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						showPlaylistItemPopup(ListPlaylists.this, values.get(position));
						return true;
					}
				});
				((ImageButton) view.findViewById(R.id.show_more))
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							showPlaylistItemPopup(ListPlaylists.this, values.get(position));
						}
					});
				return view;
			}
		};
		listView.setAdapter(adapter);

		menuAdapter = new ArrayAdapter<String>(this,
				R.layout.left_drawer_item, menuValues);
		menuListView.setAdapter(menuAdapter);

		DrawerLayout menuLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
			}
		};

		menuLayout.setDrawerListener(menuToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		/* Mise a jour contenu liste */
		connection.executeCommand(new MopidyDefaultCommandCallback() {
			@Override
			public void success(List<String> results) {
				for (String result : results) {
					if (result.startsWith("playlist: "))
						adapter.add(result.substring("playlist: ".length()));
				}
				adapter.notifyDataSetChanged();
			}
		}, "listplaylists");
		connection.executeCommand(new MopidyDefaultCommandCallback() {
			@Override
			public void success(List<String> results) {
				for (String l : results) {
					if (l.startsWith("volume: "))
						volume = Integer.parseInt(l.substring("volume: "
								.length()));
					if (l.startsWith("repeat: "))
						repeat = Integer.parseInt(l.substring("repeat: "
								.length()));
					if (l.startsWith("random: "))
						random = Integer.parseInt(l.substring("random: "
								.length()));
				}

				if( repeat == 0 ){
					repeatButton.setImageResource(R.drawable.button_repeat_inactive);
				}else{
					repeatButton.setImageResource(R.drawable.button_repeat);
				}
				if( random == 0 ){
					randomButton.setImageResource(R.drawable.button_random_inactive);
				}else{
					randomButton.setImageResource(R.drawable.button_random);
				}
			}
		}, "status");

		/*
		 * Each 500ms, a the message with the current title will be updated;
		 */
		// repeatedHandler.post(newRunnable);
		repeatedHandler.postDelayed(newRunnable, 500);

		((ImageButton) findViewById(R.id.prev))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					connection.executeCommand(null, "previous");
				}
			});
		((ImageButton) findViewById(R.id.play))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					connection.executeCommand(null, "play");
				}
			});
		((ImageButton) findViewById(R.id.stop))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					connection.executeCommand(null, "stop");
				}
			});
		((ImageButton) findViewById(R.id.next))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					connection.executeCommand(null, "next");
				}
			});
		((ImageButton) findViewById(R.id.volume))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					/*
					 * Open a popup with the volume level.
					 * 
					 * @see
					 * http://androidresearch.wordpress.com/2012/05/06/how
					 * -to-create-popups-in-android/
					 */

					// Open popup window
					if (p != null)
						showPopup(ListPlaylists.this, p);
				}
			});
		
		repeatButton = (ImageButton) findViewById(R.id.repeat);
		repeatButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if( repeat == 0 ){
						connection.executeCommand(null, "repeat 1");
						repeat = 1;
						repeatButton.setImageResource(R.drawable.button_repeat);
					}else{
						connection.executeCommand(null, "repeat 0");
						repeat = 0;
						repeatButton.setImageResource(R.drawable.button_repeat_inactive);
					}
				}
			});
		
		randomButton = (ImageButton) findViewById(R.id.random);
		randomButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( random == 0 ){
					connection.executeCommand(null, "random 1");
					random = 1;
					randomButton.setImageResource(R.drawable.button_random);
				}else{
					connection.executeCommand(null, "random 0");
					random = 0;
					randomButton.setImageResource(R.drawable.button_random_inactive);
				}
			}
		});
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

	// The method that displays the popup.
	private void showPopup(final Activity context, Point p) {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int popupWidth = size.x;
		int popupHeight = repeatButton.getHeight();
		

		// Inflate the popup_layout.xml
		RelativeLayout viewGroup = (RelativeLayout) context
				.findViewById(R.id.relativeLayout1);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_volume_selector,
				viewGroup);

		// Creating the PopupWindow
		final PopupWindow popup = new PopupWindow(context);
		popup.setContentView(layout);
		popup.setWidth(popupWidth);
		popup.setHeight(popupHeight);
		popup.setFocusable(true);

		final SeekBar volume = (SeekBar) layout.findViewById(R.id.seekBar1);
		volume.setProgress(ListPlaylists.this.volume);
		volume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				connection.executeCommand(null,
						"setvol " + seekBar.getProgress());
				ListPlaylists.this.volume = seekBar.getProgress();
			}
		});
		
		// Clear the default translucent background
		popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(layout, Gravity.NO_GRAVITY, 0 , p.y - repeatButton.getHeight());
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
		
		// Clear the default translucent background
//		popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(layout, Gravity.NO_GRAVITY, (int)(size.x * 0.1), (int)(size.y * 0.25));
	}

	// Get the x and y position after the button is draw on screen
	// (It's important to note that we can't get the position in the onCreate(),
	// because at that stage most probably the view isn't drawn yet, so it will
	// return (0, 0))
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		int[] location = new int[2];
		ImageButton button = (ImageButton) findViewById(R.id.volume);

		// Get the x, y location and store it in the location[] array
		// location[0] = x, location[1] = y.
		button.getLocationOnScreen(location);

		// Initialize the Point with x, and y positions
		p = new Point();
		p.x = location[0];
		p.y = location[1];
	}
}
