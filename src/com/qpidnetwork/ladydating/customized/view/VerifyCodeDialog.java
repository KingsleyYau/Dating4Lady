package com.qpidnetwork.ladydating.customized.view;

import com.qpidnetwork.ladydating.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * 验证码弹出框（固定Livechat使用）
 * @author Hunter
 * 2015.11.19
 */
public class VerifyCodeDialog extends Dialog implements android.view.View.OnClickListener{
	
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private LinearLayout contentView;
	
	private int DIALOG_MIN_WIDTH = (int)(280.0f * density);
	private int view_padding = (int)(24.0f * density);
	private int view_margin = (int)(20.0f * density);
	private Context mContext;
	
	private MaterialEditText etVerifyCode;
	private ImageView ivVerifyCode;
	private Button btnOk;
	
	private OnButtonOkClickListener mListener;
	
	public VerifyCodeDialog(Context context) {
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
    	
		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_livechat_verifycode, null);
		etVerifyCode = (MaterialEditText)view.findViewById(R.id.etVerifyCode);
		ivVerifyCode = (ImageView)view.findViewById(R.id.ivVerifyCode);
		btnOk = (Button)view.findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		
		LayoutParams mvParams = new LayoutParams((int)dialog_width, LayoutParams.WRAP_CONTENT);
    	view.setLayoutParams(mvParams);
    	view.setPadding(view_padding, view_padding, view_padding, view_padding);
    	contentView.addView(view);

        this.setContentView(contentView);
	}
	
	/**
	 * 设置验证码
	 * @param verifycode
	 */
	public void setVerifyBitmap(byte[] verifycode){
		Bitmap bitmap = BitmapFactory.decodeByteArray(verifycode, 0, verifycode.length);
		if((bitmap != null) && (ivVerifyCode != null)){
			ivVerifyCode.setBackgroundDrawable(new BitmapDrawable(bitmap));
		}
	}
	
	/**
	 * 设置验证码图片
	 * @param resId
	 */
	public void setVerifyBitmap(int resId){
		if(ivVerifyCode != null){
			ivVerifyCode.setBackgroundResource(resId);
		}
	}
	
	public MaterialEditText getVerifyEditText(){
		return etVerifyCode;
	}
	
	/**
	 * 读取输入的验证码
	 * @return
	 */
	private String getInputVerifyCode(){
		String verifyCode = "";
		if(etVerifyCode != null){
			verifyCode = etVerifyCode.getEditText().getText().toString();
		}
		return verifyCode;
	}
	
	/**
	 * 设置按钮响应
	 * @param click
	 */
	public void setOnButtonClick(OnButtonOkClickListener listener){
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnOk:
			if(mListener != null){
				mListener.onClick(getInputVerifyCode());
			}
			break;

		default:
			break;
		}
		
	}
	
	public interface OnButtonOkClickListener{
		public void onClick(String verifyCode);
	}
	
}
