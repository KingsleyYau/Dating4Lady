package com.qpidnetwork.ladydating.chat.invitationtemplate;

import java.util.List;
import java.util.Random;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.invitationtemplate.ChatInvitationTemplateActivity.TemplateType;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.ladydating.utility.DrawableUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatInvitationTemplateAdapter extends BaseAdapter{

	private List<InvitationItem> invitationList;
	private ChatInvitationTemplateActivity context;
	private ChatInvitationTemplateActivity.TemplateType templateType;
	private View.OnClickListener flagOnClickListener;
	
	public ChatInvitationTemplateAdapter(Context context, ChatInvitationTemplateActivity.TemplateType templateType, List<InvitationItem> invitationList, View.OnClickListener flagOnClick){
		this.flagOnClickListener = flagOnClick;
		this.context = (ChatInvitationTemplateActivity) context;
		this.invitationList = invitationList;
		this.templateType = templateType;
	}

	public ChatInvitationTemplateAdapter(Context context, ChatInvitationTemplateActivity.TemplateType templateType, List<InvitationItem> invitationList){
		this(context, templateType, invitationList, null);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return invitationList.size();
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
		InvitationItem invitationItem = invitationList.get(position);
		
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_chat_invitation_template_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.reviewFlag.setTag(position);
		holder.text.setText(invitationItem.text);
		if (templateType == TemplateType.SYSTEM){
			holder.reviewFlag.setVisibility(View.GONE);
		}else{
			Drawable drawable;
			switch(invitationItem.reviewStatus){
			case UNDER_REVIEW:
				drawable = DrawableUtil.getDrawable(R.drawable.ic_rate_review_grey600_24dp);
				break;
			case PAST:
				drawable = DrawableUtil.getDrawable(R.drawable.ic_done_all_white_24dp, context.getResources().getColor(R.color.green));
				break;
			case REJECT:
				drawable = DrawableUtil.getDrawable(R.drawable.ic_error_red_24dp);
				break;
			default:
				drawable = DrawableUtil.getDrawable(R.drawable.ic_rate_review_grey600_24dp);
				break;
			}
			
			holder.reviewFlag.setImageDrawable(drawable);
			if (flagOnClickListener != null) holder.reviewFlag.setOnClickListener(flagOnClickListener);
		}
		
		return convertView;
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
	
	
	
	public static class InvitationItem{
		
		public static enum ReviewStatus{
			UNDER_REVIEW,
			PAST,
			REJECT
			
		}
		
		public String text;
		public ReviewStatus reviewStatus;
		
		public InvitationItem(String text, ReviewStatus reviewStatus){
			this.text = text;
			this.reviewStatus = reviewStatus;
		}
		
	}

}
