package com.qpidnetwork.ladydating.chat.history;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.CircleImageView;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.tool.ImageViewLoader;

public class ChatContactListAdapter extends BaseAdapter{
	
	private Context context;
	private List<ChatContactItem> chatContactList;
	
	public ChatContactListAdapter(Context context, List<ChatContactItem> manList){
		this.context = context;
		this.chatContactList = manList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return chatContactList.size();
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
			convertView = LayoutInflater.from(context).inflate(R.layout.adapter_chathistory_contact_item, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		ChatContactItem item = chatContactList.get(position);
		
		/*头像处理*/
		if ( null != holder.imageDownLoader ) {
			// 停止回收旧Downloader
			holder.imageDownLoader.ResetImageView();
		}
		if(!TextUtils.isEmpty(item.photoUrl)){
			String localPath = FileCacheManager.getInstance().CacheImagePathFromUrl(item.photoUrl);
			holder.imageDownLoader = new ImageViewLoader(context);
			holder.imageDownLoader.SetDefaultImage(context.getResources().getDrawable(R.drawable.default_photo_64dp));
			holder.imageDownLoader.DisplayImage(holder.image, item.photoUrl, localPath, null);
		}else{
			holder.image.setImageResource(R.drawable.default_photo_64dp);
		}
		
		holder.tvUserName.setText(item.manName);
		holder.tvStartTime.setText(new SimpleDateFormat(" MMM dd, yyyy", Locale.ENGLISH).format(new Date(((long)item.startTime) * 1000)));
		if(!item.readFlag){
			holder.unreadFlag.setVisibility(View.VISIBLE);
		}else{
			holder.unreadFlag.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	private class ViewHolder{
		
		public CircleImageView image;
		public TextView tvUserName;
		public TextView tvStartTime;
		public ImageView unreadFlag;
		public ImageViewLoader imageDownLoader;
		
		public ViewHolder(View v) {
			this.image = (CircleImageView) v.findViewById(R.id.avatar);
			this.tvUserName = (TextView) v.findViewById(R.id.tvUserName);
			this.tvStartTime = (TextView) v.findViewById(R.id.tvStartTime);
			this.unreadFlag = (ImageView) v.findViewById(R.id.unreadFlag);
			this.imageDownLoader = null;
			v.setTag(this);
		}
	}

}
