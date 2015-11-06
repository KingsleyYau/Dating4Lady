package com.qpidnetwork.ladydating.home;

import java.util.List;
import java.util.Random;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LiveChatListAdapter extends BaseAdapter{

	private List<ManItem> manList;
	private Context context;

	public LiveChatListAdapter(Context context, List<ManItem> manList){
		this.context = context;
		this.manList = manList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return manList.size();
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
		ManItem manItem = manList.get(position);
		
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_home_livechat_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.masterView.setBackgroundColor((manItem.unreadFlag)? context.getResources().getColor(R.color.touch_feedback_holo_light) : Color.TRANSPARENT);
		holder.firstName.setText(manItem.firstname);
		holder.deacription.setText(manItem.description);
		holder.onlineFlag.setVisibility((manItem.onlineFlag)? View.VISIBLE : View.GONE);
		holder.unreadFlag.setVisibility((manItem.unreadFlag)? View.VISIBLE : View.GONE);
		holder.favoriteFlag.setVisibility((manItem.favoriteFlag)? View.VISIBLE : View.GONE);
		holder.inchatFlag.setVisibility((manItem.inchatFlag)? View.VISIBLE : View.GONE);

		
		return convertView;
	}
	
	

	
	private class ViewHolder{
		
		public LinearLayout masterView;
		public ImageView image;
		public TextView firstName;
		public TextView deacription;
		public ImageView onlineFlag;
		public ImageView unreadFlag;
		public ImageView favoriteFlag;
		public ImageView inchatFlag;

		
		public ViewHolder(View v){
			this.masterView = (LinearLayout)v.findViewById(R.id.masterView);
			this.image = (ImageView)v.findViewById(R.id.avatar);
			this.firstName = (TextView)v.findViewById(R.id.firstName);
			this.deacription = (TextView)v.findViewById(R.id.description);
			this.onlineFlag = (ImageView)v.findViewById(R.id.onlineFlag);
			this.unreadFlag = (ImageView)v.findViewById(R.id.readFlag);
			this.favoriteFlag = (ImageView)v.findViewById(R.id.favoriteFlag);
			this.inchatFlag = (ImageView)v.findViewById(R.id.inchatFlag);
			v.setTag(this);
		}
	}
	
	
	public static class ManItem{
		public String imageUrl;
		public String firstname;
		public String description;
		public boolean onlineFlag;
		public boolean unreadFlag;
		public boolean favoriteFlag;
		public boolean inchatFlag;
		
		
		public ManItem(String imageUrl, String firstName, String description, boolean online, boolean unread, boolean favorite, boolean inchat){
			this.imageUrl = imageUrl;
			this.firstname = firstName;
			this.description = description;
			this.onlineFlag = online;
			this.unreadFlag = unread;
			this.favoriteFlag = favorite;
			this.inchatFlag = inchat;
			
		}
		
	}

}
