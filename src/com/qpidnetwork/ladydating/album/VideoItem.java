package com.qpidnetwork.ladydating.album;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *	@author Yanni
 * 
 *	@version 2016-6-24
 */
public class VideoItem implements Parcelable{

	
	public String videoUri;

	public long duraion;
	
	public long size;//bytes
	
	public VideoItem(){
		
	};
	
	public VideoItem(String videoUri, long duration, long size){
		this.videoUri = videoUri;
		this.duraion = (long)(duration / 1000);
		this.size = size;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(videoUri);
		dest.writeLong(duraion);
		dest.writeLong(size);
	}
	
	 // 实例化静态内部对象CREATOR实现接口Parcelable.Creator  
    public static final Parcelable.Creator<VideoItem> CREATOR = new Parcelable.Creator<VideoItem>() {  
        // 将Parcel对象反序列化为VideoItem 
        public VideoItem createFromParcel(Parcel source) {  
        	VideoItem item = new VideoItem();  
        	item.videoUri = source.readString();  
        	item.duraion = source.readLong();
        	item.size = source.readLong();
            return item;  
        }  
  
        public VideoItem[] newArray(int size) {  
            return new VideoItem[size];  
        }  
    };  

}
