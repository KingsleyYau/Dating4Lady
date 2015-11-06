package com.qpidnetwork.ladydating.customized.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;

public class MaterialEditText extends TextInputLayout{

	public MaterialEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		String hint = null;
		if (attrs != null){
			int[] attrSet = new int[]{android.R.attr.hint};
			TypedArray a = context.obtainStyledAttributes(attrs, attrSet, 0, 0);
			hint = a.getString(0);
			a.recycle();
		}
		
		if (hint != null){
			this.setHint(hint);
		}
		
		AppCompatEditText editor = new AppCompatEditText(context, attrs);
		this.addView(editor);
		editor.setHint("");
		editor.setId(android.R.id.text1);
		
	}
	
	public void setNumber(){
		getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
	}
	
	public void setEmail(){
		getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
	}
	
	public void setNoPredition(){
		getEditText().setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}
	
	public void setPassword(){
		getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}
	
	public void setVisiblePassword(){
		getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
	}
}
