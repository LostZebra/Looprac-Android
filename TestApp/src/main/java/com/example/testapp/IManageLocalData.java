package com.example.testapp;

import java.util.ArrayList;

public interface IManageLocalData {
	//Contact Data
	public ArrayList<Person> getContactList();
	//Weibo
	public boolean addNewWeiboIdToSys(Person p);
	public boolean deleteWeiboIdFromSys(Person p);
	public boolean updateWeiboIdToSys(Person p);
	//Phone & ThumPhoto
	public void addNewNumberToSysContact(String contactId, String phoneNumber, int phoneType);
	public void addNewPhotoToSysContact(String contactId, String photoUrl);
	public void deleteNumFromSysContact(String contactId, String phoneId);
	//Contact
	public boolean deleteContactFromSysContact(String contactId);
	public boolean addNewContactToSysContact(Person p);
}
