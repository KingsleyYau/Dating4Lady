package com.qpidnetwork.ladydating.chat.picture;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.qpidnetwork.framework.util.UnitConversion;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentActivity;
import com.qpidnetwork.ladydating.customized.view.ViewPagerFixed;
import com.qpidnetwork.livechat.LCMessageItem;

public class LivechatPrivatePhotoPreviewActivity extends BaseFragmentActivity implements OnPageChangeListener{

	private static final String LIVECHAT_PRIVATEPHOTO = "privatephoto";

	private List<LCMessageItem> mMessageList;
	private int currPosition;
	
	private ViewPagerFixed mViewPager;
	private PrivatePhotoAdapter mAdapter;
//	private LiveChatManager mLiveChatManager;


	public static Intent getIntent(Context context, PrivatePhotoPriviewBean bean) {
		Intent intent = new Intent(context, LivechatPrivatePhotoPreviewActivity.class);
		intent.putExtra(LIVECHAT_PRIVATEPHOTO, bean);
		return intent;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_privatephoto_preview);
		
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
		
		mMessageList = new ArrayList<LCMessageItem>();
//		mLiveChatManager = LiveChatManager.newInstance(this);
		Bundle bundle = getIntent().getExtras();
		if ((bundle != null) && (bundle.containsKey(LIVECHAT_PRIVATEPHOTO))) {
			PrivatePhotoPriviewBean bean = (PrivatePhotoPriviewBean) bundle
					.getSerializable(LIVECHAT_PRIVATEPHOTO);
			if(bean.msgList != null && bean.msgList.size() > 0){
				mMessageList = bean.msgList;
			}
			currPosition = bean.currPosition;
		}else{
			currPosition = 0;
		}
		initViews();
	}
	
	@SuppressWarnings("deprecation")
	private void initViews(){
		/*cancel*/
		ImageButton buttonCancel = (ImageButton)findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		 // 分页控件
		mAdapter = new PrivatePhotoAdapter(this, mMessageList);
		mViewPager = (ViewPagerFixed) findViewById(R.id.viewPager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(currPosition);
		
		if (Build.VERSION.SDK_INT >= 21){
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this, 48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this, 48);
			((RelativeLayout.LayoutParams)buttonCancel.getLayoutParams()).topMargin = UnitConversion.dip2px(this, 18);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		/*页面切换，reset之前的ImageView放大设置*/
		if(mAdapter != null){
			if(mAdapter.getFragment(arg0 - 1) != null){
				((PrivatePhotoPreviewFragment)mAdapter.getFragment(arg0 - 1)).reset();
			}
			if(mAdapter.getFragment(arg0 + 1) != null){
				((PrivatePhotoPreviewFragment)mAdapter.getFragment(arg0 + 1)).reset();
			}
		}
	}
	
	private class PrivatePhotoAdapter extends FragmentPagerAdapter{

		private List<LCMessageItem> msgList;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;
		
		
		public PrivatePhotoAdapter(FragmentActivity activity, List<LCMessageItem> msgList) {
			super(activity.getSupportFragmentManager());
			this.msgList = msgList;
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}
		
		public Fragment getFragment(int position){
			Fragment fragment = null;
			if(mPageReference.containsKey(position)){
				fragment = mPageReference.get(position).get();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			int count = 0;
			if(msgList != null){
				count = msgList.size();
			}
			return count;
		}
		
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if(mPageReference.containsKey(position)){
				fragment = mPageReference.get(position).get();
			}
			if(fragment == null){
				fragment= PrivatePhotoPreviewFragment.getFragment(msgList.get(position));
				mPageReference.put(position, new WeakReference<Fragment>(fragment));
			}
			return fragment;
		}
	}

}
