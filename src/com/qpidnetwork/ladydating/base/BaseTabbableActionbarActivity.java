package com.qpidnetwork.ladydating.base;


import com.qpidnetwork.ladydating.R;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public abstract class BaseTabbableActionbarActivity extends BaseFragmentActivity implements ViewPager.OnPageChangeListener{

	
	private Toolbar toolbar;
	private ActionBar actionBar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private View adaptiveAcitionbarShawdow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.base_collapse_actionbar_activity);
		
		toolbar = (Toolbar) findViewById(R.id.baseToolbar);
        tabLayout = (TabLayout) findViewById(R.id.baseTabLayout);
        viewPager = (ViewPager) findViewById(R.id.baseViewPager);
        adaptiveAcitionbarShawdow = (View) findViewById(R.id.adaptiveTabbarShawdow);
        
        if (Build.VERSION.SDK_INT >= 21) adaptiveAcitionbarShawdow.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        
        getViewPager().addOnPageChangeListener(this);
        getTabbar().setupWithViewPager(setupViewPager(getViewPager()));
        
        getTabbar().setBackgroundColor(getResources().getColor(setupTabbarColor()));
		getToolBar().setBackgroundColor(getResources().getColor(setupActionbarColor()));
		
		setTabIcons();
		
	}
	
	
	
	abstract protected ViewPager setupViewPager(ViewPager viewPager);
	
	/**
	 * 
	 * @param position
	 * @return icon resourceId
	 */
	abstract protected Drawable setupTabIcon(int position);
	
	/**
	 * @return ResourceId
	 */
	abstract protected int setupActionbarColor();
	
	/**
	 * @return ResourceId
	 */
	abstract protected int setupTabbarColor();
	
	abstract protected void onMenuItemSelected(MenuItem menu);
	
	/**
	 * 
	 * @param menu
	 * @param inflater
	 * @return return false will show no menu items.
	 */
	abstract protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater);
	
	
	
	protected Toolbar getToolBar(){
		return toolbar;
	}
	
	protected ActionBar getActionbar(){
		return actionBar;
	}
	
	protected TabLayout getTabbar(){
		return tabLayout;
	}
	
	protected ViewPager getViewPager(){
		return viewPager;
	}
	

	/**
	 * 
	 * @param title
	 * @param color specify the actionbar actual title color, can not be color resourceId.
	 */
	protected void setActionbarTitle(CharSequence title, int color){
		getActionbar().setTitle(title);
		getToolBar().setTitleTextColor(color);
	}
	
	
	protected void requestHomeIcon(int resourceId){
		getActionbar().setHomeAsUpIndicator(resourceId);
		getActionbar().setDisplayHomeAsUpEnabled(true);
	}
	
	protected void requestBackIcon(int resourceId){
		getActionbar().setHomeAsUpIndicator(resourceId);
		getActionbar().setDisplayHomeAsUpEnabled(true);
	}
	
	protected void requestBackButton(){
	}
	
	/** private methods **/
	
	private void setTabIcons(){
		for(int i = 0; i < getViewPager().getAdapter().getCount(); i++){
			Drawable drawable = setupTabIcon(i);
			if (drawable == null) continue;
			getTabbar().getTabAt(i).setIcon(drawable);
		}
	}
	
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	onMenuItemSelected(item);

        return super.onOptionsItemSelected(item);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       return onCreateOptionsMenu(menu, getMenuInflater());
    }

}
