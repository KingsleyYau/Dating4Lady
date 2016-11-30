package com.qpidnetwork.ladydating.chat.downloader;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressBar;
import com.qpidnetwork.livechat.LCMagicIconItem;
import com.qpidnetwork.livechat.LCMessageItem;
import com.qpidnetwork.livechat.LiveChatManager;
import com.qpidnetwork.livechat.LiveChatManagerMagicIconListener;
import com.qpidnetwork.livechat.jni.LiveChatClientListener.LiveChatErrType;
import com.qpidnetwork.request.item.MagicIconConfig;


/**
 * 仅用于列表显示的下载器
 * 
 * @author Hunter
 * 
 */
public class MagicIconImageDownloader implements LiveChatManagerMagicIconListener{
	
	private static final int DOWNLOAD_MAGICICON_THUMB_SUCCESS = 1;
	
	private LiveChatManager mLiveChatManager;
	private ImageView magicIconPhoto;
	private MaterialProgressBar pbDownload;
	private ImageButton btnError;
	private LCMessageItem msgBean;
	
	private Context mContext;
	
	public MagicIconImageDownloader(Context context) {
		mLiveChatManager = LiveChatManager.getInstance();
		mContext = context;
	}

	public void displayMagicIconPhoto(ImageView magicIconPhoto,
			MaterialProgressBar pbDownload, LCMessageItem msgBean,
			ImageButton btnError) {
		this.magicIconPhoto = magicIconPhoto;
		this.pbDownload = pbDownload;
		this.msgBean = msgBean;
		this.btnError = btnError;
		Bitmap bitmap = ImageUtil.decodeHeightDependedBitmapFromFile((BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_default_preminum_emotion_grey_56dp)), UnitConversion.dip2px(mContext, 112));
		magicIconPhoto.setImageBitmap(ImageUtil.get2DpRoundedImage(mContext, bitmap));
		
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
		if(msgBean.getMagicIconItem()!= null){
			filePath = msgBean.getMagicIconItem().getThumbPath();
		}

		if ((!StringUtil.isEmpty(filePath)) && (new File(filePath).exists())) {
			/* 本地已存在图片 */
			if (pbDownload != null) {
				pbDownload.setVisibility(View.GONE);
			}
			/* 有缩略图，直接使用 */
			Bitmap thumb = BitmapFactory.decodeFile(msgBean.getMagicIconItem().getThumbPath());
			magicIconPhoto.setImageBitmap(thumb);
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
		mLiveChatManager.RegisterMagicIconListener(this);
		boolean success = mLiveChatManager.GetMagicIconThumbImage(msgBean.getMagicIconItem().getMagicIconId());
		if (!success) {
			if (pbDownload != null) {
				pbDownload.setVisibility(View.GONE);
			}
			mLiveChatManager.UnregisterMagicIconListener(this);
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
							.getString(R.string.livechat_download_magicicon_fail));
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
			case DOWNLOAD_MAGICICON_THUMB_SUCCESS:{
				mLiveChatManager.UnregisterMagicIconListener(MagicIconImageDownloader.this);
				if (pbDownload != null) {
					pbDownload.setVisibility(View.GONE);
				}
				if(msg.arg1 == 1){
					//success callback
					if(loadLocalFile()){
						return;
					}
				}

				onDownloadPrivatePhotoFailed();
			}break;

			default:
				break;
			}
		}
	};

	// ---------------  MagicIcon callback ----------------------
	@Override
	public void OnGetMagicIconConfig(boolean success, String errno,
			String errmsg, MagicIconConfig item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnSendMagicIcon(LiveChatErrType errType, String errmsg,
			LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnRecvMagicIcon(LCMessageItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetMagicIconSrcImage(boolean success,
			LCMagicIconItem magicIconItem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnGetMagicIconThumbImage(boolean success,
			LCMagicIconItem magicIconItem) {
		if(magicIconItem != null && 
				magicIconItem.getMagicIconId().equals(msgBean.getMagicIconItem().getMagicIconId())){
			Message msg = Message.obtain();
			msg.what = DOWNLOAD_MAGICICON_THUMB_SUCCESS;
			msg.arg1 = success?1:0;
			msg.obj = magicIconItem;
			handler.sendMessage(msg);
		}
		
	}

}
