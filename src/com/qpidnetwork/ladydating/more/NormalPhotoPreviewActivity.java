package com.qpidnetwork.ladydating.more;

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

public class NormalPhotoPreviewActivity extends BaseFragmentActivity implements
		OnPageChangeListener {

	private static final String PHOTO_URL_LIST = "privatephoto";
	private static final String CURRENT_SELECT_POSITION = "currPosition";

	private List<String> mPhotoUrls;
	private int currPosition = 0;

	private ViewPagerFixed mViewPager;
	private NormalPhotoAdapter mAdapter;

	public static void launchNoramlPhotoActivity(Context context, ArrayList<String> urlList, int position) {
		Intent intent = new Intent(context, NormalPhotoPreviewActivity.class);
		intent.putStringArrayListExtra(PHOTO_URL_LIST, urlList);
		intent.putExtra(CURRENT_SELECT_POSITION, position);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_privatephoto_preview);

		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

		mPhotoUrls = new ArrayList<String>();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey(PHOTO_URL_LIST)) {
				mPhotoUrls = bundle.getStringArrayList(PHOTO_URL_LIST);
			}
			if (bundle.containsKey(CURRENT_SELECT_POSITION)) {
				currPosition = bundle.getInt(CURRENT_SELECT_POSITION);
			}

		}
		initViews();
	}

	@SuppressWarnings("deprecation")
	private void initViews() {
		/* cancel */
		ImageButton buttonCancel = (ImageButton) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		// 分页控件
		mAdapter = new NormalPhotoAdapter(this, mPhotoUrls);
		mViewPager = (ViewPagerFixed) findViewById(R.id.viewPager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(currPosition);

		if (Build.VERSION.SDK_INT >= 21) {
			buttonCancel.getLayoutParams().height = UnitConversion.dip2px(this,
					48);
			buttonCancel.getLayoutParams().width = UnitConversion.dip2px(this,
					48);
			((RelativeLayout.LayoutParams) buttonCancel.getLayoutParams()).topMargin = UnitConversion
					.dip2px(this, 18);
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
		/* 页面切换，reset之前的ImageView放大设置 */
		if (mAdapter != null) {
			if (mAdapter.getFragment(arg0 - 1) != null) {
				((NormalPhotoFragment) mAdapter.getFragment(arg0 - 1))
						.reset();
			}
			if (mAdapter.getFragment(arg0 + 1) != null) {
				((NormalPhotoFragment) mAdapter.getFragment(arg0 + 1))
						.reset();
			}
		}
	}

	private class NormalPhotoAdapter extends FragmentPagerAdapter {

		private List<String> mPhotoUrls;
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;

		public NormalPhotoAdapter(FragmentActivity activity,
				List<String> photoUrls) {
			super(activity.getSupportFragmentManager());
			this.mPhotoUrls = photoUrls;
			mPageReference = new HashMap<Integer, WeakReference<Fragment>>();
		}

		public Fragment getFragment(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			int count = 0;
			if (mPhotoUrls != null) {
				count = mPhotoUrls.size();
			}
			return count;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			if (fragment == null) {
				fragment = NormalPhotoFragment.getInstance(mPhotoUrls.get(position));
				mPageReference.put(position, new WeakReference<Fragment>(
						fragment));
			}
			return fragment;
		}
	}

}
