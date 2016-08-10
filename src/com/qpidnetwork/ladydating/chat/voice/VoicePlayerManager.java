package com.qpidnetwork.ladydating.chat.voice;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.View;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;

public class VoicePlayerManager {

	private boolean isPlaying = false; //是否正在播放
	private int currMsgId = -1; //当前播放的Message id
	private MediaPlayer player; // 播放器
	
	private View v; //当前正在播放的View，用于控制图标变换
	private Context mContext;
	
	private static VoicePlayerManager mVoicePlayerManager;
	
	public static VoicePlayerManager getInstance(Context context){
		if(mVoicePlayerManager == null){
			mVoicePlayerManager = new VoicePlayerManager(context);
		}
		return mVoicePlayerManager;
	}
	
	private VoicePlayerManager(Context context){
		isPlaying = false;
		currMsgId = -1;
		mContext = context;
	}
	
	/**
	 * 播放语音
	 * 
	 * @param v
	 * @param filePath
	 */
	public void startPlayVoice(View v, int msgId, String filePath) {

		if(currMsgId == msgId){
			if(isPlaying){
				stopPlaying();
			}else{
				this.v = v;
				if((filePath != null) && (new File(filePath).exists())){
					/*本地文件存在开始播放*/
					startPlaying(filePath);
				}
			}
		}else{
			currMsgId = msgId;
			if(isPlaying){
				stopPlaying();
			}
			this.v = v;
			if((filePath != null) && (new File(filePath).exists())){
				/*本地文件存在开始播放*/
				startPlaying(filePath);
			}
		}
	}
	
	/**
	 * 开启播放
	 */
	private void startPlaying(String filePath) {
		player = new MediaPlayer();
		try {
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					/*自动播放结束*/
					onStopUIUpdate();
				}
			});
			player.setDataSource(filePath);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.prepare();
			player.start();
			onStartUIUpdate();
		} catch (IOException e) {
			onStopUIUpdate();
			e.printStackTrace();
		}
	}

/**
	 * 停止播放
	 */
	public void stopPlaying() {
		onStopUIUpdate();
		if (player != null) {
			player.stop();
			player.release();
			player = null;
		}
	}
	
	/**
	 * 异常或手动停止及异常播放，状态及界面修改
	 */
	@SuppressWarnings("deprecation")
	private void onStopUIUpdate(){
		isPlaying = false;
		if(v != null){
			Drawable drawable= mContext.getResources().getDrawable(R.drawable.ic_play_circle_outline_white_24dp);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			((TextView)v).setCompoundDrawables(drawable,null,null,null);
		}
	}
	
	/**
	 * 正式开始播放，界面修改
	 */
	@SuppressWarnings("deprecation")
	private void onStartUIUpdate(){
		isPlaying = true;
		if(v != null){
			Drawable drawable= mContext.getResources().getDrawable(R.drawable.ic_pause_circle_outline_white_24dp);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			((TextView)v).setCompoundDrawables(drawable,null,null,null);
		}
	}
	
}
