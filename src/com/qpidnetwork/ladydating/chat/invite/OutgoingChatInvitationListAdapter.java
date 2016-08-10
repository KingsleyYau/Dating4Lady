package com.qpidnetwork.ladydating.chat.invite;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.ExpressionImageGetter;
import com.qpidnetwork.ladydating.chat.LCMessageHelper;
import com.qpidnetwork.ladydating.customized.view.CircleImageView;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;

public class OutgoingChatInvitationListAdapter extends BaseAdapter{

	private List<LCUserItem> manList;
	private Context mContext;

	public OutgoingChatInvitationListAdapter(Context context, List<LCUserItem> manList){
		this.mContext = context;
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
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_for_outgoing_chat_invitation_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		LCUserItem manItem = manList.get(position);
		
		/*头像处理*/
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if(!TextUtils.isEmpty(manItem.imgUrl)){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(manItem.imgUrl);
			holder.imageDownLoader = new ImageViewLoader(mContext);
			holder.imageDownLoader.SetDefaultImage(mContext.getResources().getDrawable(R.drawable.default_photo_64dp));
			holder.imageDownLoader.DisplayImage(holder.image, manItem.imgUrl, localPath, null);
		}else{
			holder.image.setImageResource(R.drawable.default_photo_64dp);
		}
		
		LCMessageItem lastMsg = null;
		ArrayList<LCMessageItem> msgList = manItem.getMsgList();
		if((msgList != null) && (msgList.size() > 0)){
			lastMsg= msgList.get(msgList.size()-1);
		}
		if (lastMsg != null) {
			if ((lastMsg.msgType == MessageType.Text)
					&& (lastMsg.getTextItem() != null && lastMsg
							.getTextItem().message != null)) {
				ExpressionImageGetter imageGetter = new ExpressionImageGetter(mContext, UnitConversion.dip2px(mContext, 28),
												UnitConversion.dip2px(mContext, 28));
				holder.deacription.setText(imageGetter.getExpressMsgHTML(lastMsg.getTextItem().message));
			} else {
				String description = LCMessageHelper.generateMsgHint(mContext, lastMsg);
				holder.deacription.setText(description);
			}
		}

		holder.firstName.setText(manItem.userName);
		holder.onlineFlag.setVisibility((manItem.statusType == UserStatusType.USTATUS_ONLINE)? View.VISIBLE : View.GONE);

		return convertView;
	}
	
	private class ViewHolder{
		
		public LinearLayout masterView;
		public CircleImageView image;
		public TextView firstName;
		public TextView deacription;
		public ImageView onlineFlag;
		public ImageViewLoader imageDownLoader;

		
		public ViewHolder(View v){
			this.masterView = (LinearLayout)v.findViewById(R.id.masterView);
			this.image = (CircleImageView)v.findViewById(R.id.avatar);
			this.firstName = (TextView)v.findViewById(R.id.firstName);
			this.deacription = (TextView)v.findViewById(R.id.description);
			this.onlineFlag = (ImageView)v.findViewById(R.id.onlineFlag);
			this.imageDownLoader = null;
			v.setTag(this);
		}
	}

}
