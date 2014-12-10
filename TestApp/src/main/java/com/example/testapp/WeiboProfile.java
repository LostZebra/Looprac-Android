package com.example.testapp;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WeiboProfile extends Activity{
	ActionBar actionBar;
	WebView weiboPage;
	public void onCreate(Bundle onSavedInstance)
	{
		super.onCreate(onSavedInstance);
		setContentView(R.layout.weiboprofilepage);
		weiboPage = (WebView)findViewById(R.id.weibopage);
		weiboPage.getSettings().setJavaScriptEnabled(true);
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		String profileUrl = getIntent().getStringExtra("weibolink");
		if(profileUrl != null)
			profileUrl = "http://weibo.cn/"+profileUrl;
		weiboPage.loadUrl(profileUrl);
		//Open Url with Own Application
		weiboPage.setWebViewClient(new WebViewClient() {
		    @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        view.loadUrl(url);
		        return false;
		    }
		});
	}
	
	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater actionBarMenu = getMenuInflater();
		actionBarMenu.inflate(R.menu.webmenu,menu);
		return super.onCreateOptionsMenu(menu);
	}
	

	//Refresh or Delete Contact
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch(menuItem.getItemId())
		{
			case R.id.action_back:
			{
				weiboPage.goBack();
				return true;
			}
			case R.id.action_next:
			{
				weiboPage.goForward();
				return true;
			}
		    default:
		    	return super.onOptionsItemSelected(menuItem);
		}
	}
}
