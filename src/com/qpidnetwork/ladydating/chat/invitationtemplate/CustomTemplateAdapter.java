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

import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.ExpressionImageGetter;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.ladydating.utility.DrawableUtil;
import com.qpidnetwork.request.item.LiveChatInviteTemplateListItem;

public class CustomTemplateAdapter extends BaseAdapter{

	private List<LiveChatInviteTemplateListItem> mDataList;
	private ChatInvitationTemplateActivity context;
	private OnCustomTemplateStatusClickListener mOnCustomTemplateStatusClickListener;
	private InviteTemplateMode templateMode = InviteTemplateMode.EDIT_MODE;
	
	public CustomTemplateAdapter(Context context,  List<LiveChatInviteTemplateListItem> invitationList, InviteTemplateMode mode){
		this.context = (ChatInvitationTemplateActivity) context;
		this.mDataList = invitationList;
		this.templateMode = mode;
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
		
		/*添加表情显示*/
		ExpressionImageGetter imageGetter = new ExpressionImageGetter(context, UnitConversion.dip2px(
						context, 20), UnitConversion.dip2px(context, 20));
		holder.text.setText(imageGetter.getExpressMsgHTML(invitationItem.tempContent));
		
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
		
		//发送邀请时，隐藏状态按钮
		if(templateMode == InviteTemplateMode.CHOOSE_MODE){
			holder.reviewFlag.setVisibility(View.GONE);
		}
		
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
