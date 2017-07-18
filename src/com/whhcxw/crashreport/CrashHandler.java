package com.whhcxw.crashreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import com.whhcxw.utils.CatchException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

//使用方法 
//CrashHandler crashHandler = CrashHandler.getInstance();  
//crashHandler.init(getApplicationContext()); 

public class CrashHandler implements UncaughtExceptionHandler  {
	
	/** Debug Log tag*/ 
	public static final String TAG = "CrashHandler"; 
	/** 是否开启日志输出,在Debug状态下开启, 
	* 在Release状态下关闭以提示程序性能 
	* */ 
	public static final boolean DEBUG = true; 

	/** CrashHandler实例 */ 
	private static CrashHandler INSTANCE; 
	/** 程序的Context对象 */ 
	private Context mContext; 

	/** 使用Properties来保存设备的信息和错误堆栈信息*/ 
	private Properties mDeviceCrashInfo = new Properties(); 
	private static final String VERSION_NAME = "versionName"; 
	private static final String VERSION_CODE = "versionCode"; 
	private static final String STACK_TRACE = "STACK_TRACE"; 
	/** 错误报告文件的扩展名 */ 
	private static final String CRASH_REPORTER_EXTENSION = ".cr"; 
	private String crashFileName;

	private String mailAccount="1613949498@qq.com";
	private String mailPassword="whhcxw";
	private String mailName="";
	
	/** 保证只有一个CrashHandler实例 */ 
	private CrashHandler() {}  
	/** 获取CrashHandler实例 ,单例模式*/ 
	public static CrashHandler getInstance() { 
		if (INSTANCE == null) { 
			INSTANCE = new CrashHandler(); 
		} 
		return INSTANCE; 
	} 

	/** 
	* 初始化,注册Context对象, 
	* 获取系统默认的UncaughtException处理器, 
	* 设置该CrashHandler为程序的默认处理器 
	* 
	* 默认会去取资源 文件  crashReport_mailAccount用户名  crashReport_mailPassword密码，找不到就是初始密码
	* @param ctx 
	*/ 
	public void init(Context ctx,String name) { 
		mContext = ctx;
		int accountResId = ctx.getResources().getIdentifier("crashReport_mailAccount", "string", ctx.getPackageName());
		int pwdResId = ctx.getResources().getIdentifier("crashReport_mailPassword", "string", ctx.getPackageName());
		String _mailAccount = mailAccount;
		String _mailPassword = mailPassword;
		if( accountResId > 0 && pwdResId > 0){
			_mailAccount = ctx.getString(accountResId);
			_mailPassword = ctx.getString(pwdResId);
		}
		init(ctx,name,_mailAccount,_mailPassword);		
	}
	
