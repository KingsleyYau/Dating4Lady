package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestJniAlbum.HandleCode;
import com.qpidnetwork.request.RequestJniAlbum.ReviewStatus;
import com.qpidnetwork.request.RequestJniAlbum.VideoReviewReason;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-20
 */
public class AlbumVideoItem implements Serializable{

	private static final long serialVersionUID = -7462051178530083287L;
	
	public AlbumVideoItem(){
		
	}
	
	/**
	 * @param id	photo的id
	 * @param title	名称
	 * @param thumbUrl	缩略图url
	 * @param previewUrl 预览图url
	 * @param url 源url
	 * @param handleCode video处理结果
	 * @param reviewStatus	审核状态
	 * @param reviewReason	审核处理原因
	 */
	public AlbumVideoItem(
			  String id
			, String title
			, String thumbUrl
			, String previewUrl
			, String url
			, int handleCode
			, int reviewStatus
			, int reviewReason){
		this.id = id;
		this.title = title;
		this.thumbUrl = thumbUrl;
		this.previewUrl = previewUrl;
		this.url = url;
		this.handleCode = (handleCode >= HandleCode.UNKNOWN.ordinal() && handleCode <= HandleCode.VIDEO_PROCESS_FAILED.ordinal())
				? HandleCode.values()[handleCode]:HandleCode.UNKNOWN;
		this.reviewStatus = (reviewStatus >= ReviewStatus.Unknown.ordinal() && reviewStatus <= ReviewStatus.ReviewD.ordinal())
				? ReviewStatus.values()[reviewStatus]:ReviewStatus.Unknown;
		this.reviewReason = (reviewReason >= VideoReviewReason.VIDEO_REASON_OTHERS.ordinal() && reviewReason <= VideoReviewReason.VIDEO_REASON_REVISED_COVERANDDESC_NOSTANDARD.ordinal())
				? VideoReviewReason.values()[reviewReason]:VideoReviewReason.VIDEO_REASON_OTHERS;
	}
	
	public String id;
	public String title;
	public String thumbUrl;
	public String previewUrl;
	public String url;
	public HandleCode handleCode;
	public ReviewStatus reviewStatus;
	public VideoReviewReason reviewReason;
	

}
