package com.example.testapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

public class ContactData implements IManageLocalData{
	private ArrayList<Person> contactData = null;
	private Cursor contactListCursor = null;
	private Cursor weiboDataCursor = null;
	private ContentResolver contactResolver = null;
	
	//Constructor
	public ContactData(ContentResolver contactResolver)
	{
		this.contactData = new ArrayList<Person>();
		this.contactResolver = contactResolver;
		this.contactListCursor = contactResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		this.weiboDataCursor = contactResolver.query(WeiboData.Weibo.WEIBO_LIST_URI, null,null,null,null);
	}
		
	//Get Accessible Data
	public ArrayList<Person> getContactList()
	{
		return this.contactData;
	}
	
	//Set Accessible Data
	public void addPersonToList(int index, Person person)
	{
		contactData.add(index,person);
	}
	public void deletePersonFromList(int index)
	{
		contactData.remove(index);
	}
	public void changePersonInList(int index, Person person)
	{
		contactData.set(index,person);
	}
	
	//Read Contact from System
	public void getContactFromSystem()
	{
		int indexOfList = 0;
		//Cursor for System Contact Data
		contactData = new ArrayList<Person>();
		while(contactListCursor.moveToNext())
		{			
			addPersonToList(indexOfList,setPersonInfo());
		    indexOfList++;
		}
		sortContactsWithLetter();
	}
	
