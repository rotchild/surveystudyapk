package com.whhcxw.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.MobileCheck.SystemCode;
import com.whhcxw.MobileCheck.UserManager;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.crashreport.CrashHandler;
import com.whhcxw.crashreport.Mail;
import com.whhcxw.global.G;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

public class CatchException {
	private static final String EXCEPTION = "exception.txt";

	
	/**
	 * 保存捕获得到的信息
	 * 
	 * @param context
	 * @param jsonArray
	 */
	public static void saveException(Context context, JSONObject jsonObject) {
		FileOutputStream outputStream = null;
		try {
			outputStream = context.openFileOutput(EXCEPTION,Context.MODE_APPEND);
			JSONObject jsonObject2 = new JSONObject();
			jsonObject2.put("exception", jsonObject);
			jsonObject2.put("versionName", SystemCode.getVersionName(context));
			jsonObject2.put("username", UserManager.getInstance().getUserName());
			jsonObject2.put("date", G.getPhoneCurrentTime());
			jsonObject2.put("MODEL",Build.MODEL );
			outputStream.write((jsonObject2.toString()+",").getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取上传列表，并保存到文件中 ，发送Email
	 * @param context
	 */
	public static boolean sendExceptionEmail(Context context){
		 JSONObject jsonObject = new JSONObject();
		try {
			String isRequest = DBModle.TaskStack.IsRequest_N+","+DBModle.TaskStack.IsRequest_Y+","+DBModle.TaskStack.IsRequest_F;
			jsonObject.put("TaskStack",  getTaskStackJSONObject(DBOperator.getTaskStack(isRequest)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		saveException(context , jsonObject);
		
		String filaName =context.getFilesDir() +"/"+ EXCEPTION;
		ArrayList<String> attachments = new ArrayList<String>();
		attachments.add(filaName);
		
		return CrashHandler.getInstance().mailLog(context.getString(R.string.catchexception_title)+"_"+UserManager.getInstance().getRealName(), context.getString(R.string.catchexception_title), attachments);
	}
	
	/**
	 * 获取上传记列表的记录，转换成JSONObject格式
	 * 
	 */
	private static JSONArray getTaskStackJSONObject( ArrayList<ContentValues> list){
		JSONArray jsonArray = new JSONArray();
		try {
				for(ContentValues values : list){
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(DBModle.TaskStack.StackID, values.get(DBModle.TaskStack.StackID));
					jsonObject.put(DBModle.TaskStack.TID, values.get(DBModle.TaskStack.TID));
					jsonObject.put(DBModle.TaskStack.Action, values.get(DBModle.TaskStack.Action));
					jsonObject.put(DBModle.TaskStack.StackType, values.get(DBModle.TaskStack.StackType));
					jsonObject.put(DBModle.TaskStack.RequestURL, values.get(DBModle.TaskStack.RequestURL));
					jsonObject.put(DBModle.TaskStack.Keys, values.get(DBModle.TaskStack.Keys));
					jsonObject.put(DBModle.TaskStack.Vals, values.get(DBModle.TaskStack.Vals));
					jsonObject.put(DBModle.TaskStack.Level, values.get(DBModle.TaskStack.Level));
					jsonObject.put(DBModle.TaskStack.IsRequest, values.get(DBModle.TaskStack.IsRequest));
					jsonObject.put(DBModle.TaskStack.Description, values.get(DBModle.TaskStack.Description));
					jsonObject.put(DBModle.TaskStack.Data, values.get(DBModle.TaskStack.Data));
					jsonArray.put(jsonObject);
				}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return jsonArray;
	}
	
	/**
	 * 清除错误日志
	 * @param context
	 */
	public static void clearException(Context context){
		FileOutputStream fileOutputStream = null;
		try{
			fileOutputStream = context.openFileOutput(EXCEPTION,Context.MODE_PRIVATE);
			fileOutputStream.write("".getBytes());
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if(fileOutputStream!=null){
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	/**
	 * 读取文件内容
	 * 
	 * @param context
	 * @return
	 */
	private static String getException(Context context) {
		FileInputStream fileInputStream = null;
		ByteArrayOutputStream arrayOutputStream = null;
		String content = null;
		try {
			fileInputStream = context.openFileInput(EXCEPTION);
			byte[] bytes = new byte[1024];
			arrayOutputStream = new ByteArrayOutputStream();
			while (fileInputStream.read(bytes) != -1) {
				arrayOutputStream.write(bytes, 0, bytes.length);
			}
			content = new String(arrayOutputStream.toByteArray());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {

			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
				if(arrayOutputStream!=null){
					arrayOutputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return content;
	}
	
	
	

}
