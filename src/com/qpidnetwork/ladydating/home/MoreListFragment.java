package com.qpidnetwork.ladydating.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qpidnetwork.framework.util.SystemUtil;
import com.qpidnetwork.framework.util.ViewTools;
import com.qpidnetwork.ladydating.R;
import com.qpidnetwork.ladydating.authorization.LoginManager;
import com.qpidnetwork.ladydating.authorization.LoginParam;
import com.qpidnetwork.ladydating.base.BaseFragment;
import com.qpidnetwork.ladydating.customized.view.AutoInviteMsgSwitchDialog;
import com.qpidnetwork.ladydating.customized.view.AutoInviteMsgSwitchDialog.OnVerifyListener;
import com.qpidnetwork.ladydating.customized.view.CircleImageView;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogAlert;
import com.qpidnetwork.ladydating.customized.view.MaterialDialogSingleChoice;
import com.qpidnetwork.ladydating.more.ChangePasswordActivity;
import com.qpidnetwork.ladydating.more.ContactAgentActivity;
import com.qpidnetwork.ladydating.more.LadyProfileDetailActivity;
import com.qpidnetwork.manager.FileCacheManager;
import com.qpidnetwork.manager.MultiLanguageManager;
import com.qpidnetwork.manager.MultiLanguageManager.LanguageType;
import com.qpidnetwork.manager.PreferenceManager;
import com.qpidnetwork.tool.ImageViewLoader;

