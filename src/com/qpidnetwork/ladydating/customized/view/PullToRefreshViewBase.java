package com.qpidnetwork.ladydating.customized.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

public class PullToRefreshViewBase extends
		MultiSwipeRefreshLayout implements OnScrollListener, OnRefreshListener {

	public String TAG = PullToRefreshViewBase.class.getName();
	/**
	 * 滑动到最下面时的上拉操作（上拉最小间距）
	 */
	private int mTouchSlop;
	/**
	 * listview
	 */
	private ListView mListView;

	/**
	 * 上拉，下拉回调监听
	 */
	private OnPullRefreshListener mOnPullRefreshListener;

	/**
	 * 加载中footer
	 */
	private View mFooterView;

	/**
	 * 按下时的y坐标
	 */
	private int mYDown;
	/**
	 * 抬起时的y坐标, 与mYDown一起用于滑动到底部时判断是上拉还是下拉
	 */
	private int mLastY;
	/**
	 * 是否在加载中 ( 上拉加载更多 )
	 */
	private boolean isLoading = false;
	/**
	 * 是否可以上拉更多
	 */
	private boolean can_pull_up = true;
	/**
	 * 是否可以下拉刷新
	 */
	private boolean can_pull_down = true;

	/**
	 * @param context
	 */
	public PullToRefreshViewBase(Context context) {
		this(context, null);
	}

	public PullToRefreshViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mFooterView = new RetriableListFooter(context);
		setOnRefreshListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// 初始化ListView对象
		if (mListView == null) {
			getAbsListView();
		}
	}

	/**
	 * 获取ListView对象
	 */
	private void getAbsListView() {
		int childs = getChildCount();
		/*遍历获取下拉刷新AbsListView*/
		for(int i = 0; i<childs; i++){
			View childView = getChildAt(i);
			if (childView instanceof ListView) {
				mListView = (ListView)childView;
				mListView.addFooterView(mFooterView);
				// 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
				mListView.setOnScrollListener(this);
				break;
			}
		}
	}
	
	/**
	 * 设置下拉刷新的Listview 并初始化配置 需在setAdapter前使用
	 * @param listview
	 */
	public void setRefreshListview(ListView listview){
		mListView = listview;
		mListView.addFooterView(mFooterView);
		// 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
		mListView.setOnScrollListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// 按下
			mYDown = (int) event.getRawY();
			break;

		case MotionEvent.ACTION_MOVE:
			// 移动
			mLastY = (int) event.getRawY();
			break;

		case MotionEvent.ACTION_UP:
			// 抬起
			if (canLoadMore()) {
				loadMore();
			}
			break;
		default:
			break;
		}

		return super.dispatchTouchEvent(event);
	}

	/**
	 * 是否可以加载更多, 条件是到了最底部, listview不在加载中, 且为上拉操作.
	 * 
	 * @return
	 */
	private boolean canLoadMore() {
		Log.i(TAG, "onScroll canLoadMore isBottom : " + isBottom() + " isLoading: " + isLoading + " isPullUp: " + isPullUp() + " can_pull_up: " + can_pull_up);
		return isBottom() && !isLoading && isPullUp() && can_pull_up;
	}
	
	/**
	 * 是否可以下拉刷新
	 * @return
	 */
	private boolean canRefresh(){
		return !isLoading && can_pull_down;
	}

	/**
	 * 判断是否到了最底部
	 */
	private boolean isBottom() {

		if (mListView != null && mListView.getAdapter() != null) {
			return mListView.getLastVisiblePosition() == (mListView
					.getAdapter().getCount() - 1);
		}
		return false;
	}

	/**
	 * 是否是上拉操作
	 * 
	 * @return
	 */
	private boolean isPullUp() {
		return (mYDown - mLastY) >= mTouchSlop;
	}

	/**
	 * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
	 */
	private void loadMore() {
		if (mOnPullRefreshListener != null) {
			
			Log.i(TAG, "loadMoreRefresh");
			// 设置状态
			setLoading(true);
			//
			mOnPullRefreshListener.onPullUpToRefresh();
		}
	}

	/**
	 * @param loading
	 */
	private void setLoading(boolean loading) {
		isLoading = loading;
		if (isLoading) {
			setEnabled(false);
			mListView.addFooterView(mFooterView);
		} else {
			mListView.removeFooterView(mFooterView);
			mYDown = 0;
			mLastY = 0;
		}
	}

	/**
	 * 设置上拉，下拉回调响应
	 * @param refreshListener
	 */
	public void setOnPullRefreshListener(OnPullRefreshListener refreshListener) {
		mOnPullRefreshListener = refreshListener;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// 滚动时到了最底部也可以加载更多
		if (canLoadMore()) {
			Log.i(TAG, "onScroll canLoadMore totalItemCount: " + totalItemCount);
			loadMore();
		}
		Log.i(TAG, "onScroll can not LoadMore totalItemCount: " + totalItemCount);
	}
	
	
	@Override
	public void onRefresh() {
		if(canRefresh()){
			Log.i(TAG, "onPullDownRefresh");
			isLoading = true;
			if(mOnPullRefreshListener != null){
				mOnPullRefreshListener.onPullDownToRefresh();
			}
		}
	}
	
	/**
	 * 下拉刷新结束操作
	 */
	public void onRefreshComplete(){
		Log.i(TAG, "onRefreshComplete");
		setRefreshing(false);
		setLoading(false);
		if(can_pull_down){
			setEnabled(true);
		}
	}
	
	/** 
	 * 上拉下拉回调接口
	 * @author Hunter
	 *
	 */
	public interface OnPullRefreshListener {
		public void onPullDownToRefresh();
		public void onPullUpToRefresh();
	}
	
	/**
	 * 设置是否可以下拉刷新
	 * @param b 
	 */
	public void setCanPullDown(boolean b) {
		can_pull_down = b;
		setEnabled(b);
	}

	/**
	 * 获取是否可以下拉刷新标志位
	 * @return
	 */
	public boolean getCanPullDown() {
		return can_pull_down;
	}

	/**
	 * 设置是否可以上拉更多
	 * @param b 
	 */
	public void setCanPullUp(boolean b) {
		can_pull_up = b;
	}

	/**
	 * 获取是否可以上拉更多标志位
	 * @return
	 */
	public boolean getCanPullUp() {
		return can_pull_up;
	}
}
