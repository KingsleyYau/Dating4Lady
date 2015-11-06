package com.qpidnetwork.ladydating.common.activity;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;
import com.qpidnetwork.ladydating.utility.ActivityCodeUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class SimpleTextEditorActivity extends BaseActionbarActivity{

	public static final int ACTIVITY_CODE = ActivityCodeUtil.SIMPLE_TEXT_EDITOR_ACTIVITY_CODE;
	
	public static void launch(Context context, InputParams params){
		Intent intent = new Intent(context, SimpleTextEditorActivity.class);
		intent.putExtra(InputParams.KEY_INPUT_PARAMS, params);
		((FragmentActivity) context).startActivityForResult(intent, ACTIVITY_CODE);
	}
	
	public static class OutputParams implements Serializable{
			
		public static String KEY_OUTPUT_PARAMS = "KEY_OUTPUT_PARAMS";
		private static final long serialVersionUID = 2852650345855285729L;

		private String outputText;
		
		public void setOutputText(String outputText){
			this.outputText = outputText;
		}
		
		public String getOutputText(){
			return this.outputText;
		}
			
	}
	
	public static class InputParams implements Serializable{
		
		public static String KEY_INPUT_PARAMS = "KEY_INPUT_PARAMS";
		private static final long serialVersionUID = -4358247741279734431L;
		
		public String title;
		public int rightButtonIconResourceId = 0;
		public String inputText;
		public String hint;
		public int minLength;
		public int maxLength;
		public String formatString;  
		public boolean requireChange = false;  
		
		public void setTitle(String title){
			this.title = title;
		}
		
		public void setRightButtonResouceId(int buttonResourceId){
			this.rightButtonIconResourceId = buttonResourceId;
		}
		
		public void setInputText(String inputText){
			this.inputText = inputText;
		}
		
		public void setHint(String hint){
			this.hint = hint;
		}
		
		public void setMinLength(int minLength){
			this.minLength = minLength;
		}
		
		public void setMaxLength(int maxLength){
			this.maxLength = maxLength;
		}
		
		public void setFormatString(String formatString){
			this.formatString = formatString;
		}
		
		public void setRequireChange(boolean requireChange){
			this.requireChange = requireChange;
		}
		
	}
	
	private InputParams inputParams;
	private MaterialEditText editText;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null ||
				!extras.containsKey(InputParams.KEY_INPUT_PARAMS)){
			throw new NoSuchElementException("Please start this activity by calling SimpleTextEditorActivity.launch()");
		}
		
		inputParams = (InputParams)extras.getSerializable(InputParams.KEY_INPUT_PARAMS);
		
		editText = (MaterialEditText) findViewById(R.id.editText);
		
		if (inputParams.maxLength > 0) 
			editText.getEditText().setFilters(new InputFilter[] {new InputFilter.LengthFilter(inputParams.maxLength)});
		
		if (inputParams.inputText != null)
			editText.getEditText().setText(inputParams.inputText);
		
		if (inputParams.hint != null)
			editText.setHint(inputParams.hint);
		
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		this.setActionbarTitle((inputParams.title != null) ? inputParams.title : getString(R.string.text_editor), getResources().getColor(R.color.text_color_dark));

	}
	
	private void doSubmit(){
		editText.setErrorEnabled(false);
		String text = editText.getEditText().getText().toString();
		
		if (inputParams.minLength > 0 &&
				text.length() < inputParams.minLength){
			editText.setError(getString(R.string.require_at_least_x_characters, inputParams.minLength + ""));
			editText.setErrorEnabled(true);
			this.shakeView(editText, true);
			return;
		}
		
		if (inputParams.requireChange &&
				text.equals(inputParams.inputText)){
			editText.setError(getString(R.string.you_did_not_make_any_changes_to_the_text_yet));
			editText.setErrorEnabled(true);
			this.shakeView(editText, true);
			return;
		}
		
		if (inputParams.formatString != null){
			Pattern pattern = Pattern.compile(inputParams.formatString);
			Matcher matcher = pattern.matcher(text);
			if (!matcher.find()){
				editText.setError(getString(R.string.incorrect_format));
				editText.setErrorEnabled(true);
				this.shakeView(editText, true);
			}
		}
		
		OutputParams params = new OutputParams();
		params.setOutputText(text);
		getIntent().putExtra(OutputParams.KEY_OUTPUT_PARAMS, params);
		setResult(RESULT_OK, getIntent());
		finish();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_simple_editor;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch (menu.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.done:
			doSubmit();
			break;
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.done, menu);
		if (inputParams.rightButtonIconResourceId != 0){
			MenuItem menuItem = menu.findItem(R.id.done);
			menuItem.setIcon(inputParams.rightButtonIconResourceId);
		}
		return true;
	}

}
