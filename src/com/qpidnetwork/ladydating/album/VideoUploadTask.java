package com.qpidnetwork.ladydating.album;

import java.io.File;
import java.util.Random;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.qpidnetwork.framework.util.FileUtil;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.authorization.LoginParam;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.manager.WebsiteManager.WebSite;
import com.qpidnetwork.request.OnSaveAlbumVideoCallback;
import com.qpidnetwork.request.OnUploadVideoCallback;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.RequestJniAlbum;
import com.qpidnetwork.tool.Arithmetic;

/**
 * 实现视频文件上传到相册功能（主要分为上传视频到Livechat服务器 和 添加上传成功视频到相册两个步步骤）
 * @author Hunter Mun
 * 2016.8.1
 */
public class VideoUploadTask {
	
	private static final int UPLOAD_VIDEO_CALLBACK = 1;
	private static final int ADD_VIDEO_TO_ALBUM_CALLBACK = 2;
	
	private int taskId;
	private String albumId;
	private String videoTitle;
	private String videoSrcPath;
	private String videoThumbPath;
	private String shortVideoKey;
	private String mHidFileMd5ID = "";
	
	private Handler mHandler;
	private OnUploadVideoResultCallback mOnUploadVideoResultCallback;
	private UploadStatus mCurrentUploadStatus = UploadStatus.UPLOADING_DEFAULT;
	private long mUploadVideoRequestId = -1;
	private long mAddToAlbumRequestId = -1;
	
