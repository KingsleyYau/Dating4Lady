package com.qpidnetwork.ladydating.chat.downloader;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.ladydating.chat.picture.PictureHelper;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LCVideoItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerVideoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendVideoCallback.ResultType;
import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;
import com.qpidnetwork.request.item.LCVideoListGroupItem;
import com.qpidnetwork.request.item.LCVideoListVideoItem;

public class LivechatVideoThumbPhotoDownloader implements LiveChatManagerVideoListener{
	
	private static final int ASYN_PROCESS_PICTURE_SUCCESS = 1;
	private static final int DOWNLOAD_PICTURE_SUCCESS = 2;
	
	private ImageView mImageView;
	private String videoId;
	private VideoPhotoType sizeType;
	private int desWidth;
	private int desHeight;
	
	private Context mContext;
	private LiveChatManager mLiveChatManager;
	private Handler mHandler;
	
	private boolean isDownloading = false;
	
	public LivechatVideoThumbPhotoDownloader(Context context){
		this.mContext = context;
		mLiveChatManager = LiveChatManager.getInstance();
		mHandler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case ASYN_PROCESS_PICTURE_SUCCESS:
					Bitmap bitmap = (Bitmap)msg.obj;
					if( bitmap != null ) {
						mImageView.setImageBitmap(bitmap);
					}
					break;
				case DOWNLOAD_PICTURE_SUCCESS:{
					isDownloading = false;
					mLiveChatManager.UnregisterVideoListener(LivechatVideoThumbPhotoDownloader.this);
					LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
					if(errType == LiveChatErrType.Success){
						String localPath = getLocalPath(videoId, sizeType);
						if(!TextUtils.isEmpty(localPath)){
							//本地已经存在，无需下载
							AsynProcessPicture(localPath);
						}
					}
				}break;

				default:
					break;
				}
			}
		};
	}
	
	/**
	 * 重置图片下载
	 */
	public void resetDownloader(){
		if(isDownloading){
			mLiveChatManager.UnregisterVideoListener(this);
		}
		if(mImageView != null){
			mImageView = null;
		}
		videoId = "";
	}
	
	public void DisplayImage(ImageView imageView, String videoId, VideoPhotoType sizeType, int width, int height){
		this.mImageView = imageView;
		this.videoId = videoId;
		this.sizeType = sizeType;
		this.desWidth = width;
		this.desHeight = height;
		String localPath = getLocalPath(videoId, sizeType);
		if(!TextUtils.isEmpty(localPath)){
			//本地已经存在，无需下载
			AsynProcessPicture(localPath);
		}else{
			//本地不存在需要去下载
			mLiveChatManager.RegisterVideoListener(this);
			isDownloading = true;
			LCVideoItem item = mLiveChatManager.GetVideoItem(videoId);
			boolean success = mLiveChatManager.GetVideoPhoto(item, sizeType);
			if (!success) {
				isDownloading = false;
				mLiveChatManager.UnregisterVideoListener(this);
			}
		}
	}
	
	/**
	 * 获取Video thumb 本地地址
	 * @param videoId
	 * @param sizeType
	 * @return
	 */
	private String getLocalPath(String videoId, VideoPhotoType sizeType){
		String localPath = "";
		LCVideoItem videoItem = mLiveChatManager.GetVideoItem(videoId);
		if(videoItem != null){
			switch (sizeType) {
			case Default:
				localPath = videoItem.smallPhotoPath;
				break;
			case Big:
				localPath = videoItem.bigPhotoPath;
				break;
			default:
				break;
			}
			if(TextUtils.isEmpty(localPath) || (!new File(localPath).exists())){
				localPath = "";
			}
		}
		return localPath;
	}
	
	/**
	 * 异步线程处理图片，防止界面卡住
	 * @param filePath
	 */
	private void AsynProcessPicture(final String filePath){
		PictureHelper.THREAD_POOL_EXECUTOR.execute(new Runnable() {
			
			@Override
			public void run() {
				Bitmap tempBitmap = ImageUtil.decodeAndScaleBitmapFromFile(filePath, desWidth, desHeight);
				Bitmap newBitmap = ImageUtil.get2DpRoundedImage(mContext, tempBitmap);
				Message msg = Message.obtain();
				msg.what = ASYN_PROCESS_PICTURE_SUCCESS;
				msg.obj = newBitmap;
				mHandler.sendMessage(msg);
			}
		});
	}

	/********************* Livechat Video 相关 ********************************/
	@Override
	public void OnCheckSendVideo(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCVideoItem videoItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, VideoPhotoType photoType, LCVideoItem item) {
		if((item != null)&&(this.videoId.equals(item.videoId))){
			Message msg = Message.obtain();
			msg.what = DOWNLOAD_PICTURE_SUCCESS;
			msg.arg1 = errType.ordinal();
			msg.obj = item;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void OnGetVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvShowVideo(LCUserItem userItem, String videoId,
			String videoDesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String videoId,
			String videoDesc) {
		// TODO Auto-generated method stub
		
	}

}
