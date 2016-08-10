package com.qpidnetwork.ladydating.chat.invite;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseActionbarActivity;

public class OutgoingChatInvitationActivity extends BaseActionbarActivity{

	OutgoingChatInvitationListFragment invitationListFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		invitationListFragment = new OutgoingChatInvitationListFragment();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentReplacement, invitationListFragment, "");
        ft.commit();
        this.setActionbarTitle(getString(R.string.outging_invitation), getResources().getColor(R.color.text_color_dark));
        this.requestBackIcon(R.drawable.ic_arrow_back_grey600_24dp);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int setupContentVew() {
		// TODO Auto-generated method stub
		return R.layout.activity_outgoing_invitation;
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
		default:
			break;
		}
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return true;
	}

}
