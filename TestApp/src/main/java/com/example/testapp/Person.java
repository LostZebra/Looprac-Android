package com.example.testapp;

import java.util.ArrayList;
import java.io.Serializable;

public class Person implements Serializable{	
	//Make the Data Structure Serializable
	private static final long serialVersionUID = 1L;

	//Contact Name
	private String name;
	//Contact ID
	private String contactId; //Foreigh Key for Refer to Weibo Storage Content Provider
	//Weibo Info
	private Long weiboId;
	private String weiboScreenName;
	private String weiboLink;
	private byte[] weiboThumPic;
	private String lastWeiboContent;
	private String lastWeiboTime;
	private byte[] weiboPic;
	
	//Contact Phones ID
	private ArrayList<Long> mobilePhonesIdList = null;
	private ArrayList<Long> workPhonesIdList = null;
	private ArrayList<Long> homePhonesIdList = null;
	private ArrayList<Long> otherPhonesIdList = null;
	
	//Contact Phone(s) List
	private ArrayList<String> mobilePhonesList = null;
	private ArrayList<String> workPhonesList = null;
	private ArrayList<String> homePhonesList = null;
	private ArrayList<String> otherPhonesList = null;
		
	//Constructor
	public Person(String name, String contactId)
	{
		this.name = name;
		this.contactId = contactId;
		this.weiboId = (long) 0;
		this.weiboScreenName =  "";
		this.weiboLink = "";
		this.weiboThumPic = null;
		this.lastWeiboContent = "";
		this.lastWeiboTime = "";
		this.weiboPic = null;
		this.mobilePhonesIdList = new ArrayList<Long>();
		this.workPhonesIdList = new ArrayList<Long>();
		this.homePhonesIdList = new ArrayList<Long>();
		this.otherPhonesIdList = new ArrayList<Long>();
		this.mobilePhonesList = new ArrayList<String>();
		this.workPhonesList = new ArrayList<String>();
		this.homePhonesList = new ArrayList<String>();
		this.otherPhonesList = new ArrayList<String>();
	}
	public Person()
	{
		this.name = "";
		this.contactId = "";
		this.weiboId = (long) 0;
		this.weiboScreenName =  "";
		this.weiboLink = "";
		this.weiboThumPic = null;
		this.lastWeiboContent = "";
		this.lastWeiboTime = "";
		this.weiboPic = null;
		this.mobilePhonesIdList = new ArrayList<Long>();
		this.workPhonesIdList = new ArrayList<Long>();
		this.homePhonesIdList = new ArrayList<Long>();
		this.otherPhonesIdList = new ArrayList<Long>();
		this.mobilePhonesList = new ArrayList<String>();
		this.workPhonesList = new ArrayList<String>();
		this.homePhonesList = new ArrayList<String>();
		this.otherPhonesList = new ArrayList<String>();
		this.mobilePhonesList = new ArrayList<String>();
		this.workPhonesList = new ArrayList<String>();
		this.homePhonesList = new ArrayList<String>();
		this.otherPhonesList = new ArrayList<String>();
	}
	