	/** 
	* 初始化,注册Context对象, 
	* 获取系统默认的UncaughtException处理器, 
	* 设置该CrashHandler为程序的默认处理器 
	* 
	* @param ctx 
	*/ 
	public void init(Context ctx,String name,String _mailAccount,String _mailPassword) { 
		mContext = ctx;
		mailName=name;
		mailAccount = _mailAccount;
		mailPassword = _mailPassword;
		PackageManager pm = ctx.getPackageManager(); 
		PackageInfo pi=null;
		try {
			pi = pm.getPackageInfo(ctx.getPackageName(), 
					PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (pi != null) { 
			mailName=mailName+ (pi.versionName == null ? "not set" : pi.versionName);
		}
		Thread.setDefaultUncaughtExceptionHandler(this); 
	}

	/** 
	* 当UncaughtException发生时会转入该函数来处理 
	*/ 
	public void uncaughtException(Thread thread, Throwable ex) {
		handleException(ex);
	} 

	/** 
	* 自定义错误处理,收集错误信息 
	* 发送错误报告等操作均在此完成. 
	* 开发者可以根据自己的情况来自定义异常处理逻辑 
	* @param ex 
	* @return true:如果处理了该异常信息;否则返回false 
	*/ 
	private boolean handleException(Throwable ex) { 
		if (ex == null) { 
			return true;
		}

		//收集设备信息 
		collectCrashDeviceInfo(mContext);        
		//保存错误报告文件
		crashFileName = saveCrashInfoToFile(ex);
		
		//把底层错误信息保存到错误日志中  YL添加此内容
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("CrashHandler  saveCrashInfoToFile()", crashFileName);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CatchException.saveException(mContext, jsonObject);
		
		
		//发送错误报告到服务器
		sendCrashMail(crashFileName); 
		
		//结束程序
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);   

		return true; 
	} 

	/** 
	* 在程序启动时候, 可以调用该函数来发送以前没有发送的报告 
	*/ 
	public void sendPreviousReportsToServer() { 
		sendCrashReportsToServer(mContext); 
	} 

	/** 
	* 基于邮件的调试报告 
	*/ 
	public void mailLog(String titile,String content) { 
        Mail m = new Mail(mailAccount,mailPassword);  
		String[] toArr = { mailAccount };
		m.setTo(toArr);  
        m.setFrom(mailAccount);  
        m.setSubject(mailName+"-" + titile);  

        m.setBody(content); 
        try {  
            m.send();
        } catch (Exception e) {  
            Log.e("MailApp", "Could not send email", e);  
        }  
        return ;  
	} 
	
	/** 
	* 基于邮件的调试报告可以带附件 
	*/ 
	public boolean mailLog(String titile,String content,ArrayList<String> attachments) { 
        Mail m = new Mail(mailAccount,mailPassword);  
		String[] toArr = { mailAccount };
		m.setTo(toArr);  
        m.setFrom(mailAccount);  
        m.setSubject(mailName+"-" + titile);  
        m.setBody(content);
        
        for (String filename:attachments){
        	try {
				m.addAttachment(filename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        try {  
            return (m.send());
        } catch (Exception e) {  
            Log.e("MailApp", "Could not send email", e);
            return false;
        }  
	} 
	/** 
	* 把错误报告发送给服务器,包含新产生的和以前没发送的. 
	* 
	* @param ctx 
	*/ 
	private void sendCrashReportsToServer(Context ctx) { 
		String[] crFiles = getCrashReportFiles(ctx);
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));
			for (String fileName : sortedFiles) {
				File cr = new File(ctx.getFilesDir(), fileName);
//				postReport(cr);
				cr.delete();// 删除已发送的报告
			}
		} 
	} 

	/** 
	* 获取错误报告文件名 
	* @param ctx 
	* @return 
	*/ 
	private String[] getCrashReportFiles(Context ctx) { 
		File filesDir = ctx.getFilesDir();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		return filesDir.list(filter); 
	} 
	/** 
	* 保存错误信息到文件中 
	* @param ex 
	* @return 
	*/ 
	private String saveCrashInfoToFile(Throwable ex) {
		Writer info = new StringWriter();
		PrintWriter printWriter = new PrintWriter(info); 
		ex.printStackTrace(printWriter); 

		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		
		String result = info.toString();
		printWriter.close();
		mDeviceCrashInfo.put(STACK_TRACE, result); 

		try {
			long timestamp = System.currentTimeMillis();
			String fileName = "crash-" + timestamp + CRASH_REPORTER_EXTENSION;
			FileOutputStream trace = mContext.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			mDeviceCrashInfo.store(trace, null);
			trace.flush();
			trace.close(); 
			return fileName; 
		} catch (Exception e) { 
			Log.e(TAG, "an error occured while writing report file...", e); 
		} 
		return null; 
	} 

	/** 
	* 收集程序崩溃的设备信息 
	* 
	* @param ctx 
	*/ 
	public void collectCrashDeviceInfo(Context ctx) { 
		try {
			PackageManager pm = ctx.getPackageManager(); 
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 
					PackageManager.GET_ACTIVITIES); 
			if (pi != null) { 
				mDeviceCrashInfo.setProperty(VERSION_NAME, 
				pi.versionName == null ? "not set" : pi.versionName); 
				mDeviceCrashInfo.setProperty(VERSION_CODE, String.valueOf(pi.versionCode)); 
				//mailName=mailName+ (pi.versionName == null ? "not set" : pi.versionName);
			} 
		} catch (NameNotFoundException e) { 
			Log.e(TAG, "Error while collect package info", e); 
		} 
		//使用反射来收集设备信息.在Build类中包含各种设备信息, 
		//例如: 系统版本号,设备生产商 等帮助调试程序的有用信息 
		//具体信息请参考后面的截图 
		Field[] fields = Build.class.getDeclaredFields(); 
		for (Field field : fields) { 
			try { 
				field.setAccessible(true); 
				mDeviceCrashInfo.put(field.getName(), String.valueOf(field.get(null))); 
				if (DEBUG) { 
					Log.d(TAG, field.getName() + " : " + field.get(null)); 
				} 
			} catch (Exception e) { 
				Log.e(TAG, "Error while collect crash info", e); 
			} 
		} 
	} 

    public int sendCrashMail(String attchment) {  
        Mail m = new Mail(mailAccount,mailPassword);  
		String[] toArr = { mailAccount };
		m.setTo(toArr);  
        m.setFrom(mailAccount);  
        m.setSubject(mailName);  

        FileInputStream in;
        Properties p = new Properties();
        try {
			in = mContext.openFileInput(attchment);
	        p.load(in);
	    } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        m.setBody(p.toString().replace(",", "\r\n") ); 
        try {  
//            m.addAttachment(mContext.getFilesDir()+"/"+attchment); 
            if(m.send()) {   
            Log.i(TAG,"Email was sent successfully.");  
                          
            } else {  
                Log.i(TAG,"Email was sent failed.");  
            }  
        } catch (Exception e) {  
            Log.e("TAG", "Could not send email", e);  
        }  
  
        return 1;  
    } 
    
	public NetworkInfo getActiveNetwork(Context context){
	    if (context == null)  return null;
	    ConnectivityManager mConnMgr = (ConnectivityManager) context
	            .getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (mConnMgr == null) return null;
	    NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo(); // 获取活动网络连接信息
	    return aActiveInfo;
	}
} 
