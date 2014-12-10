package com.example.testapp;

import java.util.HashMap;
import java.util.Map;

public interface ConstantS {
	//App key
	public static final String APP_KEY = "1343376046";	
	//Callback url
	public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
	//App access limitation
	public static final String SCOPE = 
            "direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read";
	//public static final String APP_SECRET = "fc117d30595d1d1ea0917d4db8729041";
	//Phone Type
	public static final int MOBILE = 0;
	public static final int WORK = 1;
	public static final int HOME = 2;
	public static final int OTHER = 3;
	public static final int ADD = 4;
	public static final int DELETE = 5;
	public static final int ADDWEIBO = 6;
	public static final int UPDATEWEIBO = 7;
	public static final int DELETEWEIBO = 8;
	public static final int FIRST = 9;
	public static final int EXISTED = 10;
	public static final int POST_TASK = 11;
	public static final int GET_TASK = 12;
	
	// connection timeout, in milliseconds (waiting to connect)
	public static final int CONN_TIMEOUT = 3000;

	// socket timeout, in milliseconds (waiting for data)
	public static final int SOCKET_TIMEOUT = 5000;
	
	//Upload URL
	public static final String SERVICE_URL_UPLOAD = "http://10.0.2.2:8080/AndroidLogin_Server/api/upload";
	//Download URL
	public static final String SERVICE_URL_DOWNLOAD = "http://10.0.2.2:8080/AndroidLogin_Server/api/download";
	
	public static final String TAG = "WebServiceTask";
	public static final String BASS_URL = "http://10.0.2.2:8080/AndroidLogin_Server/";
	//Month Map
	@SuppressWarnings("serial")
	public static final Map<String,String> MONTHMAP = new HashMap<String,String>()
	{
		{
			put("Jan","01");put("Feb","02");
			put("Mar","03");put("Apr","04");
			put("May","05");put("Jun","06");
			put("Jul","07");put("Aug","08");
			put("Sep","09");put("Oct","10");
			put("Nov","11");put("Dec","12");
		}
	};
}
