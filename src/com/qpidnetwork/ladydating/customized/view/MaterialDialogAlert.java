package com.qpidnetwork.ladydating.customized.view;

/**
 * Author: Martin Shum
 * 
 * This view is structured by google material design
 * 
 * Set icon
 * Set Title
 * Set Message
 * Add button
 * 
 * If you have any questions please read the code 
 * through to find out the answer (Bazinga!).
 * 
 */


import com.qpidnetwork.ladydating.R;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) 
public class MaterialDialogAlert extends Dialog {
	
	private float density = this.getContext().getResources().getDisplayMetrics().density;
	private LinearLayout contentView;
	
	private int DIALOG_MIN_WIDTH = (int)(280.0f * density);
	private int view_padding = (int)(24.0f * density);
	private int view_margin = (int)(20.0f * density);
	
	public MaterialDialogAlert(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		
		contentView = new LinearLayout(this.getContext());
		contentView.setMinimumWidth(DIALOG_MIN_WIDTH);
		contentView.setBackgroundResource(R.drawable.rectangle_rounded_angle_white_bg);
		contentView.setGravity(Gravity.TOP | Gravity.CENTER);
		contentView.setOrientation(LinearLayout.VERTICAL);
		//contentView.setPadding(UnitConversion.dip2px(context, 1), UnitConversion.dip2px(context, 1), UnitConversion.dip2px(context, 1), UnitConversion.dip2px(context, 1));
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
    	
 
    	//add a view to margin the space
    	LayoutParams mvParams = new LayoutParams((int)dialog_width, LayoutParams.WRAP_CONTENT);
    	LinearLayout messageView = new LinearLayout(this.getContext());
        messageView.setLayoutParams(mvParams);
        messageView.setPadding(view_padding, view_padding, view_padding, view_padding);
        messageView.setGravity(Gravity.TOP | Gravity.CENTER);
        messageView.setOrientation(LinearLayout.VERTICAL);
  
        //icon
        LayoutParams iconParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        iconParams.setMargins(0, 0, 0, view_margin);
        iconParams.gravity = Gravity.CENTER;
        ImageView icon = new ImageView(this.getContext());
        icon.setLayoutParams(iconParams);
        icon.setId(android.R.id.icon);
        icon.setVisibility(View.GONE);
        
        //title
        LayoutParams titleParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, view_margin);
        TextView title = new TextView(this.getContext());
        title.setLayoutParams(titleParams);
        title.setTextColor(this.getContext().getResources().getColor(R.color.text_color_dark));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setId(android.R.id.title);
        title.setVisibility(View.GONE);
        
        //message
        LayoutParams msgParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        TextView message = new TextView(this.getContext());
        message.setLayoutParams(msgParams);
        message.setTextColor(this.getContext().getResources().getColor(R.color.text_color_dark));
        message.setTextSize(18);
        message.setId(android.R.id.message);
        message.setVisibility(View.GONE);
        
        
        //optional choice
        LayoutParams checkBoxParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        checkBoxParams.setMargins(0, view_margin, 0, 0);
        CheckBox checkBox = new CheckBox(this.getContext());
        checkBox.setLayoutParams(checkBoxParams);
        checkBox.setId(android.R.id.checkbox);
        checkBox.setVisibility(View.GONE);
        
        //add elements to body view
        messageView.addView(icon);
        messageView.addView(title);
        messageView.addView(message);
        messageView.addView(checkBox);
        
        //button area
        LayoutParams btnsParams = new LayoutParams((int)dialog_width, (int)(52.0f * density));
        LinearLayout buttons = new LinearLayout(this.getContext());
        buttons.setMinimumHeight((int)(52.0f * density));
        buttons.setLayoutParams(btnsParams);
        buttons.setGravity(Gravity.RIGHT | Gravity.CENTER);
        buttons.setId(android.R.id.extractArea);
        buttons.setPadding(0, 0, (int)(8.0f * density), 0);
        
        contentView.addView(messageView);
        contentView.addView(buttons);

        this.setContentView(contentView);
        
    }
    
    public LinearLayout getContentView(){
    	return contentView;
    }
    
    public void setMessage(CharSequence msg){
    	getMessage().setText(msg);
    	getMessage().setVisibility(View.VISIBLE);
    }
    
    public TextView getMessage(){
    	return (TextView)contentView.findViewById(android.R.id.message);
    }
    
    public void setTitle(CharSequence title){
    	getTitle().setText(title);
    	getTitle().setVisibility(View.VISIBLE);
    }
    
    public TextView getTitle(){
    	return (TextView)contentView.findViewById(android.R.id.title);
    }
    
    public void setCheckBox(String text, boolean checked){
    	getCheckBox().setText(text);
    	getCheckBox().setChecked(checked);
    	getCheckBox().setVisibility(View.VISIBLE);
    }
    
    public CheckBox getCheckBox(){
    	return (CheckBox)contentView.findViewById(android.R.id.checkbox);
    }
    
    public void setIconResource(int resId){
    	getIcon().setImageResource(resId);
    	getIcon().setVisibility(View.VISIBLE);
    }
    
    public ImageView getIcon(){
    	return (ImageView)contentView.findViewById(android.R.id.icon);
    }
    
    public void addButton(Button button){
    	LinearLayout v = (LinearLayout)contentView.findViewById(android.R.id.extractArea);
    	v.addView(button);
    }
    
    public void removeAllButton() {
    	LinearLayout v = (LinearLayout)contentView.findViewById(android.R.id.extractArea);
    	v.removeAllViews();
    }
    
    public Button createButton(CharSequence text, View.OnClickListener click, int id){
    	Button button = createButton(text, click);
    	button.setId(id);
    	return button;
    }
    
    public Button createButton(CharSequence text, View.OnClickListener click){
    	
    	float density = this.getContext().getResources().getDisplayMetrics().density;
    	int button_height = (int)(36.0f * density);
    	int button_margin = (int)(4.0f * density);
    	int button_padding = (int)(8.0f * density);
    	int min_width = (int)(64.0f * density);
    	
    	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, button_height);
    	params.setMargins(button_margin, 0, 0, 0);
    	
    	Button button = new Button(this.getContext());
    	button.setLayoutParams(params);
    	button.setPadding(button_padding, 0, button_padding, 0);
    	button.setBackgroundResource(R.drawable.touch_holo_light_round_angle);
    	button.setTextColor(this.getContext().getResources().getColor(R.color.blue));
    	button.setTextSize(16);
    	button.setMinWidth(min_width);
    	button.setOnClickListener(new OnClick(this, click));
    	button.setText(text);
    	button.setTypeface(null, Typeface.BOLD);
    	
    	return button;
    	
    	
    }
    
    private class OnClick implements View.OnClickListener{

    	private View.OnClickListener click;
    	private Dialog  dialog;
    	
    	public OnClick(Dialog dialog, View.OnClickListener click){
    		this.click = click;
    		this.dialog = dialog;
    	}
    	
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (click != null) click.onClick(v);
			dialog.dismiss();
		}
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
