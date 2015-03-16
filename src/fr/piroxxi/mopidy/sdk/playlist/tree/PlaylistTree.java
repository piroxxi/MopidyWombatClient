package fr.piroxxi.mopidy.sdk.playlist.tree;

import java.util.ArrayList;
import java.util.Arrays;

public class PlaylistTree extends PlaylistItem {
	private ArrayList<PlaylistItem> subTrees;

	public PlaylistTree(String value) {
		super(value);
		subTrees = new ArrayList<PlaylistItem>();
	}

	public void addPlaylist(String[] words, String playlistName) {
		if (words.length > 1) {
			System.out.println(Arrays.asList(words));
			for (PlaylistItem item : subTrees) {
				if (!item.getPlaylistName().equals(words[0]))
					continue;

				if (!(item instanceof PlaylistTree))
					continue;

				((PlaylistTree) item).addPlaylist(Arrays.copyOfRange(words, 1,
						words.length), playlistName);
				return;
			}
			PlaylistTree item = new PlaylistTree(words[0]);
			item.addPlaylist(Arrays.copyOfRange(words, 1, words.length), playlistName);
			subTrees.add(item);
		} else {
			subTrees.add(new PlaylistLeaf(words[0], playlistName));
		}
	}

	@Override
	public void printPlaylists(String prefix) {
		super.printPlaylists(prefix);
		for (PlaylistItem item : subTrees)
			item.printPlaylists(prefix + "- ");
	}

	public ArrayList<PlaylistItem> getSubTrees() {
		return subTrees;
	}

	public void setSubTrees(ArrayList<PlaylistItem> subTrees) {
		this.subTrees = subTrees;
	}
}
