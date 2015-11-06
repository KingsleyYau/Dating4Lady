package com.qpidnetwork.ladydating.album;

import java.util.ArrayList;
import java.util.Random;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.DrawableUtil;
import com.qpidnetwork.ladydating.utility.ImageUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhotoItemAdapter extends BaseAdapter{

	private ArrayList<PhotoItem> itemList;
	private Context context;

	public PhotoItemAdapter(Context context, ArrayList<PhotoItem> itemList){
		this.context = context;
		this.itemList = itemList;
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return itemList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_album_detail_list_gridview, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		
		PhotoItem item =itemList.get(position);
		
		
		holder.uploadProgress.setVisibility(View.GONE);
		holder.retryButton.setVisibility(View.GONE);
		holder.editButton.setVisibility(View.GONE);
		holder.title.setText(item.title);
		int itemSize = setupImageSize(holder.photo);
		if (item.photoUri != null){
			holder.photo.setImageBitmap(ImageUtil.decodeSampledBitmapFromFile(item.photoUri, itemSize, itemSize));
		}
		
		
		
		switch (item.reviewStatus){
		case REQUIRED_EDIT:
			holder.editButton.setVisibility(View.VISIBLE);
			break;
		case PAST:
		case UNDER_REVIEW:
		case REJECTED:
		default:
			break;
		
		}
		
		switch(item.uploadState){
		case UPLOADING:
			holder.uploadProgress.setVisibility(View.VISIBLE);
			break;
		case UPLOADED:
			break;
		case UPLOAD_FAILED:
			holder.retryButton.setVisibility(View.VISIBLE);
			break;
		}
		
		return convertView;
	}
	
	private int setupImageSize(View v){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
		int size = (DeviceUtil.getScreenSize().x - (Converter.dp2px(4 * 6))) / 3;
		params.width = size;
		params.height = size;
		v.setLayoutParams(params);
		return size;
	}
	
	public static enum ReviewStatus{
		PAST,
		UNDER_REVIEW,
		REJECTED,
		REQUIRED_EDIT
	}
	
	public static enum UploadState{
		UPLOADING,
		UPLOADED,
		UPLOAD_FAILED
	}
	
	public static class PhotoItem{
		public String title;
		public ReviewStatus reviewStatus;
		public UploadState uploadState = UploadState.UPLOADED;
		public String photoUri;
		
		public PhotoItem(String title, ReviewStatus reviewStatus){
			this.title = title;
			this.reviewStatus = reviewStatus;
		}
	}
	
	
	private class ViewHolder{
		public ImageView photo;
		public TextView title;
		public ImageButton editButton;
		public ImageButton retryButton;
		public ProgressBar uploadProgress;
		
		public ViewHolder(View convertView){
			this.photo = (ImageView)convertView.findViewById(R.id.photo);
			this.title = (TextView)convertView.findViewById(R.id.title);
			this.editButton = (ImageButton)convertView.findViewById(R.id.editButton);
			this.photo.setBackgroundColor(DrawableUtil.getRandomBrandingColor());
			this.retryButton = (ImageButton)convertView.findViewById(R.id.retryButton);
			this.uploadProgress = (ProgressBar)convertView.findViewById(R.id.uploadProgres);
			convertView.setTag(this);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
