package com.qpidnetwork.tool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import com.qpidnetwork.framework.util.ImageUtil;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.tool.FileDownloader.FileDownloaderCallback;

/**
 * 异步加载图片工具
 * 
 * @author Max.Chiu
 * 
 */
@SuppressLint("HandlerLeak")
public class ImageViewLoader {
	public interface ImageViewLoaderCallback {
		public void OnDisplayNewImageFinish();

		public void OnLoadPhotoFailed();
	}

	private enum DownloadFlag {
		SUCCESS_IMAGEVIEW,
		/**
		 * 处理图片缩放并裁剪圆角成功的消息
		 */
		SUCCESS_IMAGEVIEW_FILLET, FAIL,
	};

	private FileDownloader mFileDownloader;
	private Context mContext = null;
	private Handler mHandler = null;
	
	private ImageView imageView;
	
	private Drawable mDefaultImage = null;
	private boolean mbAlphaAnimation = false;
	private boolean mbBigFileDontUseCache = false;

	public void SetBigFileDontUseCache(boolean bBigFileDontUseCache) {
		mbBigFileDontUseCache = bBigFileDontUseCache;
	}

	public ImageViewLoader(Context context) {
		mContext = context;

		mFileDownloader = new FileDownloader(mContext);

		mHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				DownloadFlag flag = DownloadFlag.values()[msg.what];
				switch (flag) {
				case SUCCESS_IMAGEVIEW: {
					if (msg.obj instanceof Bitmap) {
						// 动作开始
						Bitmap bitmap = (Bitmap) msg.obj;
						AlphaAnimationStart(bitmap);
					}
				}
					break;
				case SUCCESS_IMAGEVIEW_FILLET: {
					// 显示已处理的缓存
					Bitmap bitmap = (Bitmap) msg.obj;
					if (null != bitmap && null != imageView) {
						imageView.setImageBitmap(bitmap);
						// AlphaAnimationStart(bitmap);
					}
				}
					break;
				case FAIL: {

				}
					break;
				default:
					break;
				}

			}
		};
	}

	public void Stop() {
		mFileDownloader.Stop();
		mHandler.removeMessages(DownloadFlag.SUCCESS_IMAGEVIEW.ordinal());
		mHandler.removeMessages(DownloadFlag.SUCCESS_IMAGEVIEW_FILLET.ordinal());
		mHandler.removeMessages(DownloadFlag.FAIL.ordinal());
	}

	/**
	 * 尝试加载文件，若不成功则下载图片，等比缩放至目标大小并显示
	 * 
	 * @param imageView
	 * @param url
	 * @param localPath
	 * @param width
	 * @param height
	 * @param callback
	 * @return
	 */
	public boolean DisplayImage(final ImageView imageView, final String url,
			final String localPath, int width, int height,
			final ImageViewLoaderCallback callback) 
	{
		/* 图片宽度限制不能大于屏幕宽度 */
		final int destWidth = width > SystemUtil.getDisplayMetrics(mContext).widthPixels ? SystemUtil
				.getDisplayMetrics(mContext).widthPixels : width;
		final int destHeight = height > SystemUtil.getDisplayMetrics(mContext).heightPixels ? SystemUtil
				.getDisplayMetrics(mContext).heightPixels : height;
				
		Stop();

		if (localPath == null) {
			return false;
		}

		if (imageView == null) {
			return false;
		}

		if (destWidth <= 0 || destHeight <= 0) {
			return false;
		}

		this.imageView = imageView;

		Bitmap tmpBitmap = null;

		File file = new File(localPath);
		if (file.exists() && file.isFile()) {
			Point displaySize = GetDisplaySize(destWidth, destHeight, -1);
			tmpBitmap = ImageUtil.decodeSampledBitmapFromFile(
									localPath,
									displaySize.x, 
									displaySize.y);
		}

		if (tmpBitmap != null) {
			// 显示缓存
			imageView.setImageBitmap(tmpBitmap);
			imageView.setVisibility(View.VISIBLE);

		} else {
			// 显示默认
			if (mDefaultImage != null) {
				imageView.setImageDrawable(mDefaultImage);
			}
		}

		if (url == null || url.length() == 0) {
			return false;
		}

		// 下载
		final Bitmap bitmap = tmpBitmap;
		mFileDownloader.StartDownload(url, localPath,
				new FileDownloaderCallback() {
					@Override
					public void onUpdate(FileDownloader loader, int progress) {
						// TODO Auto-generated method stub
						// 下载中显示小菊花
					}

					@Override
					public void onSuccess(FileDownloader loader) {
						// TODO Auto-generated method stub
						if (!loader.notModified || bitmap == null) {
							// 下载成功
							Point displaySize = GetDisplaySize(destWidth, destHeight, -1);
							Bitmap tempBitmap = ImageUtil.decodeSampledBitmapFromFile(
													localPath,
													displaySize.x, 
													displaySize.y);
							if (null != tempBitmap) {
								// 显示图片
								Message msg = Message.obtain();
								msg.what = DownloadFlag.SUCCESS_IMAGEVIEW
										.ordinal();
								msg.obj = tempBitmap;
								mHandler.sendMessage(msg);
							}
						}

						if (callback != null) {
							callback.OnDisplayNewImageFinish();
						}
					}

					@Override
					public void onFail(FileDownloader loader) {
						// TODO Auto-generated method stub
						// 下载失败显示X
						Message msg = Message.obtain();
						msg.what = DownloadFlag.FAIL.ordinal();
						mHandler.sendMessage(msg);
					}
				});

		return true;
	}

	/**
	 * 尝试加载文件，若不成功则下载图片，等比缩放至屏幕大小并显示
	 * 
	 * @param imageView
	 * @param url
	 * @param localPath
	 * @param callback
	 * @return
	 */
	public boolean DisplayImage(final ImageView imageView, final String url,
			final String localPath, final ImageViewLoaderCallback callback) 
	{
		return DisplayImage(imageView, url, localPath, 
				SystemUtil.getDisplayMetrics(mContext).widthPixels, 
				SystemUtil.getDisplayMetrics(mContext).heightPixels, callback);
	}

	/**
	 * 下载图片、等比缩放并把图片变圆，再把set到ImageView
	 * 
	 * @param imageView
	 *            ImageView
	 * @param setDefault
	 *            若ImageView已存在图片，是否先替换成默认图片（一般情况下填true）
	 * @param url
	 *            图片下载URL
	 * @param desWidth
	 *            等 比缩放后的宽度
	 * @param desHeight
	 *            等 比缩放后的高度
	 * @param topRadius
	 *            顶部圆角的半径(0：不变圆)
	 * @param bottomRadius
	 *            底部圆角的半径(0：不变圆)
	 * @param localPath
	 *            本地路径
	 * @param callback
	 * @return
	 */
	public boolean DisplayImage(final ImageView imageView,
			final boolean setDefault, final String url, final int desWidth,
			final int desHeight, final int topRadius, final int bottomRadius,
			final String localPath, final ImageViewLoaderCallback callback) {
		Stop();

		if (localPath == null) {
			return false;
		}

		if (imageView == null) {
			return false;
		}

		this.imageView = imageView;

		// 先显示默认图
		if ((this.imageView.getDrawable() == null || setDefault)
				&& mDefaultImage != null) {
			imageView.setImageDrawable(mDefaultImage);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// 显示默认图片
				// if( (imageView.getDrawable() == null || setDefault)
				// && mDefaultImage != null )
				// {
				// Message msg = Message.obtain();
				// msg.what = DownloadFlag.SET_DEFAULT_IMAGE.ordinal();
				// mHandler.sendMessage(msg);
				// }

				// 显示图片
				Bitmap bitmap = getBitmapWithFile(localPath, desWidth,
						desHeight);
				if (null != bitmap) {
					// 显示图片
					Message msg = Message.obtain();
					msg.what = DownloadFlag.SUCCESS_IMAGEVIEW_FILLET.ordinal();
					msg.obj = bitmap;
					mHandler.sendMessage(msg);
				}

				// 没有url，不下载
				if (null == url || url.length() == 0) {
					return;
				}

				// 下载
				final String srcFileName = "_srcfile";
				final String srcFilePath = localPath + srcFileName;

				mFileDownloader.SetBigFile(mbBigFileDontUseCache);
				mFileDownloader.SetUseCache(!mbBigFileDontUseCache);
				mFileDownloader.StartDownload(url, srcFilePath,
						new FileDownloaderCallback() {
							@Override
							public void onUpdate(FileDownloader loader,
									int progress) {
								// TODO Auto-generated method stub
								// 下载中显示小菊花
							}

							@Override
							public void onSuccess(FileDownloader loader) {
								// TODO Auto-generated method stub
								Bitmap bitmap = getBitmapWithFile(localPath);
								if (!loader.notModified || bitmap == null) {
									// localPath图片加载不成功或图片已经更新
									boolean result = false;
									// 处理下载图片
									Point displaySize = GetDisplaySize(desWidth, desHeight, -1);
									Bitmap bitmapBig = ImageUtil.decodeSampledBitmapFromFile(
															srcFilePath, 
															displaySize.x,
															displaySize.y);
									if (null != bitmapBig) {
										// 处理图片为圆角
										bitmapBig = ImageUtil
												.filletBitmap(
														bitmapBig,
														topRadius,
														bottomRadius,
														mContext.getResources()
																.getDisplayMetrics().density);
										// 保存本地图片
										ImageUtil.saveBitmapToFile(localPath,
												bitmapBig,
												Bitmap.CompressFormat.PNG, 100);

										result = null != bitmapBig;
									}

									if (result) {
										// 显示图片
										Message msg = Message.obtain();
										msg.what = DownloadFlag.SUCCESS_IMAGEVIEW_FILLET
												.ordinal();
										msg.obj = bitmapBig;
										mHandler.sendMessage(msg);
									}
								}

								// 回调
								if (callback != null) {
									callback.OnDisplayNewImageFinish();
								}
							}

							@Override
							public void onFail(FileDownloader loader) {
								// TODO Auto-generated method stub
								// 下载失败显示X
								Message msg = Message.obtain();
								msg.what = DownloadFlag.FAIL.ordinal();
								mHandler.sendMessage(msg);
							}
						});
			}
		}).start();

		return true;
	}

	/**
	 * 加载图片文件
	 * 
	 * @param filePath
	 *            图片路径
	 * @return
	 */
	private Bitmap getBitmapWithFile(String filePath) {
		Bitmap bitmap = null;
		File file = new File(filePath);
		if (file.exists() && file.isFile()) {
			// 加载文件
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeFile(filePath, options);
		}
		return bitmap;
	}

	/**
	 * 加载图片文件（仅当图片尺寸不大于目标尺寸时，才能加载成功）
	 * 
	 * @param filePath
	 *            图片路径
	 * @param desWidth
	 *            目标宽度
	 * @param desHeight
	 *            目标高度
	 * @return
	 */
	private Bitmap getBitmapWithFile(String filePath, int desWidth,
			int desHeight) {
		Bitmap bitmap = null;
		try {
			// 读取图片大小
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(new File(filePath)));
			BitmapFactory.Options headOptions = new BitmapFactory.Options();
			headOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, headOptions);
			if (headOptions.outWidth <= desWidth
					&& headOptions.outHeight <= desHeight) {
				// 读取图片
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeFile(filePath, options);
			}

			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bitmap;
	}

	public void SetDefaultImage(Drawable drawable) {
		mDefaultImage = drawable;
	}

	public void SetAlphaAnimation(boolean bAlphaAnimation) {
		mbAlphaAnimation = bAlphaAnimation;
	}

	public void ResetImageView() {
		// TODO Auto-generated method stub
		this.imageView = null;
		if (null != mFileDownloader) {
			mFileDownloader.StopDonotWait();
		}
	}

	private void AlphaAnimationStart(final Bitmap bitmap) {
		if (mbAlphaAnimation) {
			AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
			alphaAnim.setDuration(250);
			alphaAnim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					if (null != imageView) {
						imageView.setImageBitmap(bitmap);
					}
					AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
					alphaAnim.setDuration(250);
					imageView.startAnimation(alphaAnim);
				}
			});
			if (null != imageView) {
				imageView.startAnimation(alphaAnim);
			}
		} else {
			if (null != imageView) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}
	
	/**
	 * 获取不会OOM的显示尺寸
	 * @param destWidth		目标宽度
	 * @param destHeight	目标高度
	 * @param minSize		最小显示尺寸(默认：200，<=0则使用默认值)
	 * @return
	 */
	private Point GetDisplaySize(int destWidth, int destHeight, int minSize)
	{
		Point destSize = new Point(destWidth, destHeight);
		
		int theMinSize = minSize > 0 ? minSize : 200;

		// 若内存不足以显示，则缩小显示尺寸
        long freeMemory = Runtime.getRuntime().freeMemory();
        while (freeMemory < destSize.x * destSize.y * 3
        		&& destSize.x > 2
        		&& destSize.y > 2)
        {
        	// 判断长边是否小于最小显示尺寸
        	if (destSize.x > destSize.y) {
        		if (destSize.x < theMinSize) {
        			break;
        		}
        	}
        	else {
        		if (destSize.y < theMinSize) {
        			break;
        		}
        	}
        	
        	// 缩小
        	destSize.x /= 2;
        	destSize.y /= 2;
        }
		return destSize;
	}
}
