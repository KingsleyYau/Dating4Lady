package com.qpidnetwork.ladydating.chat.noramlexp;

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.livechat.LCMagicIconItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerMagicIconListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.item.MagicIconConfig;
import com.qpidnetwork.request.item.MagicIconItem;
import com.qpidnetwork.tool.ImageViewLoader;

public class MagicIconsOtherFragment extends BaseFragment implements
		LiveChatManagerMagicIconListener {

	private static final int GET_MAGICICON_THUNMB_CALLBACK = 1;

	private GridView gvMagicIcon;

	private List<MagicIconItem> mIconItemList;
	private MagicIconGridviewAdapter mAdapter;
	private LiveChatManager mLiveChatManager;

	private int mVpHeight = 0;
	
	public MagicIconsOtherFragment(){
		
	}

	public MagicIconsOtherFragment(int vpHeight, List<MagicIconItem> iconItemList) {
		super();
		this.mIconItemList = iconItemList;
		this.mVpHeight = vpHeight;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_magic_gridview, null);
		gvMagicIcon = (GridView) view.findViewById(R.id.gvMagicIcon);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
//		int gvHeight = (mVpHeight - UnitConversion.dip2px(mContext, 40 + 10));
		int gvHeight = mVpHeight;
		
		if(gvHeight>0){
			gvMagicIcon.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, gvHeight));
		}
		
		mLiveChatManager = LiveChatManager.getInstance();
		if (mIconItemList.size() > 0) {
			mLiveChatManager.RegisterMagicIconListener(this);
			mAdapter = new MagicIconGridviewAdapter(getActivity(),(gvHeight-UnitConversion.dip2px(mContext, 1))/2,mIconItemList);
			gvMagicIcon.setAdapter(mAdapter);
			gvMagicIcon.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					MagicIconItem item = mIconItemList.get(position);
					Intent intent = new Intent(ChatActivity.SEND_MAGICICON_ACTION);
					intent.putExtra(ChatActivity.MAGICICON_ID, item.id);
					mContext.sendBroadcast(intent);
				}
			});
		}
	}


	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		switch (msg.what) {
		case GET_MAGICICON_THUNMB_CALLBACK: {
			LCMagicIconItem item = (LCMagicIconItem) msg.obj;
			if (item != null) {
				String localPath = item.getThumbPath();
				if (!TextUtils.isEmpty(localPath)
						&& (new File(localPath).exists())) {
					updateMagicThumbImage(item);
				}
			}
		}
			break;

		default:
			break;
		}
	}

	private void updateMagicThumbImage(LCMagicIconItem item) {
		if (item != null) {
			int position = -1;
			if (mIconItemList != null) {
				for (int i = 0; i < mIconItemList.size(); i++) {
					if (mIconItemList.get(i).id.equals(item.getMagicIconId())) {
						position = i;
						break;
					}
				}
			}

			if (position >= 0) {
				/* 更新单个Item */
				View childAt = gvMagicIcon.getChildAt(position
						- gvMagicIcon.getFirstVisiblePosition());
				if (childAt != null) {
					ImageView magicIconImage = ((ImageView) childAt
							.findViewById(R.id.icon));
					new ImageViewLoader(mContext).DisplayImage(magicIconImage,
							null, item.getThumbPath(), null);
				}
			}
		}
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mLiveChatManager.UnregisterMagicIconListener(this);
	}

	// ------------------ MagicIcon relative callback --------------------------
	@Override
	public void OnGetMagicIconConfig(boolean success, String errno,
			String errmsg, MagicIconConfig item) {

	}

	@Override
	public void OnSendMagicIcon(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {

	}

	@Override
	public void OnRecvMagicIcon(LCMessageItem item) {

	}

	@Override
	public void OnGetMagicIconSrcImage(boolean success,
			LCMagicIconItem magicIconItem) {

	}

	@Override
	public void OnGetMagicIconThumbImage(boolean success,
			LCMagicIconItem magicIconItem) {
		if (success) {
			Message msg = Message.obtain();
			msg.what = GET_MAGICICON_THUNMB_CALLBACK;
			msg.obj = magicIconItem;
			sendUiMessage(msg);
		}
	}

}