	//Get Individual Contact Infor
	private Person setPersonInfo()
	{
		String contactId = contactListCursor.getString(contactListCursor.getColumnIndex(ContactsContract.Contacts._ID));
		String name = contactListCursor.getString(contactListCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		Cursor phoneCursor = contactResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+contactId,null,null);
		Person newPerson = new Person(name,contactId);
	    while(phoneCursor.moveToNext())
	    {
	    	int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
	    	switch(phoneType)
	    	{
		    	case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
		    	{
		    		newPerson.addToMobilePhonesList(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
		    		newPerson.addToMobilePhonesIdList(phoneCursor.getLong(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));
		    		break;
		    	}
		    	case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
		    	{
		    		newPerson.addToWorkPhonesList(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
		    		newPerson.addToWorkPhonesIdList(phoneCursor.getLong(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));
		    		break;
		    	}
		    	case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
		    	{
		    		newPerson.addToHomePhonesList(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
		    		newPerson.addToHomePhonesIdList(phoneCursor.getLong(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));
		    		break;
		    	}
		    	default:
		    	{
		    		newPerson.addToOtherPhonesList(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
		    		newPerson.addToOtherPhonesIdList(phoneCursor.getLong(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)));
		    		break;
		    	}
	    	}
	    }
	    phoneCursor.close();
	    
	    Bitmap thumPhoto = getContactPhoto(contactId);
	    if(thumPhoto != null)
	    {
		    ByteArrayOutputStream photoOutputStream = new ByteArrayOutputStream();
		    thumPhoto.compress(Bitmap.CompressFormat.PNG, 100, photoOutputStream);
		    byte[] photoBytes = photoOutputStream.toByteArray();
		    //Use the Weibo Thum Picture as the Photo ID of Your Contact
			newPerson.setWeiboThumPic(photoBytes);
	    }
		
	    if(localWeiboStorageExist())
	    {
	    	Cursor weiboContactCursor = contactResolver.query(WeiboData.Weibo.WEIBO_LIST_URI,null,WeiboData.Weibo._ID + "=" +contactId,null,null);
	    	if(weiboContactCursor.moveToFirst())
	    	{
	    		newPerson.changeWeiboId(weiboContactCursor.getLong(weiboContactCursor.getColumnIndex(WeiboData.Weibo.WEIBOID)));
	    		newPerson.changeWeiboScreenName(weiboContactCursor.getString(weiboContactCursor.getColumnIndex(WeiboData.Weibo.WEIBOSCREENNAME)));
	    		newPerson.changeWeiboLink(weiboContactCursor.getString(weiboContactCursor.getColumnIndex(WeiboData.Weibo.WEIBOLINK)));
	    		newPerson.setWeiboContent(weiboContactCursor.getString(weiboContactCursor.getColumnIndex(WeiboData.Weibo.WEIBOCONTENT)));
	    		newPerson.setWeiboTime(weiboContactCursor.getString(weiboContactCursor.getColumnIndex(WeiboData.Weibo.WEIBOTIME)));
	    		newPerson.setWeiboPic(weiboContactCursor.getBlob(weiboContactCursor.getColumnIndex(WeiboData.Weibo.WEIBOPIC)));
	    	}
	    	weiboContactCursor.close();
	    }
	    return newPerson;
	}
	
	//Check Local Weibo Storage
	private boolean localWeiboStorageExist() {
		if(weiboDataCursor.getCount() == 0)
			return false;
		else return true;
	}
	
	//Convert Original String to Call String
	private String convertToCallString(String phoneNumber)
	{
		String tempPhoneNumber = "("+phoneNumber.substring(0,3)+") "+phoneNumber.substring(3,6)+"-"+phoneNumber.substring(6,10);
		return tempPhoneNumber;
	}
	
	//Get Contact Photo From System
	public Bitmap getContactPhoto(String contactId)
	{
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,Long.parseLong(contactId));
		InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(contactResolver, uri); 
		Bitmap contactPhoto = BitmapFactory.decodeStream(input);
		return contactPhoto;
	}
	
	//Add Weibo Info
	public boolean addNewWeiboIdToSys(Person p)
	{
		//Variables
		String contactId = p.getContactId();
		long weiboId = p.getWeiboId();
		String weiboScreenName = p.getWeiboScreenName();
		String weiboLink = p.getWeiboLink();
		String weiboContent = p.getLastWeiboContent();
		String weiboTime = p.getLastWeiboTime();
		byte[] weiboPic = p.getWeiboPic();
		//New Content Values
		ContentValues weiboValues = new ContentValues();
		weiboValues.put(WeiboData.Weibo._ID,contactId);
		if(weiboId != 0)
			weiboValues.put(WeiboData.Weibo.WEIBOID,weiboId);
		if(weiboScreenName.length() != 0)
			weiboValues.put(WeiboData.Weibo.WEIBOSCREENNAME,weiboScreenName);
		if(weiboLink.length() != 0)
			weiboValues.put(WeiboData.Weibo.WEIBOLINK,weiboLink);
		if(weiboContent.length() != 0)
			weiboValues.put(WeiboData.Weibo.WEIBOCONTENT,weiboContent);
		if(weiboTime.length() != 0)
			weiboValues.put(WeiboData.Weibo.WEIBOTIME,weiboTime);
		if(weiboPic != null)
			weiboValues.put(WeiboData.Weibo.WEIBOPIC,weiboPic);
		try{
			contactResolver.insert(WeiboData.Weibo.WEIBO_LIST_URI,weiboValues);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	//Delete Weibo Info
	public boolean deleteWeiboIdFromSys(Person p)
	{
		try{
			contactResolver.delete(WeiboData.Weibo.WEIBO_LIST_URI,WeiboData.Weibo._ID + " = ? ",new String[]{p.getContactId()});
		    return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	//Update Weibo Info
	public boolean updateWeiboIdToSys(Person p)
	{
		ContentValues weiboValues = new ContentValues();
		weiboValues.put(WeiboData.Weibo._ID,p.getContactId());
		weiboValues.put(WeiboData.Weibo.WEIBOID,p.getWeiboId());
		weiboValues.put(WeiboData.Weibo.WEIBOSCREENNAME,p.getWeiboScreenName());
		weiboValues.put(WeiboData.Weibo.WEIBOLINK,p.getWeiboLink());
		weiboValues.put(WeiboData.Weibo.WEIBOCONTENT,p.getLastWeiboContent());
		weiboValues.put(WeiboData.Weibo.WEIBOTIME,p.getLastWeiboTime());
		weiboValues.put(WeiboData.Weibo.WEIBOPIC,p.getWeiboPic());
		try{
			contactResolver.update(WeiboData.Weibo.WEIBO_LIST_URI,weiboValues,WeiboData.Weibo._ID + " = ? ",new String[]{p.getContactId()});
		    return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
    
	//Add New Number to Existed Contact
	public void addNewNumberToSysContact(String contactId, String phoneNumber, int phoneType)
	{
		try
		{	
			String phoneNumberConconverted = convertToCallString(phoneNumber);
			ContentValues newPhoneNumValues = new ContentValues();
			newPhoneNumValues.clear();
			newPhoneNumValues.put(Data.RAW_CONTACT_ID,Integer.parseInt(contactId));
			newPhoneNumValues.put(Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE);
			newPhoneNumValues.put(Phone.NUMBER,phoneNumberConconverted);
			switch (phoneType) {
			case ConstantS.MOBILE:
				newPhoneNumValues.put(Phone.TYPE,Phone.TYPE_MOBILE);
				break;
			case ConstantS.WORK:
				newPhoneNumValues.put(Phone.TYPE,Phone.TYPE_WORK);
				break;
			case ConstantS.HOME:
				newPhoneNumValues.put(Phone.TYPE,Phone.TYPE_HOME);
				break;
			case ConstantS.OTHER:
				newPhoneNumValues.put(Phone.TYPE,Phone.TYPE_OTHER);
				break;
			default:
				break;
			}
			contactResolver.insert(Data.CONTENT_URI,newPhoneNumValues);
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	
	//Add New Photo to Existed Contact
	public void addNewPhotoToSysContact(String contactId, String photoUrl)
	{
		ContentValues values = new ContentValues();
	    int photoRow = -1;
	    String where = Data.RAW_CONTACT_ID + " = " + contactId + " AND " + Data.MIMETYPE + "=='" + Photo.CONTENT_ITEM_TYPE + "'";
	    Cursor photoCursor = contactResolver.query(Data.CONTENT_URI, null, where, null, null);
	    int id = photoCursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
	    if (photoCursor.moveToFirst()) {
	        photoRow = photoCursor.getInt(id);
	    }
	    photoCursor.close();

	    values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
	    values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
	    values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, getImageThroughUrl(photoUrl));
	    values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

	    if (photoRow >= 0) {
	    	contactResolver.update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.Data._ID + " = " + photoRow, null);
	    } else {
	    	contactResolver.insert(ContactsContract.Data.CONTENT_URI, values);
	    }
	}
	
	//Get Image through Url
	public byte[] getImageThroughUrl(String url)
	{
		URL imageUrl = null;
		Bitmap image = null;
	    try {
	    	imageUrl = new URL(url);
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
		byte[] arrbyte;
		if(image != null)
			image.compress(Bitmap.CompressFormat.JPEG, 75, stream);
		arrbyte = stream.toByteArray();
	    return arrbyte;  
	}
	
	//Detele a PhoneNumber From System
	public void deleteNumFromSysContact(String contactId, String phoneId)
	{
		Cursor deleteCursor = contactResolver.query(RawContacts.CONTENT_URI, 
	            new String[]{RawContacts._ID},
	            RawContacts.CONTACT_ID + "=?", 
	            new String[] {contactId}, null);
	    
		int rowId = 0;;
	    if(deleteCursor.moveToFirst()){
	        rowId = deleteCursor.getInt(deleteCursor.getColumnIndex(RawContacts._ID));
	    }

	    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	    String selectPhone = Data.RAW_CONTACT_ID + " = ? AND " + 
	                         Data.MIMETYPE + " = ? AND " + 
	                         Phone._ID + " = ?";
	    String[] phoneArgs = new String[] { Integer.toString(rowId), 
	                                        Phone.CONTENT_ITEM_TYPE, 
	                                        phoneId};
	    ops.add(ContentProviderOperation.newDelete(Data.CONTENT_URI)
	            .withSelection(selectPhone, phoneArgs).build());
	    try {
	        contactResolver.applyBatch(ContactsContract.AUTHORITY, ops);
	    } catch (RemoteException e) {
	        e.printStackTrace();
	    } catch (OperationApplicationException e) {
	        e.printStackTrace();
	    }
	}
	
	//Delete a Contact From System
	public boolean deleteContactFromSysContact(String contactId)
	{
		boolean success = false;
        String where = ContactsContract.Data._ID + " = ? ";
        String[] params = new String[] {contactId};

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(where, params)
                .build());
        try {
            contactResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            success = true;
        } catch (RemoteException e) {
            e.printStackTrace();
            success = false;
            return success;
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            success = false;
            return success;
        }
        //Do Something to Sync with Weibo Storage
        try{
        	contactResolver.delete(WeiboData.Weibo.WEIBO_LIST_URI,WeiboData.Weibo._ID + " = ? ",new String[]{contactId}); 
            return success;
        }
        catch(Exception e)
        {
        	success = false;
        	return success;
        }
	}
	
	//Add New Contact
	public boolean addNewContactToSysContact(Person p)
	{		
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	    int rawContactInsertIndex = ops.size();
	    ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
			.withValue(RawContacts.ACCOUNT_TYPE, null)
		    .withValue(RawContacts.ACCOUNT_NAME, null).build());
	    for(String phoneNumber : p.getMobilePhonesList())
	    {   
	    	if(phoneNumber.startsWith("("))
	    	{
	    		ops.add(ContentProviderOperation
	    			    .newInsert(ContactsContract.Data.CONTENT_URI)
	    			    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
	    			      rawContactInsertIndex)
	    			    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
	    			    .withValue(Phone.NUMBER,phoneNumber)
	    			    .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build());
	    	}
	    	else
	    	{
			    ops.add(ContentProviderOperation
				    .newInsert(ContactsContract.Data.CONTENT_URI)
				    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
				      rawContactInsertIndex)
				    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				    .withValue(Phone.NUMBER,convertToCallString(phoneNumber))
				    .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build());
	    	}
	    }
	    for(String phoneNumber : p.getWorkPhonesList())
	    {   	
	    	if(phoneNumber.startsWith("("))
	    	{
			    ops.add(ContentProviderOperation
				    .newInsert(ContactsContract.Data.CONTENT_URI)
				    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
				      rawContactInsertIndex)
				    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				    .withValue(Phone.NUMBER,phoneNumber)
				    .withValue(Phone.TYPE, Phone.TYPE_WORK).build());
	    	}
	    	else
	    	{
	    		ops.add(ContentProviderOperation
	 				.newInsert(ContactsContract.Data.CONTENT_URI)
	 		        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
	 			      rawContactInsertIndex)
	 			    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
	 			    .withValue(Phone.NUMBER,convertToCallString(phoneNumber))
	 			    .withValue(Phone.TYPE, Phone.TYPE_WORK).build());
	    	}
	    }
	    for(String phoneNumber : p.getHomePhonesList())
	    {	    	
	    	if(phoneNumber.startsWith("("))
	    	{
			    ops.add(ContentProviderOperation
				    .newInsert(ContactsContract.Data.CONTENT_URI)
				    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
				      rawContactInsertIndex)
				    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				    .withValue(Phone.NUMBER,phoneNumber)
				    .withValue(Phone.TYPE, Phone.TYPE_HOME).build());
	    	}
	    	else
	    	{
	    		ops.add(ContentProviderOperation
		 			.newInsert(ContactsContract.Data.CONTENT_URI)
		 		    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
		     	      rawContactInsertIndex)
		 		    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				    .withValue(Phone.NUMBER,convertToCallString(phoneNumber))
	 			    .withValue(Phone.TYPE, Phone.TYPE_WORK).build());
	    	}
	    }
	    for(String phoneNumber : p.getOtherPhonesList())
	    {    
	    	if(phoneNumber.startsWith("("))
	    	{
			    ops.add(ContentProviderOperation
				    .newInsert(ContactsContract.Data.CONTENT_URI)
				    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
				      rawContactInsertIndex)
				    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				    .withValue(Phone.NUMBER,phoneNumber)
				    .withValue(Phone.TYPE, Phone.TYPE_OTHER).build());
	    	}
	    	else
	    	{
	    		ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
				    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
				      rawContactInsertIndex)
				    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
				    .withValue(Phone.NUMBER,convertToCallString(phoneNumber))
				    .withValue(Phone.TYPE, Phone.TYPE_OTHER).build());
	    	}
	    }
	    ops.add(ContentProviderOperation
	    	.newInsert(ContactsContract.Data.CONTENT_URI)
		    .withValueBackReference(Data.RAW_CONTACT_ID,rawContactInsertIndex)
			.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
		    .withValue(StructuredName.DISPLAY_NAME, p.getName())
		    .build());
	    try 
	    {
	    	contactResolver.applyBatch(ContactsContract.AUTHORITY, ops);
	    	return true;
	  	} 
	    catch (RemoteException e) {
	   	   e.printStackTrace();
		} 
	    catch (OperationApplicationException e) {
	   	   e.printStackTrace();
	    }
	    return false;
	}
	
	public void clearAllLocalDataFromSys()
	{
		while(contactListCursor.moveToNext())
		{			
			String contactId = contactListCursor.getString(contactListCursor.getColumnIndex(ContactsContract.Contacts._ID));
			deleteContactFromSysContact(contactId);
		}
	}
	
	//Sort
	private void sortContactsWithLetter()
	{
		Comparator<Person> comparator = new Comparator<Person>(){
			@Override
			public int compare(Person lhs, Person rhs) {
				if(!(lhs.getName()).equals(rhs.getName()))
					return lhs.getName().compareTo(rhs.getName());
				else return lhs.getContactId().compareTo(rhs.getContactId());
			}
		};
		Collections.sort(contactData,comparator);
	}
}
