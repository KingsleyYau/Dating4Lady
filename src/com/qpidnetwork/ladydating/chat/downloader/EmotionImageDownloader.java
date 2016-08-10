package com.qpidnetwork.ladydating.chat.downloader;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.item.EmotionConfigItem;

public class EmotionImageDownloader implements LiveChatManagerEmotionListener{
	
	private LiveChatManager mLiveChatManager;
	private ImageView emotionPane;
	private ProgressBar pbDownload;
	private String emotionId;
	
	public EmotionImageDownloader(){
		mLiveChatManager = LiveChatManager.newInstance(null);
	}
	
	public void displayEmotionImage(ImageView emotionPane, ProgressBar pbDownload, String emotionId){
		this.emotionId = emotionId;
		/*设置默认图标，防止滚动过程中，由于view重用导致显示错误*/
		emotionPane.setImageResource(R.drawable.ic_default_preminum_emotion_grey_56dp);
		
		LCEmotionItem item = mLiveChatManager.GetEmotionInfo(emotionId);
		if ((!StringUtil.isEmpty(item.imagePath))
				&& (new File(item.imagePath).exists())) {
			/* 有缩略图，直接使用 */
			Bitmap thumb = BitmapFactory.decodeFile(item.imagePath);
			emotionPane.setImageBitmap(thumb);
			if(pbDownload != null){
				pbDownload.setVisibility(View.GONE);
			}
		} else {
			this.emotionPane = emotionPane;
			this.pbDownload = pbDownload;
			if(pbDownload != null){
				pbDownload.setVisibility(View.VISIBLE);
			}
			mLiveChatManager.RegisterEmotionListener(this);
			boolean success = mLiveChatManager.GetEmotionImage(emotionId);
			if(!success){
				if(pbDownload != null){
					pbDownload.setVisibility(View.GONE);
				}
				mLiveChatManager.UnregisterEmotionListener(EmotionImageDownloader.this);
			}
		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			LCEmotionItem item = (LCEmotionItem)msg.obj;
			if(item.emotionId.equals(emotionId)){
				mLiveChatManager.UnregisterEmotionListener(EmotionImageDownloader.this);
				if(pbDownload != null){
					pbDownload.setVisibility(View.GONE);
				}
			
				if ((!StringUtil.isEmpty(item.imagePath))
						&& (new File(item.imagePath).exists())) {
					/* 有缩略图，直接使用 */
					Bitmap thumb = BitmapFactory.decodeFile(item.imagePath);
					emotionPane.setImageBitmap(thumb);
					if(pbDownload != null){
						pbDownload.setVisibility(View.GONE);
					}
				}	
			}
		}
	};

	@Override
	public void OnGetEmotionConfig(boolean success, String errno, String errmsg,
			EmotionConfigItem item) {
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
	public void OnGetEmotionImage(boolean success,
			LCEmotionItem emotionItem) {
		Message msg = Message.obtain();
		if(success){
			msg.obj = emotionItem;
			handler.sendMessage(msg);
		}
		
	}

	@Override
	public void OnGetEmotionPlayImage(boolean success,
			LCEmotionItem emotionItem) {
		// TODO Auto-generated method stub
		
	}

}
