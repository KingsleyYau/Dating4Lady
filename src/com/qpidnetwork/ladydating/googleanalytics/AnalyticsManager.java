package com.qpidnetwork.ladydating.googleanalytics;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.qpidnetwork.framework.util.Log;
import com.qpidnetwork.framework.util.StringUtil;
import com.qpidnetwork.ladydating.R;

/**
 * Analytics管理类（包括GoogleAnalytics、Facebook等）
 * @author Samson Fan
 *
 */
public class AnalyticsManager 
{
	private static final String ACTIVITYNAME_SEPARATOR = "_";
	private static final String SCREENPATH_SEPARATOR = "-";
	
	/**
	 * tag页统计item
	 */
	@SuppressLint("UseSparseArrays")
	private class ActivityItem
	{
		// activity
		public Activity mActivity = null;
		// tag页的index
		private Integer mPage = null;
		// tag页的子页Map表(tag页index, sub页index)
		private HashMap<Integer, Integer> mSubPage = new HashMap<Integer, Integer>();
		// sub页的子页Map表(sub页index, sub的子页index)
		private HashMap<Integer, Integer> mSubPage2 = new HashMap<Integer, Integer>();
		// 父activity screenName
		public String mParentScreenName = "";
		// 父activity screen路径list
		public ArrayList<String> mParentScreenPathList = new ArrayList<String>();
		
		// 无效的页数
		public static final int InvalidPage = -65535;
		/**
		 * 判断tag页是否有效
		 */
		public boolean IsValidPage() 
		{
			return null != mPage;
		}
		
		/**
		 * 判断tag页的子页是否有效
		 * @return
		 */
		public boolean IsValidSubPage()
		{
			Integer subPage = mSubPage.get(mPage);
			return null != subPage;
		}

		/**
		 * 判断sub页的子页是否有效
		 * @return
		 */
		public boolean IsValidSubPage2()
		{
			boolean result = false;
			Integer subPage = mSubPage.get(mPage);
			if (null != subPage) 
			{
				Integer subPage2 = mSubPage2.get(subPage);
				result = (null != subPage2);
			}
			return result;
		}
		
		/**
		 * 设置tag页
		 * @param page
		 */
		public boolean SetPage(int page)
		{
			boolean result = false;
			mPage = null;
			if (page != InvalidPage) 
			{
				mPage = Integer.valueOf(page);
				result = true;
			}
			return result;
		}
		
		/**
		 * 设置tag页的sub页
		 */
		public boolean SetSubPage(int page, int subPage)
		{
			boolean result = SetPage(page);
			if (result)
			{
				mSubPage.remove(page);
				if (subPage != InvalidPage)
				{
					mSubPage.put(page, subPage);
					result = true;
				}
			}
			return result;
		}
		
		/**
		 * 设置sub页的子页
		 */
		public boolean SetSubPage(int page, int subPage, int subPage2)
		{
			boolean result = false;
			if (SetSubPage(page, subPage))
			{
				mSubPage2.remove(subPage);
				if (subPage2 != InvalidPage)
				{
					mSubPage2.put(subPage, subPage2);
					result = true;
				}
			}
			return result;
		}
		
		/**
		 * 获取tag页
		 * @return
		 */
		public int GetPage()
		{
			int result = InvalidPage;
			if (null != mPage)
			{
				result = mPage;
			}
			return result;
		}
		
		/**
		 * 获取sub页
		 */
		public int GetSubPage()
		{
			int result = InvalidPage;
			Integer subPage = mSubPage.get(mPage);
			if (null != subPage)
			{
				result = subPage;
			}
			return result;
		}
		
		/**
		 * 获取sub页的子页
		 */
		public int GetSubPage2()
		{
			int result = InvalidPage;
			int subPage = GetSubPage();
			if (subPage != InvalidPage) {
				Integer subPage2 = mSubPage2.get(subPage);
				if (null != subPage2)
				{
					result = subPage2;
				}
			}
			return result;
		}
		
		/**
		 * 判断是否有父activity
		 */
		public boolean IsParentScreenName()
		{
			return !mParentScreenName.isEmpty();
		}
		
		/**
		 * 设置父activity screen路径
		 * @param parentScreenPath	父screen路径
		 */
		public void SetParentScreenPath(ArrayList<String> parentScreenPathList)
		{
			mParentScreenPathList.clear();
			mParentScreenPathList.addAll(parentScreenPathList);
		}
		
