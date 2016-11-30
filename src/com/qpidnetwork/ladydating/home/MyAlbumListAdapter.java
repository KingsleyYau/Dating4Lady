package com.qpidnetwork.ladydating.home;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.RequestJniAlbum.AlbumType;
import com.qpidnetwork.request.item.AlbumListItem;
import com.qpidnetwork.tool.ImageViewLoader;


public class MyAlbumListAdapter extends BaseAdapter{

	private List<AlbumListItem> albumList;
	private Context mContext;
	
	public MyAlbumListAdapter(Context context, List<AlbumListItem> manList){
		this.mContext = context;
		this.albumList = manList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return albumList.size()+1;
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

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		
		if((convertView == null)){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_for_home_myalbum_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		
		if(position == 0){
			holder.line1.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
			holder.line2.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
			holder.flContent.setBackgroundResource(R.drawable.rectangle_stroke_blue);
			holder.image.setImageResource(R.drawable.ic_add_grey600_48dp);
			holder.albumTypeIcon.setVisibility(View.GONE);
			holder.itemNumber.setText(mContext.getResources().getString(R.string.create_album));
			holder.itemNumber.setTextColor(mContext.getResources().getColor(R.color.blue));
			holder.image.setScaleType(ScaleType.CENTER);
			holder.albumName.setVisibility(View.INVISIBLE);
			holder.image.setVisibility(View.VISIBLE);
			holder.noItemText.setVisibility(View.GONE);
		}else{
			holder.line1.setBackgroundColor(mContext.getResources().getColor(R.color.grey));
			holder.line2.setBackgroundColor(mContext.getResources().getColor(R.color.grey));
			holder.flContent.setBackgroundResource(R.drawable.rectangle_stroke_grey);
			holder.itemNumber.setTextColor(mContext.getResources().getColor(R.color.text_color_light));
			holder.image.setScaleType(ScaleType.CENTER_CROP);
			holder.albumTypeIcon.setVisibility(View.VISIBLE);
			holder.albumName.setVisibility(View.VISIBLE);
			
			AlbumListItem albumItem = albumList.get(position-1);
			
			if(!TextUtils.isEmpty(albumItem.imageUrl)){
				String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(albumItem.imageUrl);
				holder.imageDownLoader = new ImageViewLoader(mContext);
				holder.imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.default_photo_100dp));
				holder.imageDownLoader.DisplayImage(holder.image, albumItem.imageUrl, localPath, null);
			}
			
			holder.albumName.setText(albumItem.title);
			holder.albumTypeIcon.setVisibility(View.VISIBLE);
			holder.editFlag.setVisibility(View.GONE);
			
			if (albumItem.type == AlbumType.Photo){
				holder.albumTypeIcon.setImageResource(R.drawable.ic_collections_grey600_18dp);
				holder.itemNumber.setText(albumItem.count + " " + mContext.getResources().getString(R.string.Photos));
			}else if (albumItem.type == AlbumType.Video){
				holder.albumTypeIcon.setImageResource(R.drawable.ic_video_collection_grey600_18dp);
				holder.itemNumber.setText(albumItem.count + " " + mContext.getResources().getString(R.string.Videos));
			}
			
			if (albumItem.count > 0){
				holder.image.setVisibility(View.VISIBLE);
				holder.noItemText.setVisibility(View.GONE);
			}else{
				holder.image.setVisibility(View.GONE);
				holder.noItemText.setVisibility(View.VISIBLE);
			}
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
		public View line1;
		public View line2;
		public FrameLayout flContent;
		public ImageView image;
		public TextView noItemText;
		public ImageView albumTypeIcon;
		public TextView itemNumber;
		public TextView albumName;
		public ImageView editFlag;
		public ImageViewLoader imageDownLoader;

		
		public ViewHolder(View v){
			this.line1 = v.findViewById(R.id.line1);
			this.line2 = v.findViewById(R.id.line2);
			this.flContent = (FrameLayout) v.findViewById(R.id.flContent);
			this.image = (ImageView)v.findViewById(R.id.thumbnial);
			this.albumName = (TextView)v.findViewById(R.id.albumName);
			this.noItemText = (TextView)v.findViewById(R.id.noItemText);
			this.albumTypeIcon = (ImageView)v.findViewById(R.id.albumType);
			this.itemNumber  = (TextView)v.findViewById(R.id.itemNumber);
			this.editFlag = (ImageView)v.findViewById(R.id.editionFlag);
			this.imageDownLoader = null;
			v.setTag(this);
		}
	}
}
