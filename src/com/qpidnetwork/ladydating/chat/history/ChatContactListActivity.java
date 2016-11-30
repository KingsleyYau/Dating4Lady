package com.qpidnetwork.ladydating.chat.history;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;
import com.qpidnetwork.ladydating.db.ChatHistoryDB;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.livechat.LiveChatManager;

public class ChatContactListActivity extends BaseActionbarActivity implements OnItemClickListener{
	
	private static final int SYNCHRONIZE_DATA_CALLBACK = 1;
	
	private ChatHistoryDB mChatHistoryDB;
	private ChatContactListAdapter mAdapter;
	private List<ChatContactItem> mChatContactList;
	private LiveChatManager mLiveChatManager;
	
	private ListView lvChatContact;
	private int mClickPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setActionbarTitle( R.string.livechat_chat_history, getResources().getColor(R.color.text_color_dark));
		this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
		lvChatContact = (ListView)findViewById(R.id.lvChatContact);
		initData();
	}
	
	private void initData(){
		mChatHistoryDB = ChatHistoryDB.getInstance(this);
		mLiveChatManager = LiveChatManager.getInstance();
	
		mChatContactList = new ArrayList<ChatContactItem>();
		mAdapter = new ChatContactListAdapter(this, mChatContactList);
		lvChatContact.setAdapter(mAdapter);
		lvChatContact.setOnItemClickListener(this);
		
		sychronizeData();
	}
	
	/**
	 * 后台同步数据
	 */
	private void sychronizeData(){
		showProgressDialog(getResources().getString(R.string.processing));
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//本地初始化list
				List<ChatContactItem> chatConactList = mChatHistoryDB.getChatHistoryContactList();
				//更新头像字段
				for(ChatContactItem item : chatConactList){
					LCUserItem userItem = mLiveChatManager.GetUserWithId(item.manId);
					item.photoUrl = userItem.imgUrl;
				}
				Message msg = Message.obtain();
				msg.what = SYNCHRONIZE_DATA_CALLBACK;
				msg.obj = chatConactList;
				sendUiMessage(msg);
			}
		}).start();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mClickPosition >=0 && mClickPosition < mChatContactList.size()){
			//点击返回更新指定位置状态
			updateListView();
			mClickPosition = -1;
		}
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleUiMessage(msg);
		dismissProgressDialog();
		switch (msg.what) {
		case SYNCHRONIZE_DATA_CALLBACK:{
			List<ChatContactItem> chatConactList = (List<ChatContactItem>)msg.obj;
			mChatContactList.clear();
			mChatContactList.addAll(chatConactList);
			mAdapter.notifyDataSetChanged();
		}break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position >=0 && position < mChatContactList.size()){
			mClickPosition = position;
			ChatContactItem item = mChatContactList.get(position);
			LivechatChatHistoryActivity.launchLivechatHistoryActivity(this, item.manId, item.manName, item.photoUrl);
		}		
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_chathistory_contact_list;
	}

	@Override
	protected int setupThemeColor() {
		// TODO Auto-generated method stub
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		switch(menu.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.markasread:{
			mChatHistoryDB.markAllasRead();
			for(ChatContactItem item : mChatContactList){
				item.readFlag = true;
			}
			mAdapter.notifyDataSetChanged();
		}break;
		default:
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.chathistory_markasread, menu);
		return true;
	}
	
	/**
	 * 更新已读未读状态
	 * @param item
	 */
	private void updateListView(){
		/*更新单个Item*/
		 View childAt = lvChatContact.getChildAt(mClickPosition - lvChatContact.getFirstVisiblePosition());
         if(childAt != null){
    		 ChatContactItem item = mChatContactList.get(mClickPosition);
    		 if(item != null){
    			 boolean isReadFlag = mChatHistoryDB.getChatContactReadFlag(item.manId);
    			 if(isReadFlag){
    				 childAt.findViewById(R.id.unreadFlag).setVisibility(View.GONE);
    			 }else{
    				 childAt.findViewById(R.id.unreadFlag).setVisibility(View.VISIBLE);
    			 }
    		 }
         }
	}

}
