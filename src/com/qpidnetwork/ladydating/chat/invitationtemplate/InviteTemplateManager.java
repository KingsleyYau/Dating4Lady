package com.qpidnetwork.ladydating.chat.invitationtemplate;

import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.request.OnLCCustomTemplateCallback;
import com.qpidnetwork.request.OnLCSystemTemplateCallback;
import com.qpidnetwork.request.OnRequestCallback;
import com.qpidnetwork.request.RequestJniLivechat;
import com.qpidnetwork.request.item.LiveChatInviteTemplateListItem;
import com.qpidnetwork.request.item.LoginItem;

/**
 * 邀请模板管理类
 * @author Hunter
 * @since 2015.11.03
 */
public class InviteTemplateManager implements IAuthorizationCallBack{
	private String[] systemTempList;
	private LiveChatInviteTemplateListItem[] customTempList;
	private static InviteTemplateManager mInviteTemplateManager;
	
	private boolean isNeedUpdate = true;//设置个人模板是否需要更新（登陆成功，删除模板时修改）
	
	private InviteTemplateManager(){
		LoginManager.getInstance().AddListenner(this);
	}
	
	public static InviteTemplateManager newInstance(){
		if(mInviteTemplateManager == null){
			mInviteTemplateManager = new InviteTemplateManager();
		}
		return mInviteTemplateManager;
	}
	
	/**
	 * 获取系统模板，如果本地已有，不请求直接返回
	 */
	public void getSystemTemplate(final OnLCSystemTemplateCallback callback){
		if((systemTempList!=null) && (systemTempList.length >0)){
			callback.onSystemTemplate(true, "", "", systemTempList);
		}else{
			RequestJniLivechat.GetSystemTemplate(new OnLCSystemTemplateCallback() {
				
				@Override
				public void onSystemTemplate(boolean isSuccess, String errno,
						String errmsg, String[] tempList) {
					if(isSuccess){
						systemTempList = tempList;
					}
					callback.onSystemTemplate(isSuccess, errno, errmsg, tempList);
				}
			});
		}
	}
	
	/**
	 * 获取个人模板，如本地有且更新开关未打开，不更新直接返回本地
	 * @param callback
	 */
	public void getCustomTemplate(final OnLCCustomTemplateCallback callback){
		if((customTempList!=null) && (customTempList.length >0) && (!isNeedUpdate)){
			callback.onCustomTemplate(true, "", "", customTempList);
		}else{
			RequestJniLivechat.GetMyCustomTemplate(new OnLCCustomTemplateCallback() {
				
				@Override
				public void onCustomTemplate(boolean isSuccess, String errno,
						String errmsg, LiveChatInviteTemplateListItem[] tempList) {
					if(isSuccess){
						synchronized (this) {
							customTempList = tempList;
						}
						setCustomNeedUpdate(false);
					}
					callback.onCustomTemplate(isSuccess, errno, errmsg, tempList);
				}
			});
		}
	}
	
	/**
	 * 删除指定个人模板
	 * @param tempId
	 * @param callback
	 */
	public void DelCustomTemplate(final String tempId, final OnRequestCallback callback){
		RequestJniLivechat.DelCustomTemplates(tempId, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				if(isSuccess){
					synchronized (this){
						if(customTempList != null){
							//删除指定模板列表
							int pos = -1; 
							for(int i=0; i<customTempList.length; i++){
								if(customTempList[i].tempId.equals(tempId)){
									pos = i;
									break;
								}
							}
							if(pos != -1){
								for(int j=pos; j<customTempList.length-1; j++){
									customTempList[j] = customTempList[j+1];
								}
								customTempList[customTempList.length-1] = null;
							}
						}
					}
				}
				callback.OnRequest(isSuccess, errno, errmsg);
			}
		});
	}
	
	/**
	 * 增加个人模板
	 * @param tempContent
	 */
	public void addCustomTemplate(String tempContent, final OnRequestCallback callback){
		RequestJniLivechat.AddCustomTemplate(tempContent, new OnRequestCallback() {
			
			@Override
			public void OnRequest(boolean isSuccess, String errno, String errmsg) {
				if(isSuccess){
					setCustomNeedUpdate(true);
				}
				callback.OnRequest(isSuccess, errno, errmsg);
			}
		});
	}
	
	private void setCustomNeedUpdate(boolean needUpdate){
		synchronized(this){
			isNeedUpdate = needUpdate;
		}
	}

	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) {
		setCustomNeedUpdate(true);
	}

	@Override
	public void OnLogout(OperateType operateType) {
		
	}
	
	/**
	 * 模板界面类型，根据类型不同，显示效果及展示不同
	 * @author Hunter
	 *
	 */
	public enum InviteTemplateMode{
		EDIT_MODE,
		CHOOSE_MODE
	}
}
