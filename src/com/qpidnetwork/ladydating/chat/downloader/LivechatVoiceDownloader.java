package com.qpidnetwork.ladydating.chat.downloader;

import java.io.File;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerVoiceListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;

/**
 * 语音下载及播放管理
 * 
 * @author Hunter
 * 
 */
public class LivechatVoiceDownloader implements LiveChatManagerVoiceListener{

	private LiveChatManager mLiveChatManager;
	private Context mContext;
	private MaterialProgressBar pbDownload;
	private ImageButton btnError;
	private LCMessageItem msgBean;
	
	public LivechatVoiceDownloader(Context context) {
		mContext = context;
		mLiveChatManager = LiveChatManager.newInstance(null);
	}

	public void downloadAndPlayVoice(MaterialProgressBar pbDownload,
			ImageButton btnError, LCMessageItem msgBean) {
		
		this.pbDownload = pbDownload;
		this.btnError = btnError;
		this.msgBean = msgBean;
		
		
		String filePath = msgBean.getVoiceItem().filePath;
		if ((filePath != null) && (new File(filePath).exists())){
			// 本地已有，或者已经下载完成，直接播放
//			startPlayVoice(filePath);
		}else{
			if(pbDownload != null){
				pbDownload.setVisibility(View.VISIBLE);
			}
			mLiveChatManager.RegisterVoiceListener(this);
			boolean success = mLiveChatManager.GetVoice(msgBean);
			if(!success){
				mLiveChatManager.UnregisterVoiceListener(LivechatVoiceDownloader.this);
				if(pbDownload != null){
					pbDownload.setVisibility(View.GONE);
				}
				onDownloadVoiceFailed();
			}
		}
	}
	
	/**
	 * 下载语音失败公共处理
	 */
	private void onDownloadVoiceFailed(){
		if(btnError != null){
			btnError.setVisibility(View.VISIBLE);
			btnError.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					/*下载失败，提示及重新下载*/
					MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
					dialog.setMessage(mContext.getString(R.string.livechat_download_voice_fail));
					dialog.addButton(dialog.createButton(mContext.getString(R.string.retry), new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							/* 文件不存在，下载文件 */
							btnError.setVisibility(View.GONE);
							if(pbDownload != null){
								pbDownload.setVisibility(View.VISIBLE);
							}
							mLiveChatManager.RegisterVoiceListener(LivechatVoiceDownloader.this);
							boolean success = mLiveChatManager.GetVoice(msgBean);
							if(!success){
								mLiveChatManager.UnregisterVoiceListener(LivechatVoiceDownloader.this);
								if(pbDownload != null){
									pbDownload.setVisibility(View.GONE);
								}
								onDownloadVoiceFailed();
							}
						}
					}));
					dialog.addButton(dialog.createButton(mContext.getString(R.string.cancel), null));
					
					dialog.show();
				}
			});
		}
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			LCMessageItem item = (LCMessageItem)msg.obj;
			LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];

			/*多图片下载时，用于判断是否当前图片*/
			mLiveChatManager.UnregisterVoiceListener(LivechatVoiceDownloader.this);
			if(pbDownload != null){
				pbDownload.setVisibility(View.GONE);
			}
			if(errType == LiveChatErrType.Success){
				if ((item.getVoiceItem().filePath != null) && (new File(item.getVoiceItem().filePath).exists())) {
					// 本地已有，或者已经下载完成，直接播放
//					startPlayVoice(item.getVoiceItem().filePath);
					return;
				}
			}
			onDownloadVoiceFailed();
		}
	};
	
	@Override
	public void OnSendVoice(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetVoice(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		if(item.msgId == msgBean.msgId){
			/*是当前语音文件，通知界面更新*/
			Message msg = Message.obtain();
			msg.arg1 = errType.ordinal();
			msg.obj = item;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void OnRecvVoice(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}
}
