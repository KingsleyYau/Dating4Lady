package com.qpidnetwork.ladydating.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.qpidnetwork.ladydating.chat.history.ChatContactItem;
import com.qpidnetwork.livechat.LCUserItem;
import com.qpidnetwork.request.item.LCChatListItem;

public class ChatHistoryDB {
	//table name
	private static final String CHAT_HISTORY_TABLE = "ChatHistory";
	//field
	private static final String CHAT_USER_ID = "chatUserId"; //用户ID，用作多用户处理
	private static final String CHAT_TARGET_ID = "chatTargetId"; //聊天对象ID
	private static final String CHAT_TARGET_NAME = "chatTargetName"; //聊天对象名字
	private static final String CHAT_INVITE_ID = "inviteId"; //会话邀请Id
	private static final String CHAT_START_TIME = "chatStartTime"; //会话开始时间（保存有效期30天）
	private static final String CHAT_HISTORY_READFLAGS = "readFlags"; // 是否已读
	private static final String CHAT_START_TIME_ALIAS = "chatStartTimeAlias";//会话开始时间别名
	private static final String CHAT_HISTORY_READFLAGS_ALIAS = "readFlagsAlias";//是否已读别名
	
	private static final String USERID_INVITEID_AND_MANID_WHERE = CHAT_USER_ID + " =?" + " AND " + CHAT_TARGET_ID + " =?" + " AND " + CHAT_INVITE_ID + " =?";
	private static final String USERID_MANID_AND_READFLAG_WHERE = CHAT_USER_ID + " =?" + " AND " + CHAT_TARGET_ID + " =?" + " AND " + CHAT_HISTORY_READFLAGS + " =?";
	
	private static ChatHistoryDB mChatHistoryDB;
	private DatabaseHelper mDatabaseHelper;
	
	private String mUserId = "";
	
	public static ChatHistoryDB getInstance(Context context) {
		if (mChatHistoryDB == null) {
			mChatHistoryDB = new ChatHistoryDB(context);
			return mChatHistoryDB;
		} else {
			return mChatHistoryDB;
		}
	}

	private ChatHistoryDB(Context context) {
		mDatabaseHelper = DatabaseHelper.getInstance(context);
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		createTable(db);
	}
	
	private void createTable(SQLiteDatabase db) {
		String CTEATE_CHAT_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + CHAT_HISTORY_TABLE + " (" 
			+ CHAT_USER_ID + " TEXT,"
			+ CHAT_TARGET_ID + " TEXT,"
			+ CHAT_TARGET_NAME + " TEXT,"
			+ CHAT_INVITE_ID + " TEXT," 
			+ CHAT_START_TIME + " INTEGER," 	
			+ CHAT_HISTORY_READFLAGS + " BOOLEAN DEFAULT 0 NOT NULL" + ");";
		db.execSQL(CTEATE_CHAT_HISTORY_TABLE);
	}
	
	/**
	 * 清除超过30天的无效会话记录
	 * @param timestamp 本地时间和服务器时间差
	 */
	public void clearInvalidRecord(int dbTime){
		int latestValidTime = dbTime - 30 * 24 *60 * 60 ;
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		db.delete(CHAT_HISTORY_TABLE, CHAT_START_TIME + " <= ? " + " AND " + CHAT_USER_ID + " =?", new String[]{String.valueOf(latestValidTime), mUserId});
	}
	
