package com.example.testapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.net.HttpManager;
import com.weibo.sdk.android.util.AccessTokenKeeper;

import android.app.ActionBar;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

public class ContactDetail extends Activity{
	//Application Specific Flag
	public static int count = 1;
	private boolean stop = false;
	//UI Components
	private ActionBar actionBar;
	
	//Weibo UI
	private TextView weiboScreenNameText;
	private TextView weiboContentText;
	private TextView weiboTimeText;
	private ImageView weiboPic;
	private ProgressBar weiboProgress;
	private Button gotoWeiboProfilePage;
	
	//Contact UI
	private TextView name;
	
	private TextView mobilePhoneLabel;
	private TextView workPhoneLabel;
	private TextView homePhoneLabel;
	private TextView otherPhoneLabel;
	
	private Spinner mobilePhonesSpinner;
	private Spinner workPhonesSpinner;
	private Spinner homePhonesSpinner;
	private Spinner otherPhonesSpinner;
	
	private Button moreOpsForMobile;
	private Button moreOpsForWork;
	private Button moreOpsForHome;
	private Button moreOpsForOther;
	
	private Button addNewNum;
	
	//Contact Information
	private Person p;
	private static Person savedPerson;
	private ArrayList<String> mobilePhonesList;
	private ArrayList<String> workPhonesList;
	private ArrayList<String> homePhonesList;
	private ArrayList<String> otherPhonesList;
	private ArrayAdapter<String> arrayAdapter;
	
	//Weibo Information
	private String access_token = null;
	private WeiboParameters weiboParameters = null;
	private Oauth2AccessToken myAppAccessToken = null;
	
	//System
	private ContentResolver contactListResolver;
	private Cursor contactListCursor;
	private Intent parentIntent;
	
