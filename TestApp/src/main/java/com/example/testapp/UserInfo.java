package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class UserInfo extends Activity{
	private SharedPreferences userInfoPreference;
	private SharedPreferences.Editor userInfoPreferenceEditor;
	private String userName;
	private String password;
	
	public UserInfo(String userName, String password, Context appContext)
	{
		this.userName = userName;
		this.password = password;
		userInfoPreference = appContext.getSharedPreferences("testapp",1);
		userInfoPreferenceEditor = userInfoPreference.edit();
		userInfoPreferenceEditor.commit();
	}
	public UserInfo(Context appContext)
	{
		userInfoPreference = appContext.getSharedPreferences("testapp",1);
		userInfoPreferenceEditor = userInfoPreference.edit();
		userInfoPreferenceEditor.commit();
	}
	
	public void writeToPreference()
	{
		userInfoPreferenceEditor.putString("username",this.userName);
		userInfoPreferenceEditor.putString("password",this.password);
		userInfoPreferenceEditor.commit();
	}

	public String[] readFromPreference()
	{
		String[] userNameAndPassword = {null,null};
		userNameAndPassword[0] = userInfoPreference.getString("username",null);
		userNameAndPassword[1] = userInfoPreference.getString("password",null);
		return userNameAndPassword;
	}
}
