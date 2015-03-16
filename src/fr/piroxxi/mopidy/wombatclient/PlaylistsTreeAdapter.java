package fr.piroxxi.mopidy.wombatclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import fr.piroxxi.mopidy.sdk.playlist.tree.PlaylistItem;
import fr.piroxxi.mopidy.sdk.playlist.tree.PlaylistTree;

/**
 * 
 * @author PiroXXI
 * @see https://androidzoo.wordpress.com/2012/11/28/treeview-like-listview-in-androd-expandablelistview/
 */
public class PlaylistsTreeAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private ExpandableListView mListView;
	private PlaylistTree playlistTree;

	public PlaylistsTreeAdapter(Context pContext, ExpandableListView pListView, PlaylistTree playlistTree){
		this.mContext = pContext;
		this.mListView = pListView;
		this.playlistTree = playlistTree;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		PlaylistItem item = playlistTree.getSubTrees().get(groupPosition);
		if( item instanceof PlaylistTree )
			return ((PlaylistTree)item).getSubTrees().get(childPosition);
		else 
			return item;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view,
			ViewGroup parent) {
		PlaylistItem item = (PlaylistItem)getChild(groupPosition, childPosition);
		if(view == null){
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if( item instanceof PlaylistTree ){
//				view = inflater.inflate(R.layout.playlistFolder, null);

//				TextView txtCountry = (TextView)view.findViewById(R.id.txtCountry);
//				txtCountry.setText(item.getCountry());
			}else{
//				view = inflater.inflate(R.layout.playlistItem, null);

//				TextView txtCountry = (TextView)view.findViewById(R.id.txtCountry);
//				txtCountry.setText(item.getCountry());
			}
		}
		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		PlaylistItem item = playlistTree.getSubTrees().get(groupPosition);
		if( item instanceof PlaylistTree )
			return ((PlaylistTree)item).getSubTrees().size();
		else
			return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return playlistTree.getSubTrees().get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return playlistTree.getSubTrees().size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent) {
//		SampleModel model =  (SampleModel)getGroup(groupPosition);
//		if(view == null){
//			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
//			view = inflater.inflate(R.layout.group_item, null);
//		}
//
//		TextView txtContinent = (TextView)view.findViewById(R.id.txtContinent);
//		txtContinent.setText(model.getContinent());
		return view;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return true;
	}
}
