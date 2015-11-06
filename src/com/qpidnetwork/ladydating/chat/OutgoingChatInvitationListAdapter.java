package com.qpidnetwork.ladydating.chat;

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

public class OutgoingChatInvitationListAdapter extends BaseAdapter{

	private List<ManItem> manList;
	private Context context;

	public OutgoingChatInvitationListAdapter(Context context, List<ManItem> manList){
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
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_outgoing_chat_invitation_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.firstName.setText(manItem.firstname);
		holder.deacription.setText(manItem.description);
		holder.onlineFlag.setVisibility((manItem.onlineFlag)? View.VISIBLE : View.GONE);


		
		return convertView;
	}
	
	

	
	private class ViewHolder{
		
		public LinearLayout masterView;
		public ImageView image;
		public TextView firstName;
		public TextView deacription;
		public ImageView onlineFlag;

		
		public ViewHolder(View v){
			this.masterView = (LinearLayout)v.findViewById(R.id.masterView);
			this.image = (ImageView)v.findViewById(R.id.avatar);
			this.firstName = (TextView)v.findViewById(R.id.firstName);
			this.deacription = (TextView)v.findViewById(R.id.description);
			this.onlineFlag = (ImageView)v.findViewById(R.id.onlineFlag);
			v.setTag(this);
		}
	}
	
	
	public static class ManItem{
		public String imageUrl;
		public String firstname;
		public String description;
		public boolean onlineFlag;

		
		public ManItem(String imageUrl, String firstName, String description, boolean online){
			this.imageUrl = imageUrl;
			this.firstname = firstName;
			this.description = description;
			this.onlineFlag = online;

			
		}
		
	}

}
