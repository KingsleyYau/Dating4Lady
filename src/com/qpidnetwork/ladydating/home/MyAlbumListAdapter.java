package com.qpidnetwork.ladydating.home;

import java.util.List;
import java.util.Random;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.DrawableUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyAlbumListAdapter extends BaseAdapter{

	private List<AlbumItem> albumList;
	private Context context;
	private int[] brandingColors = new int[]{
			R.color.brand_color_light11,
			R.color.brand_color_light12,
			R.color.brand_color_light13,
			R.color.brand_color_light14,
			R.color.brand_color_light15,
	};
	
	public MyAlbumListAdapter(Context context, List<AlbumItem> manList){
		this.context = context;
		this.albumList = manList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return albumList.size();
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
		AlbumItem albumItem = albumList.get(position);
		
		if (position == 0){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_home_myalbum_list_create_album, null);
			((ImageView) convertView.findViewById(R.id.thumbnial)).setImageDrawable(DrawableUtil.getDrawable(R.drawable.ic_add_white_48dp, context.getResources().getColor(R.color.blue)));
			calculateImageViewSize(convertView.findViewById(R.id.thumbnial));
			return convertView;
		}
		
		convertView = LayoutInflater.from(context).inflate(R.layout.item_for_home_myalbum_list, null);
		holder = new ViewHolder(convertView);
		
		Random random = new Random();
		holder.noItemText.setBackgroundColor(context.getResources().getColor(brandingColors[random.nextInt(brandingColors.length)]));
		holder.image.setBackgroundColor(context.getResources().getColor(brandingColors[random.nextInt(brandingColors.length)]));
		
		holder.albumName.setText(albumItem.albumName);
		holder.albumTypeIcon.setVisibility(View.VISIBLE);
		holder.editFlag.setVisibility(View.GONE);
		
		if (albumItem.albumType == AlbumItem.AlbumType.PHOTO){
			holder.albumTypeIcon.setImageResource(R.drawable.ic_collections_grey600_18dp);
			holder.itemNumber.setText(albumItem.itemCount + " " + context.getResources().getString(R.string.Photos));
		}else if (albumItem.albumType == AlbumItem.AlbumType.VIDEO){
			holder.albumTypeIcon.setImageResource(R.drawable.ic_video_collection_grey600_18dp);
			holder.itemNumber.setText(albumItem.itemCount + " " + context.getResources().getString(R.string.Videos));
		}else{
			holder.editFlag.setVisibility(View.VISIBLE);
			holder.albumTypeIcon.setVisibility(View.GONE);
			holder.albumName.setText(context.getResources().getString(R.string.items_require_your_action));
			holder.itemNumber.setText(albumItem.itemCount + " " + context.getResources().getString(R.string.items));
		}
		
		if (albumItem.itemCount > 0){
			holder.image.setVisibility(View.VISIBLE);
			holder.noItemText.setVisibility(View.GONE);
		}else{
			holder.image.setVisibility(View.GONE);
			holder.noItemText.setVisibility(View.VISIBLE);
		}
		
		calculateImageViewSize(holder.image);
		calculateImageViewSize(holder.noItemText);
		
		return convertView;
	}
	
	
	private void calculateImageViewSize(View view){
		int size = (DeviceUtil.getScreenSize().x - Converter.dp2px(20)) / 2;
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
		params.height = size;
		params.width = size;
	}
	
	private class ViewHolder{
		public ImageView image;
		public TextView noItemText;
		public ImageView albumTypeIcon;
		public TextView itemNumber;
		public TextView albumName;
		public ImageView editFlag;

		
		public ViewHolder(View v){
			this.image = (ImageView)v.findViewById(R.id.thumbnial);
			this.albumName = (TextView)v.findViewById(R.id.albumName);
			this.noItemText = (TextView)v.findViewById(R.id.noItemText);
			this.albumTypeIcon = (ImageView)v.findViewById(R.id.albumType);
			this.itemNumber  = (TextView)v.findViewById(R.id.itemNumber);
			this.editFlag = (ImageView)v.findViewById(R.id.editionFlag);
			v.setTag(this);
		}
	}
	
	
	public static class AlbumItem{
		public String imageUrl;
		public String albumName;
		public String albumDescription = "";
		public AlbumType albumType;
		public int itemCount;
		
		public static enum AlbumType{
			PHOTO,
			VIDEO,
			EDIT
		}
		
		public AlbumItem(String imageUrl, String albumName, AlbumType albumType, int itemCount){
			this.imageUrl = imageUrl;
			this.albumName = albumName;
			this.albumType = albumType;
			this.itemCount = itemCount;
			
		}
		
	}

}
