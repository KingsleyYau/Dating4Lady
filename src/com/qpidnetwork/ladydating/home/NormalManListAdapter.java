package com.qpidnetwork.ladydating.home;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.bean.ManInfoBean;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestEnum.PHOTO_STATUS;
import com.qpidnetwork.tool.ImageViewLoader;

public class NormalManListAdapter extends BaseAdapter{

	private List<ManInfoBean> manList;
	private Context mContext;

	public NormalManListAdapter(Context context, List<ManInfoBean> manList){
		this.mContext = context;
		this.manList = manList;
	}
	
	@Override
	public int getCount() {
		return manList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_for_home_manlist, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		ManInfoBean manItem = manList.get(position);
		
		/*头像处理*/
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if((manItem.photo_status == PHOTO_STATUS.Yes)&&(!TextUtils.isEmpty(manItem.photoUrl))){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(manItem.photoUrl);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			holder.imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.default_photo_64dp));
			holder.imageDownLoader.DisplayImage(holder.image, manItem.photoUrl, localPath, null);
		}else{
			holder.image.setImageResource(R.drawable.default_photo_64dp);
		}
		
		if(manItem.isOnline){
			holder.onlineFlag.setVisibility(View.VISIBLE);
		}else{
			holder.onlineFlag.setVisibility(View.GONE);
		}
		
		String countryName = "";
		if(!TextUtils.isEmpty(manItem.country)){
			countryName = StringUtil.getCountryNameByCode(mContext, manItem.country);;
		}else{
			if(manItem.countryEnum.ordinal() >=0 && manItem.countryEnum.ordinal() <= Country.Other.ordinal()){
				String[] countries = mContext.getResources().getStringArray(R.array.country_without_code);
				countryName = countries[manItem.countryEnum.ordinal()];
			}
		}
		
		holder.firstName.setText(manItem.userName);
		holder.age.setText(String.valueOf(manItem.age));
		holder.country.setText(countryName);
		
		return convertView;
	}
	
	private class ViewHolder{
		public ImageView image;
		public ImageView onlineFlag;
		public TextView firstName;
		public TextView age;
		public TextView country;
		public ImageViewLoader imageDownLoader;
		
		public ViewHolder(View v){
			this.image = (ImageView)v.findViewById(R.id.image);
			this.onlineFlag = (ImageView)v.findViewById(R.id.onlineFlag);
			this.firstName = (TextView)v.findViewById(R.id.firstName);
			this.age = (TextView)v.findViewById(R.id.age);
			this.country = (TextView)v.findViewById(R.id.country);
			this.imageDownLoader = null;
			v.setTag(this);
		}
	}
}
