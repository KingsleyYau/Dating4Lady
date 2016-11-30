package com.qpidnetwork.ladydating.chat.emotion;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.item.EmotionConfigEmotionItem;
import com.qpidnetwork.request.item.EmotionConfigItem;

public class EmotionPreviewer extends PopupWindow implements LiveChatManagerEmotionListener{
	
	private static int SUCCESS = -1;
	private static int FAILED = 0;
	
	private Context context;
	public View contentView;
	
	private TextView price;
	private MaterialProgressBar progress;
	public EmotionPlayer emotionPlayer;
	private LiveChatManager mLiveChatManager;
	private EmotionConfigEmotionItem emotionItem;
	private ArrayList<String> images;
	
	
	public EmotionPreviewer(Context context){
		super(context);
		this.context = context;
		this.setHeight(LayoutParams.MATCH_PARENT);
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setTouchable(true);
		this.setFocusable(true);
		mLiveChatManager = LiveChatManager.newInstance(null);
		
		setContentView(createWindow());
		
	}
	
	@SuppressLint("InflateParams")
	private View createWindow(){
		contentView = LayoutInflater.from(context).inflate(R.layout.view_emotion_previewer, null);
		emotionPlayer = (EmotionPlayer)contentView.findViewById(R.id.emotionPlayer);
		price = (TextView)contentView.findViewById(R.id.price);
		progress = (MaterialProgressBar)contentView.findViewById(R.id.progress);
		
		return contentView;
		
	}
	
	public void setEmotionItem(EmotionConfigEmotionItem emotionItem){
		this.emotionItem = emotionItem;
		price.setText(String.format(context.getResources().getString(R.string.livechat_emotion_preview_price), "" + emotionItem.price));
//		LCEmotionItem item = mLiveChatManager.GetEmotionInfo(emotionItem.fileName);
//		if ((item.playBigImages != null) && (item.playBigImages.size() > 0)){
//			progress.setVisibility(View.GONE);
//			images = item.playBigImages;
//			handler.sendEmptyMessage(SUCCESS);
//		}else{
			progress.setVisibility(View.VISIBLE);
			emotionPlayer.setVisibility(View.INVISIBLE);
			mLiveChatManager.RegisterEmotionListener(this);
			mLiveChatManager.GetEmotionPlayImage(emotionItem.fileName);
//		}
	}
	
	
	@Override public void dismiss(){
		emotionItem = null;
		mLiveChatManager.UnregisterEmotionListener(this);
		if (emotionPlayer.isPlaying()) emotionPlayer.stop();
		progress.setVisibility(View.GONE);
		super.dismiss();
	}

	@Override
	public void OnGetEmotionConfig(boolean success, String errno,
			String errmsg, EmotionConfigItem item) {
		// TODO Auto-generated method stub
		
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
	public void OnGetEmotionImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unused")
	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
		if (this.emotionItem == null) 
			return;
		if (emotionItem == null) return;
		if (!success) return;
		if (!this.emotionItem.fileName.equals(emotionItem.emotionId)) return;
		images = emotionItem.playBigImages;
		handler.sendEmptyMessage((success)? SUCCESS : FAILED);
	}

	private Handler handler = new Handler(){
		@Override public void handleMessage(Message message){
			super.handleMessage(message);
			
			progress.setVisibility(View.GONE);
			if (message.what == SUCCESS){
				if (emotionPlayer.isPlaying()) emotionPlayer.stop();
				emotionPlayer.setVisibility(View.VISIBLE);
				emotionPlayer.setImageList(images);
				emotionPlayer.play();
			}else{
				emotionPlayer.setVisibility(View.INVISIBLE);
			}
		}
	};
	


}
