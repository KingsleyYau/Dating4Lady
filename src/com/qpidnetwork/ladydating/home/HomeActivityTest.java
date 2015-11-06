package com.qpidnetwork.ladydating.home;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.base.BaseFragmentPagerAdapter;
import com.qpidnetwork.ladydating.base.BaseTabbableActionbarActivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class HomeActivityTest extends BaseTabbableActionbarActivity {


	private int[] tabIcons = new int[]{
			R.drawable.ic_favorite_grey600_24dp,
			R.drawable.ic_favorite_grey600_24dp,
			R.drawable.ic_collections_grey600_24dp,
			R.drawable.ic_more_horiz_grey600_24dp};
	
	private int[] tabTitles = new int[]{
		R.string.fragment_title_gantlemen,
		R.string.fragment_title_my_favortes,
		R.string.fragment_title_my_albums,
		R.string.fragment_title_more
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionbarTitle(getString(tabTitles[0]), Color.WHITE);
    }

    
	@Override
	protected ViewPager setupViewPager(ViewPager viewPager) {
		// TODO Auto-generated method stub
		BaseFragmentPagerAdapter adapter = new BaseFragmentPagerAdapter(getSupportFragmentManager());
		adapter.addFragment(new ManListFragment1(), "");
		adapter.addFragment(new ManListFragment1(), "");
		adapter.addFragment(new ManListFragment1(), "");
		adapter.addFragment(new ManListFragment1(), "");
		viewPager.setAdapter(adapter);
		return viewPager;
	}
	
	@Override
	protected Drawable setupTabIcon(int pisiton) {
		// TODO Auto-generated method stub
		return getResources().getDrawable( tabIcons[pisiton]);
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
		// TODO Auto-generated method stub
		setActionbarTitle(getString(tabTitles[arg0]), Color.WHITE);
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	protected int setupActionbarColor() {
		// TODO Auto-generated method stub
		return R.color.app_theme_cd;
	}


	@Override
	protected int setupTabbarColor() {
		// TODO Auto-generated method stub
		return R.color.app_theme_cd;
	}





    

   





}
