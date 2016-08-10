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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.ImageUtil;

public class PhonePhotoBrowserAdapter extends BaseAdapter{

	private Context context;
	private ArrayList<PhotoItem> photoList;
	
	/**
	 * Below SoftReference is responsible to keep the bitmap for further reuses which has been decoded. 
	 * At the same time, it can be recycled automatically by the system if necessary.
	 */
	private Map<String, SoftReference<Bitmap>> softReferenceMap = new HashMap<String, SoftReference<Bitmap>>();
	
	public PhonePhotoBrowserAdapter(Context context, ArrayList<PhotoItem> photoList){
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
		PhotoItem item = photoList.get(position);
		
		if (convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_phone_photo_browser, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.photoUri = item.photoUri;
		holder.position = position;
		

		//holder.photo.setImageDrawable(context.getResources().getDrawable(R.drawable.default_photo_100dp));
		holder.photo.setImageDrawable(new ColorDrawable(Color.BLACK));
		
		final int viewSize = setItemLayoutSize(holder);
		
		if (Build.VERSION.SDK_INT >= 21) 
			holder.touchPoint.setBackgroundResource(R.drawable.rectangle_ripple_holo_light);
		
		if (softReferenceMap.containsKey(holder.photoUri) &&
				softReferenceMap.get(holder.photoUri).get() != null){
			holder.photo.setImageBitmap(softReferenceMap.get(item.photoUri).get());
		}else{
			holder.photo.setImageDrawable(new ColorDrawable(Color.BLACK));
			/**
			 * this trick will load image smoothly.
			 */
			new AsyncTask<ViewHolder, Void, SoftReference<Bitmap>>(){
				
				private ViewHolder holder;

				@Override
				protected SoftReference<Bitmap> doInBackground(ViewHolder... params) {
					// TODO Auto-generated method stub
					holder = params[0];
					if (softReferenceMap.containsKey(holder.photoUri) &&
						softReferenceMap.get(holder.photoUri).get() != null)
						return softReferenceMap.get(holder.photoUri);
					
					Bitmap bmp = ImageUtil.decodeSampledBitmapFromFile(holder.photoUri, viewSize, viewSize);
					softReferenceMap.put(holder.photoUri,  new SoftReference<Bitmap>(bmp));
					return softReferenceMap.get(holder.photoUri);
				}
				
				@Override
				protected void onPostExecute(SoftReference<Bitmap> softRefecence){
					super.onPostExecute(softRefecence);
					if (holder.position != position) return;
					holder.photo.setImageBitmap(softRefecence.get());
				}
				
			}.execute(holder);
		}
		
		return convertView;
	}
	
	private int setItemLayoutSize(ViewHolder holder){
		int size = (DeviceUtil.getScreenSize().x - Converter.dp2px(4)) / 3;
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.photo.getLayoutParams();
		params.height = size;
		params.width = size;
		holder.photo.setLayoutParams(params);
		holder.touchPoint.setLayoutParams(params);
		return size;
		
	}
	
	public static class PhotoItem{
		public String photoUri;
		public PhotoItem(String photoUri){
			this.photoUri = photoUri;
		}
	}
	
	private class ViewHolder{
		public ImageView photo;
		public View touchPoint;
		public int position;
		public String photoUri;
		public ViewHolder(View convertView){
			photo = (ImageView) convertView.findViewById(R.id.image);
			touchPoint = convertView.findViewById(R.id.touchPoint);
			convertView.setTag(this);
		}
	}
}
