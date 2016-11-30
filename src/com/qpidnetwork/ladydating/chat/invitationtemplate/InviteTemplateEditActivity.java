package com.qpidnetwork.ladydating.chat.invitationtemplate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialEditText;

public class InviteTemplateEditActivity extends BaseActionbarActivity implements TextWatcher{
	
	private static final int MAX_LENGTH = 160;
	private static final int MIN_LENGTH = 20;
	public static final String EDIT_TEMPALTE_CONTENT = "templateContent";
	public static final String EDIT_TEMPLATE_AUTOINVITE_FLAG = "isInviteAssistant";
	
	private MaterialEditText editText;
	private CheckBox cbAutoInvite;
	private boolean isInviteAssistant = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		this.setActionbarTitle(getString(R.string.add_new_template), getResources().getColor(R.color.text_color_dark));
		
		editText = (MaterialEditText) findViewById(R.id.editText);
		editText.getEditText().setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_LENGTH)});
		editText.setHint(getString(R.string.type_your_template_here));
		editText.getEditText().addTextChangedListener(this);
		
		cbAutoInvite = (CheckBox)findViewById(R.id.cbAutoInvite);
		cbAutoInvite.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					isInviteAssistant = true;
				}else{
					isInviteAssistant = false;
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		
	}

	@Override
	protected int setupContentVew() {
		return R.layout.activity_simple_editor;
	}

	@Override
	protected int setupThemeColor() {
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
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
		inflater.inflate(R.menu.done, menu);
		return true;
	}
	
	private void doSubmit(){
		editText.setErrorEnabled(false);
		String text = editText.getEditText().getText().toString();
		
		if (text.length() < MIN_LENGTH){
			editText.setError(getString(R.string.require_at_least_x_characters, MIN_LENGTH + ""));
			editText.setErrorEnabled(true);
			this.shakeView(editText, true);
			return;
		}
		
		Intent intent = new Intent();
		intent.putExtra(EDIT_TEMPALTE_CONTENT, text);
		intent.putExtra(EDIT_TEMPLATE_AUTOINVITE_FLAG, isInviteAssistant);
		setResult(RESULT_OK, intent);
		finish();
	}

	/*处理此EditText仅能输入ASCII的 32-127 及0X0,0x9,0xA,0xD, 防止编辑添加输入法自定义表情（emoji等）导致后台入库错误*/
	// 输入表情前EditText中的文本
	private String inputAfterText;
	// 是否重置了EditText的内容
	private boolean resetText;
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		if (!resetText) {
			inputAfterText = s.toString();
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (!resetText) {
			if (count >= 1) {// 表情符号的字符长度最小为2
				CharSequence input = s.subSequence(start, start + count);
				if (containsIllegalCharacter(input.toString())) {
					resetText = true;
					// 是表情符号就将文本还原为输入表情符号之前的内容
					editText.getEditText().setText(inputAfterText);
					CharSequence text = editText.getEditText().getText();
					if (text instanceof Spannable) {
						Spannable spanText = (Spannable) text;
						Selection.setSelection(spanText, text.length());
					}
				}
			}
		} else {
			resetText = false;
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		
	}
	
	/**
	 * 检测是否有emoji表情
	 * 
	 * @param source
	 * @return
	 */
	public static boolean containsIllegalCharacter(String source) {
		int len = source.length();
		for (int i = 0; i < len; i++) {
			char codePoint = source.charAt(i);
			if (!isCharacterlegal(codePoint)) { // 如果不能匹配,则该字符是Emoji表情
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断输入字符是否合法
	 * 
	 * @param codePoint
	 *            比较的单个字符
	 * @return
	 */
	private static boolean isCharacterlegal(char codePoint) {
		return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
				|| ((codePoint >= 0x20) && (codePoint <= 0x7F));
	}
}
