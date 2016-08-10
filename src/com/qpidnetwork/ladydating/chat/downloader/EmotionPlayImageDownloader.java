package com.qpidnetwork.ladydating.chat.downloader;

import android.os.Handler;
import android.os.Message;

import com.qpidnetwork.livechat.LCEmotionItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerEmotionListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.item.EmotionConfigItem;

public class EmotionPlayImageDownloader implements LiveChatManagerEmotionListener{

	
	private LiveChatManager mLiveChatManager;
	private LCMessageItem msgBean;
	private OnEmotionPlayImageDownloadListener listener;
	
	public EmotionPlayImageDownloader() {
		mLiveChatManager = LiveChatManager.newInstance(null);
	}
	
	public void downloadEmotionPlayImage(LCMessageItem bean, OnEmotionPlayImageDownloadListener listener){
		this.msgBean = bean;
		this.listener = listener;
//		LCEmotionItem item = mLiveChatManager.GetEmotionInfo(msgBean.getEmotionItem().emotionId);
//		if ((item.playBigImages != null) && (item.playBigImages.size() > 0)){
//			/*本地已存在*/
//			if(listener != null){
//				listener.onEmotionPlayImageDownloadSuccess(bean);
//			}
//		}else{
			/*本地不存在，去下载*/
			mLiveChatManager.RegisterEmotionListener(this);
			if(listener != null){
				listener.onEmotionPlayImageDownloadStart(bean);
			}
			boolean success = mLiveChatManager.GetEmotionPlayImage(msgBean.getEmotionItem().emotionId);
			if(!success){
				mLiveChatManager.UnregisterEmotionListener(this);
				listener.onEmotionPlayImageDownloadFail(bean);
			}
			
//		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			LCEmotionItem item = (LCEmotionItem)msg.obj;
			if((msgBean != null) && (msgBean.getEmotionItem() != null)){
				/*发送高级表情，高级表情未下载成功但消息发送失败，重新发送时，由于旧消息被删除，导致请求返回成功后空指针异常*/
				if((item != null)&&(item.emotionId.equals(msgBean.getEmotionItem().emotionId))){
					/*下载高级表情播放图片回调*/
					mLiveChatManager.UnregisterEmotionListener(EmotionPlayImageDownloader.this);
					if(msg.arg1 == 1){
						/*下载成功*/
						if ((item.playBigImages != null) && (item.playBigImages.size() > 0)) {
							/* 有缩略图，直接使用 */
							if(listener != null){
								listener.onEmotionPlayImageDownloadSuccess(msgBean);
							}
							return;
						}
					}
					if(listener != null){
						listener.onEmotionPlayImageDownloadFail(msgBean);
					}
				}
			}else{
				mLiveChatManager.UnregisterEmotionListener(EmotionPlayImageDownloader.this);
			}
		}
	};
	
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

	@Override
	public void OnGetEmotionPlayImage(boolean success, LCEmotionItem emotionItem) {
		Message msg = Message.obtain();
		msg.arg1 = success?1:0;
		msg.obj = emotionItem;
		handler.sendMessage(msg);		
	}
	
	public interface OnEmotionPlayImageDownloadListener{
		public void onEmotionPlayImageDownloadStart(LCMessageItem item);
		public void onEmotionPlayImageDownloadSuccess(LCMessageItem item);
		public void onEmotionPlayImageDownloadFail(LCMessageItem item);
	}

}
