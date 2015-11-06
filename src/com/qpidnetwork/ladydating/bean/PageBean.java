package com.qpidnetwork.ladydating.bean;

import java.io.Serializable;

/**
 * 分页处理
 * @author Hunter
 * @since 2015.5.16
 */
public class PageBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int dataCount;
	private int pageIndex; // 第一页的index为1;
	private int pageSize;

	public PageBean() {
		super();
	}

	public PageBean(int pageSize) {
		this.pageIndex = 0;
		this.pageSize = pageSize;
	}

	public PageBean(int pageIndex, int pageSize) {
		this.pageIndex = pageIndex;;
		this.pageSize = pageSize;
	}
	
	public PageBean(int dataCount, int pageIndex, int pageSize) {
		super();
		this.dataCount = dataCount;
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

	public void updateNew(PageBean pb) {
		this.dataCount = pb.getDataCount();
		this.pageIndex = pb.getPageIndex();
	}

	/**
	 * 重置当前页
	 */
	public void resetPageIndex() {
		this.pageIndex = 0;
	}

	/**
	 * 获取下一页
	 * 
	 * @return
	 */
	public int getNextPageIndex() {
		return (++ this.pageIndex);
	}
	
	public int decreasePageIndex(){
		return (this.pageIndex --);
	}

	/**
	 * 判断是否有下一页
	 * 
	 * @return
	 */
	public boolean hasNextPage() {
		return (pageIndex * pageSize < dataCount);
	}

	public int getDataCount() {
		return dataCount;
	}

	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
}
