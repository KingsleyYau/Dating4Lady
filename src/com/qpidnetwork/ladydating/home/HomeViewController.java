package com.qpidnetwork.ladydating.home;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;

public class HomeViewController implements ViewPager.OnPageChangeListener{
	
	private HomeActivity context;
	
	private View contentView;
	private Toolbar toolbar;
	private ActionBar actionBar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private View adaptiveAcitionbarShawdow;
	private MainmenuPagerAdapter mAdapter;
	
	private int[] tabIcons = new int[]{
			R.drawable.tabbar_icon_man_character,
			R.drawable.tabbar_icon_my_favorite,
			R.drawable.tabbar_icon_my_album,
			R.drawable.tabbar_icon_horiz_more};
	
	private int[] tabTitles = new int[]{
			R.string.fragment_title_gantlemen,
			R.string.fragment_title_my_favortes,
			R.string.fragment_title_my_albums,
			R.string.fragment_title_more
	};

	
	public HomeViewController(HomeActivity context){
		this.context = context;
	}
	
	public View getView(){
		if (contentView != null) return contentView;
		
		contentView = LayoutInflater.from(context).inflate(R.layout.view_home_controller, null);
		toolbar = (Toolbar) contentView.findViewById(R.id.baseToolbar);
        tabLayout = (TabLayout) contentView.findViewById(R.id.baseTabLayout);
        viewPager = (ViewPager) contentView.findViewById(R.id.baseViewPager);
        adaptiveAcitionbarShawdow = (View) contentView.findViewById(R.id.adaptiveTabbarShawdow);
        
        context.setSupportActionBar(toolbar);
        actionBar = context.getSupportActionBar();
        tabLayout.setupWithViewPager(setupViewPager(viewPager));
        
        setTabIcons();
        requestHomeIcon();
        setActionbarTitle(context.getString(tabTitles[0]));
		if (Build.VERSION.SDK_INT >= 21) adaptiveAcitionbarShawdow.setVisibility(View.GONE);
        
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_male_character_focused_24dp);
        viewPager.addOnPageChangeListener(this);
        
		return contentView;
	}
	
	private ViewPager setupViewPager(ViewPager viewPager) {
		// TODO Auto-generated method stub
		viewPager.setOffscreenPageLimit(3);
		mAdapter = new MainmenuPagerAdapter(context);
		viewPager.setAdapter(mAdapter);
		
		// 统计sub的子页(ManListFragment的默认页)
		context.onAnalyticsPageSelected(0, 0, 0);
		
		return viewPager;
	}
	
	private void setTabIcons(){
		for(int i = 0; i < viewPager.getAdapter().getCount(); i++){
			tabLayout.getTabAt(i).setIcon(tabIcons[i]);
		}
	}
	
	private void requestHomeIcon(){
		getActionbar().setHomeAsUpIndicator(R.drawable.logo_40dp);
		getActionbar().setDisplayHomeAsUpEnabled(true);
	}
	
	public void setActionbarColor(int color){
		toolbar.setBackgroundColor(color);
	}	
	
	public void setActionbarTitleColor(int color){
		toolbar.setTitleTextColor(color);
	}
	
	public void setActionbarColorResource(int resourceId){
		toolbar.setBackgroundResource(resourceId);
	}	
	
	public void setActionbarTitleColorResource(int resourceId){
		toolbar.setTitleTextColor(QpidApplication.getProcess().getResources().getColor(resourceId));
	}
	
	
	protected void setActionbarTitle(CharSequence title){
		getActionbar().setTitle(title);
	}
	
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		setActionbarTitle(context.getString(tabTitles[arg0]));
		if (arg0 != 0) tabLayout.getTabAt(0).setIcon(tabIcons[0]);
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
		// 统计screen
		context.onAnalyticsPageSelected(0, arg0);
	}

	protected Toolbar getToolbar(){
		return toolbar;
	}
	
	protected ActionBar getActionbar(){
		return actionBar;
	}
	
	protected ViewPager getViewPager(){
		return viewPager;
	}
	
	protected TabLayout getTabbar(){
		return tabLayout;
	}
	
	/**
	 * 添加或者删除收藏后返回需刷新列表
	 */
	public void refreshFaveriteList(){
		if(mAdapter != null){
			MyFavoriteListFragment fragment = (MyFavoriteListFragment)mAdapter.getFragment(1);
			if(fragment != null){
				fragment.refresh();
			}
		}
	}
	
	/**
	 * 刷新相册列表
	 */
	public void refreshAlbumsList(){
		if(mAdapter != null){
			MyAlbumListFragment fragment = (MyAlbumListFragment) mAdapter.getFragment(2);
			if(fragment != null){
				fragment.refresh();
			}
		}
	}
	
	private class MainmenuPagerAdapter extends FragmentPagerAdapter {
		
		private HashMap<Integer, WeakReference<Fragment>> mPageReference;

		public MainmenuPagerAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
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
			return 4;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			if (mPageReference.containsKey(position)) {
				fragment = mPageReference.get(position).get();
			}
			if (fragment == null) {
				if(position == 0){
					fragment = new ManListFragment();
				}else if(position == 1 ){
					fragment = new MyFavoriteListFragment();
				}else if(position == 2 ){
					fragment = new MyAlbumListFragment();
				}else {
					fragment = new MoreListFragment();
				}
				fragment.setHasOptionsMenu(true);
				mPageReference.put(position, new WeakReference<Fragment>(
						fragment));
			}
			return fragment;
		}
	}
	
}