public class MoreListFragment extends BaseFragment implements
		View.OnClickListener {

	// user profile
	private RelativeLayout mUserProfile;
	private CircleImageView ivPhoto;
	private TextView tvName;

	private LinearLayout section1;
	private LinearLayout section2;
	private LinearLayout section3;
	
	private MultiLanguageManager mMultiLanguageManager;
	private PreferenceManager mPreferenceManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_home_more_list_fragment, null);

		// 个人资料
		initUserProfile(v);

		section1 = (LinearLayout) v.findViewById(R.id.section1);
		section2 = (LinearLayout) v.findViewById(R.id.section2);
		section3 = (LinearLayout) v.findViewById(R.id.section3);

		/**
		 * my profile and change passord
		 */
		section1.addView(createItem(R.drawable.ic_lock_outline_grey600_24dp,
				getString(R.string.change_password), null, R.id.more_change_pwd));

		/**
		 * Language Notification sound Clean cache Check update
		 */
		LanguageType type = mMultiLanguageManager.getDefultLanguageChoice();
		String defaultLanguage = mMultiLanguageManager.getLanguageDescByType(type);
		if(getResources().getBoolean(R.bool.is_language_set)){
			section2.addView(createItem(R.drawable.ic_language_grey600_24dp,
					getString(R.string.Language), defaultLanguage,
					R.id.more_change_language));
		}
		section2.addView(createItem(
				R.drawable.ic_notifications_none_grey600_24dp,
				getString(R.string.notification_sound), getString(R.string.on),
				R.id.more_notif_setting));
		section2.addView(createItem(R.drawable.ic_history_grey600_24dp,
				getString(R.string.clean_cache), null, R.id.more_clean_cache));
		section2.addView(createItem(
				R.drawable.ic_system_update_grey600_24dp,
				getString(R.string.check_update),
				getString(R.string.version, SystemUtil.getVersionName(mContext)),
				R.id.more_check_update));

		/**
		 * Contact agency
		 */
		section3.addView(createItem(R.drawable.ic_call_grey600_24dp,
				getString(R.string.contact_agency), null,
				R.id.more_contact_agency));
		return v;
	}

	private View createItem(int iconResourceId, String text1, String text2,
			int itemId) {
		if (text1 == null)
			throw new NullPointerException("text 1 can not be null");

		View v = LayoutInflater.from(getActivity()).inflate(
				R.layout.item_for_home_more_list, null);
		ImageView image = (ImageView) v.findViewById(R.id.image);
		TextView textView1 = (TextView) v.findViewById(R.id.text1);
		TextView textView2 = (TextView) v.findViewById(R.id.text2);

		image.setImageResource(iconResourceId);
		textView1.setText(text1);
		if (text2 == null) {
			textView2.setVisibility(View.GONE);
		} else {
			textView2.setText(text2);
		}

		if (Build.VERSION.SDK_INT >= 21)
			v.setBackgroundResource(R.drawable.rectangle_ripple_holo_light);

		v.setId(itemId);
		v.setOnClickListener(this);
		return v;
	}

	/**
	 * 初始化个人资料区域
	 * 
	 * @param v
	 */
	private void initUserProfile(View v) {

		mUserProfile = (RelativeLayout) v.findViewById(R.id.more_user_profile);
		ivPhoto = (CircleImageView) v.findViewById(R.id.ivPhoto);
		tvName = (TextView) v.findViewById(R.id.tvName);
		mUserProfile.setOnClickListener(this);

		ivPhoto.setImageResource(R.drawable.female_default_profile_photo_40dp);
		LoginParam params = LoginManager.getInstance().GetLoginParam();
		if ((params != null) && (params.item != null)) {
			tvName.setText(params.item.firstname);
			String localPath = FileCacheManager.getInstance()
					.CacheImagePathFromUrl(params.item.photo_url);
			ImageViewLoader loader = new ImageViewLoader(mContext);
			loader.SetDefaultImage(mContext.getResources().getDrawable(
					R.drawable.female_default_profile_photo_40dp));
			ViewTools.PreCalculateViewSize(ivPhoto);
			loader.DisplayImage(ivPhoto, params.item.photo_url, localPath,
					ivPhoto.getWidth(), ivPhoto.getHeight(), null);
		}
		
		mMultiLanguageManager = new MultiLanguageManager(mContext);
		mPreferenceManager = new PreferenceManager(getActivity());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.more_user_profile:
			onProfileClick();
			break;
		case R.id.more_change_pwd:
			onChangePassword();
			break;
		case R.id.more_change_language:
			onLanguageSelected();
			break;
		case R.id.more_notif_setting:
			onNotificationSetting();
			break;
		case R.id.more_clean_cache:{
//			onCleanCache();
			AutoInviteMsgSwitchDialog dialog = new AutoInviteMsgSwitchDialog(getActivity());
			dialog.setOnVerifyListener(new OnVerifyListener() {
				
				@Override
				public void onVerifySuccess() {
					// TODO Auto-generated method stub
				}
			});
			dialog.show();
		}break;
		case R.id.more_check_update:
			onCheckUpdate();
			break;
		case R.id.more_contact_agency:
			onContactAgency();
			break;
		}
	}

	/**
	 * 点击查看用户个人资料
	 */
	private void onProfileClick() {
		Intent intent = new Intent(getActivity(), LadyProfileDetailActivity.class);
		startActivity(intent);
	}

	/**
	 * 修改密码
	 */
	private void onChangePassword() {
		Intent intent = new Intent(mContext, ChangePasswordActivity.class);
		mContext.startActivity(intent);
	}

	/**
	 * 默认语言设置
	 */
	private void onLanguageSelected() {
		LanguageType type = mMultiLanguageManager.getDefultLanguageChoice();
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(
				getActivity(), getResources().getStringArray(
						R.array.languagelist),
				new MaterialDialogSingleChoice.OnClickCallback() {

					@Override
					public void onClick(AdapterView<?> adptView, View v,
							int which) {
						// TODO Auto-generated method stub
						if (which > -1) {
							switch (which) {
							case 0:
							case 1:
								LanguageType type = LanguageType.values()[which];
								mMultiLanguageManager.setLoaclLanguage(type);
								onDescriptionReset(R.id.more_change_language,
										mMultiLanguageManager.getLanguageDescByType(type));
								break;
							default:
								break;
							}
						}
					}
				}, type.ordinal());
		dialog.show();
	}

	/**
	 * 系统通知设置
	 */
	private void onNotificationSetting() {
		String[] settings = new String[]{getResources().getString(R.string.on), 
				getResources().getString(R.string.off), getResources().getString(R.string.cancel)};
		int chooseIndex = 0;
		if(mPreferenceManager.getNotificationSwitchSetting()){
			chooseIndex = 0;
		}else{
			chooseIndex = 1; 
		}
		MaterialDialogSingleChoice dialog = new MaterialDialogSingleChoice(getActivity(), settings,
				new MaterialDialogSingleChoice.OnClickCallback() {

					@Override
					public void onClick(AdapterView<?> adptView, View v,
							int which) {
						// TODO Auto-generated method stub
						if (which > -1) {
							switch (which) {
							case 0:{
								mPreferenceManager.saveNotificationSwitchSetting(true);
								onDescriptionReset(R.id.more_notif_setting, getResources().getString(R.string.on));
							}break;
							case 1:{
								mPreferenceManager.saveNotificationSwitchSetting(false);
								onDescriptionReset(R.id.more_notif_setting, getResources().getString(R.string.off));
							}break;
							default:
								break;
							}
						}
					}
				}, chooseIndex);
		dialog.show();
	}

	/**
	 * 清除系统缓存
	 */
	private void onCleanCache() {
		MaterialDialogAlert dialog = new MaterialDialogAlert(mContext);
		dialog.setMessage(getString(R.string.more_clean_cache_tips));
		dialog.addButton(dialog.createButton(getString(R.string.ok), new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FileCacheManager.getInstance().ClearCache();
				
				MaterialDialogAlert alert = new MaterialDialogAlert(mContext);
				alert.setMessage(getResources().getString(R.string.more_clean_cache_finish_tips));
				alert.addButton(alert.createButton(getString(R.string.ok), null));
				alert.show();
			}
		}));
		dialog.addButton(dialog.createButton(getString(R.string.cancel), null));
		dialog.show();
	}

	/**
	 * 版本检测
	 */
	private void onCheckUpdate() {
		if(mContext != null){
			((HomeActivity)mContext).CheckVersion();
		}
	}

	/**
	 * 联系机构
	 */
	private void onContactAgency() {
		Intent intent = new Intent(getActivity(), ContactAgentActivity.class);
		startActivity(intent);
	}

	/**
	 * 统一修改设置中描述
	 * 
	 * @param id
	 * @param description
	 */
	private void onDescriptionReset(int id, String description) {
		if (mContext != null) {
			TextView temp = (TextView) mContext.findViewById(id).findViewById(
					R.id.text2);
			temp.setText(description);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.other_list, menu);
		MenuItem item = menu.findItem(R.id.contacts);
		if (((HomeActivity) getActivity()).liveChatUnreadCount > 0) {
			item.setIcon(R.drawable.ic_man_list_white_24dp_badged);
		} else {
			item.setIcon(R.drawable.ic_man_list_white_24dp);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

}
