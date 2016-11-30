package com.qpidnetwork.ladydating.customized.view;

import java.util.Random;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;

public class AutoInviteMsgSwitchDialog extends Dialog implements OnClickListener{
	
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private int DIALOG_MIN_WIDTH = (int)(280.0f * density);
	
	private Context mContext;
	private LinearLayout contentView;
	private int[] mVerifyCode = new int[4];
	private TextView[] mVerifyView = new TextView[4];
	private TextView tvVerifyCode1;
	private TextView tvVerifyCode2;
	private TextView tvVerifyCode3;
	private TextView tvVerifyCode4;
	
	private int mCurrentPostion = 0;//当前需要填入的方框位置
	private OnVerifyListener mListener;
	
	public AutoInviteMsgSwitchDialog(Context context) {
		super(context);
		
		this.mContext = context;
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		
		contentView = new LinearLayout(this.getContext());
		contentView.setMinimumWidth(DIALOG_MIN_WIDTH);
		contentView.setBackgroundResource(R.drawable.rectangle_rounded_angle_white_bg);
		contentView.setGravity(Gravity.TOP | Gravity.CENTER);
		contentView.setOrientation(LinearLayout.VERTICAL);
		createView();
	}
	
	protected void createView() {
		
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	
    	if (Build.VERSION.SDK_INT > 12){
    		display.getSize(size);
    	}else{
    		size.y = display.getHeight();
    		size.x = display.getWidth();
    	}
    	
    	int width_times =  Math.round((float)size.x / (56.0f * density));
    	float dialog_width = ((float)(width_times - 1) * 56.0f * density);
    	float VerifyAreaWidth = dialog_width - (24 + 16) * 2 * density;
    	
    	generateVerifyCode();
    	
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_autoinvite_switch, null);
		TextView tvVerify1 = (TextView)view.findViewById(R.id.tvVerify1);
		TextView tvVerify2 = (TextView)view.findViewById(R.id.tvVerify2);
		TextView tvVerify3 = (TextView)view.findViewById(R.id.tvVerify3);
		TextView tvVerify4 = (TextView)view.findViewById(R.id.tvVerify4);
		tvVerify1.setHint(String.valueOf(mVerifyCode[0]));
		tvVerify2.setHint(String.valueOf(mVerifyCode[1]));
		tvVerify3.setHint(String.valueOf(mVerifyCode[2]));
		tvVerify4.setHint(String.valueOf(mVerifyCode[3]));
		mVerifyView[0] = tvVerify1;
		mVerifyView[1] = tvVerify2;
		mVerifyView[2] = tvVerify3;
		mVerifyView[3] = tvVerify4;
		LinearLayout llVerifySummit = (LinearLayout)view.findViewById(R.id.llVerifySummit);
		
		tvVerifyCode1 = (TextView)view.findViewById(R.id.tvVerifyCode1);
		tvVerifyCode2 = (TextView)view.findViewById(R.id.tvVerifyCode2);
		tvVerifyCode3 = (TextView)view.findViewById(R.id.tvVerifyCode3);
		tvVerifyCode4 = (TextView)view.findViewById(R.id.tvVerifyCode4);
		LinearLayout llVerifyCode = (LinearLayout)view.findViewById(R.id.llVerifyCode);
		
		int[] postion = generateVerifyCodePosition();
		tvVerifyCode1.setText(String.valueOf(mVerifyCode[postion[0]]));
		tvVerifyCode2.setText(String.valueOf(mVerifyCode[postion[1]]));
		tvVerifyCode3.setText(String.valueOf(mVerifyCode[postion[2]]));
		tvVerifyCode4.setText(String.valueOf(mVerifyCode[postion[3]]));
		tvVerifyCode1.setOnClickListener(this);
		tvVerifyCode2.setOnClickListener(this);
		tvVerifyCode3.setOnClickListener(this);
		tvVerifyCode4.setOnClickListener(this);
		
		LinearLayout.LayoutParams verifySummitParams = (LinearLayout.LayoutParams)llVerifySummit.getLayoutParams();
		verifySummitParams.height = (int)(VerifyAreaWidth/4);
		
