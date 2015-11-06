package com.qpidnetwork.livechat;

import java.io.File;
import java.io.Serializable;

/**
 * 语音对象
 * @author Samson Fan
 */
public class LCVoiceItem implements Serializable{

	private static final long serialVersionUID = -712138699429765395L;
	/**
	 * 语音ID
	 */
	public String voiceId;
	/**
	 * 文件路径
	 */
	public String filePath;
	/**
	 * 语音时长（秒）
	 */
	public int timeLength;
	/**
	 * 文件类型（后缀）
	 */
	public String fileType;
	/**
	 * 语音发送验证码（仅发送）
	 */
	public String checkCode;
	/**
	 * 是否已付费
	 */
	public boolean charge;
	
	public LCVoiceItem() {
		voiceId = "";
		filePath = "";
		timeLength = 0;
		fileType = "";
		checkCode = "";
		charge = false;
	}
	
	public void init(
			String voiceId
			, String filePath
			, int timeLength
			, String fileType
			, String checkCode
			, boolean charge) 
	{
		this.voiceId = voiceId;
		this.timeLength = timeLength;
		this.fileType = fileType;
		this.checkCode = checkCode;
		this.charge = charge;
			
		if (!filePath.isEmpty()) {
			File file = new File(filePath);
			if (file.exists()) {
				this.filePath = filePath;
			}
		}
	}
}
