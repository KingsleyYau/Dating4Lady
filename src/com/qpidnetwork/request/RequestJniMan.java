package com.qpidnetwork.request;

import com.qpidnetwork.request.RequestEnum.*;

/**
 * 4.女士信息
 * @author Max.Chiu
 *
 */
public class RequestJniMan {
    
    /**
     * 3.1.查询男士列表（http post）
     * @param pageIndex			当前页数
     * @param pageSize			每页行数
     * @param query_type		查询类型
     * @param man_id			女士ID(长度等于0：默认)
     * @param from_age			起始年龄(小于0：默认)
     * @param to_age			结束年龄(小于0：默认)
     * @param country			国家代码
     * @param photo				是否有照片
     * @return					请求唯一标识
     */
    static public long QueryManList(
			int pageIndex,
			int pageSize,
			QueryType query_type,
			String man_id,
			int from_age,
			int to_age,
			Country country,
			boolean photo,
			OnQueryManListCallback callback
			) {
    	return QueryManList(pageIndex, pageSize, query_type.ordinal(), man_id,
    			from_age, to_age, country.ordinal(), photo, callback);
    }
    static protected native long QueryManList(
    		int pageIndex,
			int pageSize,
			int query_type,
			String man_id,
			int from_age,
			int to_age,
			int country,
			boolean photo,
			OnQueryManListCallback callback
			);
    
    /**
     * 3.2.查询男士详情（http post）
     * @param man_id			男士id
     * @param callback
     * @return					请求唯一标识
     */
    static public native long QueryManDetail(String man_id, OnQueryManDetailCallback callback);
    
    /**
     * 3.3.查询已收藏的男士列表（http post）
     * @param callback
     * @return					请求唯一标识
     */
    static public native long QueryFavourList(OnQueryFavourListCallback callback);
    
    /**
     * 3.4.收藏男士（http post）
     * @param man_id			男士id
     * @param callback
     * @return					请求唯一标识
     */
    static public native long AddFavourites(String man_id, OnRequestCallback callback);
    
    /**
     * 3.5.删除已收藏男士（http post）
     * @param man_id			男士id
     * @param callback
     * @return					请求唯一标识
     */
    static public native long RemoveFavourites(String[] man_ids, OnRequestCallback callback);
    
    /**
     * 3.6.获取最近聊天男士列表（http post）
     * @param pageIndex			当前页数
     * @param pageSize			每页行数
     * @param query_type		查询类型
     * @param callback
     * @return					请求唯一标识
     */
    static public long QueryManRecentChatList(
			int pageIndex,
			int pageSize,
			RecentChatQueryType query_type,
			OnQueryManRecentChatListCallback callback
			) {
    	return QueryManRecentChatList(pageIndex, pageSize, query_type.ordinal(), callback);
    }
    static protected native long QueryManRecentChatList(
    		int pageIndex,
			int pageSize,
			int query_type,
			OnQueryManRecentChatListCallback callback
			);
 
    /**
     * 3.7.获取最近访客列表（http post）
     * @param callback
     * @return					请求唯一标识
     */
    static public native long QueryManRecentViewList(OnQueryManRecentViewListCallback callback);
}