package com.example.testapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper{
	final String CREATE_TABLE =
			"create table weibo(id integer primary " +
			"key , weiboid , weiboscreenname, weibolink, weibocontent, weibotime, weibopic)";
	public DataBaseHelper(Context context,String name,int version)
	{
		super(context, name,null,version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		//Do Nothing
	}
	
}
