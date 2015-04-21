package fr.piroxxi.mopidy.sdk.search;

public class SearchResult {
	public String type;
	public String artist;
	public String album;
	public String title;
	public String track;
	public String date;
	public String file;

	public SearchResult(String type) {
		this.type = type;
	}
	
	public String toString(){
		return title+" - "+album+" - "+artist+" ("+date+", "+track+" tracks)";
	}
}
