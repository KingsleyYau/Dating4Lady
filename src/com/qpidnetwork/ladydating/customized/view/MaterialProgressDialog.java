package com.qpidnetwork.ladydating.customized.view;


import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.DrawableUtil;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MaterialProgressDialog extends Dialog{
	
	
	private View contentView;
	private TextView message;
	private ProgressBar progress;

	public MaterialProgressDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		contentView  = LayoutInflater.from(context).inflate(R.layout.view_material_progress_dialog, null);
		message = (TextView)contentView.findViewById(R.id.text1);
		progress = (ProgressBar)contentView.findViewById(R.id.progress);
		this.setCanceledOnTouchOutside(false);
		
		DrawableUtil.changeDrawableColor(progress.getIndeterminateDrawable(), context.getResources().getColor(R.color.blue));
		
        this.setContentView(contentView);
        
	}
	
	public TextView getMessage(){
		return message;
	}
	
	public void setMessage(CharSequence text){
		getMessage().setText(text);
	}

	public void show(String text){
		setMessage(text);
		show();
	}
	
}
