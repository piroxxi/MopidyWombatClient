package fr.piroxxi.mopidy.wombatclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import fr.piroxxi.mopidy.sdk.MopidyConnection;
import fr.piroxxi.mopidy.sdk.MopidyDefaultCommandCallback;
import fr.piroxxi.mopidy.sdk.search.SearchResult;

public class SearchActivity extends Activity {
	private ArrayAdapter<String> adapter;
	private ArrayList<String> values = new ArrayList<String>();

	private MopidyConnection connection = new MopidyConnection("192.168.0.9",
			"6600");
	
	private String currentSearch = "";
	private ArrayList<String> results = new ArrayList<String>();	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		SharedPreferences settings = getSharedPreferences(ListPlaylists.PREFS_NAME, 0);
		String addresse = settings.getString("addresse", "192.168.0.9");
		String port = settings.getString("port", "6600");
		connection.setAddress(addresse);
		connection.setPort(port);
		connection.setContext(this.getApplicationContext());
		
		// Get ListView object from xml
		ListView listView = (ListView) findViewById(R.id.list);
		
		final EditText searchField = (EditText) findViewById(R.id.editText1);
		searchField.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	currentSearch = searchField.getText().toString();
	        	updateValues();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
		
		setTitle(R.string.search);
		
		// new adapter to be updated
		adapter = new ArrayAdapter<String>(this, R.layout.left_drawer_item, values);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("piroxxi","clicked -> "+results.get(position));
			}
		});
	}
	
	public void updateValues(){
		/* Mise a jour contenu liste */
		connection.executeCommand(new MopidyDefaultCommandCallback() {
			@Override
			public void success(List<String> lines) {
				adapter.clear();
				results = new ArrayList<String>();
				HashMap<String, ArrayList<SearchResult>> searchResults = new HashMap<String, ArrayList<SearchResult>>(); 
				SearchResult currentResult = null;
				for( String line : lines ){
					if( line.startsWith("file: ") ){
						String fileType = line.substring("file: ".length());
						if( currentResult != null ){
							ArrayList<SearchResult> tt = searchResults.get(currentResult.type);
							if(tt == null){
								tt = new ArrayList<SearchResult>();
								searchResults.put(currentResult.type, tt);
							}
							tt.add(currentResult);
						}
						if( fileType.startsWith("spotify:track") ){
							currentResult = new SearchResult("Spotify track");
						} else if( fileType.startsWith("spotify:artist") ){
							currentResult = new SearchResult("Spotify artist");
						} else if( fileType.startsWith("spotify:album") ){
							currentResult = new SearchResult("Spotify album");
						}
						currentResult.file = fileType;
					}
					if( line.startsWith("Artist: ") ){
						currentResult.artist = line.substring("Artist: ".length());
					}
					if( line.startsWith("Title: ") ){
						currentResult.title = line.substring("Title: ".length());
					}
					if( line.startsWith("Album: ") ){
						currentResult.album = line.substring("Album: ".length());
					}
					if( line.startsWith("Track: ") ){
						currentResult.track = line.substring("Track: ".length());
					}
					if( line.startsWith("Date: ") ){
						currentResult.date = line.substring("Date: ".length());
					}
				}
				for( String key : searchResults.keySet() ){
					results.add("\nRésultats de type "+key+"\n");
					adapter.add("\nRésultats de type "+key+"\n");
					for( SearchResult item : searchResults.get(key) ){
						results.add(item.toString());
						adapter.add(item.toString());
					}
				}
				
				adapter.notifyDataSetChanged();
			}
		}, "search any \""+currentSearch+"\"");
	}
}
