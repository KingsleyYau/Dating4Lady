package com.qpidnetwork.ladydating.chat.downloader;

import java.io.File;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCPhotoItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback.ResultType;
import com.qpidnetwork.request.RequestJniLivechat.PhotoSizeType;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

public class LivechatPrivatePhotoDownloader implements
		LiveChatManagerPhotoListener {

	private LiveChatManager mLiveChatManager;
	private LCMessageItem msgItem;
	private PhotoSizeType photoType;
	
	private OnDownloadCallback callback;

	public LivechatPrivatePhotoDownloader(Context context) {
		mLiveChatManager = LiveChatManager.newInstance(context);
	}

	public void startDownload(LCMessageItem bean, PhotoSizeType photoType, OnDownloadCallback callback) {
		this.msgItem = bean;
		this.photoType = photoType;
		this.callback = callback;
		String localPath = getLocalFilePath(bean.getPhotoItem(), photoType);
		if(!StringUtil.isEmpty(localPath)){
			if(this.callback != null){
				this.callback.onPrivatePhotoDownloadSuccess(this, localPath);
			}
		}else{
			/*本地无，去下载*/
			mLiveChatManager.RegisterPhotoListener(this);
			boolean success = mLiveChatManager.GetPhotoWithMessage(bean.getUserItem().userId, bean, photoType);
			if(!success){
				mLiveChatManager.UnregisterPhotoListener(this);
				if(this.callback != null){
					this.callback.onPrivatePhotoDownloadFail(this);
					this.callback = null;
				}
			}
			
		}
	}

	/**
	 * 本地已存在返回本地路径，本地不存在，返回null（其中middle small 对应 拇子图路径 large对应显示图 Original对应原图）
	 * 
	 * @param bean
	 * @param photoType
	 * @return
	 */
	private String getLocalFilePath(LCPhotoItem bean, PhotoSizeType photoType) {
		String localFilePath = null;
		if(bean != null){
			if (bean.charge) {
				/* 已付费 */
				switch (photoType) {
				case Original:
					if ((!StringUtil.isEmpty(bean.srcFilePath))
							&& (new File(bean.srcFilePath).exists())) {
						localFilePath = bean.srcFilePath;
					}
					break;
				case Large:
					if ((!StringUtil.isEmpty(bean.showSrcFilePath))
							&& (new File(bean.showSrcFilePath).exists())) {
						localFilePath = bean.showSrcFilePath;
					}
					break;
				case Middle:
				case Small:
					if ((!StringUtil.isEmpty(bean.thumbSrcFilePath))
							&& (new File(bean.thumbSrcFilePath).exists())) {
						localFilePath = bean.thumbSrcFilePath;
					}
					break;
	
				default:
					break;
				}
			} else {
				/*未付费，模糊图*/
				switch (photoType) {
				case Large:
					if ((!StringUtil.isEmpty(bean.showFuzzyFilePath))
							&& (new File(bean.showFuzzyFilePath).exists())) {
						localFilePath = bean.showFuzzyFilePath;
					}
					break;
				case Middle:
				case Small:
					if ((!StringUtil.isEmpty(bean.thumbFuzzyFilePath))
							&& (new File(bean.thumbFuzzyFilePath).exists())) {
						localFilePath = bean.thumbFuzzyFilePath;
					}
					break;
	
				default:
					break;
				}
			}
		}
		return localFilePath;
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
			LCPhotoItem item = (LCPhotoItem)msg.obj;
			mLiveChatManager.UnregisterPhotoListener(LivechatPrivatePhotoDownloader.this);

			if (errType == LiveChatErrType.Success) {
				String localPath = getLocalFilePath(item, photoType);
				if(!StringUtil.isEmpty(localPath)){
					if(callback != null){
						callback.onPrivatePhotoDownloadSuccess(LivechatPrivatePhotoDownloader.this, localPath);
					}
				}
				return;
			}
			if(callback != null){
				callback.onPrivatePhotoDownloadFail(LivechatPrivatePhotoDownloader.this);
				callback = null;
			}
		}
	};
	
	/**
	 * 注销回调方法，用于某些界面后台下载，退出界面，需要清除调用情况
	 */
	public void unregisterDownloaderCallback(){
		callback = null;
	}

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if(item.msgId == msgItem.msgId){
			Message msg = Message.obtain();
			msg.arg1 = errType.ordinal();
			msg.obj = item.getPhotoItem();
			handler.sendMessage(msg);
		}
	}

	@Override
	public void OnRecvPhoto(LCMessageItem item) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void OnGetPhotoList(boolean isSuccess, String errno, String errmsg,
			LCPhotoListAlbumItem[] albums, LCPhotoListPhotoItem[] photos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnCheckSendPhoto(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		if(photoItem != null
				&& msgItem != null
				&& msgItem.getPhotoItem() != null
				&& !TextUtils.isEmpty(msgItem.getPhotoItem().photoId)
				&& msgItem.getPhotoItem().photoId.equals(photoItem.photoId)){
			Message msg = Message.obtain();
			msg.arg1 = errType.ordinal();
			msg.obj = photoItem;
			handler.sendMessage(msg);
		}
	}
	
	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId,
			String photoDesc) {
		// TODO Auto-generated method stub
		
	}
	
	public interface OnDownloadCallback {
		
		public void onPrivatePhotoDownloadSuccess(LivechatPrivatePhotoDownloader downloader, String filePath);

		public void onPrivatePhotoDownloadFail(LivechatPrivatePhotoDownloader downloader);
	}

}
