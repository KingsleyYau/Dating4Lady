package com.qpidnetwork.manager;

import com.qpidnetwork.ladydating.R;

import android.content.Context;

public class WebsiteManager {
	
	private Context mContext;
	public WebSite mWebSite;
	private static WebsiteManager mWebsiteManager;
	
	private WebsiteManager(Context context){
		mContext = context;
		mWebSite = new WebSite();
		loadData();
	}
	
	public static WebsiteManager newInstance(Context context){
		if(mWebsiteManager == null){
			mWebsiteManager = new WebsiteManager(context);
		}
		return mWebsiteManager;
	}
	
	public static WebsiteManager getInstance(){
		return mWebsiteManager;
	}
	
	
	private void loadData(){
		if(mContext != null){
			mWebSite.isDemo = mContext.getResources().getBoolean(R.bool.is_demo);
			mWebSite.phoneInfoSiteKey = mContext.getResources().getString(R.string.phoneinfo_site_key);
			mWebSite.websiteId = mContext.getResources().getString(R.string.web_site_id);
			if(mWebSite.isDemo){
				mWebSite.webHost = mContext.getResources().getString(R.string.web_host_demo);
				mWebSite.webSiteHost = mContext.getResources().getString(R.string.www_web_host_demo);
			}else{
				mWebSite.webHost = mContext.getResources().getString(R.string.web_host_normal);
				mWebSite.webSiteHost = mContext.getResources().getString(R.string.www_web_host_normal);
			}
		}
	}
	
	public class WebSite{
		public WebSite(){
			
		}
		public boolean isDemo = false;
		public String phoneInfoSiteKey;
		/**
		 * 站点ID
		 */
		public String websiteId;
		/**
		 * 应用服务器站点host
		 */
		public String webHost;
		/**
		 * www服务器站点host
		 */
		public String webSiteHost;
 	}
}
