package com.qpidnetwork.ladydating.googleanalytics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 仅用于跟踪统计的FragmentActivity基类（如：GoogleAnalytics）
 * @author Samson Fan
 *
 */
public class AnalyticsFragmentActivity extends AppCompatActivity
{
	protected static final String PARENT_SCREENNAME = "ParentScreenName";
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		String parentScreenName = "";
		if (null != getIntent() && null != getIntent().getExtras())
		{
			Bundle bundle = getIntent().getExtras();
			if(bundle.containsKey(PARENT_SCREENNAME)){
				parentScreenName = bundle.getString(PARENT_SCREENNAME);
			}
		}
		// 统计activity onCreate()状态
		AnalyticsManager.newInstance().ReportCreate(this, parentScreenName);
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// 提交screen path
		AnalyticsManager.newInstance().ReportScreenPathWithOnDestroy(this);

		// 统计activity onDestroy()状态
		AnalyticsManager.newInstance().ReportDestroy(this);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		
		// 统计activity onRestart()状态
		AnalyticsManager.newInstance().ReportStart(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// 统计activity onStart()状态
		AnalyticsManager.newInstance().ReportStart(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 统计activity onResume()状态
		AnalyticsManager.newInstance().ReportResume(this);
		// 提交screen path
		AnalyticsManager.newInstance().ReportScreenPath(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// 统计activity onPause()状态
		AnalyticsManager.newInstance().ReportPause(this);
	}
	
	@Override
	protected void onStop() 
	{
		super.onStop();
		
		// 统计activity onStop()状态
		AnalyticsManager.newInstance().ReportStop(this);
	}

	/**
	 * 统计activity tag
	 * @param page
	 */
	public void onAnalyticsPageSelected(int page) 
	{
		// 统计activity onPageSelected()状态
		if (AnalyticsManager.newInstance().ReportPageSelected(this, page)) {
			// 提交screen path
			AnalyticsManager.newInstance().ReportScreenPath(this);			
		}
	}
	
	/**
	 * 统计activity tag的子页
	 * @param page
	 * @param subPage
	 */
	public void onAnalyticsPageSelected(int page, int subPage) 
	{
		// 统计activity onPageSelected()的子页状态
		if (AnalyticsManager.newInstance().ReportPageSelected(this, page, subPage)) {
			// 提交screen path
			AnalyticsManager.newInstance().ReportScreenPath(this);
		}
	}
	
	/**
	 * 统计activity tag页的sub页的子页
	 * @param page
	 * @param subPage
	 */
	public void onAnalyticsPageSelected(int page, int subPage, int subPage2) 
	{
		// 统计activity onPageSelected()的子页状态
		if (AnalyticsManager.newInstance().ReportPageSelected(this, page, subPage, subPage2)) {
			// 提交screen path
			AnalyticsManager.newInstance().ReportScreenPath(this);
		}
	}
	
	/**
	 * 统计activity event
	 * @param category
	 * @param action
	 * @param label
	 */
	public void onAnalyticsEvent(String category, String action, String label)
	{
		// 统计activity event
		AnalyticsManager.newInstance().ReportEvent(category, action, label);
	}
	
	/**
	 * 获取ScreenName
	 */
	protected String getScreenName()
	{
		return AnalyticsManager.newInstance().GetScreenName(this);
	}
}
