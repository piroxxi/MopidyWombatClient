package fr.piroxxi.mopidy.sdk.playlist.tree;

public class PlaylistItem {
	private String playlistName;
	private String playlistFullName;
	public PlaylistItem(String playlistName){
		this.playlistName = playlistName;
		this.playlistFullName = null;
	}
	public PlaylistItem(String playlistName, String playlistFullName){
		this.playlistName = playlistName;
		this.playlistFullName = playlistFullName;
	}
	public String getPlaylistName(){
		return playlistName;
	}
	public void setPlaylistName(String playlistName){
		this.playlistName = playlistName;
	}
	public String getPlaylistFullName(){
		return playlistFullName;
	}
	public void setPlaylistFullName(String playlistFullName){
		this.playlistFullName = playlistFullName;
	}
	public void printPlaylists() {
		printPlaylists("");
	}
	public void printPlaylists(String prefix) {
		if ( playlistFullName != null )
			System.out.println(prefix+playlistName+" ["+playlistFullName+"]");
		else
			System.out.println(prefix+playlistName);
	}
}
