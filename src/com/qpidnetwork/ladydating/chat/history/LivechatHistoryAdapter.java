package com.qpidnetwork.ladydating.chat.history;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.request.item.LCChatListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LivechatHistoryAdapter extends BaseAdapter{
	
	private Context mContext;
	private LCChatListItem[] mChatList;
	private Boolean[] mUnreadFlags;
	
	public LivechatHistoryAdapter(Context context, LCChatListItem[] chatList){
		this.mContext = context;
		this.mChatList = chatList;
	}
	
	/**
	 * 更新未读状态
	 * @param unreadFlags
	 */
	public void updateUnreadFlags(Boolean[] unreadFlags){
		if(unreadFlags == null || unreadFlags.length != mChatList.length){
			return;
		}else{
			mUnreadFlags = unreadFlags;
			notifyDataSetChanged();
		}
	}
	
	/**
	 * 更新未读状态
	 * @param position
	 */
	public void updateUnreadFlagByPosition(int position){
		if(position < mUnreadFlags.length){
			mUnreadFlags[position] = false;
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mChatList.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
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
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_history_list, null);
			holder = new ViewHolder(convertView);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		LCChatListItem item = mChatList[position];
		boolean unreadFlags = false;
		if(mUnreadFlags != null &&
				position < mUnreadFlags.length){
			unreadFlags = mUnreadFlags[position];
		}
		holder.tvSortId.setText(String.valueOf(position + 1));
		holder.tvStartTime.setText(item.startTime);
		holder.tvDuring.setText(item.duringTime);
		if(unreadFlags){
			holder.unreadFlag.setVisibility(View.VISIBLE);
		}else{
			holder.unreadFlag.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	private class ViewHolder{
		
		public TextView tvSortId;
		public TextView tvStartTime;
		public TextView tvDuring;
		public ImageView unreadFlag;
		
		public ViewHolder(View view){
			tvSortId = (TextView)view.findViewById(R.id.tvSortId);
			tvStartTime = (TextView)view.findViewById(R.id.tvStartTime);
			tvDuring = (TextView)view.findViewById(R.id.tvDuring);
			unreadFlag = (ImageView)view.findViewById(R.id.unreadFlag);
			view.setTag(this);
		}
	}

}
