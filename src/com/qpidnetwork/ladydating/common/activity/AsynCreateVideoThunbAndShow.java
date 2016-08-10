package com.qpidnetwork.ladydating.common.activity;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.qpidnetwork.ladydating.utility.ImageUtil;

public class AsynCreateVideoThunbAndShow {
	
	private static final int CREATE_VIDEO_THUMB_CALLBACK = 1;
	
	private Context mContext = null;
	private Handler mHandler = null;
	private ImageView imageView;
	private Drawable mDefaultImage = null;
	
	public AsynCreateVideoThunbAndShow(Context context){
		mContext = context;
		mHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				switch (msg.what) {
				case CREATE_VIDEO_THUMB_CALLBACK: {
					if (msg.obj instanceof Bitmap) {
						// 动作开始
						Bitmap bitmap = (Bitmap) msg.obj;
						if(imageView != null){
							imageView.setImageBitmap(bitmap);
						}
					}
				}break;

				default:
					break;
				}

			}
		};
	}
	
	public void SetDefaultImage(Drawable drawable) {
		mDefaultImage = drawable;
	}
	
	public void Reset(){
		this.imageView = null;
	}
	
	public void DisplayImage(ImageView imageview, final String localPath, final String videoUri, final int width, final int height){
		this.imageView = imageview;
		Bitmap tmpBitmap = null;
		File file = new File(localPath);
		if (file.exists() && file.isFile()) {
			tmpBitmap = BitmapFactory.decodeFile(localPath, null);
		}
		if (tmpBitmap != null) {
			// 显示缓存
			imageView.setImageBitmap(tmpBitmap);
		} else {
			// 显示默认
			if (mDefaultImage != null) {
				imageView.setImageDrawable(mDefaultImage);
			}
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Bitmap thumbBitmap = ImageUtil.createVideoThumbnail(videoUri, width, height);
					if(thumbBitmap != null){
						ImageUtil.saveBitmapToFile(localPath, thumbBitmap, CompressFormat.JPEG, 100);
					}
					Message msg = Message.obtain();
					msg.what = CREATE_VIDEO_THUMB_CALLBACK;
					msg.obj = thumbBitmap;
					mHandler.sendMessage(msg);
				}
			}).start();
		}
	}
}
