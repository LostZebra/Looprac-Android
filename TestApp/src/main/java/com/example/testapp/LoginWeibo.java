package com.example.testapp;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.util.AccessTokenKeeper;

public class LoginWeibo extends Activity{
	Button loginButton;
	ActionBar actionBar;
	private Oauth2AccessToken myAppAccessToken;
	
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		setContentView(R.layout.loginweibo);
		//ActionBar Settings
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		//Login Button
		loginButton = (Button)findViewById(R.id.login);
		loginButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				loginProcess();
			}			
		});
	}
	public void loginProcess()
	{
		myAppAccessToken = AccessTokenKeeper.readAccessToken(this);
		//Login Operation
		Weibo mWeibo = Weibo.getInstance(ConstantS.APP_KEY, ConstantS.REDIRECT_URL, ConstantS.SCOPE);
		mWeibo.anthorize(LoginWeibo.this, new AutheriazeDialogListener());
	}
	public class AutheriazeDialogListener implements WeiboAuthListener
	{
		@Override
		public void onCancel() {
			Toast cancelToast = Toast.makeText(LoginWeibo.this,"Cancel login",Toast.LENGTH_SHORT);
			cancelToast.show();
		}
	    //When login is successful
		@Override
		public void onComplete(Bundle arg0) {
			String token = arg0.getString("access_token");
			String expiresTime = arg0.getString("expires_in");
			if(token == null)
			{
				Toast confirmToastShort = Toast.makeText(LoginWeibo.this,"No Valid Token",Toast.LENGTH_SHORT);
				confirmToastShort.show();
			}
			myAppAccessToken = new Oauth2AccessToken(token,expiresTime);
			if(myAppAccessToken.isSessionValid()){
				Toast confirmToast = Toast.makeText(LoginWeibo.this,"Authorize Success!",Toast.LENGTH_SHORT);
				confirmToast.show();
				//Store Weibo Cookie
				AccessTokenKeeper.keepAccessToken(LoginWeibo.this,myAppAccessToken);
				finish();
			}		
		}
	
		@Override
		public void onError(WeiboDialogError arg0) {
			Toast errorToast = Toast.makeText(LoginWeibo.this,"Error Login",Toast.LENGTH_SHORT);
			errorToast.show();
		}
	
		@Override
		public void onWeiboException(WeiboException arg0) {
			Toast exceptionToast = Toast.makeText(LoginWeibo.this, "Some Exceptions Occur", Toast.LENGTH_SHORT);
			exceptionToast.show();
		}
	}
}
