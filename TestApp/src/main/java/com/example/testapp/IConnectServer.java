package com.example.testapp;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public interface IConnectServer {
	//Get Response String
	public String queryStringForGet(String url);
	public String queryStringForPost(String url);
	//Get Http Response
	public HttpResponse getHttpResponse(HttpPost postRequest) throws ClientProtocolException, IOException;
	public HttpResponse getHttpResponse(HttpGet getRequest) throws ClientProtocolException, IOException;
}
