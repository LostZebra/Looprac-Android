package com.example.testapp;

import java.util.ArrayList;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.util.AccessTokenKeeper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BriefView extends Activity{
	ListView contactListView;
	
	private BaseAdapter contactListAdapter = null;
	
	private Cursor contactListCursor;
	private ContentResolver contactResolver;
	private IManageLocalData manageLocalData;
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
		//Load basic interface
		super.onCreate(savedInstance);
		setContentView(R.layout.contactlist);
		if(!hasContactInfo())
		{
			AlertDialog.Builder noContactInfoAlert = new AlertDialog.Builder(this).setTitle("Notice")
					.setMessage("No Contact Information Found!")
					.setNegativeButton("OK, I Know", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}});
			noContactInfoAlert.create().show();
		}
		else
		{
			//Read Sys Contact Data and Load Default User Interface
			manageLocalData = new ContactData(contactResolver);				
			((ContactData) manageLocalData).getContactFromSystem();
			contactListView = (ListView)findViewById(R.id.contactlist);
			contactListAdapter = setUpDefaultUserInterface();
			contactListView.setAdapter(contactListAdapter);
		}	
		//Response to System Contact Change
		contactResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,new ContactsObserver(new Handler()));
		//Response to Local Weibo Contact Change
		contactResolver.registerContentObserver(WeiboData.Weibo.WEIBO_LIST_URI,true,new WeiboContentObserver(new Handler()));
	}
	
	//Check If Sys Contact Exists
	public boolean hasContactInfo()
	{
		contactResolver = getContentResolver();
		contactListCursor = contactResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if(contactListCursor.getCount() == 0)
			return false;
		else return true;
	}
	
	private BaseAdapter setUpDefaultUserInterface()
	{
		//Infomation
		BaseAdapter tempContactListAdapter = new BaseAdapter()
		{	
			LinearLayout briefViewEntry;
			LayoutInflater inflater = (LayoutInflater)BriefView.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			ArrayList<Person> localContactList = manageLocalData.getContactList();
			String name;
			
			@Override
			public int getCount() {
				return localContactList.size();
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				briefViewEntry = (LinearLayout)inflater.inflate(R.layout.briefviewentry,null);
				if(position == 0 || localContactList.get(position).getName().charAt(0) != localContactList.get(position-1).getName().charAt(0))
				{
					TextView letterlabel = (TextView) briefViewEntry.getChildAt(0).findViewById(R.id.letterlabel);
					letterlabel.setVisibility(0);
					letterlabel.setText(Character.toString(localContactList.get(position).getName().charAt(0)));
				}
				setBriefViewEntry(briefViewEntry,position);
				setBriefEntryListener(briefViewEntry,position);
				return briefViewEntry;
			}
			
			//Set Entry Content
			public void setBriefViewEntry(LinearLayout briefViewEntry, int position)
			{
				//Set Photo ID
				//If There Is No Weibo Photo, then Use System Default Photo
				byte[] photoBytes = localContactList.get(position).getWeiboThumPic();
				if(photoBytes != null)
				{
					Bitmap thumPhoto = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
					if(thumPhoto != null)
					{
						ImageView photoView = (ImageView)((LinearLayout) briefViewEntry.getChildAt(1)).getChildAt(0).findViewById(R.id.contactimage);
						photoView.setImageBitmap(thumPhoto);
						photoView.setScaleType(ImageView.ScaleType.FIT_XY);
					}
				}
				//Name
				name = localContactList.get(position).getName();
				TextView nameText = (TextView) ((LinearLayout) briefViewEntry.getChildAt(1)).getChildAt(1).findViewById(R.id.contactname);
				nameText.setText(name);
			}
			
			//Click on the item and go to the editing interface
			public void setBriefEntryListener(LinearLayout briefViewEntry, int position)
			{
				final int index = position;
				briefViewEntry.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						Bundle contactData = new Bundle();
						contactData.putSerializable("contact",localContactList.get(index));
						Intent contactDetail = new Intent(BriefView.this,ContactDetail.class);
						contactDetail.putExtras(contactData);
						startActivity(contactDetail);
					}});
			}
		};
		return tempContactListAdapter;
	}
	
	//Observer for Sys Contact Change
	private final class ContactsObserver extends ContentObserver
	{
		public ContactsObserver(Handler changeHandler)
		{
			super(changeHandler);
		}
		//Refresh contact list
		public void onChange(boolean selfChange)
		{
			contactListCursor = contactResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if(contactListCursor.getCount() == 0)
			{
				setContentView(R.layout.contactlist);
			}
			else {
				manageLocalData = new ContactData(contactResolver);
				((ContactData) manageLocalData).getContactFromSystem();
				if(manageLocalData.getContactList().size() != 0)
				{
					contactListView = (ListView)findViewById(R.id.contactlist);
					contactListAdapter = setUpDefaultUserInterface();
					contactListView.setAdapter(contactListAdapter);
				}
			}
		}
	}
	
	//Observer for Sys Weibo Data Change
	private final class WeiboContentObserver extends ContentObserver
	{
		public WeiboContentObserver(Handler changeHandler)
		{
			super(changeHandler);
		}
		//Refresh contact list
		public void onChange(boolean selfChange)
		{
			contactListCursor = contactResolver.query(WeiboData.Weibo.WEIBO_LIST_URI, null, null, null, null);
			manageLocalData = new ContactData(contactResolver);
			((ContactData) manageLocalData).getContactFromSystem();
			if(manageLocalData.getContactList().size() != 0)
			{
				contactListView = (ListView)findViewById(R.id.contactlist);
				contactListAdapter = setUpDefaultUserInterface();
				contactListView.setAdapter(contactListAdapter);
			}
		}
	}
	
	//Create menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater optionsMenu = getMenuInflater();
		optionsMenu.inflate(R.menu.briefviewmenu,menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	//Create onClick response to those menu
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch(menuItem.getItemId())
		{
			case R.id.action_new:
			{
				Intent addNewContact = new Intent(BriefView.this,AddNewContact.class);
				startActivity(addNewContact);
				return true;
			}
			case R.id.action_refresh:
			{
				manageLocalData = new ContactData(contactResolver);
				((ContactData) manageLocalData).getContactFromSystem();
				if(manageLocalData.getContactList().size() != 0)
				{
					contactListView = (ListView)findViewById(R.id.contactlist);
					contactListAdapter = setUpDefaultUserInterface();
					contactListView.setAdapter(contactListAdapter);
				}
				return true;
			}
			case R.id.action_backup:
			{
				manageLocalData = new ContactData(contactResolver);
				((ContactData) manageLocalData).getContactFromSystem();
				int size = manageLocalData.getContactList().size();
				Bundle intBundle = new Bundle();
				intBundle.putInt("size",size);
				Intent backupAndRestoreIntent = new Intent(BriefView.this,BackupAndRestore.class);
				backupAndRestoreIntent.putExtras(intBundle);
				startActivity(backupAndRestoreIntent);
				return true;
			}
			case R.id.syncweibo:
			{
				if(!checkForWeiboCookie())
				{
					Intent loginWeibo = new Intent(BriefView.this,LoginWeibo.class);
					startActivity(loginWeibo);
				}
				else 
					Toast.makeText(BriefView.this,"Already in sync",Toast.LENGTH_SHORT).show();		
				return true;
			}
			default:
				return true;
		}
	}
	
	public boolean checkForWeiboCookie()
	{
		Oauth2AccessToken myAppAccessToken;
		myAppAccessToken = AccessTokenKeeper.readAccessToken(this);
		return myAppAccessToken.isSessionValid();
	}
}
