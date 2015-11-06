package com.qpidnetwork.ladydating.common.activity;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;



public class SimpleTextPreviewerActivity extends BaseActionbarActivity{
	
	public static final int ACTIVITY_CODE = ActivityCodeUtil.SAMPLE_TEXT_PREVIEW_ACTIVITY_CODE;
	
	public static String INPUT_TITLE = "";
	public static String INPUT_TEXT = "INPUT_TEXT";
	
	private String inputTitle;
	private String inputText;
	
	private TextView text;
	
	public static void launch(Context context, String inputTitle, String inputText){
		Intent intent = new Intent(context, SimpleTextPreviewerActivity.class);
		intent.putExtra(INPUT_TITLE, inputTitle);
		intent.putExtra(INPUT_TEXT, inputText);
		context.startActivity(intent);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = this.getIntent().getExtras();
		if (extras != null){
			if (extras.containsKey(INPUT_TITLE)) inputTitle = extras.getString(INPUT_TITLE);
			if (extras.containsKey(INPUT_TEXT)) inputText = extras.getString(INPUT_TEXT);
		}
		
		text = (TextView) findViewById(R.id.text);
		
		if (inputText != null) text.setText(inputText);
		this.setActionbarTitle((inputTitle == null) ? getString(R.string.not_titled) : inputTitle, getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_simple_text_previewer;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		if (menu.getItemId() == android.R.id.home) finish();
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return true;
	}

}
