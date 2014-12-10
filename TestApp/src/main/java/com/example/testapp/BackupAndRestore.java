package com.example.testapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BackupAndRestore extends Activity
{
	ActionBar actionBar;
	TextView personOnLocal;
	Button backup, restore;
	IManageLocalData manageLocalData;
	String userName = "";
	UserInfo userInfo;
	public void onCreate(Bundle onSavedBundle)
	{
		super.onCreate(onSavedBundle);
		setContentView(R.layout.backupandrestore);
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		userInfo = new UserInfo(BackupAndRestore.this);
		userName = userInfo.readFromPreference()[0];
		Intent parentIntent = getIntent();
		Bundle intBundle = parentIntent.getExtras();
		int size = intBundle.getInt("size");
		if(size != -1)
		{
			backup = (Button)findViewById(R.id.actionbackup);
			restore = (Button)findViewById(R.id.actionrestore);
			personOnLocal = (TextView)findViewById(R.id.onlocal);
		    if(size == 0)
		    	backup.setEnabled(false);
		    personOnLocal.setText(size+" contacts");
			setOnBackupButtonListener();
			setOnRestoreButtonListener();
			manageLocalData = new ContactData(getContentResolver());
		}
		else
			Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
	}
	
	public void setOnBackupButtonListener()
	{
		backup.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				WebServiceTask wst = new WebServiceTask(ConstantS.POST_TASK, BackupAndRestore.this,"Posting data...");
                String[] contactInfoInString = organizeContactInfo();
                int contactInfoSize = contactInfoInString.length;
                for(int i = 0; i < contactInfoSize; i++)
                {
					wst.addNameValuePair("Transfer"+i, contactInfoInString[i]);
					System.out.println(contactInfoInString[i]);
                }
				// the passed String is the URL we will POST to
				wst.execute(new String[] { ConstantS.SERVICE_URL_UPLOAD });
				Toast.makeText(BackupAndRestore.this, "Backup Complete!",Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}
	

	public void setOnRestoreButtonListener()
	{
		restore.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				WebServiceTask wst = new WebServiceTask(ConstantS.GET_TASK, BackupAndRestore.this,"Restoring data...");
				wst.execute(new String[] { ConstantS.SERVICE_URL_DOWNLOAD+"?username=justin"});
			}
		});
	}
	
	//Organize Contact Information for Upload
	public String[] organizeContactInfo()
	{
		Person nextPerson = null;
		String mobilePhonesString = "";
		String workPhonesString = "";
		String homePhonesString = "";
		String otherPhonesString = "";
		((ContactData) manageLocalData).getContactFromSystem();
		ArrayList<Person> localContactList = manageLocalData.getContactList();
		int contactListSize = localContactList.size();
		String[] contactInfoInString = new String[contactListSize];
		
		for(int i = 0; i < contactListSize; i++)
		{
			mobilePhonesString = "";
			workPhonesString = "";
			homePhonesString = "";
			otherPhonesString = "";
			contactInfoInString[i] = "";
			nextPerson = localContactList.get(i);
			contactInfoInString[i] += nextPerson.getContactId()+'/';
			contactInfoInString[i] += nextPerson.getName()+'/';
			String weiboScreenName = nextPerson.getWeiboScreenName();
			if(weiboScreenName.length() == 0)
				contactInfoInString[i] += "Empty/";
			else
				contactInfoInString[i] += weiboScreenName+'/';
			ArrayList<String> mobilePhonesList = nextPerson.getMobilePhonesList();
			ArrayList<String> workPhonesList = nextPerson.getWorkPhonesList();
			ArrayList<String> homePhonesList = nextPerson.getHomePhonesList();
			ArrayList<String> otherPhonesList = nextPerson.getOtherPhonesList();
			if(mobilePhonesList.size() == 0)
				contactInfoInString[i] += "Empty/";
			else
			{
				for(String mobilePhoneNumber : mobilePhonesList)
				{
					mobilePhonesString += mobilePhoneNumber+'%';
					System.out.println(mobilePhonesString);
				}
				contactInfoInString[i] += mobilePhonesString+'/';
			}
			if(workPhonesList.size() == 0)
				contactInfoInString[i] += "Empty/";
			else
			{
				for(String workPhoneNumber : workPhonesList)
					workPhonesString += workPhoneNumber+'%';
				contactInfoInString[i] += workPhonesString+'/';
			}
			if(homePhonesList.size() == 0)
				contactInfoInString[i] += "Empty/";
			else
			{
				for(String homePhoneNumber : homePhonesList)
					homePhonesString += homePhoneNumber+'%';
				contactInfoInString[i] += homePhonesString+'/';
			}
			if(otherPhonesList.size() == 0)
				contactInfoInString[i] += "Empty";
			else
			{
				for(String otherPhoneNumber : otherPhonesList)
					otherPhonesString += otherPhoneNumber+'%';
				contactInfoInString[i] += otherPhonesString;
			}
		}
		return contactInfoInString;
	}
	
	private class WebServiceTask extends AsyncTask<String, Integer, String> {
        private int jsonSize = 0;
		private int taskType;
		private Context mContext = null;
		private String processMessage = null;

		private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		private ProgressDialog pDlg = null;

		public WebServiceTask(int taskType, Context mContext,
				String processMessage) {

			this.taskType = taskType;
			this.mContext = mContext;
			this.processMessage = processMessage;
		}

		public void addNameValuePair(String name, String value) {
			params.add(new BasicNameValuePair(name, value));
		}

		private void showProgressDialog() {
			pDlg = new ProgressDialog(mContext);
			pDlg.setMessage(processMessage);
			pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDlg.setCancelable(false);
			pDlg.setIndeterminate(true);
			pDlg.show();
		}

		@Override
		protected void onPreExecute() {
			showProgressDialog();
		}

		protected String doInBackground(String... urls) {

			String url = urls[0];
			String result = "";

			HttpResponse response = doResponse(url);

			try {
				if (response.getStatusLine().getStatusCode() == 200) {
					result = inputStreamToString(response.getEntity()
							.getContent());
				}

			} catch (IllegalStateException e) {
				Log.e(ConstantS.TAG, e.getLocalizedMessage(), e);

			} catch (IOException e) {
				Log.e(ConstantS.TAG, e.getLocalizedMessage(), e);
			}

			return result;
		}

		@Override
		protected void onPostExecute(String response) {
			String weiboScreenName;
			if(taskType == ConstantS.GET_TASK)
			{
				try {
					JSONArray jsa = new JSONArray(response);
					jsonSize = jsa.length();
					if(jsonSize != 0)
					{
						((ContactData) manageLocalData).clearAllLocalDataFromSys();
						Person newPerson = new Person();
						for(int i = 0; i < jsa.length(); i++)
						{
							JSONObject jso = jsa.getJSONObject(i);
							newPerson.changeContactId(jso.getString("id"));
							newPerson.changeName(jso.getString("name"));
							weiboScreenName = jso.getString("weiboScreenName");
							if(weiboScreenName.equals("Empty"))
								newPerson.changeWeiboScreenName("");
							else
								newPerson.changeWeiboScreenName(weiboScreenName);
							String mobilePhonesList = jso.getString("mobilePhonesList");
							String workPhonesList = jso.getString("workPhonesList");
							String homePhonesList = jso.getString("homePhonesList");
							String otherPhonesList = jso.getString("otherPhonesList");
							String[] mobilePhonesListArray = mobilePhonesList.split("%");
							String[] workPhonesListArray = workPhonesList.split("%");
							String[] homePhonesListArray = homePhonesList.split("%");
							String[] otherPhonesListArray = otherPhonesList.split("%");
							for(String mobilePhones : mobilePhonesListArray)
							{
								if(!mobilePhones.equals("Empty"))
								{
									newPerson.addToMobilePhonesList(mobilePhones);
									System.out.println(mobilePhones);
								}
							}
							for(String workPhones : workPhonesListArray)
							{
								if(!workPhones.equals("Empty"))
									newPerson.addToWorkPhonesList(workPhones);
							}
							for(String homePhones : homePhonesListArray)
							{
								if(!homePhones.equals("Empty"))
									newPerson.addToHomePhonesList(homePhones);
							}
							for(String otherPhones : otherPhonesListArray)
							{
							    if(!otherPhones.equals("Empty"))
									newPerson.addToOtherPhonesList(otherPhones);
							}
							manageLocalData.addNewContactToSysContact(newPerson);
						}
					}
				} catch (Exception e) {
					Toast.makeText(BackupAndRestore.this, "No contact data received!", Toast.LENGTH_SHORT).show();
					Log.e(ConstantS.TAG, e.getLocalizedMessage(), e);
				}

			}
			pDlg.dismiss();
		}
		

		// Establish connection and socket (data retrieval) timeouts
		private HttpParams getHttpParams() {

			HttpParams htpp = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(htpp, ConstantS.CONN_TIMEOUT);
			HttpConnectionParams.setSoTimeout(htpp, ConstantS.SOCKET_TIMEOUT);

			return htpp;
		}

		private HttpResponse doResponse(String url) {
			// DefaultHttpClient
			HttpClient httpclient = new DefaultHttpClient(getHttpParams());

			HttpResponse response = null;

			try {
				switch (taskType) {

				case ConstantS.POST_TASK:
					HttpPost httppost = new HttpPost(url);
					// Add parameters
					httppost.setEntity(new UrlEncodedFormEntity(params));

					response = httpclient.execute(httppost);
					Log.wtf("response code(POST_TASK)", response
							.getStatusLine().getStatusCode() + "");
					break;
				case ConstantS.GET_TASK:
					System.out.println(url);
					HttpGet httpget = new HttpGet(url);
					response = httpclient.execute(httpget);
					Log.wtf("response code(GET_TASK)", response.getStatusLine()
							.getStatusCode() + "");
					break;
				}
			} catch (Exception e) {
				Log.e(ConstantS.TAG, e.getLocalizedMessage(), e);

			}
			return response;
		}

		private String inputStreamToString(InputStream is) {

			String line = "";
			StringBuilder total = new StringBuilder();

			// Wrap a BufferedReader around the InputStream
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			try {
				// Read response until the end
				while ((line = rd.readLine()) != null) {
					total.append(line);
				}
			} catch (IOException e) {
				Log.e(ConstantS.TAG, e.getLocalizedMessage(), e);
			}

			// Return full string
			return total.toString();
		}
	}
}