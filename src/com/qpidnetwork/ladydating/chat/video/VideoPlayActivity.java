package com.qpidnetwork.ladydating.chat.video;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.ladydating.chat.downloader.LivechatVideoThumbPhotoDownloader;
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

public class VideoPlayActivity extends BaseFragmentActivity implements OnClickListener, Callback, OnSeekCompleteListener, 
			OnCompletionListener, OnErrorListener, OnPreparedListener, OnVideoSizeChangedListener, LiveChatManagerVideoListener{
	
	private static final String LIVECHAT_MESSAGE_ID = "msgId";
	
	private static final int DOWNLOAD_VIDEO_THUMB_CALLBACK = 1;
	private static final int DOWNLOAD_VIDEO_CALLBACK = 2;
	private static final int ON_PLAY_UPDATE = 3;
	private static final int ON_PLAY_STOP = 4;
	
	/*视频播放及计时*/
	private SurfaceView surfaceView;
	private TextView textView;
	
	/*暂停大图*/
	private ImageView imageView;
	private ImageButton buttonPlay;
	
	/*下载中*/
	private LinearLayout llLoading;
	
	/*下载视频失败*/
	private LinearLayout llErrorPage;
	private Button btnRetry;
	
	/*播放器*/
	private MediaPlayer mediaPlayer;
	
	/*data数据区*/
	private LiveChatManager mLiveChatManager;
	private int msgId = -1;
	private String targetId = "";
	private LCMessageItem mMsgItem;
	
	public static void launchVideoPlayActivity(Context context, int msgId, String targetId){
		Intent intent = new Intent(context, VideoPlayActivity.class);
		intent.putExtra(LIVECHAT_MESSAGE_ID, msgId);
		intent.putExtra(ChatActivity.CHAT_TARGET_ID, targetId);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_livechat_video_play);
		
		InitPlayer();
		initViews();
		initData();
	}
	
	private void InitPlayer(){
		try {
			mediaPlayer = new MediaPlayer();
	        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        mediaPlayer.setLooping(false);

	        mediaPlayer.setOnPreparedListener(this);
	        mediaPlayer.setOnCompletionListener(this);  
	        mediaPlayer.setOnSeekCompleteListener(this);
	        mediaPlayer.setOnErrorListener(this);   
	        mediaPlayer.setOnVideoSizeChangedListener(this);

		} catch (Exception e) {
            // TODO: handle exception  
        	Log.d("VideoPlayActivity", "InitPlayer( Exception :" + e.getMessage() + " )");
        }
	}
	
	private void initViews(){
		surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
		surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(this); 
        
		textView = (TextView)findViewById(R.id.textView);
        textView.setText("");
        
		imageView = (ImageView)findViewById(R.id.imageView);
		buttonPlay = (ImageButton)findViewById(R.id.buttonPlay);
		
		llLoading = (LinearLayout)findViewById(R.id.llLoading);
		llErrorPage = (LinearLayout)findViewById(R.id.llErrorPage);
		btnRetry = (Button)findViewById(R.id.btnRetry);
		
		buttonPlay.setOnClickListener(this);
		btnRetry.setOnClickListener(this);
	}
	
	private void initData(){
		mLiveChatManager = LiveChatManager.getInstance();
		mLiveChatManager.RegisterVideoListener(this);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			if(bundle.containsKey(LIVECHAT_MESSAGE_ID)){
				msgId = bundle.getInt(LIVECHAT_MESSAGE_ID);
			}
			if(bundle.containsKey(ChatActivity.CHAT_TARGET_ID)){
				targetId = bundle.getString(ChatActivity.CHAT_TARGET_ID);
			}
		}
		if((msgId >= 0) && (!TextUtils.isEmpty(targetId))){
			mMsgItem = mLiveChatManager.GetMessageWithMsgId(targetId, msgId);
		}
		if(mMsgItem == null){
			finish();
			return;
		}
		downloadVideoThumb();
		if((mMsgItem.getVideoItem() != null) && (mMsgItem.getVideoItem().videoItem != null)
				&& (!TextUtils.isEmpty(mMsgItem.getVideoItem().videoItem.videoPath))){
			//本地有直接播放尝试
			playVideo(mMsgItem.getVideoItem().videoItem.videoPath);
		}else{
			downloadVideo();
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		switch (msg.what) {
		case DOWNLOAD_VIDEO_THUMB_CALLBACK:{
			LCVideoItem videoItem = (LCVideoItem)msg.obj;
			DisplayMetrics display = SystemUtil.getDisplayMetrics(this);
			new LivechatVideoThumbPhotoDownloader(this).DisplayImage(imageView, videoItem.videoId, 
					VideoPhotoType.Big, display.widthPixels, display.heightPixels);
		}break;
		
		case DOWNLOAD_VIDEO_CALLBACK:{
			LiveChatErrType errType = LiveChatErrType.values()[msg.arg1];
			if(errType == LiveChatErrType.Success){
				LCMessageItem item = (LCMessageItem)msg.obj;
				playVideo(item.getVideoItem().videoItem.videoPath);
			}else{
				onDownloadVideoError();
			}
		}break;
		
		case ON_PLAY_UPDATE:{
			String text = String.valueOf((mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()) / 1000);
			textView.setText(text);
			textView.setVisibility(View.VISIBLE);
			Message newMsg = Message.obtain();
			newMsg.what = msg.what;
			sendUiMessageDelayed(newMsg, 1000);
		}break;

		case ON_PLAY_STOP:{
			removeUiMessage(ON_PLAY_UPDATE);
			onStopStatus();
		}break;

		default:
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopPlayVideo();
    	if( mediaPlayer != null ) {
    		mediaPlayer.release();
    	}
    	if(mLiveChatManager != null){
    		mLiveChatManager.UnregisterVideoListener(this);
    	}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonPlay:{
			if((mMsgItem != null) && (mMsgItem.getVideoItem() != null) && (mMsgItem.getVideoItem().videoItem != null)
					&&(!TextUtils.isEmpty(mMsgItem.getVideoItem().videoItem.videoPath))){
				playVideo(mMsgItem.getVideoItem().videoItem.videoPath);
			}
		}break;
		
		case R.id.btnRetry:{
			downloadVideo();
		}break;

		default:
			break;
		}
	}
	
	/**
	 * 下载Video
	 */
	private void downloadVideo(){
		onDownloadingStatus();
		mLiveChatManager.GetVideo(mMsgItem);
	}
	
	/**
	 * 下载缩略图
	 */
	private void downloadVideoThumb(){
		mLiveChatManager.GetVideoPhoto(mMsgItem.getVideoItem().videoItem,VideoPhotoType.Big);
	}
	
	/**
	 * 播放Video
	 */
	private void playVideo(String videoLocalPath){
		mediaPlayer.reset(); 
    	
		if( videoLocalPath != null ) {
			// 设置需要播放的视频  
			try {
				mediaPlayer.setDataSource(videoLocalPath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		onPlayingStatus();
	}
	
	/**
	 * 停止播放Video
	 */
	private void stopPlayVideo(){
		
		Message msg = Message.obtain();
        msg.what = ON_PLAY_STOP;
        sendUiMessage(msg);
        
    	if( mediaPlayer != null && mediaPlayer.isPlaying() ) {
    		mediaPlayer.stop();
    	}
    	
	}
	
	/************************** 相关View 显示控制 *******************************/
	/**
	 * Video 下载中
	 */
	private void onDownloadingStatus(){
		llLoading.setVisibility(View.VISIBLE);
		surfaceView.setVisibility(View.GONE);
		textView.setVisibility(View.GONE);
		imageView.setVisibility(View.GONE);
		buttonPlay.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
	}
	
	/**
	 * Video 播放结束暂停中
	 */
	private void onStopStatus(){
		llLoading.setVisibility(View.GONE);
		surfaceView.setVisibility(View.GONE);
		textView.setVisibility(View.GONE);
		imageView.setVisibility(View.VISIBLE);
		buttonPlay.setVisibility(View.VISIBLE);
		llErrorPage.setVisibility(View.GONE);
	}
	
	/**
	 * Video 播放中
	 */
	private void onPlayingStatus(){
		llLoading.setVisibility(View.GONE);
		surfaceView.setVisibility(View.VISIBLE);
//		textView.setVisibility(View.VISIBLE);
		imageView.setVisibility(View.GONE);
		buttonPlay.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.GONE);
	}
	
	/**
	 * Video 下载失败
	 */
	private void onDownloadVideoError(){
		llLoading.setVisibility(View.GONE);
		surfaceView.setVisibility(View.GONE);
		textView.setVisibility(View.GONE);
		imageView.setVisibility(View.GONE);
		buttonPlay.setVisibility(View.GONE);
		llErrorPage.setVisibility(View.VISIBLE);
	}

	/***************************SurfaceView Callback **********************************/
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("VideoPlayActivity", "surfaceCreated()");
		try {
			// 把视频画面输出到SurfaceView
			mediaPlayer.setDisplay(holder);
	        mediaPlayer.prepareAsync();
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("VideoPlayActivity", "surfaceCreated( Exception : " + e.getMessage() + ")");
		}		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("VideoPlayActivity", "surfaceChanged( " +
				"format : " + format + ", " + 
				"width : " + width + ", " +
				"height : " + height +
				" )");		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("VideoPlayActivity", "surfaceDestroyed()"); 		
	}

	/*************************** MediaPlayer 相关控制 **************************************/
	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.d("VideoPlayActivity", "onVideoSizeChanged( " +
				"width : " + width + ", " +
				"height : " + height +
				" )");		
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d("VideoPlayActivity", "onPrepared()");
        
		AutoResetVideoViewSize();
		
		// 播放  
        mediaPlayer.start();
        
        Message msg = Message.obtain();
        msg.what = ON_PLAY_UPDATE;
        sendUiMessage(msg);		
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d("VideoPlayActivity", "onError()");
		mediaPlayer.reset();
		
		Message msg = Message.obtain();
        msg.what = ON_PLAY_STOP;
        sendUiMessage(msg);	
        
        return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopPlayVideo();
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d("VideoPlayActivity", "onSeekComplete( " + mp.getCurrentPosition() + " )"); 		
	}
	
	/**
     * 自适应视频大小
     */
    @SuppressWarnings("deprecation")
	public void AutoResetVideoViewSize() {
        int width = 0;
        int height = 0;
        int videoWidth = 0;
        int videoHeight = 0;
        if( mediaPlayer != null ) {
            videoWidth = mediaPlayer.getVideoWidth();
            videoHeight = mediaPlayer.getVideoHeight();
        }
        
        if( videoWidth != 0 && videoHeight != 0 ) {
        	// 按照视频长边按比例缩放
        	
        	Display display = getWindowManager().getDefaultDisplay();  

        	int dpWidth = display.getWidth();  
        	int dpHeight = display.getHeight();  
        	
            if ( dpWidth > dpHeight ) {  
            	// 横屏
            	height = dpHeight;
        		width = (int) (1.0f * videoWidth * dpHeight / videoHeight );
            } else {
            	// 竖屏
            	width = dpWidth;
        		height = (int) (1.0f * videoHeight * width / videoWidth );
            }
        	
        	Log.d("VideoPlayActivity", "AutoResetVideoViewSize( " +
    				"videoWidth : " + videoWidth + ", " +
    				"videoHeight : " + videoHeight + ", " +
    				"dpWidth : " + dpWidth + ", " +
    				"dpHeight : " + dpHeight + 
    				" )");
        	
        	Log.d("VideoPlayActivity", "AutoResetVideoViewSize( " +
					"width : " + width + ", " +
					"height : " + height +
					" )");
        	if( surfaceView != null ) {
            	surfaceView.getLayoutParams().width = width;
            	surfaceView.getLayoutParams().height = height;
    			surfaceView.getHolder().setFixedSize(width, height);
        	}
        }
    }

    /********************** Video Livechat 相关  *************************/
	@Override
	public void OnCheckSendVideo(LiveChatErrType errType, ResultType result,
			String errno, String errmsg, LCUserItem userItem,
			LCVideoItem videoItem) {
		
	}

	@Override
	public void OnSendVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		
	}

	@Override
	public void OnGetVideoPhoto(LiveChatErrType errType, String errno,
			String errmsg, VideoPhotoType photoType, LCVideoItem item) {
		if(errType == LiveChatErrType.Success){
			if((item != null) && (!TextUtils.isEmpty(item.videoId)) 
					&& (mMsgItem != null) && (item.videoId.equals(mMsgItem.getVideoItem().videoItem.videoId))){
				Message msg = Message.obtain();
		        msg.what = DOWNLOAD_VIDEO_THUMB_CALLBACK;
		        msg.obj = item;
		        sendUiMessage(msg);
			}
		}
	}

	@Override
	public void OnGetVideo(LiveChatErrType errType, String errno,
			String errmsg, LCMessageItem item) {
		if(item != null && (mMsgItem != null) &&(item.msgId == mMsgItem.msgId)){
			Message msg = Message.obtain();
	        msg.what = DOWNLOAD_VIDEO_CALLBACK;
	        msg.arg1 = errType.ordinal();
	        msg.obj = item;
	        sendUiMessage(msg);
		}
	}

	@Override
	public void OnRecvShowPhoto(LCUserItem userItem, String videoId,
			String videoDesc) {
		
	}

	@Override
	public void OnGetVideoList(boolean isSuccess, String errno, String errmsg,
			LCVideoListGroupItem[] groups, LCVideoListVideoItem[] videos) {
		
	}

	@Override
	public void OnRecvShowVideo(LCUserItem userItem, String videoId,
			String videoDesc) {
		
	}
	


}
