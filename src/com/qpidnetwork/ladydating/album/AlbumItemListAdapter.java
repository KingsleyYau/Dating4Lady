package com.qpidnetwork.ladydating.album;

import java.util.ArrayList;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.UnscrollableGridView;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AlbumItemListAdapter extends BaseAdapter{

	public Context context;
	public ArrayList<AlbumItem> albumItemList;
	
	public AlbumItemListAdapter(Context context, ArrayList<AlbumItem> photoItems){
		this.context = context;
		this.albumItemList = photoItems;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return albumItemList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		convertView = LayoutInflater.from(context).inflate(R.layout.item_for_album_detail_list, null);
		holder = new ViewHolder(convertView);
		AlbumItem item = albumItemList.get(position);
		
		
		if (item.category == Category.PAST) holder.category.setText(R.string.approved);   //审核通过
		if (item.category == Category.UNDER_REVIEW) holder.category.setText(R.string.under_review);  //审核中
		if (item.category == Category.REQUIRED_EDIT) holder.category.setText(R.string.edition_required);  //打回 - 要求修改
		if (item.category == Category.REJECTED) holder.category.setText(R.string.rejected);  //拒绝
		
		holder.photoGridView.setAdapter(new PhotoItemAdapter(context, item.photoItems));
		if (Build.VERSION.SDK_INT < 21){
			holder.photoGridView.setSelector(R.drawable.touch_feedback_holo_light);
		}
		
		return convertView;
	}
	
	public static enum Category{
		PAST,
		UNDER_REVIEW,
		REQUIRED_EDIT,
		REJECTED
	}
	
	public static class AlbumItem{
		public Category category;
		public ArrayList<PhotoItemAdapter.PhotoItem> photoItems;
		
		public AlbumItem(Category category, ArrayList<PhotoItemAdapter.PhotoItem> photoItems){
			this.category = category;
			this.photoItems = photoItems;
		}
	}
	
	private class ViewHolder{
		
		public TextView category;
		public UnscrollableGridView photoGridView;
		
		public ViewHolder(View v){
			category = (TextView)v.findViewById(R.id.category);
			photoGridView = (UnscrollableGridView) v.findViewById(R.id.gridView);
			v.setTag(this);
		}
	}

}
