package com.qpidnetwork.ladydating.customized.view;

import com.qpidnetwork.ladydating.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MaterialRaisedButton extends CardView{
	
	private float desity = this.getContext().getResources().getDisplayMetrics().density;
	private int txt_size = (int)(16.00);
	private int space = (int)(8.00 * desity);
	private int elevation = (int)(2.00 * desity);
	private int radius = (int)(2.00 * desity);
	
	public MaterialRaisedButton(Context context) {
		super(context);
	}
	
	public MaterialRaisedButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setThis(attrs);
	}
	
	public MaterialRaisedButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setThis(attrs);
	}
	
	private void setThis(AttributeSet attrs){
		
		int icon = R.drawable.ic_launcher;
		String title = "Button";
		int color = Color.GRAY;
		int background = 0;
		int touch_feedback = 0;
		

		
		if (attrs != null){
			TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.RaisedButton);
			icon = a.getResourceId(R.styleable.RaisedButton_android_src, 0);
			title = a.getString(R.styleable.RaisedButton_android_text);
			color = a.getColor(R.styleable.RaisedButton_android_textColor, Color.GRAY);
			background = a.getColor(R.styleable.RaisedButton_android_background, 0);
			touch_feedback = a.getResourceId(R.styleable.RaisedButton_touch_feedback, 0);
			elevation  = (int)a.getDimension(R.styleable.RaisedButton_elevation, elevation);
			a.recycle();
		}
		
		this.setUseCompatPadding(true);
		this.setClickable(true);
		this.setCardElevation(elevation);
		this.setPreventCornerOverlap(false);
		this.setRadius(radius);
		this.addView(createButton(title, icon, color, touch_feedback));
		if (background != 0) this.setCardBackgroundColor(background);
		
	}

	private LinearLayout createButton(CharSequence name, int iconResourceId, int textColor, int touch){
		
		LinearLayout view = new LinearLayout(this.getContext());
		CardView.LayoutParams params = new CardView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		view.setLayoutParams(params);
		view.setGravity(Gravity.CENTER);
		
		if( Build.VERSION.SDK_INT >= 17 ) {
			view.setOrientation(LinearLayout.HORIZONTAL);
		}
		
		ImageView icon = new ImageView(this.getContext());
		LinearLayout.LayoutParams ic_params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ic_params.setMargins(0, 0, space, 0);
		icon.setLayoutParams(ic_params);
		icon.setImageResource(iconResourceId);
		icon.setVisibility(View.GONE);
		icon.setId(R.id.button_icon);
		if (iconResourceId != 0){
			icon.setVisibility(View.VISIBLE);
		}
		
		
		TextView title = new TextView(this.getContext());
		title.setId(android.R.id.title);
		LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		title.setLayoutParams(tv_params);
		title.setTextSize(txt_size);
		title.setTextColor(textColor);
		title.setVisibility(View.GONE);
		if (name != null && name.length() != 0){
			title.setText(name);
			title.setVisibility(View.VISIBLE);
		}
		
		view.addView(icon);
		view.addView(title);
		if (touch != 0) view.setBackgroundResource(touch);
		
		
		return view;
	}
	
	public void setButtonBackground(int color){
		this.setCardBackgroundColor(color);
	}
	
	public void setButtonTitle(CharSequence text){
		TextView t = (TextView)this.findViewById(android.R.id.title);
		t.setText(text);
		t.setVisibility(View.VISIBLE);
	}
	
	public void setButtonIcon(int resourceId){
		ImageView icon = (ImageView)this.findViewById(R.id.button_icon);
		icon.setImageResource(resourceId);
		icon.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}
	

}
