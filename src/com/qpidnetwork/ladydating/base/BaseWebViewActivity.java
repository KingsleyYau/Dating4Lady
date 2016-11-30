package com.qpidnetwork.ladydating.base;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qpidnetwork.ladydating.QpidApplication;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.bean.RequestBaseResponse;
import com.qpidnetwork.ladydating.customized.view.MaterialRaisedButton;
import com.qpidnetwork.manager.WebsiteManager;
import com.qpidnetwork.request.RequestJni;
import com.qpidnetwork.request.item.CookiesItem;
import com.qpidnetwork.request.item.LoginItem;

public abstract class BaseWebViewActivity extends BaseActionbarActivity implements IAuthorizationCallBack{
	
	private static final int LOGIN_CALLBACK = 10001;
	
	private WebView mWebview;
	
	//error page
	private View errorPage;
	private MaterialRaisedButton btnErrorRetry;
	
	private boolean isSessionOutTimeError = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initViews();
	}
	
	private void initViews(){
		mWebview = (WebView)findViewById(R.id.webView);
		WebSettings webSettings = mWebview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// 允许JS弹出框
		webSettings.setDefaultTextEncodingName("utf-8");
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDomStorageEnabled(true);
		mWebview.setWebViewClient(client);
		
		//error page
		errorPage = (View)findViewById(R.id.errorPage);
		btnErrorRetry = (MaterialRaisedButton)findViewById(R.id.btnErrorRetry);
		btnErrorRetry.setButtonTitle(getString(R.string.retry));
		btnErrorRetry.setOnClickListener(this);
		btnErrorRetry.requestFocus();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		LoginManager.getInstance().AddListenner(this);
	};
	
	@Override
	protected void onPause() {
		super.onPause();
		LoginManager.getInstance().RemoveListenner(this);
	};
	
	
	WebViewClient client = new WebViewClient() {  
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			showProgressDialog(getString(R.string.loading));
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			dismissProgressDialog();
		}
		
	    @Override  
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	boolean bFlag = false;
	    	if(url.contains("MBCE0003")){
				//处理session过期重新登陆
				isSessionOutTimeError = true;
				errorPage.setVisibility(View.VISIBLE);
				bFlag = true;
			}else{
				bFlag = dealOverrideUrl(url);
			}
	    	if( !bFlag ) {
	    		return super.shouldOverrideUrlLoading(view, url);
	    	}
	        return true;  
	    } 
	    
		@Override  
	    public void onReceivedHttpAuthRequest(WebView view,
	            HttpAuthHandler handler, String host, String realm) {
			if (QpidApplication.isDemo) {
				handler.proceed("test", "5179");
			} else {
		        handler.cancel();
			}
	    }
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			//普通页面错误
			errorPage.setVisibility(View.VISIBLE);
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnErrorRetry:{
			errorPage.setVisibility(View.GONE);
			if(isSessionOutTimeError){
				isSessionOutTimeError = false;
				showProgressDialog(getString(R.string.loading));
				LoginManager.getInstance().LoginBySessionOuttime();
			}else{
				mWebview.reload();
			}
		}break;

		default:
			break;
		}
	}
	
	/**
	 * 处理重定向Url
	 * @param url
	 * @return
	 */
	protected abstract boolean dealOverrideUrl(String url);
	
	/**
	 * Session过期重登陆处理
	 * @return
	 */
	protected abstract void reloadDestUrl();

	@Override
	protected int setupContentVew() {
		return R.layout.activity_base_webview;
	}

	@Override
	protected int setupThemeColor() {
		return R.color.white;
	}

	@Override
	protected void onMenuItemSelected(MenuItem menu) {
		
	}

	@Override
	protected boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		return true;
	}
	
	@Override
	protected void handleUiMessage(Message msg) {
		super.handleUiMessage(msg);
		boolean isCatch = false;
		switch (msg.what) {
		case LOGIN_CALLBACK:{
			isCatch = true;
			RequestBaseResponse response = (RequestBaseResponse)msg.obj;
			if(response.isSuccess){
				//session 过期重新登陆
				reloadDestUrl();
			}else{
				//显示错误页（加载页面错误）
				dismissProgressDialog();
				isSessionOutTimeError = true;
				errorPage.setVisibility(View.VISIBLE);
			}
		}break;

		default:
			break;
		}
		if(isCatch){
			return;
		}
	}
	
	
	
	/**
	 * 同步Cookies
	 */
	protected void synCookies(){
		/*加载男士资料*/
		String domain = WebsiteManager.getInstance().mWebSite.webHost;
		
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();
		
		CookiesItem[] cookieList = RequestJni.GetCookiesItem();
		if(cookieList != null && cookieList.length > 0){
			for(CookiesItem item : cookieList){
				if(item != null){
					String sessionString = item.cName + "=" + item.value;
					cookieManager.setCookie(item.domain, sessionString);	
				}
			}
		}
//		String phpSession = RequestJni.GetCookies(domain.substring(domain.indexOf("http://") + 7, domain.length()));
//		Log.i("BaseWebViewActivity", "The cookie is : " + phpSession);
//		cookieManager.setCookie(domain, phpSession); // 
		CookieSyncManager.getInstance().sync();
		
		mWebview.clearCache(true);
	}
	
	/**
	 * 加载Url
	 * @param url
	 */
	protected void loadUrl(String url){
		mWebview.loadUrl(url);
		mWebview.requestFocusFromTouch();
	}

	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) {
		Message msg = Message.obtain();
		msg.what = LOGIN_CALLBACK;
		RequestBaseResponse response = new RequestBaseResponse(isSuccess, errno, errmsg, item);
		msg.obj = response;
		sendUiMessage(msg);
	}

	@Override
	public void OnLogout(OperateType operateType) {
		
	}

}
