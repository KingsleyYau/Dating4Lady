package com.qpidnetwork.livechat;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import com.qpidnetwork.request.RequestJniLivechat.PhotoModeType;
import com.qpidnetwork.request.RequestJniLivechat.PhotoSizeType;


/**
 * 图片对象
 * @author Samson Fan
 */
public class LCPhotoItem implements Serializable{

	private static final long serialVersionUID = 8719070618687111312L;
	/**
	 * 图片ID
	 */
	public String photoId;
	/**
	 * 图片描述
	 */
	public String photoDesc;
	/**
	 * 发送ID（仅发送）
	 */
	public String sendId;
	/**
	 * 用于显示不清晰图的路径
	 */
	public String showFuzzyFilePath;
	/**
	 * 拇指不清晰图路径
	 */
	public String thumbFuzzyFilePath;
	/**
	 * 原图路径
	 */
	public String srcFilePath;
	/**
	 * 用于显示的原图路径
	 */
	public String showSrcFilePath;
	/**
	 * 拇指原图路径
	 */
	public String thumbSrcFilePath;
	/**
	 * 是否已付费
	 */
	public boolean charge;
	/**
	 * 处理状态定义
	 */
	enum DownloadStatus {
		/**
		 * 没有下载
		 */
		DownloadNothing,
		/**
		 * 正在下载模糊拇指图
		 */
		DownloadThumbFuzzyPhoto,
		/**
		 * 正在下载模糊显示图
		 */
		DownloadShowFuzzyPhoto,
		/**
		 * 正在下载清晰拇指图
		 */
		DownloadThumbSrcPhoto,
		/**
		 * 正在下载清晰显示图
		 */
		DownloadShowSrcPhoto,
		/**
		 * 正在下载原图
		 */
		DownloadSrcPhoto,
	}
	ArrayList<DownloadStatus> downloadStatusList;
	
	
	public LCPhotoItem() {
		photoId = "";
		sendId = "";
		photoDesc = "";
		showFuzzyFilePath = "";
		thumbFuzzyFilePath = "";
		srcFilePath = "";
		showSrcFilePath = "";
		thumbSrcFilePath = "";
		charge = false;
		downloadStatusList = new ArrayList<DownloadStatus>();
	}
	
	public void init(
			String photoId
			, String sendId
			, String photoDesc
			, String showFuzzyFilePath
			, String thumbFuzzyFilePath
			, String srcFilePath
			, String showSrcFilePath
			, String thumbSrcFilePath
			, boolean charge) 
	{
		this.photoId = photoId;
		this.sendId = sendId;
		this.photoDesc = photoDesc;
		this.charge = charge;
		
		if (!showFuzzyFilePath.isEmpty()) {
			File file = new File(showFuzzyFilePath);
			if (file.exists()) {
				this.showFuzzyFilePath = showFuzzyFilePath;
			}
		}
		
		if (!thumbFuzzyFilePath.isEmpty()) {
			File file = new File(thumbFuzzyFilePath);
			if (file.exists()) {
				this.thumbFuzzyFilePath = thumbFuzzyFilePath;
			}
		}
		
		if (!srcFilePath.isEmpty()) {
			File file = new File(srcFilePath);
			if (file.exists()) {
				this.srcFilePath = srcFilePath;
			}
		}
		
		if (!showSrcFilePath.isEmpty()) {
			File file = new File(showSrcFilePath);
			if (file.exists()) {
				this.showSrcFilePath = showSrcFilePath;
			}
		}
		
		if (!thumbSrcFilePath.isEmpty()) {
			File file = new File(thumbSrcFilePath);
			if (file.exists()) {
				this.thumbSrcFilePath = thumbSrcFilePath;
			}
		}
	}
	
	/**
	 * 判断是否正在下载
	 * @param modeType
	 * @param sizeType
	 * @return
	 */
	public boolean  IsDownloading(PhotoModeType modeType, PhotoSizeType sizeType)
	{
		boolean result = false;
		
		synchronized (downloadStatusList)
		{
			// 获取对应的下载状态
			DownloadStatus status = GetDownloadStatus(modeType, sizeType);
			// 判断下载状态是否存在状态列表
			result = downloadStatusList.contains(status);
		}
		
		return result;
	}
	
	/**
	 * 根据mode及size添加下载状态
	 * @param modeType
	 * @param sizeType
	 */
	public void AddDownloading(PhotoModeType modeType, PhotoSizeType sizeType)
	{
		synchronized (downloadStatusList)
		{
			DownloadStatus status = GetDownloadStatus(modeType, sizeType);
			if (status != DownloadStatus.DownloadNothing 
					&& !downloadStatusList.contains(status)) 
			{
				downloadStatusList.add(status);
			}
		}
	}
	
	/**
	 * 根据mode及size移除下载状态
	 * @param modeType
	 * @param sizeType
	 */
	public void RemoveDownloading(PhotoModeType modeType, PhotoSizeType sizeType)
	{
		synchronized (downloadStatusList)
		{
			DownloadStatus status = GetDownloadStatus(modeType, sizeType);
			if (downloadStatusList.contains(status)) {
				downloadStatusList.remove(status);
			}
		}
	}
	
	/**
	 * 根据mode及size获取对应的下载状态
	 * @param modeType	
	 * @param sizeType
	 * @return
	 */
	public static DownloadStatus GetDownloadStatus(PhotoModeType modeType, PhotoSizeType sizeType)
	{
		DownloadStatus status = DownloadStatus.DownloadNothing;
		if (modeType == PhotoModeType.Clear) {
			if (sizeType == PhotoSizeType.Large
				|| sizeType == PhotoSizeType.Middle) 
			{
				status = DownloadStatus.DownloadShowSrcPhoto;
			}
			else if (sizeType == PhotoSizeType.Original) 
			{
				status = DownloadStatus.DownloadSrcPhoto;
			}
			else {
				status = DownloadStatus.DownloadThumbSrcPhoto;
			}
		}
		else if (modeType == PhotoModeType.Fuzzy) {
			if (sizeType == PhotoSizeType.Large
				|| sizeType == PhotoSizeType.Middle) 
			{
				status = DownloadStatus.DownloadShowFuzzyPhoto;
			}
			else {
				status = DownloadStatus.DownloadThumbFuzzyPhoto;
			}
		}
		return status;
	}
}