	public VideoUploadTask(){
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				RequestBaseResponse response = (RequestBaseResponse)msg.obj;
				switch(msg.what){
				case UPLOAD_VIDEO_CALLBACK:{
					String hidFileMd5ID = "";
					mUploadVideoRequestId = -1; 
					String identifyValues = (String)response.body;
					if(!TextUtils.isEmpty(identifyValues)){
						String[] values = identifyValues.split("\\|");
						if(values.length >=3){
							hidFileMd5ID = values[2];
						}
					}
					if(response.isSuccess 
							&& !TextUtils.isEmpty(hidFileMd5ID)){
						mCurrentUploadStatus = UploadStatus.SUMMITING_TO_ALBUM;
						mHidFileMd5ID = hidFileMd5ID;
						addVideoToAlbum(mHidFileMd5ID);
					}else{
						mCurrentUploadStatus = UploadStatus.UPLOADING_DEFAULT;
						if(mOnUploadVideoResultCallback != null){
							mOnUploadVideoResultCallback.uploadVideoFailed(taskId, "");
						}
					}
				}break;
				case ADD_VIDEO_TO_ALBUM_CALLBACK:{
					mAddToAlbumRequestId = -1;
					if(response.isSuccess){
						mCurrentUploadStatus = UploadStatus.UPLOADING_DEFAULT;
						if(mOnUploadVideoResultCallback != null){
							mOnUploadVideoResultCallback.uploadVideoSuccess(taskId);
						}
					}else{
						if(mOnUploadVideoResultCallback != null){
							mOnUploadVideoResultCallback.uploadVideoFailed(taskId, response.errno);
						}
					}
				}break;
				}
			}
		};
	}
	
	public void VideoUpload(int taskId, String albumId, String videoTitle, String videoPath, String thumbPath){
		this.taskId = taskId;
		this.albumId = albumId;
		this.videoTitle = videoTitle;
		this.videoSrcPath = videoPath;
		this.videoThumbPath = thumbPath;
		
		//无效上传直接回调
		if(TextUtils.isEmpty(albumId)
				|| TextUtils.isEmpty(videoTitle)
				|| TextUtils.isEmpty(videoSrcPath)
				|| TextUtils.isEmpty(videoThumbPath)){
			if(mOnUploadVideoResultCallback != null){
				mOnUploadVideoResultCallback.uploadVideoFailed(taskId, "");
			}
			return;
		}
		
		UploadVideoFile(videoSrcPath);
	}
	
	/**
	 * @param videoPath
	 */
	private void UploadVideoFile(String videoPath){
		LoginParam params = LoginManager.getInstance().GetLoginParam();
		WebSite website = WebsiteManager.getInstance().mWebSite;
		File videoFile = new File(videoPath);
		if(params == null ||
				params.item == null ||
				website == null ||
				TextUtils.isEmpty(videoPath) ||
				!videoFile.exists()){
			if(mOnUploadVideoResultCallback != null){
				mOnUploadVideoResultCallback.uploadVideoFailed(taskId, "");
			}
			return;
		}
		createShortVideoKey(params.item.agent, params.item.lady_id);//生成并保存ShortVideoKey
		mCurrentUploadStatus = UploadStatus.UPLOADING_VIDEO_FILE;
		mUploadVideoRequestId = RequestJniAlbum.UploadVideoFile(params.item.agent, params.item.lady_id, Integer.valueOf(website.websiteId), shortVideoKey, website.isDemo?1:0, videoPath, FileUtil.getMimeType(videoFile), new OnUploadVideoCallback() {
			
			@Override
			public void OnUploadVideo(boolean isSuccess, String errno, String errmsg,
					String identifyValues) {
				Message msg = Message.obtain();
				msg.what = UPLOAD_VIDEO_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, identifyValues);
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	
	/**
	 * 上传视频到相册
	 * @param hidFileMd5ID
	 */
	private void addVideoToAlbum(String hidFileMd5ID){
		mAddToAlbumRequestId = RequestJniAlbum.AddAlbumVideo(albumId, videoTitle, shortVideoKey, hidFileMd5ID, videoThumbPath, new OnSaveAlbumVideoCallback() {
			
			@Override
			public void onSaveAlbumVideo(boolean isSuccess, String errno,
					String errmsg, String videoId) {
				Message msg = Message.obtain();
				msg.what = ADD_VIDEO_TO_ALBUM_CALLBACK;
				RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, videoId);
				msg.obj = response;
				mHandler.sendMessage(msg);				
			}
		});
	}
	
	/**
	 * 生成shortvideokey
	 * @param agentId
	 * @param womanId
	 */
	private void createShortVideoKey(String agentId, String womanId){
		shortVideoKey = "";
		String temp = agentId + womanId + (System.currentTimeMillis()/1000);
		temp += (new Random().nextInt(9999-1000)+1000);
		shortVideoKey = Arithmetic.MD5(temp.getBytes(), temp.getBytes().length);
	}
	
	public interface OnUploadVideoResultCallback{
		public void uploadVideoSuccess(int taskId);
		public void uploadVideoFailed(int taskId, String errNo);
	}
	
	/**
	 * 设置上传视频回调
	 * @param callback
	 */
	public void setUploadVideoResultCallback(OnUploadVideoResultCallback callback){
		mOnUploadVideoResultCallback = callback;
	}
	
	/**
	 * 获取当前上传进度
	 * @return
	 */
	public int getUploadProgress(){
		int progress = 0;
		if(mCurrentUploadStatus == UploadStatus.UPLOADING_VIDEO_FILE
				&& mUploadVideoRequestId != -1){
			int sendLength = RequestJni.GetSendLength(mUploadVideoRequestId);
			int contentLength = RequestJni.GetUploadContentLength(mUploadVideoRequestId);
			if(contentLength > 0){
				progress = (int)(((float)sendLength)* 98/contentLength);
			}
		}else if(mCurrentUploadStatus == UploadStatus.SUMMITING_TO_ALBUM){
			progress = 98;
		}
		return progress;
	}
	
	/**
	 * 停止上传
	 */
	public void cancelUpload(){
		if(mUploadVideoRequestId != -1){
			RequestJni.StopRequest(mUploadVideoRequestId);
		}
		if(mAddToAlbumRequestId != -1){
			RequestJni.StopRequest(mAddToAlbumRequestId);
		}
		mHandler.removeMessages(UPLOAD_VIDEO_CALLBACK);
		mHandler.removeMessages(ADD_VIDEO_TO_ALBUM_CALLBACK);
		if(mOnUploadVideoResultCallback != null){
			mOnUploadVideoResultCallback = null;
		}
		mUploadVideoRequestId = -1;
		mAddToAlbumRequestId = -1;
		mCurrentUploadStatus = UploadStatus.UPLOADING_DEFAULT;
		shortVideoKey = "";
		mHidFileMd5ID = "";
	}
	
	/**
	 * 上传失败点击Retry重新尝试上传
	 */
	public void retry(){
		if(mCurrentUploadStatus == UploadStatus.SUMMITING_TO_ALBUM){
			if(!TextUtils.isEmpty(mHidFileMd5ID)){
				addVideoToAlbum(mHidFileMd5ID);
			}
		}else{
			//无效上传直接回调
			if(TextUtils.isEmpty(albumId)
					|| TextUtils.isEmpty(videoTitle)
					|| TextUtils.isEmpty(videoSrcPath)
					|| TextUtils.isEmpty(videoThumbPath)){
				if(mOnUploadVideoResultCallback != null){
					mOnUploadVideoResultCallback.uploadVideoFailed(taskId, "");
				}
				return;
			}
			UploadVideoFile(videoSrcPath);
		}
	}
	
	/**
	 * 获取上传视频Title
	 * @return
	 */
	public String getVideoTitle(){
		return videoTitle;
	}
	
	/**
	 * 记录当前Task上传状态转换
	 */
	private enum UploadStatus{
		UPLOADING_DEFAULT,
		UPLOADING_VIDEO_FILE,
		SUMMITING_TO_ALBUM
	}
}
