package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity{
	
	boolean loginSuccess = true;
	
	//Main activity
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		setContentView(R.layout.main);
		loginSuccess = checkForCookie();
		if(!loginSuccess)
			loginForService();
		else {
			loadContact();
		}
	}
	
	//Check for UserInfo Cookie
	public boolean checkForCookie()
	{
		UserInfo userInfo = new UserInfo(this);
		String[] userNameAndPassword = userInfo.readFromPreference();
		if(userNameAndPassword[0] == null || userNameAndPassword[1] == null)
			return false;
		else 
			return true;
	}
	
	//Load Login Interface
	public void loginForService()
	{
		Intent loginIntent = new Intent(StartActivity.this,LoginService.class);
		startActivity(loginIntent);
	}
	//If Already Login, Load Contact List Interface Directly
	public void loadContact()
	{
		Intent loginService = new Intent(StartActivity.this,BriefView.class);
		startActivity(loginService);
	}
}
	