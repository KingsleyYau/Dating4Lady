package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestJniAlbum.ReviewReason;
import com.qpidnetwork.request.RequestJniAlbum.ReviewStatus;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-20
 */
public class AlbumPhotoItem implements Serializable{

	private static final long serialVersionUID = -7462051178530083287L;
	
	public AlbumPhotoItem(){
		
	}
	
	/**
	 * @param id	photo的id
	 * @param title	名称
	 * @param thumbUrl	缩略图url
	 * @param url 源url
	 * @param reviewStatus	审核状态
	 * @param reviewReason	审核处理原因
	 */
	public AlbumPhotoItem(String id,String title,String thumbUrl,String url,int reviewStatus,int reviewReason){
		this.id = id;
		this.title = title;
		this.thumbUrl = thumbUrl;
		this.url = url;
		this.reviewStatus = (reviewStatus >= ReviewStatus.Unknown.ordinal() && reviewStatus <= ReviewStatus.ReviewD.ordinal())
				? ReviewStatus.values()[reviewStatus]:ReviewStatus.Unknown;
		this.reviewReason = (reviewReason >= ReviewReason.REASON_OTHERS.ordinal() && reviewReason <= ReviewReason.REASON_REVISED_COVERANDDESC_NOSTANDARD.ordinal())
				? ReviewReason.values()[reviewReason]:ReviewReason.REASON_OTHERS;
	}
	
	public String id;
	public String title;
	public String thumbUrl;
	public String url;
	public ReviewStatus reviewStatus;
	public ReviewReason reviewReason;
	

}
