package com.example.testapp;

import android.net.Uri;
import android.provider.BaseColumns;

public final class WeiboData 
{
	public static final String AUTHORITY = "com.example.testapp.providers.weibocontentproviders";
	public static final class Weibo implements BaseColumns
	{
		//Define Data Columns
		public static final String _ID = "id";
		public static final String WEIBOID = "weiboid";
		public static final String WEIBOSCREENNAME = "weiboscreenname";
		public static final String WEIBOLINK = "weibolink";
		public static final String WEIBOCONTENT = "weibocontent";
		public static final String WEIBOTIME = "weibotime";
		public static final String WEIBOPIC = "weibopic";
		
		public static final Uri WEIBO_LIST_URI = Uri.parse("content://"+AUTHORITY+"/weiboinlist");
		public static final Uri WEIBO__URI = Uri.parse("content://"+AUTHORITY+"/weibosingle");
	}
}