	//Get Accessible Data
	public String getName()
	{
		return this.name;
	}
	public String getContactId()
	{
		return this.contactId;
	}
	public long getWeiboId()
	{
		return this.weiboId;
	}
	public String getWeiboScreenName()
	{
		return this.weiboScreenName;
	}
	public String getWeiboLink()
	{
		return this.weiboLink;
	}
	public byte[] getWeiboThumPic()
	{
		return this.weiboThumPic;
	}
	public String getLastWeiboContent()
	{
		return this.lastWeiboContent;
	}
	public String getLastWeiboTime()
	{
		return this.lastWeiboTime;
	}
	public byte[] getWeiboPic()
	{
		return this.weiboPic;
	}
	public ArrayList<Long> getMobilePhonesIdList()
	{
		return this.mobilePhonesIdList;
	}
	public ArrayList<Long> getWorkPhonesIdList()
	{
		return this.mobilePhonesIdList;
	}
	public ArrayList<Long> getHomePhonesIdList()
	{
		return this.mobilePhonesIdList;
	}
	public ArrayList<Long> getOtherPhonesIdList()
	{
		return this.mobilePhonesIdList;
	}
	public ArrayList<String> getMobilePhonesList()
	{
		return this.mobilePhonesList;
	}
	public ArrayList<String> getWorkPhonesList()
	{
		return this.workPhonesList;
	}
	public ArrayList<String> getHomePhonesList()
	{
		return this.homePhonesList;
	}
	public ArrayList<String> getOtherPhonesList()
	{
		return this.otherPhonesList;
	}
	public int getSize()
	{
		return this.mobilePhonesList.size()+this.workPhonesList.size()+this.homePhonesList.size()+this.otherPhonesList.size()+1;
	}
	
	//Edit Accessible Data
	public void changeName(String newName)
	{
		this.name = newName;
	}
	public void changeContactId(String contactId)
	{
		this.contactId = contactId;
	}
	public void changeWeiboId(long newId)
	{
		this.weiboId = newId;
	}
	public void changeWeiboScreenName(String newScreenName)
	{
		this.weiboScreenName = newScreenName;
	}
	public void changeWeiboLink(String newLink)
	{
		this.weiboLink = newLink;
	}
	public void setWeiboThumPic(byte[] weiboPhotoL)
	{
		this.weiboThumPic = weiboPhotoL;
	}
	public void setWeiboContent(String lastWeiboContent)
	{
		this.lastWeiboContent = lastWeiboContent;
	}
	public void setWeiboTime(String lastWeiboTime)
	{
		this.lastWeiboTime = lastWeiboTime;
	}
	public void setWeiboPic(byte[] weiboPic)
	{
		this.weiboPic = weiboPic;
	}
	public void addToMobilePhonesIdList(long phoneId)
	{
		this.mobilePhonesIdList.add(phoneId);
	}
	public void addToWorkPhonesIdList(long phoneId)
	{
		this.workPhonesIdList.add(phoneId);
	}
	public void addToHomePhonesIdList(long phoneId)
	{
		this.homePhonesIdList.add(phoneId);
	}
	public void addToOtherPhonesIdList(long phoneId)
	{
		this.otherPhonesIdList.add(phoneId);
	}
	public void addToMobilePhonesList(String phoneNumber)
	{
		this.mobilePhonesList.add(phoneNumber);
	}
	public void addToWorkPhonesList(String phoneNumber)
	{
		this.workPhonesList.add(phoneNumber);
	}
	public void addToHomePhonesList(String phoneNumber)
	{
		this.homePhonesList.add(phoneNumber);
	}
	public void addToOtherPhonesList(String phoneNumber)
	{
		this.otherPhonesList.add(phoneNumber);
	}
	public void removeFromMobilePhonesIdList(long phoneId)
	{
		this.mobilePhonesIdList.remove(phoneId);
	}
	public void removeFromWorkPhonesIdList(long phoneId)
	{
		this.workPhonesIdList.remove(phoneId);
	}
	public void removeFromHomePhonesIdList(long phoneId)
	{
		this.homePhonesIdList.remove(phoneId);
	}
	public void removeFromOtherPhonesIdList(long phoneId)
	{
		this.otherPhonesIdList.remove(phoneId);
	}
	public void removeFromMobilePhonesList(String phoneNumber)
	{
		this.mobilePhonesList.remove(phoneNumber);
	}
	public void removeFromWorkPhonesList(String phoneNumber)
	{
		this.workPhonesList.remove(phoneNumber);
	}
	public void removeFromHomePhonesList(String phoneNumber)
	{
		this.homePhonesList.remove(phoneNumber);
	}
	public void removeFromOtherPhonesList(String phoneNumber)
	{
		this.otherPhonesList.remove(phoneNumber);
	}
}
