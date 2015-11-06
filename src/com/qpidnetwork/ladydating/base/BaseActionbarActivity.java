package com.qpidnetwork.ladydating.base;

import com.qpidnetwork.ladydating.R;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

public abstract class BaseActionbarActivity extends BaseFragmentActivity implements View.OnClickListener{
	
	
	private Toolbar toolbar;
	private ActionBar actionBar;
	private RelativeLayout customizedContentViewHolder;
	private View adaptiveAcitionbarShawdow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_actionbar_activity);
		
		toolbar = (Toolbar) findViewById(R.id.baseToolbar);
		customizedContentViewHolder = (RelativeLayout) findViewById(R.id.customizedContentViewHolder);
		adaptiveAcitionbarShawdow = (View) findViewById(R.id.adaptiveTabbarShawdow);
		
		if (Build.VERSION.SDK_INT >= 21) adaptiveAcitionbarShawdow.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        
        getToolBar().setBackgroundColor(getResources().getColor(setupThemeColor()));
        setupContentView(customizedContentViewHolder, setupContentVew());
	}
	
	
	

	private void setupContentView(RelativeLayout customizedContentViewHolder, int customizedContentView){
		View view = LayoutInflater.from(this).inflate(customizedContentView, null);
		customizedContentViewHolder.addView(view);
	}
	
	abstract protected int setupContentVew();
	
	/**
	 * Must be color resourceId.
	 * @return
	 */
	abstract protected int setupThemeColor();
	abstract protected void onMenuItemSelected(MenuItem menu);
	
	/**
	 * 
	 * @param menu
	 * @param inflater
	 * @return return false will show no menu items.
	 */
	abstract protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater);
	
	
	protected void requestHomeIcon(int resourceId){
		getActionbar().setHomeAsUpIndicator(resourceId);
		getActionbar().setDisplayHomeAsUpEnabled(true);
	}
	
	protected void requestBackIcon(int resourceId){
		getActionbar().setHomeAsUpIndicator(resourceId);
		getActionbar().setDisplayHomeAsUpEnabled(true);
	}
	
	protected Toolbar getToolBar(){
		return toolbar;
	}
	
	protected ActionBar getActionbar(){
		return actionBar;
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
	
	/**
	 * 
	 * @param title string resource Id
	 * @param color specify the actionbar actual title color, can not be color resourceId.
	 */
	protected void setActionbarTitle(int titleStringResourceId, int color){
		getActionbar().setTitle(titleStringResourceId);
		getToolBar().setTitleTextColor(color);
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