		/**
		 * 获取父screen路径
		 * @return
		 */
		public String GetParentScreenPath()
		{
			String screenPath = "";
			for (String parentScreenPath : mParentScreenPathList)
			{
				if (!screenPath.isEmpty()) {
					screenPath += SCREENPATH_SEPARATOR;
				}
				screenPath += parentScreenPath;
			}
			return screenPath;
		}
		
		/**
		 * 获取screen路径
		 * @param screenName
		 * @return
		 */
		public String GetScreenPath(String screenName)
		{
			String screenPath = GetParentScreenPath();
			if (!screenPath.isEmpty()) {
				screenPath += SCREENPATH_SEPARATOR;
			}
			screenPath += screenName;
			return screenPath;
		}
	}
	
	/**
	 * 注册类型
	 */
	public enum RegisterType
	{
		Facebook,	// facebook注册
		MyCompany	// 我司注册
	}
	
	// 单例
	private static AnalyticsManager sInstance = null;
	
	// application
	private Application mApplication = null;
	
	// 站点名称
	private String mWebSiteName = "";
	
	// activity统计列表
	private ArrayList<ActivityItem> mActivityStack = new ArrayList<ActivityItem>();
	
	// 用于跟踪的用户ID
	private String mGaUserId = "";
	
	// GoogleAnalytics变量
	private Tracker mGaTracker = null;
	private GoogleAnalytics mGaAnalytics = null;
	
	// Facebook变量
//	AppEventsLogger mFbLogger = null;
	
	/**
	 * 获取单例
	 * @param application
	 * @return
	 */
	public static AnalyticsManager newInstance()
	{
		if (null == sInstance) {
			sInstance = new AnalyticsManager();
		}
		return sInstance;
	}
	
	/**
	 * 构造函数
	 */
	private AnalyticsManager()
	{
	}
	
	/**
	 * 初始化函数
	 * @param application	应用实例
	 * @param configResId	跟踪配置资源ID
	 * @param webSiteName	站点ID
	 * @return
	 */
	public boolean init(Application application, int configResId, String webSiteName)
	{
		boolean result = false;
		if (null != application) 
		{
			mApplication = application;
			mWebSiteName = webSiteName;
			// GA跟踪初始化
			result = GaInit(application, configResId);
			// Facebook跟踪初始化
//			result = result && FbInit(application);
		}
		return result;
	}
	
	public void ReportCreate(Activity activity, String parentScreenName)
	{
		// 添加到activity栈
		ActivityItem item = new ActivityItem();
		item.mActivity = activity;
		item.mParentScreenName = parentScreenName;
		item.SetParentScreenPath(GetCurrActivityScreenPathList());
		AddActivityItem(item);
		
		// 打log
		Log.d("AnalyticsManager", "ReportCreate() activityName:%s", activity.getClass().getName());
	}
	
	/**
	 * activity onDestroy统计
	 * @param activity
	 */
	public void ReportDestroy(Activity activity)
	{
		// 移出activity栈
		RemoveActivityItem(activity);
		
		// 打log
		Log.d("AnalyticsManager", "ReportDestroy() activityName:%s", activity.getClass().getName());
	}
	
	/**
	 * activity onStart统计
	 * @param activity
	 */
	public void ReportStart(Activity activity) 
	{
		if (null != activity) {
			// 转换screenName
			String screenName = GetSiteScreenName(activity);
			
			// 处理start
			if (!screenName.isEmpty()) {
				ReportStartProc(activity, screenName);
			}
		}
	}
	
	/**
	 * activity onResume统计
	 * @param activity
	 */
	public void ReportResume(Activity activity)
	{
		if (null != activity) {
			// 转换screenName
			String screenName = GetSiteScreenName(activity);
			
			// 处理resume
			if (!screenName.isEmpty()) {
				ReportResumeProc(activity, screenName);
			}
			
			// facebook开始统计
//			FbReportResume(activity);
		}
	}
	
	/**
	 * activity onPause统计
	 * @param activity
	 */
	public void ReportPause(Activity activity)
	{
		if (null != activity) {
			// 转换screenName
			String screenName = GetSiteScreenName(activity);

			// 处理pause
			if (!screenName.isEmpty()) {
				ReportPauseProc(activity, screenName);
			}
			
			// facebook停止统计
//			FbReportPause(activity);
		}
	}
	
