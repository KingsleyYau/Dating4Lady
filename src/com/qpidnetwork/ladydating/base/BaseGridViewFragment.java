package com.qpidnetwork.ladydating.base;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.customized.view.HeaderableGridView;
import com.qpidnetwork.ladydating.customized.view.MultiSwipeRefreshLayout;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class BaseGridViewFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{
	
	private MultiSwipeRefreshLayout refreshLayout;
	private ProgressBar progressBar;
	private HeaderableGridView gridView;
	private TextView emptyView;
	
	
	
	/**
	 * First step
	 */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.base_gridview_fragment, container, false);
		refreshLayout = (MultiSwipeRefreshLayout)rootView.findViewById(R.id.refreshLayout);
		progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
		gridView = (HeaderableGridView)rootView.findViewById(R.id.gridView);
		emptyView = (TextView)rootView.findViewById(R.id.emptyView);
		
		gridView.setEmptyView(emptyView);
		refreshLayout.setSwipeableChildren(R.id.gridView, R.id.emptyView);
		refreshLayout.setOnRefreshListener(this);
		if (Build.VERSION.SDK_INT < 21) gridView.setSelector(R.drawable.touch_feedback_holo_light);
		
        return rootView;
    }
	
	
	/**
	 * Second step
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		onFragmentCreated(savedInstanceState);
		setupGridView(gridView);
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
	abstract protected void setupGridView(HeaderableGridView gridView);

	
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
	
	protected MultiSwipeRefreshLayout getRefreshLayout(){
		return refreshLayout;
	}
	
	protected ProgressBar getProgressBar(){
		return progressBar;
	}
	
	protected HeaderableGridView getGridView(){
		return gridView;
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