	//Interface
	IManageLocalData manageLocalData;
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
		//Load Basic Interface
		super.onCreate(savedInstance);
		setContentView(R.layout.contactdetail);
		organizeView();
		//Enable Main Thread Network Connection
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		/*
		//Get Data from Upper Activity
		myAppAccessToken = AccessTokenKeeper.readAccessToken(this);
		//Check If Token is still Valid
		if(myAppAccessToken.isSessionValid())
			access_token = myAppAccessToken.getToken();	
		//Register to System Contact Change
		contactListResolver = getContentResolver();
		contactListResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,new ContactsObserver(new Handler()));
		manageLocalData = new ContactData(contactListResolver);
		//Get Contact Data and Fill the Content
		parentIntent = getIntent();
		p = (Person)parentIntent.getSerializableExtra("contact");
		defaultInterfaceContent(p);
		*/
	}
	
	//Activity OnStart
	@Override
	public void onStart()
	{
		super.onStart();
		//Get Data from Upper Activity
		myAppAccessToken = AccessTokenKeeper.readAccessToken(this);
		//Check If Token is still Valid
		if(myAppAccessToken.isSessionValid())
			access_token = myAppAccessToken.getToken();	
		//Register to System Contact Change
		contactListResolver = getContentResolver();
		contactListResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,new ContactsObserver(new Handler()));
		manageLocalData = new ContactData(contactListResolver);
		//Get Contact Data and Fill the Content
		if(count == 1)
		{
			parentIntent = getIntent();
			p = (Person)parentIntent.getSerializableExtra("contact");
			defaultInterfaceContent(p,false);
		}
		else
		{
			defaultInterfaceContent(savedPerson,true);
			count = 1;
		}
	}
	
	public void organizeView()
	{
		//ActionBar Settings
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		
		//Load Weibo UI
		weiboProgress = (ProgressBar)findViewById(R.id.weiboprogress);
		weiboScreenNameText = (TextView)findViewById(R.id.weibolink);
		weiboTimeText = (TextView)findViewById(R.id.weibotime);
		weiboContentText = (TextView)findViewById(R.id.weibocontent);
		weiboPic = (ImageView)findViewById(R.id.weibopic);
		gotoWeiboProfilePage = (Button)findViewById(R.id.gotoweiboprofilepage);
		
		//Loading Contact UI Components
		name = (TextView)findViewById(R.id.contact_name_second);
		mobilePhoneLabel = (TextView)findViewById(R.id.mobilephonelabel);
		workPhoneLabel = (TextView)findViewById(R.id.workphonelabel);
		homePhoneLabel = (TextView)findViewById(R.id.homephonelabel);
		otherPhoneLabel = (TextView)findViewById(R.id.otherphonelabel);
		
		mobilePhonesSpinner = (Spinner)findViewById(R.id.mobilephonesspinner);
		workPhonesSpinner = (Spinner)findViewById(R.id.workphonesspinner);
		homePhonesSpinner = (Spinner)findViewById(R.id.homephonesspinner);
		otherPhonesSpinner = (Spinner)findViewById(R.id.otherphonesspinner);
		
		moreOpsForMobile = (Button)findViewById(R.id.moreformobile);
		moreOpsForWork = (Button)findViewById(R.id.moreforwork);
		moreOpsForHome = (Button)findViewById(R.id.moreforhome);
		moreOpsForOther = (Button)findViewById(R.id.moreforother);
		
		addNewNum = (Button)findViewById(R.id.addnewnum);
	}
    
	//Fill the content
	public void defaultInterfaceContent(Person p, boolean backFromAnotherActivityFlag)
	{
		//Weibo Id & Screen Name
		//Get Weibo Id and Screen Name 
		System.out.println(p.getContactId());
		long weiboId = p.getWeiboId();
		String weiboScreenName = p.getWeiboScreenName();
		String weiboLink = p.getWeiboLink();
		
		//If Weibo Id Existed in Local Database
		if(weiboScreenName.length() != 0)
		{
			//If No Internet Access Available
			String internetConnectivity = checkForInternetConnectivity();
			if(internetConnectivity.equals("Disconnected") || backFromAnotherActivityFlag == true)
			{
				String weiboContent = p.getLastWeiboContent();
				String weiboTime = p.getLastWeiboTime();
				byte[] weiboPicByte = p.getWeiboPic();

				//Set Last Weibo Content and Weibo Time Using Local Store Data 
				//WeiboScreenName
				weiboScreenNameText.setText(weiboScreenName);
				//Content				
				weiboContentText.setVisibility(View.VISIBLE);
				weiboContentText.setText(weiboContent);
				//Time
				weiboTimeText.setVisibility(View.VISIBLE);
				weiboTimeText.setText(weiboTime);
				//Pic
				weiboPic.setVisibility(View.VISIBLE);
				weiboPic.setImageBitmap(BitmapFactory.decodeByteArray(weiboPicByte, 0, weiboPicByte.length));
				weiboPic.setScaleType(ImageView.ScaleType.FIT_XY);	
				//Listener
			    setWeiboProfilePageListener(weiboLink);
			}
			//If Internet Access Available
			else {
				weiboScreenNameText.setText(weiboScreenName);
				//Connect to Internent and Refresh Content
				WeiboTask weiboTask = new WeiboTask(ContactDetail.this);
				if(weiboId == 0)
					weiboTask.execute(weiboScreenName,(long)0,ConstantS.FIRST,ConstantS.ADDWEIBO);
				else
					weiboTask.execute(weiboScreenName,weiboId,ConstantS.EXISTED,ConstantS.UPDATEWEIBO);
			}
		}
		else
			//Can't Go to Weibo Profile Page Right Now
			gotoWeiboProfilePage.setEnabled(false);
			
		
		//Name
		name.setText(p.getName());
		
		//Phone info
		mobilePhonesList = p.getMobilePhonesList();
		if(mobilePhonesList.size() != 0)
		{
			changePhoneUI(0,mobilePhonesList);
		}
		workPhonesList = p.getWorkPhonesList();
		if(workPhonesList.size() != 0)
		{
			changePhoneUI(1,workPhonesList);
		}	
		homePhonesList = p.getHomePhonesList();
		if(homePhonesList.size() != 0)
		{
			changePhoneUI(2,homePhonesList);
		}	
		otherPhonesList = p.getOtherPhonesList();
		if(otherPhonesList.size() != 0)
		{
			changePhoneUI(3,otherPhonesList);
		}
		
		//Listener
	    setEditWeiboScreenNameListener(weiboScreenNameText);
		setAddNewNumListener(addNewNum);
	}
	
	//Pump Out Operations (Call, Text, OR Delete)
	public void setMoreOperationsButtonListener(Button button, Spinner phonesList)
	{
		final Spinner tempPhonesList = phonesList;
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				moreOperationsDialog(tempPhonesList.getSelectedItem().toString(),v.getId());
			}		
		});
	}
	
	//Set Weibo Id Editing Dialog Listener
	public void setEditWeiboScreenNameListener(TextView weiboScreenNameText)
	{
		final TextView tempWeiboScreenNameText = weiboScreenNameText;
		weiboScreenNameText.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String tempLink = tempWeiboScreenNameText.getText().toString();
				if(tempLink.length() == 0)
				{
					loadIdEditDialog(null);
				}
				else {
					loadIdEditDialog(tempLink);
				}
			}
		});
	}
	
	//Go to Certain Contact's Weibo Profile Page
	public void setWeiboProfilePageListener(String weiboLink)
	{
		final String weiboLinkFinal = weiboLink;
		//Able to Go to Weibo Profile Page Again
		gotoWeiboProfilePage.setEnabled(true);
		//OnClick Listener
		gotoWeiboProfilePage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle profileUrl = new Bundle();
				profileUrl.putString("weibolink",weiboLinkFinal);
				Intent profilePageIntent = new Intent(ContactDetail.this,WeiboProfile.class);
				profilePageIntent.putExtras(profileUrl);
				startActivity(profilePageIntent);
			}
		});
	}
	
	//Pump out Weibo Editing Dialog
	public void loadIdEditDialog(String weiboScreenName)
	{
		final String weiboScreenNameFinal = weiboScreenName;
		//Set Text for the Weibo TextView Zone in the Dialog
		LinearLayout temp = new LinearLayout(ContactDetail.this);
		temp = (LinearLayout)getLayoutInflater().inflate(R.layout.editweiboid,null);
		final EditText editTextFinal = (EditText) temp.getChildAt(1);
		editTextFinal.setText(weiboScreenName);
		//New Dialog for Weibo Id Editing
		AlertDialog.Builder addNewWeiboDialog = new AlertDialog.Builder(ContactDetail.this)
		.setTitle("Weibo")
	    .setView(temp)
	    .setIcon(R.drawable.ic_action_add_person)
	    .setPositiveButton("Add",new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String editResult = editTextFinal.getText().toString();
				//New Weibo Async Task
				WeiboTask weiboTask = new WeiboTask(ContactDetail.this);
				if(editResult.length() != 0)
				{							
					//UI Refresh
					weiboScreenNameText.setText(editResult);
					//Background Sync Sith System
					if(weiboScreenNameFinal == null)
					{
						//No Weibo Id Yet, Add a New One
						weiboTask.execute(editResult,(long)0,ConstantS.FIRST,ConstantS.ADDWEIBO);
					}
					else if(!editResult.equals(weiboScreenNameFinal))
					{
						//Change Existed Weibo Id
						weiboTask.execute(editResult,(long)0,ConstantS.FIRST,ConstantS.UPDATEWEIBO);
					}
				}
				else if(weiboScreenNameFinal != null) {
					//Delete the Existed Weibo Account
					weiboScreenNameText.setText("Ops, No Account Yet");
					weiboTask.execute(editResult,(long)0,ConstantS.FIRST,ConstantS.DELETEWEIBO);
				}
			}
	    })
	    .setNegativeButton("Cancel",new DialogInterface.OnClickListener()
	    {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Do Nothing						
			}
	    });
	    addNewWeiboDialog.show();
	}
	
	//More Operaions Dialog
	public void moreOperationsDialog(String phoneNumber,int id)
	{
		final int idFinal = id;
		final String phoneNumberFinal = phoneNumber;
		final String[] callAndText = {"Call to "+phoneNumberFinal,"Text to "+phoneNumberFinal,"Delete"};
		AlertDialog.Builder callOrText = new AlertDialog.Builder(ContactDetail.this)
		.setTitle("More...")
		.setIcon(R.drawable.ic_action_call)
		.setAdapter(new ArrayAdapter<String>(ContactDetail.this,R.layout.callandtext,callAndText),new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0)
				{
					Uri callToUri = Uri.parse("tel" +
							":"+phoneNumberFinal);   
					Intent callIntent = new Intent(Intent.ACTION_DIAL, callToUri);
					startActivity(callIntent);  
				}
				else if (which == 1)
				{
					Uri textToUri = Uri.parse("smsto:"+phoneNumberFinal);    
	                Intent textIntent = new Intent(Intent.ACTION_SENDTO,textToUri);
	                startActivity(textIntent);    
				}
				else {
					deletePhoneItemFromList(idFinal,phoneNumberFinal);
				}
			}});
		callOrText.create().show();
	}
	
	public void setAddNewNumListener(Button addNewNum)
	{
		addNewNum.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				LinearLayout addNew = new LinearLayout(ContactDetail.this);
				addNew = (LinearLayout)getLayoutInflater().inflate(R.layout.addnewnum,null);
				final Spinner phoneSpinner = ((Spinner)addNew.getChildAt(1));
				final EditText phoneEditText = ((EditText)addNew.getChildAt(0));
				AlertDialog.Builder addNewNumDialog = new AlertDialog.Builder(ContactDetail.this)
				.setTitle("New")
			    .setView(addNew)
			    .setIcon(R.drawable.ic_action_edit)
			    .setPositiveButton("Add",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String phoneType = phoneSpinner.getSelectedItem().toString();
						String phoneNum = phoneEditText.getText().toString();
						if(phoneNum.length() != 0)
						{							
							if(phoneType.equals("MOBILE"))
							{
								//UI Refresh
								p.addToMobilePhonesList(phoneNum);
								mobilePhonesList = p.getMobilePhonesList();
								changePhoneUI(0,mobilePhonesList);
								//Background Sync Sith System
								OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
								changeTask.execute(p.getContactId(),phoneNum,ConstantS.MOBILE,ConstantS.ADD);
							}
							else if(phoneType.equals("WORK"))
							{
								//UI Refresh
								p.addToWorkPhonesList(phoneNum);
								workPhonesList = p.getWorkPhonesList();
								changePhoneUI(1,workPhonesList);
								//Background Sync With System
								OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
								changeTask.execute(p.getContactId(),phoneNum,ConstantS.WORK,ConstantS.ADD);
							}
							else if(phoneType.equals("HOME"))
							{
								//UI Refresh
								p.addToHomePhonesList(phoneNum);
								homePhonesList = p.getHomePhonesList();
								changePhoneUI(2,homePhonesList);
								//Background Sync With System
								OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
								changeTask.execute(p.getContactId(),phoneNum,ConstantS.HOME,ConstantS.ADD);
							}
							else {
								//UI Refresh
								p.addToOtherPhonesList(phoneNum);
								otherPhonesList = p.getOtherPhonesList();
								changePhoneUI(3,otherPhonesList);
								//Background Sync With System
								OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
								changeTask.execute(p.getContactId(),phoneNum,ConstantS.OTHER,ConstantS.ADD);
							}
						}
					}})
			    .setNegativeButton("Cancel",new DialogInterface.OnClickListener()
			    {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Do Nothing						
					}
			    });
			    addNewNumDialog.show();
			}});
		
	}
	
	//Delete items
	public void deletePhoneItemFromList(int id, String phoneNumber)
	{
		switch(id)
		{
			case R.id.moreformobile:
			{
				//UI Refresh
				Long phoneId = p.getMobilePhonesIdList().get(p.getMobilePhonesList().indexOf(phoneNumber));
				p.removeFromMobilePhonesList(phoneNumber);
				p.removeFromMobilePhonesIdList(phoneId);
				mobilePhonesList = p.getMobilePhonesList();
				changePhoneUI(0,mobilePhonesList);
				//Background Sync With System
				OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
				changeTask.execute(p.getContactId(),phoneId.toString(),ConstantS.MOBILE,ConstantS.DELETE);
				break;
			}
			case R.id.moreforwork:
			{
				//UI Refresh
				p.removeFromWorkPhonesList(phoneNumber);
				workPhonesList = p.getWorkPhonesList();
				changePhoneUI(1,workPhonesList);
				//Background Sync With System
				OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
				changeTask.execute(p.getContactId(),phoneNumber,ConstantS.WORK,ConstantS.DELETE);
				break;
			}
			case R.id.moreforhome:
			{
				//UI Refresh
				p.removeFromHomePhonesList(phoneNumber);
				homePhonesList = p.getHomePhonesList();
                changePhoneUI(2,homePhonesList);
				//Background Sync With System
				OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
				changeTask.execute(p.getContactId(),phoneNumber,ConstantS.HOME,ConstantS.DELETE);
				break;
			}
			case R.id.moreforother:
			{
				//UI Refresh
				p.removeFromOtherPhonesList(phoneNumber);
				otherPhonesList = p.getOtherPhonesList();
				changePhoneUI(3,otherPhonesList);
				//Background Sync With System
				OnChangeTask changeTask = new OnChangeTask(ContactDetail.this);
				changeTask.execute(p.getContactId(),phoneNumber,ConstantS.OTHER,ConstantS.DELETE);
				break;
			}
		}
	}
	
	//Change Phone List UI
	public void changePhoneUI(int type, ArrayList<String> phonesList)
	{
		switch (type) {
			case ConstantS.MOBILE:
			{		
				if(phonesList.size() >= 1)
				{
					mobilePhonesSpinner.setVisibility(View.VISIBLE);
					mobilePhoneLabel.setVisibility(View.VISIBLE);
					moreOpsForMobile.setVisibility(View.VISIBLE);
					setMoreOperationsButtonListener(moreOpsForMobile,mobilePhonesSpinner);
					arrayAdapter = new ArrayAdapter<String>(ContactDetail.this,android.R.layout.simple_list_item_1,phonesList);
					mobilePhonesSpinner.setAdapter(arrayAdapter);	
				}
				else {
					mobilePhonesSpinner.setVisibility(View.GONE);
					mobilePhoneLabel.setVisibility(View.GONE);
					moreOpsForMobile.setVisibility(View.GONE);
				}
				break;
			}
			case ConstantS.WORK:
			{
				if(phonesList.size() >= 1)
				{
					workPhonesSpinner.setVisibility(View.VISIBLE);
					workPhoneLabel.setVisibility(View.VISIBLE);
					moreOpsForWork.setVisibility(View.VISIBLE);
					setMoreOperationsButtonListener(moreOpsForWork,workPhonesSpinner);
					arrayAdapter = new ArrayAdapter<String>(ContactDetail.this,android.R.layout.simple_list_item_1,phonesList);
					workPhonesSpinner.setAdapter(arrayAdapter);	
				}
				else {
					workPhonesSpinner.setVisibility(View.GONE);
					workPhoneLabel.setVisibility(View.GONE);
					moreOpsForWork.setVisibility(View.GONE);
				}
				break;
			}
			case ConstantS.HOME:
			{
				if(phonesList.size() >= 1)
				{
					homePhonesSpinner.setVisibility(View.VISIBLE);
					homePhoneLabel.setVisibility(View.VISIBLE);
					moreOpsForHome.setVisibility(View.VISIBLE);
					setMoreOperationsButtonListener(moreOpsForHome,homePhonesSpinner);
					arrayAdapter = new ArrayAdapter<String>(ContactDetail.this,android.R.layout.simple_list_item_1,phonesList);
					homePhonesSpinner.setAdapter(arrayAdapter);	
				}
				else {
					homePhonesSpinner.setVisibility(View.GONE);
					homePhoneLabel.setVisibility(View.GONE);
					moreOpsForHome.setVisibility(View.GONE);
				}
				break;
			}
			case ConstantS.OTHER:
			{
				if(phonesList.size() >= 1)
				{
					otherPhonesSpinner.setVisibility(View.VISIBLE);
					otherPhoneLabel.setVisibility(View.VISIBLE);
					moreOpsForOther.setVisibility(View.VISIBLE);
					setMoreOperationsButtonListener(moreOpsForOther,otherPhonesSpinner);
					arrayAdapter = new ArrayAdapter<String>(ContactDetail.this,android.R.layout.simple_list_item_1,otherPhonesList);
					otherPhonesSpinner.setAdapter(arrayAdapter);	
				}
				else {
					otherPhonesSpinner.setVisibility(View.GONE);
					otherPhoneLabel.setVisibility(View.GONE);
					moreOpsForOther.setVisibility(View.GONE);
				}
				break;
			}
		}
	}
	
	//Async Weibo Task
	class WeiboTask extends AsyncTask<Object,Integer,String>
	{
		Context asyncContext;
		int type;
		int isExisted;
		
		//Weibo Data We Care About
		long weiboId = 0;
		String weiboScreenName = null;
		String weiboLink = null;
		String weiboThumPicUrl = null;
		String weiboContent = null;
		String weiboTime = null;
		String weiboPicUrl = null;
		String retweetContent = null;
		String retweetSource = null;
		String retweetPicUrl = null;
		Bitmap tempWeiboPic = null;
		byte[] weiboPicByte = null;
		
		//Constructor
		public WeiboTask(Context context)
		{
			this.asyncContext = context;
		}
		
		//Backgound Task
		@Override
		protected String doInBackground(Object... params) {
			//Get Weibo Data from Input Parameters
			type = (Integer)params[3];	
			isExisted = (Integer)params[2];
			weiboId = (Long)params[1];
			weiboScreenName = (String)params[0];
			//Variables for Internet Access
			String url;
			String result;
			JSONObject obj;
			//Get Weibo Access Token
			if(weiboScreenName != null && stop == false)
			{
				stop = true;
				if(isExisted == ConstantS.FIRST)
				{
					//Call Weibo User Information API Using ScreenName
					url = "https://api.weibo.com/2/users/show.json";  
				    weiboParameters = new WeiboParameters();
				    weiboParameters.add("access_token", access_token);
				    weiboParameters.add("screen_name", weiboScreenName);
				    weiboParameters.add("source",ConstantS.APP_KEY);
				}
				else
				{
					//Call Weibo User Information API Using Weibo Id
					url = "https://api.weibo.com/2/users/show.json";  
				    weiboParameters = new WeiboParameters();
				    weiboParameters.add("access_token", access_token);
				    weiboParameters.add("uid",weiboId);
				    weiboParameters.add("source",ConstantS.APP_KEY);
				}
				try {  
			        result = HttpManager.openUrl(url, HttpManager.HTTPMETHOD_GET, weiboParameters, null);  
			        obj = new JSONObject(result);  
			        //Weibo Data from Internet
			        weiboId = obj.getLong("id");
			        weiboScreenName = (String) obj.get("screen_name");
			        weiboLink = (String) obj.get("profile_url");
			        p.changeWeiboLink(weiboLink);
			        weiboContent = (String) ((JSONObject) obj.get("status")).get("text");
			        //Don't Have a Choice
			        weiboTime = (String) ((JSONObject) obj.get("status")).get("created_at");
			        weiboTime = timeFormatConvert(weiboTime);					
			        weiboPicUrl = (String) ((JSONObject) obj.get("status")).get("bmiddle_pic");
			        weiboThumPicUrl = (String) obj.get("profile_image_url");
			        ContactDetail.this.runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
			                // This code will always run on the UI thread, therefore is safe to modify UI elements.
			            	setWeiboProfilePageListener(weiboLink);
			            	weiboTimeText.setText(weiboTime);
			            	weiboContentText.setText(weiboContent);
			            }
			        });
			        manageLocalData.addNewPhotoToSysContact(p.getContactId(),weiboThumPicUrl);
			    } catch (WeiboException e) {  
			        e.printStackTrace();  
			    } catch (JSONException e) {  
			        e.printStackTrace();  
			    }
				weiboPicByte = getImageThroughUrl(weiboPicUrl);
				tempWeiboPic = BitmapFactory.decodeByteArray(weiboPicByte, 0, weiboPicByte.length);
				switch(type)
				{
					case ConstantS.UPDATEWEIBO:
					{
						syncWithSysWeibo(p.getContactId(),weiboId,weiboScreenName,weiboLink,weiboContent,weiboTime,weiboPicByte,ConstantS.UPDATEWEIBO);
						break;
					}
					case ConstantS.ADDWEIBO:
					{
						syncWithSysWeibo(p.getContactId(),weiboId,weiboScreenName,weiboLink,weiboContent,weiboTime,weiboPicByte,ConstantS.ADDWEIBO);
						break;
					}
					case ConstantS.DELETEWEIBO:
					{
						syncWithSysWeibo(p.getContactId(),weiboId,null,null,null,null,null,ConstantS.DELETEWEIBO);
						break;
					}
					default:
						break;
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			weiboScreenNameText.setText(weiboScreenName);
			weiboContentText.setVisibility(View.VISIBLE);
			weiboTimeText.setVisibility(View.VISIBLE);
			if(weiboPicUrl != null)
			{									
				weiboPic.setVisibility(View.VISIBLE);
				weiboPic.setImageBitmap(tempWeiboPic);
				weiboPic.setScaleType(ImageView.ScaleType.FIT_CENTER);
			}
			weiboProgress.setVisibility(View.GONE);
		}
		
		@Override
		protected void onPreExecute()
		{
			System.out.println("Begin!");
			weiboContentText.setVisibility(View.GONE);
			weiboTimeText.setVisibility(View.GONE);
			weiboPic.setVisibility(View.GONE);
		    weiboProgress.setVisibility(View.VISIBLE);
		}
		
		//Get Image Through Url
		public byte[] getImageThroughUrl(String imageUrlStr)
		{
			URL imageUrl = null;
			Bitmap image = null;
		    try {
		    	imageUrl = new URL(imageUrlStr);
			} catch (MalformedURLException e) {  
		        e.printStackTrace();  
		    }  
		    try {  
		        HttpURLConnection conn = (HttpURLConnection) imageUrl  
		                .openConnection();  
		        conn.setDoInput(true);  
		        conn.connect();  
		        InputStream is = conn.getInputStream();  
		        image = BitmapFactory.decodeStream(is);  
		        is.close();  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }  
			ByteArrayOutputStream stream = new ByteArrayOutputStream();   
			byte[] arrbyte=null;
			if(image!=null)
				image.compress(Bitmap.CompressFormat.JPEG, 75, stream);
			arrbyte=stream.toByteArray();		 
		    return arrbyte;  		
		}
	}
	
	//Convert Time Format 
	public String timeFormatConvert(String originalTimeFormat)
	{
		//Time Zone Sync with China
		TimeZone time = TimeZone.getTimeZone("GMT+8"); 
		System.out.println(time);
		TimeZone.setDefault(time);
		//Get Weibo Time Data and Process it into Standard Format
		int lengthOri = originalTimeFormat.length();
		String newTimeFormat = originalTimeFormat.substring(lengthOri-4,lengthOri)+"-"+"11"+"-"+
				originalTimeFormat.substring(8,10)+" "+originalTimeFormat.substring(11,19);
		String timeNow = Calendar.getInstance().getTime().toString();
		int lengthNow = timeNow.length();
		timeNow = timeNow.substring(lengthNow-4,lengthNow)+"-"+"11"+"-"+
				timeNow.substring(8,10)+" "+timeNow.substring(11,19);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			Date weiboTimeWithFormat = dateFormat.parse(newTimeFormat);
			Date timeNowWithFormat = dateFormat.parse(timeNow);
			long diff = timeNowWithFormat.getTime() - weiboTimeWithFormat.getTime();
			long days = diff / (1000 * 60 * 60 * 24);
			if(days == 0)
			{
				newTimeFormat = "Today "+newTimeFormat.substring(11,16);
			}
			else if(days == 1)
			{
				newTimeFormat = "Yesterday "+newTimeFormat.substring(11,16);
			}
			else if(days <= 10 && days > 2)
			{
				newTimeFormat = days+" ago";
			}
			else {
				newTimeFormat = newTimeFormat.substring(0,newTimeFormat.length()-3);
			}
		}
		catch (Exception e)
		{
			Toast.makeText(ContactDetail.this, "Time Format Wrong", Toast.LENGTH_SHORT).show();
		}
		return newTimeFormat;
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
			syncWithSysContact((String)params[0],(String)params[1],(Integer)params[2],(Integer)params[3]);
			return null;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			Toast.makeText(asyncContext,"Sync done!",Toast.LENGTH_SHORT).show();
		}
	}
	
	public void syncWithSysWeibo(String contactId,long weiboId,String weiboScreenName,String weiboLink,String weiboContent,String weiboTime,byte[] weiboPicByte,int type)
	{
		boolean success = false;
		switch (type) {
			case ConstantS.ADDWEIBO:
				p.changeWeiboId(weiboId);
				p.changeWeiboScreenName(weiboScreenName);
				p.changeWeiboLink(weiboLink);
				p.setWeiboContent(weiboContent);
			    p.setWeiboPic(weiboPicByte);
				p.setWeiboTime(weiboTime);
				success = manageLocalData.addNewWeiboIdToSys(p);
				if(!success)
					Toast.makeText(ContactDetail.this, "Encounter an error when adding Weibo information",Toast.LENGTH_SHORT).show();
				break;
			case ConstantS.UPDATEWEIBO:
				p.changeWeiboId(weiboId);
				p.changeWeiboScreenName(weiboScreenName);
				p.changeWeiboLink(weiboLink);
				p.setWeiboContent(weiboContent);
				p.setWeiboPic(weiboPicByte);
				p.setWeiboTime(weiboTime);
				success = manageLocalData.updateWeiboIdToSys(p);
				if(!success)
					Toast.makeText(ContactDetail.this, "Encounter an error when updating Weibo information",Toast.LENGTH_SHORT).show();
				break;
			case ConstantS.DELETEWEIBO:
				success = manageLocalData.deleteWeiboIdFromSys(p);
				if(!success)
					Toast.makeText(ContactDetail.this, "Encounter an error when deleting Weibo information",Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}
	}
	
	//Sync with system
	public void syncWithSysContact(String contactId, String phoneNumber, int phoneType, int ops)
	{
		//Sync
		if(ops == ConstantS.ADD)
			manageLocalData.addNewNumberToSysContact(contactId,phoneNumber,phoneType);
		else if(ops == ConstantS.DELETE)
		{
			manageLocalData.deleteNumFromSysContact(contactId,phoneNumber);
		}
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
			contactListCursor = contactListResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			if(contactListCursor.getCount() == 0)
			{
				Intent contactDetail = new Intent(ContactDetail.this,BriefView.class);
				startActivity(contactDetail);
			}
			else {
				((ContactData) manageLocalData).getContactFromSystem();
				for(Person tempPerson : manageLocalData.getContactList())
				{
					if(tempPerson.getContactId().equals(p.getContactId()))
					{
						p = tempPerson;
						break;
					}
				}
				defaultInterfaceContent(p,false);
			}
		}
	}
	
	//Activity OnStop
	@Override
	public void onStop()
	{
		count++;
		System.out.println(count);
		savedPerson = p;
		super.onStop();
		this.finish();
	}
	
	//Activity OnDestroy
	public void onDestroy()
	{
		count = 1;
		super.onDestroy();
	}

	//Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater actionBarMenu = getMenuInflater();
		actionBarMenu.inflate(R.menu.generalmenu,menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	//Refresh or Delete Contact
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		switch(menuItem.getItemId())
		{
			case R.id.action_delete:
			{
				manageLocalData.deleteContactFromSysContact(p.getContactId());
				Toast.makeText(ContactDetail.this, "Deleted!",Toast.LENGTH_SHORT).show();
				//End This Acitiviy
				finish();
				return true;
			}
			case R.id.action_refresh_second:
			{
				stop = false;
				contactListCursor = contactListResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
				if(contactListCursor.getCount() == 0)
				{
					Intent contactDetail = new Intent(ContactDetail.this,BriefView.class);
					startActivity(contactDetail);
				}
				else {
					((ContactData) manageLocalData).getContactFromSystem();
					for(Person tempPerson : manageLocalData.getContactList())
					{
						if(tempPerson.getContactId().equals(p.getContactId()))
						{
							p = tempPerson;
							break;
						}
					}
					defaultInterfaceContent(p,false);
				}
				return true;
			}
		    default:
		    	return super.onOptionsItemSelected(menuItem);
		}
	}
	
	private String checkForInternetConnectivity()
	{
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		State mobile = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		State wifi = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

		if(mobile==State.CONNECTED||mobile==State.CONNECTING)
			return "3G";
		else if(wifi==State.CONNECTED||wifi==State.CONNECTING)
			return "Wifi";
		else return "Disconnect";
	}
}
	
