package com.qpidnetwork.livechat;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import com.qpidnetwork.request.RequestJniLivechat.VideoPhotoType;


/**
 * 视频对象
 * @author Samson Fan
 */
public class LCVideoItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6947486405767164898L;
	/**
	 * 视频ID
	 */
	public String videoId;
	/**
	 * 视频描述
	 */
	public String videoDesc;
	/**
	 * 大图文件路径
	 */
	public String bigPhotoPath;
	/**
	 * 小图文件路径
	 */
	public String smallPhotoPath;
	/**
	 * 视频文件路径
	 */
	public String videoPath;
	/**
	 * 下载状态定义
	 */
	public enum DownloadStatus {
		/**
		 * 没有下载
		 */
		DownloadNothing,
		/**
		 * 正在下载大图文件
		 */
		DownloadBigPhoto,
		/**
		 * 正在下载小图文件
		 */
		DownloadSmallPhoto,
		/**
		 * 正在下载视频文件
		 */
		DownloadVideo,
	}
	ArrayList<DownloadStatus> downloadStatusList;
	
	
	public LCVideoItem() {
		videoId = "";
		videoDesc = "";
		bigPhotoPath = "";
		smallPhotoPath = "";
		videoPath = "";
		downloadStatusList = new ArrayList<DownloadStatus>();
	}
	
	public void init(
			String videoId
			, String videoDesc
			, String bigPhotoPath
			, String smallPhotoPath
			, String videoPath) 
	{
		this.videoId = videoId;
		this.videoDesc = videoDesc;
		
		SetBigPhotoPath(bigPhotoPath);
		SetSmallPhotoPath(smallPhotoPath);
		SetVideoPath(videoPath);
	}
	
	/**
	 * 设置视频大图路径
	 * @param bigPhotoPath	大图路径
	 * @return
	 */
	public boolean SetBigPhotoPath(String bigPhotoPath)
	{
		boolean result = false;
		if (!bigPhotoPath.isEmpty()) {
			File file = new File(bigPhotoPath);
			if (file.exists()) {
				this.bigPhotoPath = bigPhotoPath;
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 设置视频小图路径
	 * @param smalllPhotoPath	小图路径
	 * @return
	 */
	public boolean SetSmallPhotoPath(String smallPhotoPath)
	{
		boolean result = false;
		if (!smallPhotoPath.isEmpty()) {
			File file = new File(smallPhotoPath);
			if (file.exists()) {
				this.smallPhotoPath = smallPhotoPath;
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 设置视频路径
	 * @param videoPath		视频路径
	 * @return
	 */
	public boolean SetVideoPath(String videoPath)
	{
		boolean result = false;
		if (!videoPath.isEmpty()) {
			File file = new File(videoPath);
			if (file.exists()) {
				this.videoPath = videoPath;
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * 设置视频图片路径
	 * @param filePath		文件路径
	 * @param photoType		图片类型
	 * @return
	 */
	public boolean SetVideoPhotoPath(String filePath, VideoPhotoType photoType)
	{
		boolean result = false;
		switch(photoType)
		{
		case Big:
			result = SetBigPhotoPath(filePath);
			break;
		case Default:
			result = SetSmallPhotoPath(filePath);
			break;
		}
		return result;
	}
	
	/**
	 * 判断是否正在下载图片文件
	 * @param photoType		图片类型
	 * @return
	 */
	public boolean  IsDownloadingPhoto(VideoPhotoType photoType)
	{
		boolean result = false;
		
		synchronized (downloadStatusList)
		{
			// 获取对应的下载状态
			DownloadStatus status = GetDownloadPhotoStatus(photoType);
			// 判断下载状态是否存在状态列表
			result = downloadStatusList.contains(status);
		}
		
		return result;
	}
	
	/**
	 * 判断是否正在下载视频文件
	 * @return
	 */
	public boolean IsDownloadingVideo()
	{
		boolean result = false;
		
		synchronized (downloadStatusList)
		{
			// 判断下载状态是否存在状态列表
			result = downloadStatusList.contains(DownloadStatus.DownloadVideo);
		}
		
		return result;
	}
	
	/**
	 * 添加下载图片状态
	 * @param photoType		图片类型
	 */
	public void AddDownloadingPhoto(VideoPhotoType photoType)
	{
		synchronized (downloadStatusList)
		{
			DownloadStatus status = GetDownloadPhotoStatus(photoType);
			if (status != DownloadStatus.DownloadNothing 
					&& !downloadStatusList.contains(status)) 
			{
				downloadStatusList.add(status);
			}
		}
	}
	
	/**
	 * 添加下载视频状态
	 */
	public void AddDownloadingVideo()
	{
		synchronized (downloadStatusList)
		{
			if (!downloadStatusList.contains(DownloadStatus.DownloadVideo))
			{
				downloadStatusList.add(DownloadStatus.DownloadVideo);
			}
		}
	}
	
	/**
	 * 移除图片下载状态
	 * @param photoType		图片类型
	 */
	public void RemoveDownloadingPhoto(VideoPhotoType photoType)
	{
		synchronized (downloadStatusList)
		{
			DownloadStatus status = GetDownloadPhotoStatus(photoType);
			if (downloadStatusList.contains(status)) {
				downloadStatusList.remove(status);
			}
		}
	}
	
	/**
	 * 移除视频下载状态
	 */
	public void RemoveDownloadingVideo()
	{
		synchronized (downloadStatusList)
		{
			if (downloadStatusList.contains(DownloadStatus.DownloadVideo)) {
				downloadStatusList.remove(DownloadStatus.DownloadVideo);
			}
		}
	}
	
	/**
	 * 根据图片类型获取对应的下载图片状态
	 * @param photoType		图片类型	
	 * @return
	 */
	public static DownloadStatus GetDownloadPhotoStatus(VideoPhotoType photoType)
	{
		DownloadStatus status = DownloadStatus.DownloadNothing;
		if (photoType == VideoPhotoType.Big) {
			status = DownloadStatus.DownloadBigPhoto;
		}
		else if (photoType == VideoPhotoType.Default) {
			status = DownloadStatus.DownloadSmallPhoto;
		}
		return status;
	}
	
	/**
	 * 根据下载状态获取图片类型
	 * @param status	下载状态
	 * @return
	 */
	public static VideoPhotoType GetVideoPhotoTypeWithStatus(DownloadStatus status)
	{
		VideoPhotoType photoType = VideoPhotoType.Default;
		if (status == DownloadStatus.DownloadBigPhoto) {
			photoType = VideoPhotoType.Big;
		}
		else if (status == DownloadStatus.DownloadSmallPhoto) {
			photoType = VideoPhotoType.Default;
		}
		return photoType;
	}
}