		LinearLayout.LayoutParams verifyCodeParams = (LinearLayout.LayoutParams)llVerifyCode.getLayoutParams();
		verifyCodeParams.height = (int)VerifyAreaWidth;
		
		float textsize = calculateTextSize((int)(VerifyAreaWidth/4 - 8 * density), "2", tvVerify1);
		tvVerify1.setTextSize(textsize);
		tvVerify2.setTextSize(textsize);
		tvVerify3.setTextSize(textsize);
		tvVerify4.setTextSize(textsize);
		tvVerifyCode1.setTextSize(textsize);
		tvVerifyCode2.setTextSize(textsize);
		tvVerifyCode3.setTextSize(textsize);
		tvVerifyCode4.setTextSize(textsize);
		
		LayoutParams mvParams = new LayoutParams((int)dialog_width, LayoutParams.WRAP_CONTENT);
    	view.setLayoutParams(mvParams);
    	contentView.addView(view);
    	
        this.setContentView(contentView);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tvVerifyCode1:
		case R.id.tvVerifyCode2:
		case R.id.tvVerifyCode3:
		case R.id.tvVerifyCode4:{
			String value = ((TextView)v).getText().toString();
			if(!TextUtils.isEmpty(value)
					&& value.equals(String.valueOf(mVerifyCode[mCurrentPostion]))){
				mVerifyView[mCurrentPostion].setText(String.valueOf(value));
				if(mCurrentPostion >= 3){
					//输入完成
					if(mListener != null){
						mListener.onVerifySuccess();
					}
					dismiss();
				}else{
					mCurrentPostion++;
				}
			}else{
				shakeView(mVerifyView[mCurrentPostion], true);
			}
		}break;
		default:
			break;
		}
	}
	
	private void shakeView(View v, boolean vibrate){
		
		if(vibrate){
			try{
				Vibrator vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);  
		        long [] pattern = {100, 200, 0};   //stop | vibrate | stop | vibrate
		        vibrator.vibrate(pattern, -1); 
			}catch(Exception e){
				//No vibrate if no permission
			}
		}
		v.requestFocus();
		Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake_anim);
		v.startAnimation(shake);
	}
	
	/**
	 *  随机生成4位验证码
	 */
	private void generateVerifyCode(){
		Random random = new Random();
		int startArray[] = {0,1,2,3,4,5,6,7,8,9};//seed array        
		for(int i = 0; i < 4; i++)    
		{    
		    int seed = random.nextInt(startArray.length - i);//从剩下的随机数里生成    
		    mVerifyCode[i] = startArray[seed];//赋值给结果数组    
		    startArray[seed] = startArray[startArray.length - i - 1];//把随机数产生过的位置替换为未被选中的值。    
		}  
	}
	
	/**
	 * 获取VerifyCode position的随机
	 * @return
	 */
	private int[] generateVerifyCodePosition(){
		Random random = new Random();
		int startArray[] = {0,1,2,3};//seed array 
		int[] resultArray = new int[4]; 
		for(int i = 0; i < 4; i++)    
		{    
		    int seed = random.nextInt(startArray.length - i);//从剩下的随机数里生成    
		    resultArray[i] = startArray[seed];//赋值给结果数组    
		    startArray[seed] = startArray[startArray.length - i - 1];//把随机数产生过的位置替换为未被选中的值。    
		}
		return resultArray;
	}
	
	/**
	 * 根据给定view 最大宽度和字符串，计算合适的字体大小，防止字体过大溢出
	 * @param maxWidth
	 * @param text
	 * @param view
	 * @return
	 */
	private float calculateTextSize(int maxWidth, String text, TextView view){
		TextPaint textPaint = view.getPaint();
		Rect bounds = new Rect();
		float textSize = textPaint.getTextSize();
		if(maxWidth > 0){
			textPaint.getTextBounds(text, 0, text.length(), bounds);
			while(bounds.width() > maxWidth
					|| bounds.height() > maxWidth){
				textSize -= 1;
				textPaint.setTextSize(textSize);
				textPaint.getTextBounds(text, 0, text.length(), bounds);
			}
		}
		return textSize;
	}
	
	public void setOnVerifyListener(OnVerifyListener listener){
		mListener = listener;
	}
	
	public interface OnVerifyListener{
		public void onVerifySuccess();
	}

}
