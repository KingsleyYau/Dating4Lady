package com.qpidnetwork.ladydating.home;

import me.tangke.slidemenu.SlideMenu;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.chat.OutgoingChatInvitationActivity;
import com.qpidnetwork.ladydating.chat.invitationtemplate.ChatInvitationTemplateActivity;
import com.qpidnetwork.ladydating.chat.invitationtemplate.InviteTemplateManager.InviteTemplateMode;
import com.qpidnetwork.ladydating.customized.view.MaterialAppBar;
import com.qpidnetwork.ladydating.customized.view.MaterialDropDownMenu;
import com.qpidnetwork.ladydating.utility.Converter;
import com.qpidnetwork.ladydating.utility.DeviceUtil;

public class HomeLivechatViewController implements MaterialDropDownMenu.OnClickCallback,
												   View.OnClickListener{
	
	private HomeActivity homeActivity;
	private View contentView;
	private LiveChatListFragment liveChatListFragment;
	private MaterialAppBar appbar;
	
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
		
		String[] overflowMenu = new String[]{
				homeActivity.getString(R.string.invitation_template),
				homeActivity.getString(R.string.outging_invitation)
		};
		
		Point point = new Point();
		point.x = Converter.dp2px(240);
		point.y = LayoutParams.WRAP_CONTENT;
		
		appbar.changeDefaulTouchFeedback(R.drawable.touch_feedback_holo_dark_circle);
		appbar.setAppbarBackgroundColor(Color.BLACK);
		appbar.setOnButtonClickListener(this);
		appbar.addButtonToRight(R.id.expand, "", R.drawable.ic_launch_left_white_24dp);
		appbar.addButtonToRight(R.id.search, "", R.drawable.ic_search_white_24dp);
		appbar.addOverflowButton(overflowMenu, this, R.drawable.ic_more_vert_white_24dp, point);
		appbar.setTitle(homeActivity.getString(R.string.chat), Color.WHITE);
		
		liveChatListFragment = new LiveChatListFragment();
		FragmentTransaction ft = homeActivity.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.livechatlistFragmentRepleacement, liveChatListFragment, "");
        ft.commit();
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
		default:
			break;
		}
	}
	
	private void openOutgoingInvitationList(){
		homeActivity.startActivity(new Intent(homeActivity, OutgoingChatInvitationActivity.class));
	}
	
	private void openInvitationTemplateManager(){
		ChatInvitationTemplateActivity.launchInviteTemplateActivity(homeActivity, InviteTemplateMode.EDIT_MODE);
	}

}