	/**
	 * activity OnStop统计
	 * @param activity
	 */
	public void ReportStop(Activity activity) 
	{
		if (null != activity) {
			// 转换screenName
			String screenName = GetSiteScreenName(activity);
			
			// 处理stop
			if (!screenName.isEmpty()) {
				ReportStopProc(activity, screenName);
			}
		}
	}
	
	/**
	 * 当有新page seleted时，是否需要提交当前page
	 * @param activity
	 * @return
	 */
	public boolean IsReportCurrPageWithSeleted(Activity activity)
	{
		boolean isReport = false;
		
		ActivityItem item = GetActivityItem(activity);
		if (null != item) 
		{
			isReport = item.IsValidPage();
			isReport = isReport && IsCurrActivityItem(activity);
		}
		
		return isReport;
	}
	
	/**
	 * activity onPageSelected统计
	 * @param activity
	 * @param page
	 */
	public boolean ReportPageSelected(Activity activity, int page)
	{
		boolean isReport = false;
		if (null != activity) 
		{
			ActivityItem item = GetActivityItem(activity);
			if (null != item) 
			{
				// 判断是否需要提交
				isReport = IsReportCurrPageWithSeleted(activity);
				isReport = isReport && item.GetPage() != page;
				
				// 把之前的tag页置为pause状态
				if (isReport) {
					ReportPause(activity);
				}
				
				// 设置当前tag页
				item.SetPage(page);
				
				// 把当前tag页置为resume状态
				if (isReport) {
					ReportResume(activity);
				}
			}
		}
		
		return isReport;
	}
	
	/**
	 * activity onPageSelected统计
	 * @param activity
	 * @param page
	 * @param subPage
	 */
	public boolean ReportPageSelected(Activity activity, int page, int subPage)
	{
		boolean isReport = false;
		if (null != activity) 
		{
			ActivityItem item = GetActivityItem(activity);
			if (null != item) 
			{
				// 判断是否需要提交
				isReport = IsReportCurrPageWithSeleted(activity);
				isReport = isReport && (item.GetPage() != page || item.GetSubPage() != subPage);
				
				// 把之前的tag页置为pause状态
				if (isReport) {
					ReportPause(activity);
				}
				
				// 设置当前tag页的子页
				item.SetSubPage(page, subPage);
				
				// 把当前tag页置为resume状态
				if (isReport) {
					ReportResume(activity);
				}
			}
		}
		
		return isReport;
	}
	
	/**
	 * activity onPageSelected统计
	 * @param activity
	 * @param page
	 * @param subPage
	 * @param subPage2
	 */
	public boolean ReportPageSelected(Activity activity, int page, int subPage, int subPage2)
	{
		boolean isReport = false;	
		if (null != activity) 
		{
			ActivityItem item = GetActivityItem(activity);
			if (null != item) 
			{
				// 判断是否需要提交
				isReport = item.IsValidPage();
				isReport = isReport && IsCurrActivityItem(activity);
				isReport = isReport && (item.GetPage() != page || item.GetSubPage() != subPage || item.GetSubPage2() != subPage2);
				
				// 把之前的tag页置为pause状态
				if (isReport) {
					ReportPause(activity);
				}
				
				// 设置当前tag页的子页
				item.SetSubPage(page, subPage, subPage2);
				
				// 把当前tag页置为resume状态
				if (isReport) {
					ReportResume(activity);
				}
			}
		}
		return isReport;
	}
	
	/**
	 * 统计screen路径
	 */
	public void ReportScreenPath(Activity activity)
	{
		ActivityItem activityItem = GetActivityItem(activity);
		if (null != activityItem) {
			String screenPath = activityItem.GetScreenPath(GetScreenName(activityItem.mActivity));
			if (!screenPath.isEmpty()) {
				GaReportScreenPath(screenPath);
			}
		}
	}
	
	/**
	 * onDestroy时，统计screen路径
	 * @param activity
	 */
	public void ReportScreenPathWithOnDestroy(Activity activity)
	{
//		ReportScreenPath(activity);
		if (!IsCurrActivityItem(activity)) {
			// destroy非当前activity, 更新所有screen path
			UpdateScreenPathWithOnDestroy(activity);
		}
	}
	
	/**
	 * 统计event
	 */
	public void ReportEvent(String category, String action, String label)
	{
		// Ga统计
		GaReportEvent(category, action, label);
	}
	
	/**
	 * 设置用户跟踪ID
	 * @param gaUserId	用户的跟踪ID
	 */
	public void setGAUserId(String gaUserId)
	{
		if (!StringUtil.isEmpty(gaUserId))
		{
			mGaUserId = gaUserId;
			
			// 设置GA用户跟踪ID
			GaSetUserId(mGaUserId);
		}
	}
	
