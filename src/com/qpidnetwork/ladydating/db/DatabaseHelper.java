package com.qpidnetwork.ladydating.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qpidnetwork.framework.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
	
	private static DatabaseHelper databaseHelperInstent ;
	private static String DATABASE_NAME = "qoidnetwork.db" ;
	private static int DATABASE_VERSION = 206;
	
	private DatabaseHelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
	}
	
	public static DatabaseHelper getInstance(Context context) {
		if((databaseHelperInstent == null)) {
			databaseHelperInstent = new DatabaseHelper(context.getApplicationContext(), DATABASE_NAME);
			return databaseHelperInstent;
		} else {
			return databaseHelperInstent;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d("DatabaseHelper.onCreate", "onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.d("DatabaseHelper.onUpgrade", "onUpgrade");
	}

}
