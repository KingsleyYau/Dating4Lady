package com.qpidnetwork.ladydating.customized.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.utility.Converter;

public class RetriableListFooter extends LinearLayout{
	

	private View footerView;
	private LinearLayout loadingView;
	private MaterialRaisedButton retryButton;
	private TextView noMoreText;
	private ViewType loadType = ViewType.LOADING;
	
	public static enum ViewType{
		LOADING,
		RETRY,
		NO_MORE
	}
	
	public RetriableListFooter(Context context){
		this(context, null);
	}
	
	public RetriableListFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, Converter.dp2px(72)));
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		footerView = LayoutInflater.from(context).inflate(R.layout.view_retriable_list_footer, null);
		footerView.setLayoutParams(params);
		
		loadingView = (LinearLayout)footerView.findViewById(R.id.loadMoreView);
		retryButton = (MaterialRaisedButton)footerView.findViewById(R.id.btnRetryLoadMore);
		noMoreText = (TextView)footerView.findViewById(R.id.noMoreItemText);
		retryButton.setButtonTitle(getContext().getString(R.string.retry));
		
		this.addView(footerView);
		showView(ViewType.LOADING);
	}
	
	public void showView(ViewType viewType){
		loadType = viewType;
		switch(viewType){
		case LOADING:
			showLoadingView();
			break;
		case RETRY:
			showRetryButton();
			break;
		case NO_MORE:
			showNoMoreMessage();
			break;
		}
	}
	
	public ViewType getCurrentViewType(){
		return loadType;
	}
	public void setNoMoreMessage(CharSequence message){
		noMoreText.setText(message);
	}
	
	private void showLoadingView(){
		loadingView.setVisibility(View.VISIBLE);
		retryButton.setVisibility(View.GONE);
		noMoreText.setVisibility(View.GONE);
	}
	
	
	private void showRetryButton(){
		loadingView.setVisibility(View.GONE);
		retryButton.setVisibility(View.VISIBLE);
		noMoreText.setVisibility(View.GONE);
	}
	
	private void showNoMoreMessage(){
		loadingView.setVisibility(View.GONE);
		retryButton.setVisibility(View.GONE);
		noMoreText.setVisibility(View.VISIBLE);
	}


}