	/**
	 * 用户注册成功
	 * @param gaUserId
	 */
	public void RegisterSuccess(RegisterType registerType)
	{
		// GA注册成功统计
		GaRegisterSuccess(registerType);
		// Facebook注册成功统计
//		FbRegisterSuccess(registerType);
	}
	
	/**
	 * 获取注册类型提交的字符串
	 * @param registerType	注册类型
	 * @return
	 */
	private String GetRegisterTypeString(RegisterType registerType)
	{
		// 转换注册方式字符串
		String regMethod = "";
		switch (registerType)
		{
		case Facebook:
			regMethod = "Facebook";
			break;
		case MyCompany:
			regMethod = "MyCompany";
			break;
		}
		return regMethod;
	}
	
	// ------------------- activity stack处理 ---------------------
	/**
	 * 获取栈中的activity item
	 * @param activity
	 * @return
	 */
	private ActivityItem GetActivityItem(Activity activity)
	{
		ActivityItem result = null;
		for (ActivityItem item : mActivityStack)
		{
			if (item.mActivity == activity) {
				result = item;
			}
		}
		return result;
	}
	
	/**
	 * 获取栈指定activity item的前一个activity item
	 * @param activity
	 * @return
	 */
	private ActivityItem GetPreActivityItem(Activity activity)
	{
		boolean found = false;
		ActivityItem preItem = null;
		for (ActivityItem item : mActivityStack)
		{
			if (item.mActivity == activity) {
				found = true;
				break;
			}
			preItem = item;
		}
		return found ? preItem : null;
	}
	
	/**
	 * 移除activity item
	 * @param activity
	 * @return
	 */
	private boolean RemoveActivityItem(ActivityItem item)
	{
		boolean result = false;
		result = mActivityStack.remove(item);
		return result;
	}
	
	/**
	 * 移除activity
	 * @param activity
	 * @return
	 */
	private boolean RemoveActivityItem(Activity activity)
	{
		boolean result = false;
		ActivityItem item = GetActivityItem(activity);
		if (null != item) {
			RemoveActivityItem(item);
		}
		return result;
	}
	
	/**
	 * 添加activity item
	 * @param item
	 * @return
	 */
	private boolean AddActivityItem(ActivityItem item)
	{
		boolean result = false;
		if (null == GetActivityItem(item.mActivity))
		{
			result = mActivityStack.add(item);
		}
		return result;
	}
	
	/**
	 * 判断当前activity栈中的最后一个
	 * @param activity
	 * @return
	 */
	private boolean IsCurrActivityItem(Activity activity)
	{
		boolean result = false;
		if (!mActivityStack.isEmpty())
		{
			ActivityItem item = mActivityStack.get(mActivityStack.size() - 1);
			result = (item.mActivity == activity);
		}
		return result;
	}
	
	/**
	 * 获取上一个activity screenName
	 * @param activity
	 * @return
	 */
	private String GetPreActivityScreenName(Activity activity)
	{
		String preScreenName = "";
		ActivityItem preItem = GetPreActivityItem(activity);
		if (null != preItem) {
			preScreenName = GetScreenName(preItem.mActivity);
		}
		return preScreenName;
	}
	
	/**
	 * 获取当前activity的screen路径list
	 * @return
	 */
	private ArrayList<String> GetCurrActivityScreenPathList()
	{
		ArrayList<String> screenPathList = new ArrayList<String>();
		if (!mActivityStack.isEmpty())
		{
			// 获取当前activity的父screen path list
			ActivityItem item = mActivityStack.get(mActivityStack.size()-1);
			screenPathList.addAll(item.mParentScreenPathList);
			
			// 添加当前activity的screen name
			String screenName = GetScreenName(item.mActivity);
			if (!screenName.isEmpty()) {
				screenPathList.add(screenName);
			}
		}
		return screenPathList;
	}
	
