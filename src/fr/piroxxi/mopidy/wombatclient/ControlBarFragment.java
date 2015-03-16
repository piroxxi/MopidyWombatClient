package fr.piroxxi.mopidy.wombatclient;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import fr.piroxxi.mopidy.sdk.MopidyDefaultCommandCallback;

public class ControlBarFragment extends Fragment {
	private int volume;
	private int repeat;
	private ImageButton repeatButton;
	private int random;
	private ImageButton randomButton;
	
//	private Point p;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		// Retrieve the current state from the server
		ListPlaylists.connection.executeCommand(new MopidyDefaultCommandCallback() {
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
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	View fragmentView = inflater.inflate(R.layout.control_bar_fragment, container, false);
		
		// Add Buttons listners

		((ImageButton) fragmentView.findViewById(R.id.prev))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ListPlaylists.connection.executeCommand(null, "previous");
				}
			});
		((ImageButton) fragmentView.findViewById(R.id.play))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ListPlaylists.connection.executeCommand(null, "play");
				}
			});
		((ImageButton) fragmentView.findViewById(R.id.stop))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ListPlaylists.connection.executeCommand(null, "stop");
				}
			});
		((ImageButton) fragmentView.findViewById(R.id.next))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ListPlaylists.connection.executeCommand(null, "next");
				}
			});
		((ImageButton) fragmentView.findViewById(R.id.volume))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int[] location = new int[2];
					ImageButton button = (ImageButton) v.findViewById(R.id.volume);

					// Get the x, y location and store it in the location[] array
					// location[0] = x, location[1] = y.
					button.getLocationOnScreen(location);

					// Initialize the Point with x, and y positions
					Point p = new Point();
					p.x = location[0];
					p.y = location[1];
					
					/*
					 * Open a popup with the volume level.
					 */
					showPopup(getActivity(), p);
				}
			});
		
		repeatButton = (ImageButton) fragmentView.findViewById(R.id.repeat);
		repeatButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if( repeat == 0 ){
						ListPlaylists.connection.executeCommand(null, "repeat 1");
						repeat = 1;
						repeatButton.setImageResource(R.drawable.button_repeat);
					}else{
						ListPlaylists.connection.executeCommand(null, "repeat 0");
						repeat = 0;
						repeatButton.setImageResource(R.drawable.button_repeat_inactive);
					}
				}
			});
		
		randomButton = (ImageButton) fragmentView.findViewById(R.id.random);
		randomButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( random == 0 ){
					ListPlaylists.connection.executeCommand(null, "random 1");
					random = 1;
					randomButton.setImageResource(R.drawable.button_random);
				}else{
					ListPlaylists.connection.executeCommand(null, "random 0");
					random = 0;
					randomButton.setImageResource(R.drawable.button_random_inactive);
				}
			}
		});
        return fragmentView;
    }


	// The method that displays the popup.
	private void showPopup(final Activity context, Point p) {
		Display display = getActivity().getWindowManager().getDefaultDisplay();
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
		volume.setProgress(ControlBarFragment.this.volume);
		volume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				ListPlaylists.connection.executeCommand(null,
						"setvol " + seekBar.getProgress());
				ControlBarFragment.this.volume = seekBar.getProgress();
			}
		});
		
		// Clear the default translucent background
		popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(layout, Gravity.NO_GRAVITY, 0 , p.y - repeatButton.getHeight());
	}
}
