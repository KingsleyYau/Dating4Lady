package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.ExpressionImageGetter;
import com.qpidnetwork.ladydating.chat.LCMessageHelper;
import com.qpidnetwork.ladydating.chat.contact.ContactManager;
import com.qpidnetwork.ladydating.customized.view.CircleImageView;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCMessageItem.MessageType;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCUserItem.ChatType;
import com.qpidnetwork.livechat.jni.LiveChatClient.UserStatusType;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;

public class LiveChatListAdapter extends BaseAdapter{

	private List<LCUserItem> contactsList;
	private Context context;
	private ContactManager mContactManager;

	public LiveChatListAdapter(Context context, List<LCUserItem> manList){
		this.context = context;
		this.contactsList = manList;
		this.mContactManager = ContactManager.getInstance();
	}
	
	@Override
	public int getCount() {
		return contactsList.size();
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
			convertView = LayoutInflater.from(context).inflate(R.layout.item_for_home_livechat_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		LCUserItem contactItem = contactsList.get(position);
		
		boolean readFlag =  mContactManager.getReadFlag(contactItem.userId);
		boolean isInchat = false;
		if(contactItem.chatType ==ChatType.InChatCharge ||
				contactItem.chatType == ChatType.InChatUseTryTicket){
			isInchat = true;
		}
		
		/*头像处理*/
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if(!TextUtils.isEmpty(contactItem.imgUrl)){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(contactItem.imgUrl);
			holder.imageDownLoader = new ImageViewLoader(context);
			holder.imageDownLoader.SetDefaultImage(context.getResources().getDrawable(R.drawable.default_photo_64dp));
			holder.imageDownLoader.DisplayImage(holder.image, contactItem.imgUrl, localPath, null);
		}else{
			holder.image.setImageResource(R.drawable.default_photo_64dp);
		}
		
		LCMessageItem lastMsg = contactItem.GetLastTalkMsg();
		if (lastMsg != null) {
			holder.llAgeOrCountry.setVisibility(View.GONE);
			holder.deacription.setVisibility(View.VISIBLE);
			if ((lastMsg.msgType == MessageType.Text)
					&& (lastMsg.getTextItem() != null && lastMsg
							.getTextItem().message != null)) {
				ExpressionImageGetter imageGetter = new ExpressionImageGetter(context, UnitConversion.dip2px(context, 20),
												UnitConversion.dip2px(context, 20));
				holder.deacription.setText(imageGetter.getExpressMsgHTML(lastMsg.getTextItem().message));
			} else {
				String description = LCMessageHelper.generateMsgHint(context, lastMsg);
				holder.deacription.setText(description);
			}
		}else{
			holder.llAgeOrCountry.setVisibility(View.VISIBLE);
			holder.deacription.setVisibility(View.GONE);
			holder.age.setText(String.valueOf(contactItem.age));
			holder.country.setText(StringUtil.getCountryNameByCode(context, contactItem.country));
		}
		
		
		holder.masterView.setBackgroundColor(!readFlag? context.getResources().getColor(R.color.touch_feedback_holo_light) : Color.TRANSPARENT);
		holder.firstName.setText(contactItem.userName);
		holder.onlineFlag.setVisibility((contactItem.statusType == UserStatusType.USTATUS_ONLINE) ? View.VISIBLE : View.GONE);
		holder.unreadFlag.setVisibility(readFlag ? View.GONE : View.VISIBLE);
		holder.favoriteFlag.setVisibility(View.GONE);
		holder.inchatFlag.setVisibility(isInchat ? View.VISIBLE : View.GONE);

		return convertView;
	}

	private class ViewHolder {

		public LinearLayout masterView;
		public CircleImageView image;
		public TextView firstName;
		public LinearLayout llAgeOrCountry;
		public TextView age;
		public TextView country;
		public TextView deacription;
		public ImageView onlineFlag;
		public ImageView unreadFlag;
		public ImageView favoriteFlag;
		public ImageView inchatFlag;
		public ImageViewLoader imageDownLoader;

		public ViewHolder(View v) {
			this.masterView = (LinearLayout) v.findViewById(R.id.masterView);
			this.image = (CircleImageView) v.findViewById(R.id.avatar);
			this.firstName = (TextView) v.findViewById(R.id.firstName);
			this.llAgeOrCountry = (LinearLayout) v.findViewById(R.id.llAgeOrCountry);
			this.age = (TextView) v.findViewById(R.id.age);
			this.country = (TextView) v.findViewById(R.id.country);
			this.deacription = (TextView) v.findViewById(R.id.description);
			this.onlineFlag = (ImageView) v.findViewById(R.id.onlineFlag);
			this.unreadFlag = (ImageView) v.findViewById(R.id.readFlag);
			this.favoriteFlag = (ImageView) v.findViewById(R.id.favoriteFlag);
			this.inchatFlag = (ImageView) v.findViewById(R.id.inchatFlag);
			this.imageDownLoader = null;
			v.setTag(this);
		}
	}
}
