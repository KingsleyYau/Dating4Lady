package com.qpidnetwork.request;

public class RequestJniAlbum {

	/**
	 * 4.1 查询Album列表
	 * @param callback
	 */
	public static native long QueryAlbumList(OnQueryAlbumListCallback callback);
	
	public enum AlbumType {
		Unknown,
		Photo, 
		Video
	}
	
	/**
	 * 4.2 创建Album
	 * @param type
	 * @param title
	 * @param desc
	 * @param callBack
	 * @return
	 */
	public static long CreateAlbum(AlbumType type, String title, String desc, OnCreateAlbumCallback callBack){
		return CreateAlbum(type.ordinal(), title, desc, callBack);
	}
	
	protected static native long CreateAlbum(int type, String title, String desc, OnCreateAlbumCallback callBack);
	
	/**
	 * 4.3 修改Album
	 * @param type
	 * @param title
	 * @param desc
	 * @param callBack
	 * @return
	 */
	public static long EditAlbum(String albumId, AlbumType type, String title, String desc, OnEditAlbumCallback callBack){
		return EditAlbum(albumId, type.ordinal(), title, desc, callBack);
	}
	protected static native long EditAlbum(String albumId, int type, String title, String desc, OnEditAlbumCallback callBack);
	
	/**
	 * 4.4 删除Album
	 * @param albumId
	 * @param type
	 * @param callBack
	 * @return
	 */
	public static long DeleteAlbum(String albumId,AlbumType type,final OnDeleteAlbumCallback callBack){
		return DeleteAlbum(albumId, type.ordinal(), callBack);
	}
	protected static native long DeleteAlbum(String albumId, int type, OnDeleteAlbumCallback callBack);
	
	public enum HandleCode {
		UNKNOWN,
		WAITING_FOR_TRANSCODING,//待转码处理中 
		TRANSCODED_ADN_DOWNLOAD,//已转码并成功下载
		ERROE_AFTER_DOWNLOAD,//下载转码后的视频出错
		VIDEO_PROCESS_FAILED //视频处理失败
	}
	
	public enum ReviewStatus {
		Unknown, //未知错误
		ReviewP,//等待机构审核
		ReviewE,//等待亚媒审核
		ReviewY,//审核通过
		ReviewN,//审核不通过
		ReviewD//打回修改
	}
	
	public enum PhotoReviewReason {
		PHOTO_REASON_OTHERS,//其他
		PHOTO_REASON_NON_SELF,//照片中未出現女士本人
		PHOTO_REASON_SIMILAR_PHOTO,//照片與女士資料照片:信件照片不同
		PHOTO_REASON_FACIAL_BLUR,//照片中女士容貌模糊:失真
		PHOTO_REASON_PHOTO_UPSIDE_DOWN,//照片倒置或无法显示
		PHOTO_REASON_APPEARANCE_NOT_MATCH,//女士衣着或仪态不符合要求
		PHOTO_REASON_FACE_PROFILEPHOTO_NOMATCH,//女士容貌或身材与女士资料照片差异过大
		PHOTO_REASON_PHOTO_CONTAIN_TEXTORWATERMARK,//照片中含有不符合要求的文字:水印等内容
		PHOTO_REASON_REVISED_DESC_NOSTANDARD,//描述不規範
		PHOTO_REASON_REVISED_COVER_NOSTANDARD,//封面不規範
		PHOTO_REASON_REVISED_COVERANDDESC_NOSTANDARD //封面及描述均不規範
	}
	
