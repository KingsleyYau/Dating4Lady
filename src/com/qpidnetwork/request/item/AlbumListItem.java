package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestJniAlbum.AlbumType;

/**
 * @author Yanni
 * 
 * @version 2016-6-17
 * 
 * 相册列表数据
 */
public class AlbumListItem implements Serializable {

	private static final long serialVersionUID = -5472956130238756702L;

	public AlbumListItem() {

	}

	/**
	 * 相册列表详情
	 * 
	 * @param id
	 *            album的id（不同类型id可能重复）
	 * @param type
	 *            （0：photo，1：video，2：未审核通过）
	 * @param title
	 *            名称
	 * @param desc
	 *            描述
	 * @param imageurl
	 *            封面图URL
	 * @param count
	 *            photo或video个数
	 * @param createtime
	 *            创建时间（Unix timestamp）
	 */

	public AlbumListItem(String id, int type, String title, String desc,
			String imageUrl, int count, int createTime) {
		this.id = id;
		this.type = (type >= AlbumType.Unknown.ordinal() && type <= AlbumType.Video.ordinal())
				? AlbumType.values()[type]:AlbumType.Unknown;
		this.title = title;
		this.desc = desc;
		this.imageUrl = imageUrl;
		this.count = count;
		this.createTime = createTime;
	}

	public String id;
	public AlbumType type;
	public String title;
	public String desc;
	public String imageUrl;
	public int count;
	public int createTime;

}
