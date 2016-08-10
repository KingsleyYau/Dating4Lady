package com.qpidnetwork.ladydating.chat.emotion;

import java.util.Arrays;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.item.EmotionConfigItem;

public class NormalEmotionFragment extends BaseFragment implements LiveChatManagerEmotionListener{
	
	private GridView gvEmotion;
	private MaterialProgressBar pbDownload;
	private LiveChatManager mLiveChatManager;
	public OnItemClickCallback itemClickCallback;
	
	
	public interface OnItemClickCallback{
		public void onItemClick();
		public void onItemLongClick();
		public void onItemLongClickUp();
	}
	
	
	public void setOnItemClickCallback(OnItemClickCallback callback){
		this.itemClickCallback = callback;
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_emotion_gridview, null);
		gvEmotion = (GridView)view.findViewById(R.id.gvEmotion);
		pbDownload = (MaterialProgressBar)view.findViewById(R.id.progress);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mLiveChatManager = LiveChatManager.newInstance(null);
		
		/*同步配置*/
		pbDownload.setVisibility(View.VISIBLE);
		mLiveChatManager.RegisterEmotionListener(this);
		mLiveChatManager.GetEmotionConfig();
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		pbDownload.setVisibility(View.GONE);
		mLiveChatManager.UnregisterEmotionListener(this);
		if(msg.arg1 == 1){
			EmotionConfigItem item = (EmotionConfigItem)msg.obj;
			if(getActivity() != null){
				gvEmotion.setAdapter(new EmotionGridviewAdapter(getActivity(), Arrays.asList(item.ladyEmotionList), gvEmotion));
			}
		}
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		mLiveChatManager.UnregisterEmotionListener(this);
	}

	@Override
	public void OnGetEmotionConfig(boolean success, String errno, String errmsg,
			EmotionConfigItem item) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.arg1 = success?1:0;
		msg.obj = item;
		sendUiMessage(msg);
	}

	@Override
	public void OnSendEmotion(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvEmotion(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetEmotionImage(boolean success,
			LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetEmotionPlayImage(boolean success,
			LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

}
