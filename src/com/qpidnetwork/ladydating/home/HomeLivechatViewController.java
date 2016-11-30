package com.qpidnetwork.ladydating.home;

import me.tangke.slidemenu.SlideMenu;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.history.ChatContactListActivity;
import com.qpidnetwork.ladydating.chat.history.ChatHistoryUpdateManager;
import com.qpidnetwork.ladydating.chat.invitationtemplate.ChatInvitationTemplateActivity;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.ladydating.chat.invite.OutgoingChatInvitationActivity;
import com.qpidnetwork.ladydating.customized.view.MaterialAppBar;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.db.ChatHistoryDB;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;
import com.qpidnetwork.livechat.LiveChatManager;

public class HomeLivechatViewController implements MaterialDropDownMenu.OnClickCallback,
												   View.OnClickListener{
	
	private HomeActivity homeActivity;
	private View contentView;
	private LiveChatListFragment liveChatListFragment;
	private MaterialAppBar appbar;
	
	/* 广播用于activity间数据传递 */
	private BroadcastReceiver mBroadcastReceiver;
	private ChatHistoryDB mChatHistoryDB;
	
	private static int staticOffSet = 56;  //in dp
	
	public HomeLivechatViewController(HomeActivity context){
		this.homeActivity = context;
	}
	
	public View getView(){
		if (contentView != null) return contentView;

		contentView = LayoutInflater.from(homeActivity).inflate(R.layout.view_home_livechat_controller, null);
		SlideMenu.LayoutParams params = new SlideMenu.LayoutParams(DeviceUtil.getScreenSize().x - getStaticOffset(), LayoutParams.MATCH_PARENT);
		contentView.setLayoutParams(params);
		appbar = (MaterialAppBar) contentView.findViewById(R.id.appbar);
		
		setupController();
		return contentView;
	}
	
	private void setupController(){
		
		appbar.changeDefaulTouchFeedback(R.drawable.touch_feedback_holo_dark_circle);
		appbar.setAppbarBackgroundColor(Color.BLACK);
		appbar.setOnButtonClickListener(this);
		appbar.addButtonToRight(R.id.expand, "", R.drawable.ic_launch_left_white_24dp);
		appbar.addButtonToRight(R.id.search, "", R.drawable.ic_search_white_24dp);
		appbar.addButtonToRight(R.id.chathistory, "", R.drawable.ic_history_white_24dp);
		mChatHistoryDB = ChatHistoryDB.getInstance(homeActivity);
		mChatHistoryDB.clearInvalidRecord(0);//清除无用的本地记录
		if(mChatHistoryDB.isContainUnreadHistory()){
			appbar.pushBadgeById(R.id.chathistory, homeActivity.getResources().getColor(R.color.red));
		}else{
			appbar.cancelBadgeById(R.id.chathistory);
		}
		appbar.addButtonToRight(R.id.overflow, "", R.drawable.ic_more_vert_white_24dp);
		appbar.setTitle(homeActivity.getString(R.string.chat), Color.WHITE);
		
		liveChatListFragment = new LiveChatListFragment();
		FragmentTransaction ft = homeActivity.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.livechatlistFragmentRepleacement, liveChatListFragment, "");
        ft.commit();
        
        //监听获取联系人消息历史结束，刷新聊天历史按钮
		mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(ChatHistoryUpdateManager.ACTION_UPDATE_CHATHISTORY_FINISH)) {
					if(mChatHistoryDB.isContainUnreadHistory()){
						appbar.pushBadgeById(R.id.chathistory, homeActivity.getResources().getColor(R.color.red));
					}else{
						appbar.cancelBadgeById(R.id.chathistory);
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ChatHistoryUpdateManager.ACTION_UPDATE_CHATHISTORY_FINISH);
		homeActivity.registerReceiver(mBroadcastReceiver, filter);
	}
	
	//更新是否有未读历史消息状态
	public void updateChatHistoryStatus(){
		if(mChatHistoryDB.isContainUnreadHistory()){
			appbar.pushBadgeById(R.id.chathistory, homeActivity.getResources().getColor(R.color.red));
		}else{
			appbar.cancelBadgeById(R.id.chathistory);
		}
	}
	
	public int getStaticOffset() {
		return Converter.dp2px(staticOffSet);
	}
	
	public int getOffset() {
		SlideMenu.LayoutParams params = (SlideMenu.LayoutParams) getView().getLayoutParams();
		return DeviceUtil.getScreenSize().x - params.width;
	}
	
	public Point getSize(){
		Point size = DeviceUtil.getScreenSize();
		size.x -= getOffset();
		return size;
	}
	
	public void setOffset(int offset) {

		if (offset == 0) {
			appbar.setButtonIconById(R.id.expand, R.drawable.ic_launch_left_back_white_24dp);
		} else {
			appbar.setButtonIconById(R.id.expand, R.drawable.ic_launch_left_white_24dp);
		}

		SlideMenu.LayoutParams params = (SlideMenu.LayoutParams) getView().getLayoutParams();
		params.width = DeviceUtil.getScreenSize().x - offset;
		getView().requestLayout();

	}

	
	/**
	 * View onClick
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.expand:
			homeActivity.scrollToLeftEdge();
			break;
		case R.id.search:{
			Intent intent = new Intent(homeActivity, ContactSearchActivity.class);
			homeActivity.startActivity(intent);
			homeActivity.overridePendingTransition(R.anim.anim_donot_animate,
					R.anim.anim_donot_animate);
		}break;
		case R.id.chathistory:{
			Intent intent = new Intent(homeActivity, ChatContactListActivity.class);
			homeActivity.startActivity(intent);
		}break;
		case R.id.overflow:{
			String[] overflowMenu = new String[3];
			overflowMenu[0] = homeActivity.getResources().getString(R.string.invitation_template);
			overflowMenu[1] = homeActivity.getResources().getString(R.string.outging_invitation);
			if(LiveChatManager.getInstance().GetAutoInviteStatus()){
				overflowMenu[2] = homeActivity.getResources().getString(R.string.livechat_autoinvite_menu_on);
			}else{
				overflowMenu[2] = homeActivity.getResources().getString(R.string.livechat_autoinvite_menu_off);
			}
			Point point = new Point();
			point.x = Converter.dp2px(240);
			point.y = LayoutParams.WRAP_CONTENT;
			MaterialDropDownMenu drop_menu = new MaterialDropDownMenu(homeActivity, overflowMenu, this, point);
			drop_menu.showAsDropDown(v);
		}break;
		}
	}
	
	/**
	 * Drop down menu onClick
	 */
	@Override
	public void onClick(AdapterView<?> adptView, View v, int which) {
		// TODO Auto-generated method stub
		switch(which){
		case 0:
			openInvitationTemplateManager();
			break;
		case 1:
			openOutgoingInvitationList();
			break;
		case 2:{
			LiveChatManager.getInstance().GetAutoInviteMsgSwitchStatus();
			if(LiveChatManager.getInstance().GetAutoInviteStatus()){
				MaterialDialogAlert dialog = new MaterialDialogAlert(homeActivity);
				dialog.setTitle(homeActivity.getResources().getString(R.string.livechat_autoinvite_deactive_title));
				dialog.setMessage(homeActivity.getResources().getString(R.string.livechat_autoinvite_deactive_desc));
				dialog.addButton(dialog.createButton(homeActivity.getString(R.string.deactive), new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						LiveChatManager.getInstance().CloseOrOpenAutoInvite(false);
					}
				}));
				dialog.addButton(dialog.createButton(homeActivity.getString(R.string.ok),null));
				dialog.show();
				
			}else{
				homeActivity.showAutoInviteDialog();
			}
		}break;
		default:
			break;
		}
	}
	
	private void openOutgoingInvitationList(){
		homeActivity.startActivity(new Intent(homeActivity, OutgoingChatInvitationActivity.class));
//		Intent intent = new Intent(homeActivity, ChatActivity.class);
//		homeActivity.startActivity(intent);
	}
	
	private void openInvitationTemplateManager(){
		ChatInvitationTemplateActivity.launchInviteTemplateActivity(homeActivity, InviteTemplateMode.EDIT_MODE);
	}
	
	public void onDestroy(){
		homeActivity.unregisterReceiver(mBroadcastReceiver);
	}

}
