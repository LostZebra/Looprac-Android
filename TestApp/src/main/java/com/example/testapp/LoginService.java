package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginService extends Activity{
	//UI Components
	Button loginButton = null;
	Button cancelLogin = null;
	EditText userNameText = null;
	EditText passwordText = null;
	EditText repasswordText = null;
	TextView registerTextView = null;
	//Application Specific
	boolean registerFlag = false;
	@Override
	public void onCreate(Bundle onSavedInstance)
	{
		super.onCreate(onSavedInstance);
		setContentView(R.layout.loginwebservice);
		loginButton = (Button) findViewById(R.id.loginwebservice);
		cancelLogin = (Button) findViewById(R.id.cancellogin);
		userNameText = (EditText) findViewById(R.id.username);
		passwordText = (EditText) findViewById(R.id.password);
		repasswordText = (EditText) findViewById(R.id.repassword);
		registerTextView = (TextView) findViewById(R.id.registerfornew);
		loginButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if(!registerFlag)
				{
					String userName = userNameText.getText().toString();
					String password = passwordText.getText().toString();
					if(userName.length() == 0 || password.length() == 0)
					{
						Toast.makeText(LoginService.this,"Username or Password is empty!",Toast.LENGTH_SHORT).show();
					}
					else {
						//if Login Succeed
						if(login(userName,password))
						{
							//Write to SharedPreferences
							UserInfo userInfo = new UserInfo(userName,password,LoginService.this);
							userInfo.writeToPreference();
							//Go to BriefView
							Intent contactList = new Intent(LoginService.this,BriefView.class);
							startActivity(contactList);
						}
					}
				}
			}});
		cancelLogin.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				userNameText.setText("");
				passwordText.setText("");
				if(registerFlag)
					repasswordText.setText("");
			}
		});
		registerTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!registerFlag)
				{
					registerFlag = true;
					repasswordText.setVisibility(View.VISIBLE);
					loginButton.setText("Register");
				}
				else
				{
					registerFlag = false;
					repasswordText.setVisibility(View.GONE);
					loginButton.setText("Login");
				}
			}
		});
	}
	
	//Login the Server
	public boolean login(String username, String password)
	{
		String loginResult = queryServer(username,password);
		System.out.println(loginResult);
		if (loginResult.equals("Failed"))
			return false; 
		else if(loginResult.equals("Succeed"))
			return true;
		else return false;
	}
	
	//Connect to Server
	public String queryServer(String username,String password)
	{
        /*
		//Query Server for Result
		String request = ConstantS.BASS_URL + "api/servlet/database?username="
				+ username + "&password=" + password;
		IConnectServer connectServer = new HttpUtil();
		return connectServer.queryStringForGet(request);
		*/
        return "Succeed";
	}	
}
