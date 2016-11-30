package com.qpidnetwork.ladydating.chat.downloader;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.picture.PictureHelper;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
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

/**
 * 仅用于列表显示的下载器
 * 
 * @author Hunter
 * 
 */
public class PrivatePhotoDownloader implements LiveChatManagerPhotoListener {
	
	private static final int ASYN_PROCESS_PICTURE_SUCCESS = 0;
	private static final int GET_PHOTO_SUCCESS = 1;
	private static final int GET_SELF_PHOTO_SUCCESS = 2;

	private LiveChatManager mLiveChatManager;
	private ImageView privatePhoto;
	private MaterialProgressBar pbDownload;
	private ImageButton btnError;
	private LCMessageItem msgBean;

	private Context mContext;

	public PrivatePhotoDownloader(Context context) {
		mLiveChatManager = LiveChatManager.newInstance(null);
		mContext = context;
	}

	public void displayPrivatePhoto(ImageView privatePhoto,
			MaterialProgressBar pbDownload, LCMessageItem msgBean,
			ImageButton btnError) {
		this.privatePhoto = privatePhoto;
		this.pbDownload = pbDownload;
		this.msgBean = msgBean;
		this.btnError = btnError;
		Bitmap bitmap = ImageUtil.decodeHeightDependedBitmapFromFile((BitmapFactory.decodeResource(mContext.getResources(), R.drawable.private_photo_unviewed_110_150dp)), UnitConversion.dip2px(mContext, 112));
		privatePhoto.setImageBitmap(ImageUtil.get2DpRoundedImage(mContext, bitmap));
		
		if(!loadLocalFile()){
			reloadPhoto();
		}
	}
	
	/**
	 *  如果本地文件有，加载本地文件
	 * @return 是否加载成功
	 */
	private boolean loadLocalFile(){
		
		boolean isLoad = false;
		if (btnError != null) {
			btnError.setVisibility(View.GONE);
		}
		String filePath = "";
		if (msgBean.getPhotoItem().charge) {
			/* 已经付费，下载清晰图 */
			filePath = msgBean.getPhotoItem().showSrcFilePath;
		} else {
			/* 未付费显示模糊图 */
			filePath = msgBean.getPhotoItem().showFuzzyFilePath;
		}
		
		/*对于发出的图片，可能src不为null，一样要显示*/
		if(StringUtil.isEmpty(filePath)&&!StringUtil.isEmpty(msgBean.getPhotoItem().srcFilePath)){
			filePath = msgBean.getPhotoItem().srcFilePath;
		}

		if ((!StringUtil.isEmpty(filePath)) && (new File(filePath).exists())) {
			/* 本地已存在图片 */
			if (pbDownload != null) {
				pbDownload.setVisibility(View.GONE);
			}
			AsynProcessPicture(filePath);
			
			isLoad = true;
		}
		return isLoad;
	}
	
	/**
	 * 本地没有或下载失败，重新下载
	 */
	private void reloadPhoto(){
		if(btnError != null){
			btnError.setVisibility(View.GONE);
		}
		if (pbDownload != null) {
			pbDownload.setVisibility(View.VISIBLE);
		}
		mLiveChatManager
				.RegisterPhotoListener(PrivatePhotoDownloader.this);
		boolean success = mLiveChatManager
				.GetPhotoWithMessage(msgBean.getUserItem().userId, msgBean,
						PhotoSizeType.Large);
		if (!success) {
			if (pbDownload != null) {
				pbDownload.setVisibility(View.GONE);
			}
			mLiveChatManager
					.UnregisterPhotoListener(PrivatePhotoDownloader.this);
			onDownloadPrivatePhotoFailed();
		}
	}

	/**
	 * 下载私密照失败公共处理
	 */
	private void onDownloadPrivatePhotoFailed() {
		if (btnError != null) {
			btnError.setVisibility(View.VISIBLE);
			btnError.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					/* 下载失败，提示及重新下载 */
					MaterialDialogAlert dialog = new MaterialDialogAlert(
							mContext);
					dialog.setMessage(mContext
							.getString(R.string.livechat_download_photo_fail));
					dialog.addButton(dialog.createButton(
							mContext.getString(R.string.retry),
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									/* 文件不存在，下载文件 */
									reloadPhoto();
								}
							}));
					dialog.addButton(dialog.createButton(
							mContext.getString(R.string.cancel),
							null));

					dialog.show();
				}
			});
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ASYN_PROCESS_PICTURE_SUCCESS:{
				Bitmap bitmap = (Bitmap)msg.obj;
				if( bitmap != null ) {
					privatePhoto.setImageBitmap(bitmap);
				}
			}break;
			case GET_PHOTO_SUCCESS:{
				LCMessageItem item = (LCMessageItem) msg.obj;
				LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
				if (item.getPhotoItem().photoId
						.equals(msgBean.getPhotoItem().photoId)) {
					/* 多图片下载时，用于判断是否当前图片 */
					mLiveChatManager
							.UnregisterPhotoListener(PrivatePhotoDownloader.this);
					if (pbDownload != null) {
						pbDownload.setVisibility(View.GONE);
					}
					if (errType == LiveChatErrType.Success) {
						if(loadLocalFile()){
							return;
						}
					}
					onDownloadPrivatePhotoFailed();
				}
			}break;
			
			case GET_SELF_PHOTO_SUCCESS:{
				LCPhotoItem item = (LCPhotoItem) msg.obj;
				LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
				if (item.photoId
						.equals(msgBean.getPhotoItem().photoId)) {
					/* 多图片下载时，用于判断是否当前图片 */
					mLiveChatManager
							.UnregisterPhotoListener(PrivatePhotoDownloader.this);
					if (pbDownload != null) {
						pbDownload.setVisibility(View.GONE);
					}
					if (errType == LiveChatErrType.Success) {
						if(!TextUtils.isEmpty(item.showSrcFilePath)){
							if ((!StringUtil.isEmpty(item.showSrcFilePath)) && (new File(item.showSrcFilePath).exists())) {
								AsynProcessPicture(item.showSrcFilePath);
								return;
							}
						}
					}
					onDownloadPrivatePhotoFailed();
				}
			}break;

			default:
				break;
			}
		}
	};

	@Override
	public void OnSendPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnGetPhoto(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		Message msg = Message.obtain();
		msg.what = GET_PHOTO_SUCCESS;
		msg.arg1 = errType.ordinal();
		msg.obj = item;
		handler.sendMessage(msg);
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
		Message msg = Message.obtain();
		msg.what = GET_SELF_PHOTO_SUCCESS;
		msg.arg1 = errType.ordinal();
		msg.obj = photoItem;
		handler.sendMessage(msg);		
	}
	
	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String photoId,
			String photoDesc) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 异步线程处理图片，防止界面卡住
	 * @param filePath
	 */
	private void AsynProcessPicture(final String filePath){
		PictureHelper.THREAD_POOL_EXECUTOR.execute(new Runnable() {
			
			@Override
			public void run() {
				Bitmap tempBitmap = ImageUtil.decodeHeightDependedBitmapFromFile(filePath, UnitConversion.dip2px(mContext, 112));
				Bitmap newBitmap = ImageUtil.get2DpRoundedImage(mContext, tempBitmap);
				Message msg = Message.obtain();
				msg.what = ASYN_PROCESS_PICTURE_SUCCESS;
				msg.obj = newBitmap;
				handler.sendMessage(msg);
			}
		});
	}
	
}
