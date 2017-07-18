package com.whhcxw.MobileCheck;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class UserManager {

	private final String TAG = this.getClass().getSimpleName();
	
	private final String PREFERENCES_USER = "preferences_user";
	
	private SharedPreferences mPreferences; 
	
	private Context mContext;
	
	private static UserManager instance;
	private UserManager(){
		
	}
	
	public static UserManager getInstance(){
		if(instance == null){
			instance = new UserManager();
		}
		return instance;		
	}
	
	public void init(Context ctx){
		mContext = ctx;
		mPreferences = getUserPreferences();
	}
	
	/**
	 * 返回登录用户
	 * */
	public String getUserName(){
		return mPreferences.getString("UserName", "");
	}
	
	/**
	 * 返回访问信令
	 * */
	public String getAccessToken(){
		return mPreferences.getString("AccessToken", "123");
	}
	
	/**
	 * 返回是否登录
	 * */
	public boolean isLogin(){
		return mPreferences.getBoolean("IsLogin", false);
	}
	
	/**
	 * 返回用户IP
	 */
	public String getIP(){
		return mPreferences.getString("ip", "");
	}
	
	/**
	 * 返回用户ID
	 */
	public String getUserID(){
		return mPreferences.getString("UserID", "");
	}
	
	/**
	 * 返回用户UserClass
	 */
	public String getUserClass(){
		return mPreferences.getString("Userclass", "");
	}
	
	/**
	 * 返回用户IMEI
	 */
	public String getIMEI(){
		return mPreferences.getString("IMEI", "");
	}
	

	/**
	 * 返回用户Telephone
	 */
	public String getTelephone(){
		return mPreferences.getString("Telephone", "");
	}
	
	/**
	 * 返回用户Email
	 */
	public String getEmail(){
		return mPreferences.getString("Email", "");
	}
	
	/**
	 * 返回用户RealName
	 */
	public String getRealName(){
		return mPreferences.getString("RealName", "");
	}
	
	/**
	 * 返回用户AreaID
	 */
	public String getAreaID(){
		return mPreferences.getString("AreaID", "");
	}
	/**
	 * 返回用户功能信息
	 */
	public String getRoleTypes(){
		return mPreferences.getString("RoleTypes", "");
	}
	
	public String getPassWord(){
		return mPreferences.getString("PassWord", "");
	}

	
	
	
	/**
	 * 保存在SharedPreferences 里
	 * @return 返回是否保存成功
	 * */
	public boolean saveUserInfo(Context ctx,String UserID,String UserName,String UserClass,String IMEI,String Telephone,String Email,String RealName,String AccessToken,String AreaID,String mRoleTypes,String PassWord){
		Editor editor = mContext.getSharedPreferences(PREFERENCES_USER,Context.MODE_PRIVATE).edit();	
		editor.putString("UserID", UserID);
		editor.putString("UserName", UserName);
		editor.putString("Userclass", UserClass);
		editor.putString("IMEI", IMEI);
		editor.putString("Telephone", Telephone);
		editor.putString("Email", Email);
		editor.putString("RealName", RealName);
		editor.putString("AccessToken", AccessToken);
		editor.putString("AreaID", AreaID);
		editor.putString("RoleTypes", mRoleTypes);
		editor.putString("PassWord", PassWord);
		editor.putBoolean("IsLogin", true);		
		
		boolean saveSucc = editor.commit();
		if(saveSucc == false){
			Log.e(TAG, "saveUserInfo error  context:");
		}
		return saveSucc;
	}
	
	public SharedPreferences getUserPreferences(){
		return mContext.getSharedPreferences(PREFERENCES_USER,Context.MODE_PRIVATE);
	}
	

	/**
	 * 设置用户是否登录
	 * @param b
	 * @return
	 */
	public boolean userIsLogin(boolean b){
		Editor editor = mContext.getSharedPreferences(PREFERENCES_USER,Context.MODE_PRIVATE).edit();	
		editor.putBoolean("IsLogin", b);		
		boolean saveSucc = editor.commit();
		if(saveSucc == false){
			Log.e(TAG, "saveUserInfo error  context:");
		}
		return saveSucc;
	}
	
	/**
	 *  保存网络状态改变
	 * @param b
	 * @return
	 */
	public boolean IsConnection(boolean b){
		Editor editor = mContext.getSharedPreferences(PREFERENCES_USER,Context.MODE_PRIVATE).edit();	
		editor.putBoolean("IsConnection", b);		
		boolean saveSucc = editor.commit();
		if(saveSucc == false){
			Log.e(TAG, "userIsConnection error  context:");
		}
		return saveSucc;
	}
	
	/**
	 * 返回  网络状态
	 */
	public boolean getIsConnection(){
		return mPreferences.getBoolean("IsConnection", false);
	}
	
	
	
	/**
	 * 保存系统参数
	 * @param cxt
	 * @param FPS 帧率
	 * @param BIT 码率
	 * @param pw_ph 幅面宽度*幅面高度
	 * @return
	 */
	public boolean saveSettingSystem(Context cxt,String FPS ,String BIT,String pw_ph,int PwPhSelect){
		Editor editor = mContext.getSharedPreferences(PREFERENCES_USER,Context.MODE_PRIVATE).edit();
		editor.putString("FPS", FPS);
		editor.putString("BIT", BIT);
		editor.putString("pw_ph", pw_ph);
		editor.putInt("pw_phSelect", PwPhSelect);
		boolean saveSucc = editor.commit();
		if(saveSucc == false){
			Log.e(TAG, "saveSettingSystem error  context:");
		}
		return saveSucc;
	}
	
	/**
	 * 返回  帧率
	 */
	public String getFPS(){
		return mPreferences.getString("FPS", "");
	}
	
	/**
	 * 返回  码率
	 */
	public String getBIT(){
		return mPreferences.getString("BIT", "");
	}

	/**
	 * 返回  幅面宽度*幅面高度
	 */
	public String getPw_ph(){
		return mPreferences.getString("pw_ph", "");
	}
	
	/**
	 * 返回 幅面 选择的ID
	 */
	public int getPwPhSelect(){
		return mPreferences.getInt("pw_phSelect", 0);
	}

	
}