	/**
	 * activity destroy时更新screen路径
	 * @param activity
	 */
	private void UpdateScreenPathWithOnDestroy(Activity activity)
	{
		// 获取当前activity的screen name
		String screenName = GetScreenName(activity);
		
		// 更新screen path
		if (!mActivityStack.isEmpty() && !screenName.isEmpty())
		{
			if (!IsCurrActivityItem(activity))
			{
				// 不是destroy当前activity，更新所有非activity的screen path
				for (ActivityItem acItem : mActivityStack) 
				{
					if (acItem.mActivity != activity)
					{
						int i = 0;
						while (i < acItem.mParentScreenPathList.size()) 
						{
							// 删除screen path中对应的screen name
							if (acItem.mParentScreenPathList.get(i).equals(screenName)) {
								acItem.mParentScreenPathList.remove(i);
								continue;
							}
							i++;
						}
					}
				}
			}
		}
	}
	
	// ------------------- ScreenName转换处理 --------------------
	/**
	 * 把Activity名称转换为ScreenName
	 * @param activityName
	 * @return
	 */
	private String ActivityNameToScreenName(String activityName)
	{
		String screenName = "";
		// 通过配置文件转换ScreenName
		try {
			String resName = activityName.replaceAll("\\.", ACTIVITYNAME_SEPARATOR);
			resName = resName.replaceAll("-", ACTIVITYNAME_SEPARATOR);
			Field f = R.string.class.getField(resName);
			Integer sid = f.getInt(null);
			screenName = mApplication.getResources().getString(sid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return screenName;
	}
	
	/**
	 * 获取activity的ScreenName
	 * @param activity
	 * @return
	 */
	public String GetScreenName(Activity activity)
	{
		String screenName = "";
		
		// 获取activity转化的screenName
		String activityName = activity.getClass().getName();
		screenName = ActivityNameToScreenName(activityName);
		if (screenName.isEmpty()) 
		{
			// 获取activity tag页的screen转化名称
			ActivityItem item = GetActivityItem(activity);
			if (null != item) 
			{
				activityName = activity.getClass().getName();
				if (item.IsValidPage()) 
				{
					// 有tag页
					activityName += ACTIVITYNAME_SEPARATOR + item.GetPage();
					
					if (item.IsValidSubPage())
					{
						// 有sub页
						activityName += ACTIVITYNAME_SEPARATOR + item.GetSubPage();
						
						// 有sub页的子页
						if (item.IsValidSubPage2())
						{
							activityName += ACTIVITYNAME_SEPARATOR + item.GetSubPage2();
						}
					}
				}
				else if (item.IsParentScreenName()) 
				{
					// 有父activity
					activityName += ACTIVITYNAME_SEPARATOR + item.mParentScreenName;
				}
				else {
					// 默认以上一个activity为父activity
					String preActivityScreenName = GetPreActivityScreenName(activity);
					if (!preActivityScreenName.isEmpty()) {
						activityName += ACTIVITYNAME_SEPARATOR + preActivityScreenName;
					}
				}
			}
			screenName = ActivityNameToScreenName(activityName);
		}
		
		if (screenName.isEmpty()) {
			Log.e("AnalyticsManager", "GetScreenName() is empty, activity:%s", activity.getClass().getName());
		}
		
		return screenName;
	}
	
	/**
	 * 获取带站点名的ScreenName
	 * @param screenName
	 * @return
	 */
	private String GetSiteScreenName(Activity activity)
	{
		String name = "";
		String screenName = GetScreenName(activity);
		if (!screenName.isEmpty()) {
			name = mWebSiteName + ACTIVITYNAME_SEPARATOR + screenName;
		}
		return name;
	}
	
	// ------------------- Report处理 --------------------
	/**
	 * activity onStart统计处理函数
	 * @param activity
	 * @param screenName
	 */
	private void ReportStartProc(Activity activity, String screenName)
	{

	}
	
	/**
	 * activity onStart统计处理函数
	 * @param activity
	 * @param screenName
	 */
	private void ReportStopProc(Activity activity, String screenName)
	{

	}
	
	/**
	 * activity onStart统计处理函数
	 * @param activity
	 * @param screenName
	 */
	private void ReportResumeProc(Activity activity, String screenName)
	{
		// GA开始activity统计
		GaReportStart(screenName);
	}
	
	/**
	 * activity onStart统计处理函数
	 * @param activity
	 * @param screenName
	 */
	private void ReportPauseProc(Activity activity, String screenName)
	{
		// GA停止activity统计
		GaReportStop(screenName);
	}
	// -------------- Google Analytics --------------
	/**
	 * GA初始化
	 * @param application	application
	 * @param configResId	
	 * @return
	 */
	private boolean GaInit(Application application, int configResId)
	{
		boolean result = false;
		mGaAnalytics = GoogleAnalytics.getInstance(application);
		if (null != mGaAnalytics) {
			mGaTracker = mGaAnalytics.newTracker(configResId);
		}
		result = (null != mGaTracker && null != mGaAnalytics);
		return result;
	}
	
	/**
	 * GA开始activity统计
	 * @param screenName
	 */
	private void GaReportStart(String screenName) 
	{
		if (null != mGaAnalytics && null != mGaTracker) 
		{
			Log.d("AnalyticsManager", "GaReportStart() screenName:%s", screenName);
			
			mGaTracker.setScreenName(screenName);
			mGaTracker.send(new HitBuilders.ScreenViewBuilder().build());
		}
	}
	
	/**
	 * GA停止activity统计
	 * @param screenName
	 */
	private void GaReportStop(String screenName) 
	{
		if (null != mGaAnalytics && null != mGaTracker) 
		{
			Log.d("AnalyticsManager", "GaReportStop() screenName:%s", screenName);
			try{
				mGaAnalytics.dispatchLocalHits();
			}catch(Exception e){
				
			}
		}
	}
	
	/**
	 * Ga统计screen路径
	 * @param screenPath
	 */
	private void GaReportScreenPath(String screenPath)
	{
		if (null != mGaAnalytics && null != mGaTracker) 
		{
			Log.d("AnalyticsManager", "GaReportScreenPath() screenPath:%s", screenPath);
			mGaTracker.send(new HitBuilders.EventBuilder()
						    .setCategory("APPActionEvent")
						    .setAction("APPAction")
						    .setLabel(screenPath)
						    .build());
		}
	}
	
	/**
	 * GA设置user id
	 * @param gaUserId	用户的跟踪ID	
	 */
	private void GaSetUserId(String gaUserId)
	{
		if (null != mGaTracker
			&& !StringUtil.isEmpty(gaUserId)) 
		{
			Log.d("AnalyticsManager", "GaSetUserId() userId:%s", gaUserId);
			
			mGaTracker.set("&uid", gaUserId);
			mGaTracker.send(new HitBuilders.EventBuilder().setCategory("userid").setAction("User Sign In").build());
			mGaTracker.send(new HitBuilders.EventBuilder().setCategory("userid").setCustomDimension(2, gaUserId).build());
		}
	}
	
	/**
	 * GA注册成功
	 */
	private void GaRegisterSuccess(RegisterType registerType)
	{
		if (null != mGaTracker) 
		{
			// 获取注册类型字符串
			String regType = GetRegisterTypeString(registerType);
			
			// 打印log
			Log.d("AnalyticsManager", "GaRegisterSuccess() regType:%s", regType);
			
			// Build and send an Event.
			mGaTracker.send(new HitBuilders.EventBuilder()
				.setCategory("registerCategory")
				.setAction("registerSuccess")
				.setLabel(regType)
				.build());
		}
	}
	
	/**
	 * Ga统计event
	 */
	private void GaReportEvent(String category, String action, String label)
	{
		if (null != mGaTracker) 
		{
			Log.d("AnalyticsManager", "GaReportEvent() category:%s, action:%s, label:%s"
					, category, action, label);
			
			// Build and send an Event.
			mGaTracker.send(new HitBuilders.EventBuilder()
				.setCategory(category)
				.setAction(action)
				.setLabel(label)
				.build());
		}
	}
	
	// -------------- Facebook Analytics --------------
//	private boolean FbInit(Application application)
//	{
//		boolean result = false;
//		if (null != application) {
//			mFbLogger = AppEventsLogger.newLogger(application);
//		}
//		result = (null != mFbLogger);
//		return result;
//	}
//	
//	/**
//	 * Facebook开始activity统计
//	 * @param activity
//	 */
//	private void FbReportResume(Activity activity)
//	{
//		AppEventsLogger.activateApp(activity);
//	}
//	
//	/**
//	 * Facebook停止activity统计
//	 * @param activity
//	 */
//	private void FbReportPause(Activity activity) 
//	{
//		AppEventsLogger.deactivateApp(activity);
//	}
//	
//	/**
//	 * Facebook注册成功统计
//	 * @param registerType	注册类型
//	 */
//	private void FbRegisterSuccess(RegisterType registerType)
//	{
//		if (null != mFbLogger) 
//		{
//			// 获取注册类型字符串
//			String regType = GetRegisterTypeString(registerType);
//			
//			// 提交注册event
//			Bundle parameters = new Bundle();
//			parameters.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, regType);
//			mFbLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, parameters);
//		}
//	}
}
