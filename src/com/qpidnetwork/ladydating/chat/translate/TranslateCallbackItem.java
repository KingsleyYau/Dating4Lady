package com.qpidnetwork.ladydating.chat.translate;

public class TranslateCallbackItem {
	
	public boolean isSuccess = false;
	public String seq;
	public String originalText;
	public String tranlatedText;
	
	public TranslateCallbackItem(boolean isSuccess, String seq, String originalText, String tranlatedText){ 
		this.isSuccess = isSuccess;
		this.seq = seq;
		this.originalText = originalText;
		this.tranlatedText = tranlatedText;
	}
}
