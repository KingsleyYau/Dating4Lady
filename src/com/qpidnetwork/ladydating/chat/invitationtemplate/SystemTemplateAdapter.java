package com.qpidnetwork.ladydating.chat.invitationtemplate;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;

public class SystemTemplateAdapter extends BaseAdapter{
	
	private Context mContext;
	private List<String> mDataList;
	
	public SystemTemplateAdapter(Context context, List<String> dataList){
		this.mContext = context;
		this.mDataList = dataList;
	}

	@Override
	public int getCount() {
		int dataCount = 0;
		if(mDataList != null){
			dataCount = mDataList.size();
		}
		return dataCount;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_system_template, null);
			holder.tvContent = (TextView)convertView.findViewById(R.id.tvContent);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.tvContent.setText(mDataList.get(position));
		return convertView;
	}
	
	private class ViewHolder{
		public TextView tvContent;
	}

}
