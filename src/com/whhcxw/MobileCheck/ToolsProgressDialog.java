package com.whhcxw.MobileCheck;

import android.app.ProgressDialog;
import android.content.Context;

public class ToolsProgressDialog {

	private static Context mContext;
	private static String mMessage;
	
	public static ToolsProgressDialog getInitProgressDialog(Context context,String message) {
		ToolsProgressDialog toolsProgressDialog = new ToolsProgressDialog();                                                              
		mContext = context;
		mMessage = message;
		return toolsProgressDialog;
	}
	
	@SuppressWarnings("unused")
	public ProgressDialog showProgressDialog(){
		ProgressDialog mProgressDialog = new ProgressDialog(mContext);		
		mProgressDialog.setMessage(mMessage);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		return mProgressDialog;
	}
}
