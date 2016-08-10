package com.qpidnetwork.ladydating.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.chat.ChatActivity;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;

public class ContactSearchActivity extends BaseFragmentActivity implements OnEditorActionListener, TextWatcher, OnClickListener, OnItemClickListener{
	
	private ImageView ivCancle;
	private EditText etSearchFilter;
	private ListView lvContainer;
	
	private List<LCUserItem> mContactList;
	private LiveChatListAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts_search);
		initViews();
	}
	
	private void initViews(){
		LinearLayout includeSearch = (LinearLayout) findViewById(R.id.includeSearch);
		includeSearch.setBackgroundColor(Color.BLACK);
		
		ivCancle = (ImageView)findViewById(R.id.ivCancle);
		if (Build.VERSION.SDK_INT >= 21 ) {
			ivCancle.getLayoutParams().height = UnitConversion.dip2px(this, 48);
			ivCancle.getLayoutParams().width = UnitConversion.dip2px(this, 48);
		}
		ivCancle.setOnClickListener(this);
		
		etSearchFilter = (EditText)findViewById(R.id.etSearchFilter);
		etSearchFilter.setHint(R.string.contact_search_filter_hint);
		/*设置键盘搜索键响应*/
		etSearchFilter.setOnEditorActionListener(this);
		etSearchFilter.addTextChangedListener(this);
		
		lvContainer = (ListView)findViewById(R.id.lvContainer);
		if (Build.VERSION.SDK_INT < 21){
			lvContainer.setSelector(R.drawable.touch_feedback_holo_light);
		}
		
		mContactList = new ArrayList<LCUserItem>();
		mAdapter = new LiveChatListAdapter(this, mContactList);
		lvContainer.setAdapter(mAdapter);
		lvContainer.setOnItemClickListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mContactList.clear();
		if (s.toString().length() == 0){
			lvContainer.setVisibility(View.GONE);
		}else{
			mContactList.addAll(getContactsByIdOrName(s.toString()));
			mAdapter.notifyDataSetChanged();
			lvContainer.setVisibility(View.VISIBLE);
		}		
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_SEARCH){
			onSearchByIdOrName();
			return true;
		}
		return false;	
	}
	
	private void onSearchByIdOrName(){
		String key = etSearchFilter.getText().toString().trim();
		if((key == null) ||(key.equals(""))){
			return;
		}
		
		mContactList.clear();
		mContactList.addAll(getContactsByIdOrName(key));
		mAdapter.notifyDataSetChanged();
		lvContainer.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 模糊查找联系人列表（ID或用户名）
	 * 
	 * @param key
	 * @return
	 */
	public List<LCUserItem> getContactsByIdOrName(String key) {
		key = key.toUpperCase(Locale.ENGLISH);
		List<LCUserItem> tempList = new ArrayList<LCUserItem>();
		String keyEncode = Pattern.quote(key);
		Pattern p = Pattern.compile("^(.*" + keyEncode + ".*)$");
		List<LCUserItem> contactList = LiveChatManager.getInstance().GetContactList();
		for (LCUserItem bean : contactList) {
			if ((p.matcher(bean.userId.toUpperCase(Locale.ENGLISH)).find())
					|| (p.matcher(bean.userName.toUpperCase(Locale.ENGLISH)).find())) {
				tempList.add(bean);
			}
		}
		return tempList;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivCancle:{
			finish();
			overridePendingTransition(R.anim.anim_donot_animate, R.anim.anim_donot_animate);  
		}break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		LCUserItem item = mContactList.get(position);
		ChatActivity.launchChatActivity(this, item.userId, item.userName, item.imgUrl);
		finish();
	}
}
