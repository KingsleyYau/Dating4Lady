package com.qpidnetwork.ladydating.chat.invitationtemplate;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.DrawableUtil;
import com.qpidnetwork.request.item.LiveChatInviteTemplateListItem;

public class CustomTemplateAdapter extends BaseAdapter{

	private List<LiveChatInviteTemplateListItem> mDataList;
	private ChatInvitationTemplateActivity context;
	private OnCustomTemplateStatusClickListener mOnCustomTemplateStatusClickListener;
	
	public CustomTemplateAdapter(Context context,  List<LiveChatInviteTemplateListItem> invitationList){
		this.context = (ChatInvitationTemplateActivity) context;
		this.mDataList = invitationList;
	}
	
	@Override
	public int getCount() {
		int datacount = 0;
		if(mDataList != null){
			datacount = mDataList.size();
		}
		return datacount;
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
		LiveChatInviteTemplateListItem invitationItem = mDataList.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_custom_template, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.reviewFlag.setTag(position);
		holder.text.setText(invitationItem.tempContent);
		
		Drawable drawable;
		switch(invitationItem.tempStatus){
		case Pending:
			drawable = DrawableUtil.getDrawable(R.drawable.ic_rate_review_grey600_24dp);
			break;
		case Audited:
			drawable = DrawableUtil.getDrawable(R.drawable.ic_done_all_white_24dp, context.getResources().getColor(R.color.green));
			break;
		case Rejected:
			drawable = DrawableUtil.getDrawable(R.drawable.ic_error_red_24dp);
			break;
		default:
			drawable = DrawableUtil.getDrawable(R.drawable.ic_rate_review_grey600_24dp);
			break;
		}
			
		holder.reviewFlag.setImageDrawable(drawable);
		
		holder.reviewFlag.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mOnCustomTemplateStatusClickListener != null){
					mOnCustomTemplateStatusClickListener.onStatusClick(v);
				}
			}
		});
		
		return convertView;
	}
	
	
	public void setOnCustomTemplateStatusClickListener(OnCustomTemplateStatusClickListener listener){
		this.mOnCustomTemplateStatusClickListener = listener;
	}
	

	
	private class ViewHolder{
		public ImageView reviewFlag;
		public TextView text;
		
		public ViewHolder(View v){
			this.reviewFlag = (ImageView)v.findViewById(R.id.review_flag);
			this.text = (TextView)v.findViewById(R.id.text);
			v.setTag(this);
		}
	}
	
	public interface OnCustomTemplateStatusClickListener{
		public void onStatusClick(View v);
	}
}
