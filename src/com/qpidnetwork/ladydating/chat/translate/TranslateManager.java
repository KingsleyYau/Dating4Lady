package com.qpidnetwork.ladydating.chat.translate;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.qpidnetwork.ladydating.authorization.IAuthorizationCallBack;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.manager.PreferenceManager;
import com.qpidnetwork.request.OnTranslateTextCallback;
import com.qpidnetwork.request.RequestJniCommon;
import com.qpidnetwork.request.item.LoginItem;

/**
 * 管理翻译相关设置，并临时存储翻译内容（msgId相关）
 * @author Hunter
 *
 */
public class TranslateManager implements IAuthorizationCallBack{
	private Context mContext;
	private PreferenceManager mPreferenceManager; //存储默认翻译设置
	private static TranslateManager mTranslateManager;
	
	//存储聊天列表文字消息翻译结果（key： msgId+翻译目标语言 value：translateResult）
	private HashMap<String, String> mMsgTranslateResult = new HashMap<String, String>();
	
	//调用翻译接口时的回调接口
	private List<OnTranslateMessageCallback> mTrasnlateListeners;
	
	private TranslateManager(Context context){
		this.mContext = context;
		mPreferenceManager = new PreferenceManager(context);
		mTrasnlateListeners = new ArrayList<OnTranslateMessageCallback>();
	}
	
	public static TranslateManager newInstance(Context context){
		if(mTranslateManager == null){
			mTranslateManager = new TranslateManager(context);
		}
		return mTranslateManager;
	}
	
	public static TranslateManager getInstance(){
		return mTranslateManager;
	}
	
	/**
	 * 注册监听
	 * @param listener
	 */
	public void RegisterTranslateListener(OnTranslateMessageCallback listener){
		synchronized (mTrasnlateListeners) {
			if(mTrasnlateListeners != null){
				mTrasnlateListeners.add(listener);
			}
		}
	}
	
	/**
	 * 注销监听
	 * @param listener
	 */
	public void UnregisterTranslateListener(OnTranslateMessageCallback listener){
		synchronized (mTrasnlateListeners) {
			if(mTrasnlateListeners != null){
				mTrasnlateListeners.remove(listener);
			}
		}
	}
	
	/**
	 * 读取默认翻译设置
	 * @param manId
	 * @return
	 */
	public String getDefaultTranslateLang(String manId){
		return mPreferenceManager.getDefaulteTranslateForMan(manId);
	}
	
	/**
	 * 选择后设定默认翻译语言
	 * @param manId
	 * @param transLang
	 */
	public void saveDefaultTranslateLang(String manId, String transLang){
		mPreferenceManager.saveDefaulteTranslateForMan(manId, transLang);
	}
	
	/**
	 * 存储翻译结果，方便重新获取翻译结果
	 * @param msgId
	 * @param tranlateResult
	 */
	public void saveMsgTranslateResult(String msgId, String translateLang, String tranlateResult){
		synchronized (mMsgTranslateResult) {
			mMsgTranslateResult.put(msgId + translateLang, tranlateResult);
		}
	}
	
	/**
	 * 获取指定消息文字翻译结果
	 * @param msgId
	 * @return
	 */
	public String getMsgTranslateResult(String msgId, String translateLang){
		synchronized(mMsgTranslateResult){
			return mMsgTranslateResult.get(msgId + translateLang);
		}
	}
	
	/**
	 * 从同步配置中读取翻译目标语言列表
	 * @return
	 */
	public ArrayList<TransLangBean> parseDestLangList(){
		ArrayList<TransLangBean> itemList = new ArrayList<TransLangBean>();
		String[] langs = LoginManager.getInstance().getSynConfigItem().translateLanguages;
		List<String> labels = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < langs.length; i++) {
			if (i % 2 == 0) {
				labels.add(langs[i]);
			} else {
				values.add(langs[i]);
			}
		}
		for(int i=0; i<values.size(); i++){
			TransLangBean bean = new TransLangBean();
			bean.langLabel = labels.get(i);
			bean.langDesc = values.get(i);
			itemList.add(bean);
		}
		return itemList;
	}
	
	/**
	 * 翻译文字
	 * @param msgId
	 * @param targetLanguage
	 * @param content
	 */
	public void translateText(final String seq, final String targetLanguage, final String content){
		final String rep = "8q4q1q";
		Pattern pattern = Pattern.compile("\\[img:\\d{1,2}\\]");
		Matcher matcher = pattern.matcher(content);
		String tmpMsg = content;
		final ArrayList<String> groupList = new ArrayList<String>();
		while (matcher.find()) {
			String group = matcher.group();
			tmpMsg = tmpMsg.replace(group, rep);
			groupList.add(group);
		}
		tmpMsg = URLEncoder.encode(tmpMsg);
		String appId = "78280AF4DFA1CE1676AFE86340C690023A5AC139";
		RequestJniCommon.TranslateText(appId, "", targetLanguage, tmpMsg, new OnTranslateTextCallback() {
			
			@Override
			public void OnTranslateText(long requestId, boolean isSuccess, String text) {
				String result = text;
				for (String group : groupList) {
					result = result.replaceFirst(rep, group);
				}
				saveMsgTranslateResult(seq, targetLanguage, result);
				synchronized (mTrasnlateListeners) {
					if(mTrasnlateListeners != null){
						for(OnTranslateMessageCallback callback : mTrasnlateListeners){
							callback.OnTranslateText(seq, isSuccess, content, result);
						}
					}
				}
			}
		});
	}

	@Override
	public void OnLogin(OperateType operateType, boolean isSuccess,
			String errno, String errmsg, LoginItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnLogout(OperateType operateType) {
		if(operateType == OperateType.MANUAL){
			//手动注销，LivechatManager 重置MsgId,需清空
			if(mMsgTranslateResult != null){
				mMsgTranslateResult.clear();
			}
		}
	}
	
	public interface OnTranslateMessageCallback{
		public void OnTranslateText(String seq, boolean isSuccess, String orignalText, String text);
	}
}
