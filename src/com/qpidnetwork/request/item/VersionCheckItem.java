package com.qpidnetwork.request.item;

import java.io.Serializable;

public class VersionCheckItem implements Serializable{
	
	private static final long serialVersionUID = -5695390926721818986L;
	
	public VersionCheckItem() {
		
	}

	/**
	 * 
	 * @param verCode	客户端内部版本号
	 * @param verName	客户端显示版本号
	 * @param apkUrl	客户端apk下载地址
	 */
	public VersionCheckItem (
			int verCode,
			String verName,
			String apkUrl
			) {
		this.verCode = verCode;
		this.verName = verName;
		this.apkUrl = apkUrl;
	}
	
	public int verCode;
	public String verName;
	public String apkUrl;

}
