package com.example.testapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Spinner;

public class AddNewContact extends Activity{
	private ContentResolver contactResolver;
	ActionBar actionBar;
	EditText lastNameText;
	EditText firstNameText;
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		setContentView(R.layout.addnewcontact);
		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		contactResolver = getContentResolver();
		lastNameText = (EditText) findViewById(R.id.lastname);
		firstNameText = (EditText) findViewById(R.id.firstname);
	}
	
	private void preProcessContactInfo()
	{
		Person newPerson = new Person();
		String lastName = lastNameText.getText().toString();
		String firstName = firstNameText.getText().toString();
		if(lastName.length() != 0 || firstName.length() != 0)
		{
			newPerson.changeName(firstName+" "+lastName);
			String weiboScreenName = ((EditText) findViewById(R.id.weiboid)).getText().toString();
			//If Weibo Screen Name is not Empty
			if(weiboScreenName.length() != 0)
			{
				newPerson.changeWeiboScreenName(weiboScreenName);
			}
			else
				newPerson.changeWeiboScreenName("");
			//WeiboId is 0 for New Contact
			newPerson.changeWeiboId(0);
			String newPhone1 = ((EditText) findViewById(R.id.newphone1)).getText().toString();
			String newPhone2 = ((EditText) findViewById(R.id.newphone2)).getText().toString();
			String newPhone3 = ((EditText) findViewById(R.id.newphone3)).getText().toString();
			String newPhone4 = ((EditText) findViewById(R.id.newphone4)).getText().toString();
			if(newPhone1.length() != 0)
			{
				newPerson = addNewPhone(newPhone1,((Spinner) findViewById(R.id.newphonetype1)).getSelectedItem().toString(),newPerson);
			}
			if(newPhone2.length() != 0)
			{
				newPerson = addNewPhone(newPhone1,((Spinner) findViewById(R.id.newphonetype2)).getSelectedItem().toString(),newPerson);
			}
			if(newPhone3.length() != 0)
			{
				newPerson = addNewPhone(newPhone1,((Spinner) findViewById(R.id.newphonetype3)).getSelectedItem().toString(),newPerson);
			}
			if(newPhone4.length() != 0)
			{
				newPerson = addNewPhone(newPhone1,((Spinner) findViewById(R.id.newphonetype4)).getSelectedItem().toString(),newPerson);
			}
			ContactData contactData = new ContactData(contactResolver);
			contactData.addNewContactToSysContact(newPerson);
		}
	}
	
	private Person addNewPhone(String phoneNumber, String phoneType, Person newPerson)
	{
		if(phoneType.equals("MOBILE"))
		{
			newPerson.addToMobilePhonesList(phoneNumber);
		}
		else if(phoneType.equals("WORK"))
		{
			newPerson.addToWorkPhonesList(phoneNumber);
		}
		else if(phoneType.equals("HOME"))
		{
			newPerson.addToHomePhonesList(phoneNumber);
		}
		else {
			newPerson.addToOtherPhonesList(phoneNumber);
		}
		return newPerson;
	}
	
	//AsyncTask to sync change with system
	class OnChangeTask extends AsyncTask<Object,Integer,String>
	{
		Context asyncContext;
		public OnChangeTask(Context context)
		{
			asyncContext = context;
		}

		@Override
		protected String doInBackground(Object... params) {
			preProcessContactInfo();
			return null;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			Toast.makeText(asyncContext,"Contact Saved!",Toast.LENGTH_SHORT).show();
		}
	}
	
	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater actionBarMenu = getMenuInflater();
		actionBarMenu.inflate(R.menu.voidmenu,menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	//Save Contact and Go Back
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch(menuItem.getItemId())
		{
			case android.R.id.home:
			{
			    if(firstNameText.getText().length() == 0 || lastNameText.getText().length() == 0)
			    {
			    	Toast.makeText(AddNewContact.this, "Key Information Missing", Toast.LENGTH_SHORT).show();
			    }
			    else
			    {
					OnChangeTask onChangeTask = new OnChangeTask(AddNewContact.this);
					onChangeTask.execute();
					Intent back = new Intent(AddNewContact.this,BriefView.class);
					startActivity(back);
			    }
				return true;
			}
			case R.id.action_cancel:
				finish();
		    default:
		    	return super.onOptionsItemSelected(menuItem);
		}
	}
}
