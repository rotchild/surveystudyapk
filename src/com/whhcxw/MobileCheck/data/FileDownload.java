package com.whhcxw.MobileCheck.data;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.whhcxw.MobileCheck.net.HttpParams;

/**
 * 文件下载
 * */
public class FileDownload {	
	/**
	 * 下载
	 * */
	public static void download(String url,BinaryHttpResponseHandler responseHandler){
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		asyncHttpClient.get(url, responseHandler);
	}
}
