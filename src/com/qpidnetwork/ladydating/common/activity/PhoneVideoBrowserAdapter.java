package com.qpidnetwork.ladydating.common.activity;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;

import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.album.VideoItem;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.manager.FileCacheManager;

public class PhoneVideoBrowserAdapter extends BaseAdapter{

	private Context context;
	private ArrayList<VideoItem> photoList;
	
	/**
	 * Below SoftReference is responsible to keep the bitmap for further reuses which has been decoded. 
	 * At the same time, it can be recycled automatically by the system if necessary.
	 */
	private Map<String, SoftReference<Bitmap>> softReferenceMap = new HashMap<String, SoftReference<Bitmap>>();
	
	public PhoneVideoBrowserAdapter(Context context, ArrayList<VideoItem> photoList){
		this.context = context;
		this.photoList = photoList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return photoList.size();
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder;
		final VideoItem item = photoList.get(position);
		int size = (DeviceUtil.getScreenSize().x - Converter.dp2px(4)) / 3;
		if (convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_phone_video_browser, null);
			holder = new ViewHolder(convertView);
			setItemLayoutSize(holder, size);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		

		holder.duration.setText(item.duraion + "s");
		holder.thumbnailPhoto.setImageDrawable(new ColorDrawable(Color.BLACK));
		//holder.duration.setText(item.duraion + " " + context.getString(R.string.seconds));

		
		if (Build.VERSION.SDK_INT >= 21) 
			holder.touchPoint.setBackgroundResource(R.drawable.rectangle_ripple_holo_light);
		
		if(holder.mDownloader != null){
			holder.mDownloader.Reset();
		}
		if(!TextUtils.isEmpty(item.videoUri)){
			String localPath = FileCacheManager.getInstance().CacheVideoThumbnailFromVideoUri(item.videoUri);
			holder.mDownloader = new AsynCreateVideoThunbAndShow(context);
			//holder.mDownloader.SetDefaultImage(context.getResources().getDrawable(R.drawable.default_photo_64dp));
			holder.mDownloader.DisplayImage(holder.thumbnailPhoto, localPath, item.videoUri, size, size);
		}
		
		return convertView;
	}
	

	private int setItemLayoutSize(ViewHolder holder, int size){

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.thumbnailPhoto.getLayoutParams();
		params.height = size;
		params.width = size;
		RelativeLayout.LayoutParams duraionParams = (RelativeLayout.LayoutParams)holder.duration.getLayoutParams();
		duraionParams.width = size / 2;
		duraionParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		holder.thumbnailPhoto.setLayoutParams(params);
		holder.touchPoint.setLayoutParams(params);
		holder.duration.setLayoutParams(duraionParams);
		return size;	
	}
	
	private class ViewHolder{
		public TextView duration;
		public ImageView thumbnailPhoto;
		public View touchPoint;
		public AsynCreateVideoThunbAndShow mDownloader;
		public ViewHolder(View convertView){
			duration = (TextView) convertView.findViewById(R.id.duration);
			thumbnailPhoto = (ImageView) convertView.findViewById(R.id.image);
			touchPoint = convertView.findViewById(R.id.touchPoint);
			mDownloader = null;
			convertView.setTag(this);
		}
	}
	

}
