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
import com.qpidnetwork.livechat.LCPhotoItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerPhotoListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.OnLCCheckSendPhotoCallback.ResultType;
import com.qpidnetwork.request.RequestJniLivechat.PhotoSizeType;
import com.qpidnetwork.request.item.LCPhotoListAlbumItem;
import com.qpidnetwork.request.item.LCPhotoListPhotoItem;

public class LivechatSelfPhotoDownloader implements LiveChatManagerPhotoListener{
	
	private static final int ASYN_PROCESS_PICTURE_SUCCESS = 0;
	private static final int DOWNLOAD_PICTURE_SUCCESS = 1;
	
	private ImageView mImageView;
	private String photoId;
	private PhotoSizeType sizeType;
	private int desWidth;
	private int desHeight;
	
	private Context mContext;
	private LiveChatManager mLiveChatManager;
	private Handler mHandler;
	
	private boolean isDownloading = false;

	public LivechatSelfPhotoDownloader(Context context){
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
					mLiveChatManager.UnregisterPhotoListener(LivechatSelfPhotoDownloader.this);
					LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
					if(errType == LiveChatErrType.Success){
						String localPath = getLocalPath(photoId, sizeType);
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
			mLiveChatManager.UnregisterPhotoListener(this);
		}
		if(mImageView != null){
			mImageView = null;
		}
		photoId = "";
	}
	
	public void DisplayImage(ImageView imageView, String photoId, PhotoSizeType sizeType, int width, int height){
		this.mImageView = imageView;
		this.photoId = photoId;
		this.sizeType = sizeType;
		this.desWidth = width;
		this.desHeight = height;
		String localPath = getLocalPath(photoId, sizeType);
		if(!TextUtils.isEmpty(localPath)){
			//本地已经存在，无需下载
			AsynProcessPicture(localPath);
		}else{
			//本地不存在需要去下载
			mLiveChatManager.RegisterPhotoListener(this);
			isDownloading = true;
			boolean success = mLiveChatManager.GetSelfPhoto(photoId, sizeType);
			if (!success) {
				isDownloading = false;
				mLiveChatManager.UnregisterPhotoListener(this);
			}
		}
	}
	
	private String getLocalPath(String photoId, PhotoSizeType sizeType){
		String localPath = "";
		LCPhotoItem photoItem = mLiveChatManager.GetSelfPhotoItem(photoId);
		if(photoItem != null){
			switch (sizeType) {
			case Small:
				localPath = photoItem.thumbSrcFilePath;
				break;
			case Middle:
				localPath = photoItem.thumbSrcFilePath;
				break;
			case Large:
				localPath = photoItem.showSrcFilePath;
				break;
			case Original:
				localPath = photoItem.srcFilePath;
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
	
/*************************  Livechat Callback ********************************/
	@Override
	public void OnCheckSendPhoto(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCPhotoItem photoItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetSelfPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCPhotoItem photoItem) {
		if((photoItem != null)&&(this.photoId.equals(photoItem.photoId))){
			Message msg = Message.obtain();
			msg.what = DOWNLOAD_PICTURE_SUCCESS;
			msg.arg1 = errType.ordinal();
			msg.obj = photoItem;
			mHandler.sendMessage(msg);
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
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId,
			String photoDesc) {
		// TODO Auto-generated method stub
		
	}

}
