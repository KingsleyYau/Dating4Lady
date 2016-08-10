package com.qpidnetwork.request;


/**
 * LiveChat获取私密照片
 */
public abstract class OnLCGetPhotoCallback {
	public abstract void OnLCGetPhoto(
			long requestId
			, boolean isSuccess
			, String errno
			, String errmsg
			, String photoId
			, RequestJniLivechat.PhotoSizeType sizeType
			, RequestJniLivechat.PhotoModeType modeType
			, String filePath);
	public void OnLCGetPhoto(
			long requestId
			, boolean isSuccess
			, String errno
			, String errmsg
			, String photoId
			, int size
			, int mode
			, String filePath)
	{
		RequestJniLivechat.PhotoSizeType sizeType = RequestJniLivechat.PhotoSizeType.values()[size];
		RequestJniLivechat.PhotoModeType modeType = RequestJniLivechat.PhotoModeType.values()[mode];
		OnLCGetPhoto(requestId, isSuccess, errno, errmsg, photoId, sizeType, modeType, filePath);
	}
}
