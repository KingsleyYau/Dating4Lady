package com.qpidnetwork.request.item;

/**
 * Magic Icon Type
 * @author Hunter 
 * @since 2016.4.7
 */
public class MagicIconType {
	
	public MagicIconType(){
		
	}
	/**
	 * 
	 * @param id typeId
	 * @param title typeTitle
	 */
	public MagicIconType(
			String id,
			String title){
		this.id = id;
		this.title = title;
	}
	public String id;
	public String title;
}