	/**
	 * 是否有未读历史消息
	 * @return
	 */
	public boolean isContainUnreadHistory(){
		int count = 0;
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		String COUNT_UNREAD = "SELECT COUNT(*) FROM " + CHAT_HISTORY_TABLE + " WHERE " + CHAT_HISTORY_READFLAGS + " = ?" + " AND " + CHAT_USER_ID + " =?";
		Cursor cursor = db.rawQuery(COUNT_UNREAD, new String[]{String.valueOf(0), mUserId});
		try {
			if(cursor != null){
			    if (cursor.moveToFirst()) {
			    	count = cursor.getInt(0);
			    }
			}
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		
		return count > 0 ? true:false;
	}
	
	/**
	 * 获取联系人聊天历史返回入库
	 * @param chatHistory
	 */
	public void InsertChatHistory(LCChatListItem chatHistory){
		if(chatHistory != null){
			SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(CHAT_USER_ID, mUserId);
			values.put(CHAT_TARGET_ID, chatHistory.manId);
			values.put(CHAT_TARGET_NAME, chatHistory.manName);
			values.put(CHAT_INVITE_ID, chatHistory.inviteId);
			int startTime = 0;
			try {
				startTime = (int)(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(chatHistory.startTime).getTime()/1000);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			values.put(CHAT_START_TIME, startTime);
			values.put(CHAT_HISTORY_READFLAGS, false);
			if(!containItem(chatHistory.manId, chatHistory.inviteId)
					&& startTime != 0){
				db.insert(CHAT_HISTORY_TABLE, CHAT_INVITE_ID, values);
			}
		}
	}
	
	/**
	 * 添加本地聊天记录到聊天历史
	 * @param item
	 * @param inviteID
	 * @param startTime
	 */
	public void AddLocalToChatHistory(LCUserItem item, String inviteId, int startTime){
		if(!TextUtils.isEmpty(inviteId) && (TextUtils.isEmpty(item.userId))){ 
			SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(CHAT_USER_ID, mUserId);
			values.put(CHAT_TARGET_ID, item.userId);
			values.put(CHAT_TARGET_NAME, item.userName);
			values.put(CHAT_INVITE_ID, inviteId);
			values.put(CHAT_START_TIME, startTime);
			values.put(CHAT_HISTORY_READFLAGS, true);
			if(!containItem(item.userId, inviteId)){
				db.insert(CHAT_HISTORY_TABLE, CHAT_INVITE_ID, values);
			}
		}
	}
	
	/**
	 * 标记所有历史记录为已读
	 */
	public void markAllasRead(){
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(CHAT_HISTORY_READFLAGS, true);
		db.update(CHAT_HISTORY_TABLE, values, CHAT_USER_ID + " =?", new String[]{mUserId});
	}
	
	/**
	 * 更新聊天历史已读状态
	 * @param manId
	 * @param inviteId
	 * @param readFlag
	 */
	public void UpdateChatHistory(String manId, String inviteId, boolean readFlag){
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(CHAT_HISTORY_READFLAGS, readFlag);
		if(containItem(manId, inviteId)){
			db.update(CHAT_HISTORY_TABLE, values, USERID_INVITEID_AND_MANID_WHERE, new String[]{mUserId, manId, inviteId});
		}
	}
	
	/**
	 * 获取有聊天历史的联系人列表
	 * @return
	 */
	public List<ChatContactItem> getChatHistoryContactList(){
		List<ChatContactItem> contactList = new ArrayList<ChatContactItem>();
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		String queryBuilder = "SELECT *, max(" + CHAT_START_TIME + ") as " + CHAT_START_TIME_ALIAS
									+ ", min(" + CHAT_HISTORY_READFLAGS + ") as " + CHAT_HISTORY_READFLAGS_ALIAS
									+ " FROM " + CHAT_HISTORY_TABLE + " WHERE " + CHAT_USER_ID + " =?" + " group by " + CHAT_TARGET_ID + " order by " + CHAT_START_TIME_ALIAS + " DESC";
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(queryBuilder, new String[]{mUserId});
			if(cursor != null && cursor.moveToFirst()){
				while(!cursor.isAfterLast()) {
					contactList.add(retrieveChatHistoryContactItem(cursor));
					cursor.moveToNext();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		return contactList;
	}
	
	/**
	 * 获取和指定男士的聊天已读状态
	 * @param manId
	 * @return
	 */
	public boolean getChatContactReadFlag(String manId){
		boolean isReadFlag = true;
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.query(CHAT_HISTORY_TABLE, new String[]{CHAT_INVITE_ID}, CHAT_TARGET_ID + " = ?" + " AND " + CHAT_USER_ID + " = ?" + " AND " + CHAT_HISTORY_READFLAGS + " =?", 
					new String[]{manId, mUserId, String.valueOf(0)}, null, null, null);
			if(cursor != null){
			    if (cursor.moveToFirst()) {
			    	isReadFlag = cursor.getInt(0)>0? false : true;
			    }
			}
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		return isReadFlag;
	}
	
	/**
	 * 解析为数据库读取数据为指定ChatContactItem
	 * @param cursor
	 * @return
	 */
	private ChatContactItem retrieveChatHistoryContactItem(Cursor cursor){
		ChatContactItem item = new ChatContactItem();
		int target_id_index = cursor.getColumnIndex(CHAT_TARGET_ID);
		int target_name_index = cursor.getColumnIndex(CHAT_TARGET_NAME);
		int start_time_alias_index = cursor.getColumnIndex(CHAT_START_TIME_ALIAS);
		int readfalg_alias_index = cursor.getColumnIndex(CHAT_HISTORY_READFLAGS_ALIAS);
		item.manId = cursor.getString(target_id_index);
		item.manName = cursor.getString(target_name_index);
		item.startTime = cursor.getInt(start_time_alias_index);
		if(cursor.getInt(readfalg_alias_index) == 0){
			item.readFlag = false;
		}else{
			item.readFlag = true;
		}
		return item;
	}
	
	/**
	 * 获取所有男士会话阅读状态
	 * @param manId
	 * @return
	 */
	public HashMap<String, Boolean> getAllInviteReadFlag(String manId){
		HashMap<String, Boolean> mInviteMap = new HashMap<String, Boolean>();
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Cursor cursor = null;
		try{
			cursor = db.query(CHAT_HISTORY_TABLE, new String[]{CHAT_INVITE_ID, CHAT_HISTORY_READFLAGS}, CHAT_TARGET_ID + " = ?" + " AND " + CHAT_USER_ID + " = ?", 
					new String[]{manId, mUserId}, null, null, null);
			if(cursor.moveToFirst()){
				while (!cursor.isAfterLast()) {
					int invite_id_index = cursor.getColumnIndex(CHAT_INVITE_ID);
					int readflag_index = cursor.getColumnIndex(CHAT_HISTORY_READFLAGS);
					String inviteId = cursor.getString(invite_id_index);
					boolean unreadFlag = true;
					if(cursor.getInt(readflag_index) == 1){
						unreadFlag = false;
					}else{
						unreadFlag = true;
					}
					mInviteMap.put(inviteId, unreadFlag);
					cursor.moveToNext();
				}
			}
		}catch(Exception e){
			
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		return mInviteMap;
	}
	
	/**
	 * 把指定会员的聊天记录指定为已读
	 * @param manId
	 */
	public void markAllAsReadByManId(String manId){
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(CHAT_HISTORY_READFLAGS, true);
		db.update(CHAT_HISTORY_TABLE, values, CHAT_TARGET_ID + " =?" + " AND " + CHAT_USER_ID + " = ?" , new String[]{manId, mUserId});
	}
	
	/**
	 * 获取指定男士未读历史消息数目
	 * @param manId
	 * @return
	 */
	public int getAllUnreadChatHistoryCount(String manId){
		int count = 0;
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Cursor result = db.query(CHAT_HISTORY_TABLE, 
				new String[]{CHAT_INVITE_ID}, 
				USERID_MANID_AND_READFLAG_WHERE, 
				new String[]{mUserId, manId, "0"}, 
				null, 
				null, 
				null);
		if(result != null){
			count = result.getCount();
			result.close();
		}
		return count;
	}
	
	/**
	 * 记录已存在
	 * @param inviteId
	 */
	private boolean containItem(String manId, String inviteId){
		boolean isExists = false;
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Cursor result = db.query(CHAT_HISTORY_TABLE, 
				new String[]{CHAT_INVITE_ID}, 
				USERID_INVITEID_AND_MANID_WHERE, 
				new String[]{mUserId, manId, inviteId}, 
				null, 
				null, 
				null);
		if(result != null){
			isExists = (result.getCount()>0);
			result.close();
		}
		
		return isExists;
	}
	
	/**
	 * 同步用户Id
	 * @param userId
	 */
	public void synUserId(String userId){
		mUserId = userId;
	}
}
