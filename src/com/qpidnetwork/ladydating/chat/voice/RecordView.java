package com.qpidnetwork.ladydating.chat.voice;

import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.ToastUtil;
import com.qpidnetwork.ladydating.R;

public class RecordView extends View implements OnErrorListener{
	
	private MediaRecorder recorder; // 录音器
	private Paint mPaint;
	private float mRadius;
	private Context mContext;
	private long startTime = 0;
	private long lastUpdate = 0;
	private OnRecordTimeChangeListener onRecordTimeChangeListener;
	
	public RecordView(Context context) {
		super(context);
		initRecordlView(context);
	}

	public RecordView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RecordView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initRecordlView(context);
	}
	
	private void initRecordlView(Context context){	
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(R.color.thin_grey));
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
		mRadius = 0;
		mContext = context;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		float radius = (float)Math.sqrt((width*width)/4 + (height*height)/4);
		canvas.drawCircle(((float)width)/2,((float)height)/2, (mRadius*radius)/30, mPaint);
	}
	
	public void startRecording(String saveFilePath){
		setVisibility(View.VISIBLE);
		recorder = new MediaRecorder();
		recorder.setOnErrorListener(this);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setMaxDuration(30 * 1000);
		recorder.setOutputFile(saveFilePath);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		try {
			recorder.prepare();
			recorder.start();
		} catch(IllegalStateException e){
			 recorder = null;
		}catch (IOException e) {
			e.printStackTrace();
			ToastUtil.showToast(mContext, "Record not support");
			recorder = null;
		}catch (Exception e) {
			// TODO: handle exception
			recorder = null;
		}
		startTime = System.currentTimeMillis();
		lastUpdate = System.currentTimeMillis();
		updateMicStatus(); 
	}
	
	public void stopRecording(){
		/*清除view显示*/
		mRadius = 0;
		invalidate();
		setVisibility(View.GONE);
		mHandler.removeCallbacks(mUpdateMicStatusTimer);
//		mHandler.postDelayed(stopRecorder, 100);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				/*异步开线程关闭recorder，防止和关闭动画冲突，导致界面重绘卡死*/
				if (recorder != null) {
					try {
						recorder.setOnErrorListener(null);
						recorder.stop();
						recorder.reset();
						recorder.release();
					} catch (Exception e) {
						e.printStackTrace();
					}
					recorder = null;
				}
			}
		}).start();
		startTime = 0;
		lastUpdate = 0;
	}
	
	/*定时刷新时间及音量状态*/
	private int BASE = 500;
	private int SPACE = 100; //间隔取样时间
	
	private Handler mHandler = new Handler();
	
	private Runnable mUpdateMicStatusTimer = new Runnable(){
		public void run() {
			updateMicStatus();
		};
	};
	
	private void updateMicStatus(){
		if(System.currentTimeMillis() - lastUpdate >= 1000){
			if(onRecordTimeChangeListener != null){
				onRecordTimeChangeListener.onRecordTimeCallback((System.currentTimeMillis() - startTime)/1000);
			}
			lastUpdate = System.currentTimeMillis();
		}
		if(recorder != null){
			double ratio = (double)recorder.getMaxAmplitude() /BASE;
			double db = 0;//分贝
			if (ratio > 1)  
                db = 20 * Math.log10(ratio);   
			mRadius = (float)db;
			invalidate();
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE); 
		}
	}
	
	public void setOnRecordTimeChangeListener(OnRecordTimeChangeListener listener){
		this.onRecordTimeChangeListener = listener;
	}
	
	public interface OnRecordTimeChangeListener{
		public void onRecordTimeCallback(long recordTime);
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		try {
			if (recorder != null){
				recorder.reset();
				recorder = null;
			}			
		} catch (IllegalStateException e) {
			Log.w("RecordView", "stopRecord", e);
		} catch (Exception e) {
			Log.w("RecordView", "stopRecord", e);
		}
	}
}