	public enum VideoReviewReason {
		VIDEO_REASON_OTHERS,//其他
		VIDEO_REASON_NON_SELF,//视频中未出现女士本人
		VIDEO_REASON_SIMILAR_VIDEOSHOW,//视频与videoshow相似
		VIDEO_REASON_FACIAL_BLUR,//视频中女士容貌模糊/失真
		VIDEO_REASON_VIDEO_UPSIDE_DOWN,//视频倒置/无法显示
		VIDEO_REASON_APPEARANCE_NOT_MATCH,//视频中女士衣着/仪态不符合要求
		VIDEO_REASON_FACE_PHOTO_NOMATCH,//视频中女士容貌/身材与资料照片差异过大
		VIDEO_REASON_EXIST_SIMILAR_SHORTVIDEO,//女士已上传内容相似的微视频
		VIDEO_REASON_VIDEO_PIX_NOMATCH, //视频像素低于1280*720 
		VIDEO_REASON_VIDEO_BLUR, //视频画面变形
		VIDEO_REASON_VIDEO_VOICE_NOMATCH, //視頻中女士說話無聲音/聲音與口型不同步
		VIDEO_REASON_VIDEO_CONTAIN_TEXTORWATERMARK,//視頻中含有不符合要求的文字/水印等內容 
		VIDEO_REASON_VIDEO_CONTAIN_CONTACTINFO, //視頻中含有女士/機構聯繫方式 
		VIDEO_REASON_VIDEO_PROFILE_NOMATCH, //視頻中信息與系統資料不符
		VIDEO_REASON_VIDEO_SITE_NOMATCH, //視頻中內容與本站無關
		VIDEO_REASON_VIDEO_TOO_SHORT,//视频时长不足8秒
		VIDEO_REASON_REVISED_DESC_NOSTANDARD,//描述不規範
		VIDEO_REASON_REVISED_COVER_NOSTANDARD,//封面不規範
		VIDEO_REASON_REVISED_COVERANDDESC_NOSTANDARD //封面及描述均不規範
	}
	
	/**
	 * 4.5 查询Photo列表
	 * @param albumId
	 * @param callBack
	 * @return
	 */
	public static native long QueryAlbumPhotoList(String albumId, OnQueryAlbumPhotoListCallback callBack);
	
	/**
	 * 4.6 添加Photo
	 * @param albumId
	 * @param title
	 * @param photoUrl
	 * @param callBack
	 * @return
	 */
	public static native long AddAlbumPhoto(String albumId, String title, String photoUrl, OnSaveAlbumPhotoCallback callBack);
	
	/**
	 * 4.7 修改Photo
	 * @param albumId
	 * @param title
	 * @param callBack
	 * @return
	 */
	public static native long EditAlbumPhoto(String albumId, String title, OnEditAlbumPhotoCallback callBack);
	
	/**
	 * 4.8 查询Video列表
	 * @param albumId
	 * @param callBack
	 * @return
	 */
	public static native long QueryAlbumVideoList(String albumId, OnQueryAlbumVideoListCallback callBack);
	
	/**
	 * 4.9 上传Video文件
	 * @param agencyID
	 * @param womanID
	 * @param siteId
	 * @param shortVideoKey
	 * @param serverType
	 * @param filePath
	 * @param mimeType
	 * @param callBack
	 * @return
	 */
	public static native long UploadVideoFile(String agencyID, String womanID, int siteId, String shortVideoKey, int serverType, String filePath, String mimeType, OnUploadVideoCallback callBack);	
	
	/**
	 * 4.10 添加Video
	 * @param albumId
	 * @param title
	 * @param shortVideoKey
	 * @param shortVideoKey
	 * @param thumbnailUri
	 * @param callBack
	 * @return
	 */
	public static native long AddAlbumVideo(String albumId, String title, String shortVideoKey, String hidFileMd5ID, String thumbnailUri, OnSaveAlbumVideoCallback callBack);
	
	/**
	 * 4.10 修改Video
	 * @param albumId
	 * @param title (可无，无则表示不用修改)
	 * @param thumbnailUri(可无，无则表示不用修改)
	 * @param callBack
	 * @return
	 */
	public static native long EditAlbumVideo(String albumId, String title, String thumbnailUri, OnEditAlbumVideoCallback callBack);
	
}
