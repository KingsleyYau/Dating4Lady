package com.qpidnetwork.request.item;

import java.io.Serializable;

public class SynConfigItem implements Serializable{
	
	private static final long serialVersionUID = -7261447099845711699L;

	public SynConfigItem(){
		socketHost = "";
		socketPort = 0;
		socketVersion = "";
		socketFromId = 0;
		translateUrl = "";
		translateLanguages = new String[0];
		apkVersionCode = 0;
		apkVersionName = "";
		apkVersionUrl = "";
		siteUrl = "";
		liveChatVoiceHost = "";
	}
	
	public SynConfigItem(
			String socketHost,
			int socketPort,
			String socketVersion,
			int socketFromId,
			String translateUrl,
			String[] translateLanguages,
			int apkVersionCode,
			String apkVersionName,
			String apkVersionUrl,
			String siteUrl,
			String liveChatVoiceHost
			){
		this.socketHost = socketHost;
		this.socketPort = socketPort;
		this.socketVersion = socketVersion;
		this.socketFromId = socketFromId;
		this.translateUrl = translateUrl;
		this.translateLanguages = translateLanguages;
		this.apkVersionCode = apkVersionCode;
		this.apkVersionName = apkVersionName;
		this.apkVersionUrl = apkVersionUrl;
		this.siteUrl = siteUrl;
		this.liveChatVoiceHost = liveChatVoiceHost;
	}
	
	public String socketHost;
	public int socketPort;
	public String socketVersion;
	public int socketFromId;
	public String translateUrl;
	public String[] translateLanguages;
	public int apkVersionCode;
	public String apkVersionName;
	public String apkVersionUrl;
	public String siteUrl;
	public String liveChatVoiceHost;
}
