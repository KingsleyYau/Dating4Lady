package com.qpidnetwork.ladydating.home;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.bean.ManInfoBean;

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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_for_home_myfavorite_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		ManInfoBean manItem = manList.get(position);
		
		holder.onlineFlag.setVisibility(View.GONE);
		holder.firstName.setText(manItem.userName);
		holder.age.setText(String.valueOf(manItem.age));
		holder.country.setText(manItem.country);
		
		return convertView;
	}
	
	private class ViewHolder{
		public ImageView image;
		public ImageView onlineFlag;
		public TextView firstName;
		public TextView age;
		public TextView country;
		
		public ViewHolder(View v){
			this.image = (ImageView)v.findViewById(R.id.image);
			this.onlineFlag = (ImageView)v.findViewById(R.id.onlineFlag);
			this.firstName = (TextView)v.findViewById(R.id.firstName);
			this.age = (TextView)v.findViewById(R.id.age);
			this.country = (TextView)v.findViewById(R.id.country);
			v.setTag(this);
		}
	}
}
