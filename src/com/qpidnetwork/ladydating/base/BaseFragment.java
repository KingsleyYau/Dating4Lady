package com.qpidnetwork.ladydating.base;


import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.qpidnetwork.ladydating.customized.view.FlatToast;
import com.qpidnetwork.ladydating.customized.view.MaterialProgressDialog;

public class BaseFragment extends Fragment{
	
	protected Activity mContext;
	
	private FlatToast flatToast;
	private MaterialProgressDialog progressBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		flatToast = new FlatToast(getActivity());
		progressBar = new MaterialProgressDialog(getActivity());
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = activity;
	}
	
	protected void showProgressToast(String text){
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		flatToast.setProgressing(text);
		if((getActivity() != null)&&(getActivity() instanceof BaseFragmentActivity)){
			if(((BaseFragmentActivity)getActivity()).isActivityVisible()){
				flatToast.show();
			}
		}
	}
	
	protected void showFailedToast(String text){
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		flatToast.setFailed(text);
		if((getActivity() != null)&&(getActivity() instanceof BaseFragmentActivity)){
			if(((BaseFragmentActivity)getActivity()).isActivityVisible()){
				flatToast.show();
			}
		}
	}
	
	protected void showDoneToast(String text){
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		flatToast.setDone(text);
		if((getActivity() != null)&&(getActivity() instanceof BaseFragmentActivity)){
			if(((BaseFragmentActivity)getActivity()).isActivityVisible()){
				flatToast.show();
			}
		}
	}
	
	protected void cancelToast(){
		if(flatToast != null){
			flatToast.cancelImmediately();
		}
	}

	
	protected void showProgressDialog(String text){
		if(flatToast != null){
			flatToast.cancelImmediately();// cancel flat toast if it's showing before popuping a dialog.
		}
		if(!progressBar.isShowing() && (getActivity() != null)&&(getActivity() instanceof BaseFragmentActivity)){
			if(((BaseFragmentActivity)getActivity()).isActivityVisible()){
				progressBar.setMessage(text);
				progressBar.show();
			}
		}
	}
	
	protected void dismissProgressDialog(){
		try {
			if( progressBar != null ) {
				progressBar.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDetach() {
		if (flatToast.isShowing()) 
			flatToast.cancelImmediately();
		if (progressBar.isShowing()) 
			progressBar.dismiss();
		super.onDetach();
	}
	
	protected Handler mUiHandler = new UiHandler(this) {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (getFragmentReference() != null && getFragmentReference().get() != null) {
                handleUiMessage(msg);
            }
        };
    };

    private static class UiHandler extends Handler {
        private final WeakReference<Fragment> mFragmentReference;

        public UiHandler(Fragment activity) {
            mFragmentReference = new WeakReference<Fragment>(activity);
        }

        public WeakReference<Fragment> getFragmentReference() {
            return mFragmentReference;
        }
    }
    
    /**
     * 处理更新UI任务
     * 
     * @param msg
     */
    protected void handleUiMessage(Message msg) {
    }
    
    /**
     * 发送UI更新操作
     * 
     * @param msg
     */
    protected void sendUiMessage(Message msg) {
        mUiHandler.sendMessage(msg);
    }

    protected void sendUiMessageDelayed(Message msg, long delayMillis) {
        mUiHandler.sendMessageDelayed(msg, delayMillis);
    }

    /**
     * 发送UI更新操作
     * 
     * @param what
     */
    protected void sendEmptyUiMessage(int what) {
        mUiHandler.sendEmptyMessage(what);
    }

    protected void sendEmptyUiMessageDelayed(int what, long delayMillis) {
        mUiHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

}
