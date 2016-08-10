package com.qpidnetwork.ladydating.base;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.bean.PageBean;
import com.qpidnetwork.ladydating.customized.view.ExtendableListView;
import com.qpidnetwork.ladydating.customized.view.PullToRefreshViewBase;
import com.qpidnetwork.ladydating.customized.view.PullToRefreshViewBase.OnPullRefreshListener;

public abstract class BaseListViewFragment extends BaseFragment implements OnPullRefreshListener{
	
	/*默认一页30条*/
	protected PageBean pageBean = new PageBean(6);
	
	private PullToRefreshViewBase refreshLayout;
	private ProgressBar progressBar;
	private ExtendableListView listView;
	private TextView emptyView;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.base_listview_fragment, container, false);
		refreshLayout = (PullToRefreshViewBase)rootView.findViewById(R.id.refreshLayout);
		progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
		listView = (ExtendableListView)rootView.findViewById(R.id.listView);
		emptyView = (TextView)rootView.findViewById(R.id.emptyView);
		
		listView.setEmptyView(emptyView);
		refreshLayout.setSwipeableChildren(R.id.listView, R.id.emptyView);
		refreshLayout.setRefreshListview(listView);
		refreshLayout.setOnPullRefreshListener(this);
		
		if (Build.VERSION.SDK_INT < 21){
			listView.setSelector(R.drawable.touch_feedback_holo_light);
		}
		
        return rootView;
    }
	
	/**
	 * 避免列表初始化更新过程中滑动加载更多或则上拉刷新冲突问题
	 */
	public void setIsRefreshing(){
		if(refreshLayout != null){
			refreshLayout.setIsRefreshing();
		}
	}
	
	/**
	 * Second step
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		onFragmentCreated(savedInstanceState);
		setupListView(listView);
	}
	
	/**
	 * this method will be called when the fragment is created and is attached to the mother activity
	 * @param savedInstanceState
	 */
	abstract protected void onFragmentCreated(Bundle savedInstanceState);
	
	/**
	 * this method will be called after onFragmentCreated(Bundle savedInstanceState) is done
	 * @param gridView
	 */
	abstract protected void setupListView(ExtendableListView listView);
	
	@Override
	public void onPullDownToRefresh() {
		
	}
	
	@Override
	public void onPullUpToRefresh() {
		
	}
	
	/**
	 * 加载数据结束回调
	 */
	public void onRefreshComplete(){
		refreshLayout.onRefreshComplete();
	}

	
	/**
	 * This method will be called by issuing one of below methods 
	 * sendUiMessage(int what, int arg1, int arg2, Object obj)
	 * sendUiMessage(int what, int arg1, int arg)
	 * sendUiMessage(int what, Object obj)
	 * sendUiMessage(int what)
	 * @param Message
	 */
	abstract protected void handleUiMessage(Message msg);
	
	
	public void sendUiMessage(int what, int arg1, int arg2, Object obj){
		Message msg = Message.obtain();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		uiMessageHandler.sendMessage(msg);
	}
	
	public void sendUiMessage(int what, int arg1, int arg2){
		Message msg = Message.obtain();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		uiMessageHandler.sendMessage(msg);
	}

	
	public void sendUiMessage(int what, Object obj){
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		uiMessageHandler.sendMessage(msg);
	}
	
	public void sendUiMessage(int what){
		uiMessageHandler.sendEmptyMessage(what);
	}
	
	protected void setEmptyText(String emptyText){
		emptyView.setText(emptyText);
	}
	
	protected PullToRefreshViewBase getRefreshLayout(){
		return refreshLayout;
	}
	
	protected ProgressBar getProgressBar(){
		return progressBar;
	}
	
	protected ExtendableListView getListView(){
		return listView;
	}
	
	protected TextView getEmptyView(){
		return emptyView;
	}
	
	
	private Handler uiMessageHandler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			handleUiMessage(msg);
		}
	};
	
	
}
