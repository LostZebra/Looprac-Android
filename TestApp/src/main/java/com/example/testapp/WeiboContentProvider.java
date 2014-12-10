package com.example.testapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class WeiboContentProvider extends ContentProvider
{
	private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int WEIBOINLIST = 1;
	private static final int WEIBOSINGLE = 2;
	private DataBaseHelper dbHelper;
	static
	{
		matcher.addURI(WeiboData.AUTHORITY,"weiboinlist",WEIBOINLIST);
		matcher.addURI(WeiboData.AUTHORITY,"weibosingle",WEIBOSINGLE);
	}
	
	@Override
	public boolean onCreate() {
		dbHelper = new DataBaseHelper(this.getContext(),"localweibodb1.db3",1);
		return true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase tempDb = dbHelper.getReadableDatabase();
		int num = 0;
		switch(matcher.match(uri))
		{
			case WEIBOINLIST:
			{
				num = tempDb.delete("weibo",selection,selectionArgs);
				break;
			}
			case WEIBOSINGLE:
			{
				long id = ContentUris.parseId(uri);
				String selectionClause = WeiboData.Weibo._ID + "=" + id;
				if(selection != null && !"".equals(selection))
				{
					selectionClause = selectionClause + " and " + selection;
				}
				num = tempDb.delete("weibo",selectionClause,selectionArgs);
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown Uri:"+uri);
		}
		getContext().getContentResolver().notifyChange(uri,null);
		return num;
	}
	
	@Override
	public String getType(Uri uri) {
		switch (matcher.match(uri)) {
		case WEIBOINLIST:
			return "vnd.android.cursor.dir/com.example.testapp.weibo";
		case WEIBOSINGLE:
			return "vnd.android:cursor.item/com.example.testapp.weibo";
		default:
			throw new IllegalArgumentException("Unknown Uri:"+uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase tempDb = dbHelper.getReadableDatabase();
		switch(matcher.match(uri))
		{
			case WEIBOINLIST:
			{
				long rowId = tempDb.insert("weibo",WeiboData.Weibo._ID,values);
				if(rowId > 0)
				{
					Uri peopleUri = ContentUris.withAppendedId(uri,rowId);
					getContext().getContentResolver().notifyChange(peopleUri, null);
					return peopleUri;
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown Uri:"+uri);
		}
		return null;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase tempDb = dbHelper.getReadableDatabase();
		switch(matcher.match(uri))
		{
			case WEIBOINLIST:
			{
				return tempDb.query("weibo",projection,selection,selectionArgs,null,null,sortOrder);
			}
			case WEIBOSINGLE:
			{
				long id = ContentUris.parseId(uri);
				String selectionClause = WeiboData.Weibo._ID + "=" + id;
				if(selection != null && !"".equals(selection))
				{
					selectionClause = selection + " and " + selectionClause;
				}
				return tempDb.query("weibo",projection,selectionClause,selectionArgs,null,null,sortOrder);
			}
			default:
				throw new IllegalArgumentException("Unknown Uri:"+uri);
		}
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase tempDb = dbHelper.getReadableDatabase();
		int num = 0;
		switch(matcher.match(uri))
		{
			case WEIBOINLIST:
			{
				num = tempDb.update("weibo", values, selection, selectionArgs);
				break;
			}
			case WEIBOSINGLE:
			{
				long id = ContentUris.parseId(uri);
				String selectionClause = WeiboData.Weibo._ID + "=" + id;
				if(selection != null && !"".equals(selection))
				{
					selectionClause = selectionClause + " and " + selection; 
				}
				num = tempDb.update("weibo",values,selectionClause,selectionArgs);
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown Uri:"+uri);
		}
		getContext().getContentResolver().notifyChange(uri,null);
		return num;
	}
	
}
