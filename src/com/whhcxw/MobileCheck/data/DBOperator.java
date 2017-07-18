package com.whhcxw.MobileCheck.data;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.whhcxw.MobileCheck.UserManager;
import com.whhcxw.MobileCheck.data.DBModle.*;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.global.G;

import android.content.ContentValues;
import android.util.Log;

/**
 * 数据库操作类
 * */
public class DBOperator {
	
	public static final String TAG = DBOperator.class.getSimpleName();
	

	/**
	 * 创建任务查勘任务
	 * @param cv 案件必要条件
	 * @return boolean 案件是否创建成功
	 * */
	public static boolean createTask(ContentValues cv){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		try{
			String CaseNo = cv.getAsString(Task.CaseNo);
			String TaskType = cv.getAsString(Task.TaskType);
			String selection = Task.CaseNo + " = ? and " + Task.TaskType + " = ?";
			ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.TASK, new String[]{Task.TID},selection, new String[]{CaseNo,TaskType});
			if(cvList.size() > 0){
				Log.d(TAG, "task exist create stop");
				return false;
			}
			sqLiteManager.beginTransaction();		
			if(sqLiteManager.Insert(DBModle.TASK, cv) > 0){				
				cvList = sqLiteManager.Select(DBModle.TASK, new String[]{Task.TID},selection, new String[]{CaseNo,TaskType});
				if(cvList.size() > 0){
					Log.d(TAG, "create task succ");
					String TID = cvList.get(0).getAsString(Task.TID);
					String FrontState = cv.getAsString(Task.FrontState);					
					String Longitude = cv.getAsString(Task.Longitude);
					String Latitude = cv.getAsString(Task.Latitude);
					if(updateTaskSate(TID, FrontState, Longitude, Latitude)){
						//创建任务并且创建字任务流成功
						Log.d(TAG, "create tasklog succ");
						sqLiteManager.setTransactionSuccessful(); // //设置事务处理成功，不设置会自动回滚不提交						
						result = true;
					}
				}					
			}
		}catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "create task fail" + e.getMessage());
		}finally{
			try {
				sqLiteManager.endTransaction();
			} catch (Exception e2) {
			}			
		}				
		
		return result;
	}
	
	/**
	 * 获取任务列表
	 * @param FrontOperator 前端操作员
	 * @return ArrayList<ContentValues> 返回所有任务
	 * */
	public static ArrayList<ContentValues> getTasks(String FrontOperator){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		
		String selection = DBModle.Task.FrontOperator + " = ? ";
		String[] selectionArgs = {FrontOperator};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.TASK, new String[]{"*"}, selection,selectionArgs,null,null, DBModle.Task.TID +" desc");		
		
		return cvList;
	}
	
	/**
	 * 获取任务
	 * @param TID 任务编号
	 * @return ArrayList<ContentValues> 返回任务
	 * */
	public static ContentValues getTask(String TID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		
		String selection = DBModle.Task.TID + " = ? ";
		String[] selectionArgs = {TID};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.TASK, new String[]{"*"}, selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}		
		return cv;
	}
	
	/**
	 * 获取任务
	 * @param CaseNo
	 * @param TaskType
	 * @return ContentValues 返回任务
	 */
	public static ContentValues getTask(String CaseNo , String TaskType){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		
		String selection = DBModle.Task.CaseNo + " = ? and " + DBModle.Task.TaskType + " = ?";
		String[] selectionArgs = {CaseNo,TaskType};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.TASK, new String[]{"*"}, selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	/**
	 * 拒绝任务
	 * @param TID 任务ID 
	 * @param Longitude
	 * @param Latitude
	 * @return boolean 是否拒绝任务成功
	 * */
	public static boolean refuseTask(String TID,String Longitude,String Latitude){
		return operatorTask(TID,Longitude,Latitude,DBModle.TaskLog.FrontState_REFUSE);
	}
	
	/**
	 * 完成任务
	 * @param FrontPrice 估损金额
	 * @param TID 任务ID 
	 * @param Longitude
	 * @param Latitude
	 * @return boolean 是否拒绝任务成功
	 * */
	public static boolean finishTask(String FrontPrice,String TID,String Longitude,String Latitude){				
		ContentValues taskCV = getTask(TID);
		String currentStage = taskCV.getAsString(DBModle.Task.FrontState);
		if(currentStage.equals(DBModle.TaskLog.FrontState_SUPPLY)){
			String args = DBModle.TaskLog.FrontState_FINISH + "," + DBModle.TaskLog.FrontState_SUPPLY;
			deleteTaskLog(TID, args);
		}
		String selection = DBModle.Task.TID + " = " + TID;
		ContentValues cv = new ContentValues();
		cv.put(DBModle.Task.FrontPrice, FrontPrice);
		if(SQLiteManager.getInstance().Update(DBModle.TASK, cv, selection, new String[]{}) > 0){
			return operatorTask(TID,Longitude,Latitude,DBModle.TaskLog.FrontState_FINISH);
		}else{
			Log.e(TAG, "finishTask update EstPrice error");
			return false;
		}
	}
	/**
	 * 销案
	 * @param TID 任务ID 
	 * @param Longitude
	 * @param Latitude
	 * @return boolean 是否销案成功
	 * */
	public static boolean clearTask(String TID,String Longitude,String Latitude,String taskState){
		return operatorTask(TID,Longitude,Latitude,taskState);
	}
	
	
	
	/**
	* 上报任务（风险，大案）  改变任务状态，并且上传任务
	* @param cv 任务信息
	* @param leader 领导
	* @param other 其他人， 多个用 “,” 分隔
	* @param MessageType 推送消息类型
	* @return boolean 是否操作成功
	* */
	public static boolean repostTask(ContentValues cv,String leader,String other,String MessageType){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		if(createTask(cv)){
			String CaseNo = cv.getAsString(DBModle.Task.CaseNo);
			String TaskType = cv.getAsString(DBModle.Task.TaskType);
			//将新的数据赋值给cv
			cv = getTask(CaseNo,TaskType);
			if(cv != null){
				String TID = cv.getAsString(DBModle.Task.TID);
				String selection = DBModle.Task.TID + " = ? ";
				String[] selectionArgs = {TID};
				ArrayList<ContentValues> taskLogCVList =  sqLiteManager.Select(DBModle.TASKLOG, new String[]{"*"}, selection, selectionArgs);
				if(taskLogCVList.size() > 0){
					String TaskJsonList = getTaskJsonList(cv,taskLogCVList);
					if(!TaskJsonList.equals("")){
						String AccessToken  = UserManager.getInstance().getAccessToken();
						String RequestURL = HttpParams.REPORTTASK;
						String Message = "news";
						String Keys = "TaskJsonList|Leader|Other|Message|MessageType|AccessToken";
						String Values =TaskJsonList + "|" + leader + "|" + other + "|" + Message +"|" + MessageType + "|" +AccessToken;
						ContentValues cvTaskStack = new ContentValues();
						cvTaskStack.put(DBModle.TaskStack.TID, TID);
						cvTaskStack.put(DBModle.TaskStack.Action, DBModle.TaskStack.Action_Http);						
						cvTaskStack.put(DBModle.TaskStack.StackType, "");
						cvTaskStack.put(DBModle.TaskStack.Level, DBModle.TaskStack.Level_Http);
						cvTaskStack.put(DBModle.TaskStack.IsRequest, DBModle.TaskStack.IsRequest_Y);
						cvTaskStack.put(DBModle.TaskStack.Description, "");
						cvTaskStack.put(DBModle.TaskStack.Data, "");
						cvTaskStack.put(DBModle.TaskStack.RequestURL, RequestURL);
						cvTaskStack.put(DBModle.TaskStack.Keys, Keys);
						cvTaskStack.put(DBModle.TaskStack.Vals, Values);
						//插入堆栈表
						long taskStackRt = sqLiteManager.Insert(DBModle.TASKSTACK, cvTaskStack);
						if(taskStackRt > 0){
							result = true;
						}
					}else{
						Log.e(TAG, "repostTask fail  getTaskJsonList error");
					}
				}else{
					Log.e(TAG, "repostTask fail  tasklog not find error");
				}						
			}else{
				Log.e(TAG, "repostTask fail  create error");
			}
		}
		return result;
	}
	 
	/**
	* 创建推修任务
	* @param cv 任务信息   backOperator 为选择的修理厂接待员
	* @param UserName 推送人
	* @param MessageType
	* @return boolean 是否操作成功
	* */
	public static boolean createGarageTask(ContentValues cv,String UserName,String MessageType,String BackOperator){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		if(createTask(cv)){
			String CaseNo = cv.getAsString(DBModle.Task.CaseNo);
			String TaskType = cv.getAsString(DBModle.Task.TaskType);
			//将新的数据赋值给cv
			cv = getTask(CaseNo,TaskType);
			if(cv != null){
				String TID = cv.getAsString(DBModle.Task.TID);
				String selection = DBModle.Task.TID + " = ? ";
				String[] selectionArgs = {TID};
				ArrayList<ContentValues> taskLogCVList =  sqLiteManager.Select(DBModle.TASKLOG, new String[]{"*"}, selection, selectionArgs);
				if(taskLogCVList.size() > 0){
					String TaskJsonList = getTaskJsonList(cv,taskLogCVList);
					if(!TaskJsonList.equals("")){
						String other = "";
						String Message = "news Garage Task";
						String AccessToken = UserManager.getInstance().getAccessToken();
						String RequestURL = HttpParams.REPORTTASK;
						String Keys = "TaskJsonList|Leader|Other|Message|MessageType|AccessToken|BackOperator";
						String Values = TaskJsonList + "|" + UserName + "|" + other + "|" + Message + "|" +MessageType +"|" + AccessToken+"|"+BackOperator;						
						ContentValues cvTaskStack = new ContentValues();
						cvTaskStack.put(DBModle.TaskStack.TID, TID);
						cvTaskStack.put(DBModle.TaskStack.Action, DBModle.TaskStack.Action_Http);						
						cvTaskStack.put(DBModle.TaskStack.StackType, "");
						cvTaskStack.put(DBModle.TaskStack.Level, DBModle.TaskStack.Level_Http);
						cvTaskStack.put(DBModle.TaskStack.IsRequest, DBModle.TaskStack.IsRequest_Y);
						cvTaskStack.put(DBModle.TaskStack.Description, "");
						cvTaskStack.put(DBModle.TaskStack.Data, "");
						cvTaskStack.put(DBModle.TaskStack.RequestURL, RequestURL);
						cvTaskStack.put(DBModle.TaskStack.Keys, Keys);
						cvTaskStack.put(DBModle.TaskStack.Vals, Values);
						//插入堆栈表
						long taskStackRt = sqLiteManager.Insert(DBModle.TASKSTACK, cvTaskStack);
						if(taskStackRt > 0){
							result = true;
						}
					}else{
						Log.e(TAG, "repostTask fail  getTaskJsonList error");
					}
				}else{
					Log.e(TAG, "repostTask fail  tasklog not find error");
				}						
			}else{
				Log.e(TAG, "repostTask fail  create error");
			}
		}
		return result;
	}
	
	/**
	* 修改推修任务
	* @param TID 任务信息
	* @param GarageID 修理厂ID
	* @param CarTypeID 车型ID
	* @param FrontPrice 推修金额
	* @param UserName 修理厂接待员
	* @param MessageType 推送类型
	* @return boolean 是否操作成功
	* */
	public static boolean updateGarageTask(String TID,String GarageID,String CarTypeID,String FrontPrice,String UserName,String MessageType ,String Message){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		ContentValues cv = new ContentValues();
		cv.put(DBModle.Task.GarageID, GarageID);
		cv.put(DBModle.Task.CarTypeID, CarTypeID);
		cv.put(DBModle.Task.FrontPrice, FrontPrice);
		cv.put(DBModle.Task.BackOperator, UserName);
		String selection = DBModle.Task.TID + " = ? ";
		String[] selectionArgs = {TID};
		long rt = sqLiteManager.Update(DBModle.TASK, cv, selection, selectionArgs);
		if(rt > 0){
			//将新的数据赋值给cv
			cv = getTask(TID);
			if(cv != null){
				//删除log 记录 和上传任务 并插入新的数据
				sqLiteManager.Delete(DBModle.TASKSTACK, selection, selectionArgs);
				
				ArrayList<ContentValues> taskLogCVList =  sqLiteManager.Select(DBModle.TASKLOG, new String[]{"*"}, selection, selectionArgs);
				if(taskLogCVList.size() > 0){
					String TaskJsonList = getTaskJsonList(cv,taskLogCVList);
					if(!TaskJsonList.equals("")){
						String other = "";
						String AccessToken = UserManager.getInstance().getAccessToken();
						String RequestURL = HttpParams.REPORTTASK;
						String Keys = "TaskJsonList|Leader|Other|MessageType|BackOperator|Message|AccessToken";;
						String Values = TaskJsonList + "|" + UserName + "|" + other + "|" +MessageType +"|"+UserName+"|"+Message+"|"+AccessToken;
						ContentValues cvTaskStack = new ContentValues();
						cvTaskStack.put(DBModle.TaskStack.TID, TID);
						cvTaskStack.put(DBModle.TaskStack.Action, DBModle.TaskStack.Action_Http);						
						cvTaskStack.put(DBModle.TaskStack.StackType, "");
						cvTaskStack.put(DBModle.TaskStack.Level, DBModle.TaskStack.Level_Http);
						cvTaskStack.put(DBModle.TaskStack.IsRequest, DBModle.TaskStack.IsRequest_Y);
						cvTaskStack.put(DBModle.TaskStack.Description, "");
						cvTaskStack.put(DBModle.TaskStack.Data, "");
						cvTaskStack.put(DBModle.TaskStack.RequestURL, RequestURL);
						cvTaskStack.put(DBModle.TaskStack.Keys, Keys);
						cvTaskStack.put(DBModle.TaskStack.Vals, Values);
						//插入堆栈表
						long taskStackRt = sqLiteManager.Insert(DBModle.TASKSTACK, cvTaskStack);
						if(taskStackRt > 0){
							result = true;
						}
					}else{
						Log.e(TAG, "repostTask fail  getTaskJsonList error");
					}
				}else{
					Log.e(TAG, "repostTask fail  tasklog not find error");
				}						
			}else{
				Log.e(TAG, "repostTask fail  create error");
			}
		}
		return result;
	}
	
	
	/**
	 * 修改TaskLog
	 * @param TID 任务ID
	 * @param values 需要修改的字段
	 * @return boolean true 修改成功 false 修改失败
	 */
	public static boolean updateTaskLog(String TID,ContentValues values,String FrontState){
		SQLiteManager liteManager = SQLiteManager.getInstance();
		String taskSelection = TaskLog.TID + " = " + TID + " and " + TaskLog.FrontState + " = " + FrontState;
		long update = liteManager.Update(DBModle.TASKLOG, values, taskSelection, new String[]{});
		if(update==-1){
			return false;
		}
		return true;
	}
	
	


	/**
	 * 操作任务  改变任务状态，并且上传任务
	 * @param TID 任务ID 
	 * @param Longitude
	 * @param Latitude
	 * @param FrontState 前段状态
	 * @return boolean 是否操作成功
	 * */
	private static boolean operatorTask(String TID,String Longitude,String Latitude,String FrontState){
		boolean result = false;
//		SQLiteManager sqLiteManager = SQLiteManager.getInstance();		
//		String selection = DBModle.Task.TID + " = ? ";
//		String[] selectionArgs = {TID};
		if(updateTaskSate(TID, FrontState, Longitude, Latitude)){
			result = uploadCaseService(TID,FrontState);
//			ArrayList<ContentValues> taskCVList =  sqLiteManager.Select(DBModle.TASK, new String[]{"*"}, selection, selectionArgs);
//			if(taskCVList.size() > 0){
//				ArrayList<ContentValues> taskLogCVList =  sqLiteManager.Select(DBModle.TASKLOG, new String[]{"*"}, selection, selectionArgs);
//				if(taskLogCVList.size() > 0){
//					ContentValues taskCv = taskCVList.get(0); 
//					String TaskJsonList = getTaskJsonList(taskCv,taskLogCVList);
//					if(!TaskJsonList.equals("")){
//						String AccessToken = UserManager.getInstance().getAccessToken();
//						String RequestURL = HttpParams.CREATETASK;
//						String Keys = "TaskJsonList|AccessToken";
//						String Values = TaskJsonList+ "|" + AccessToken;
//						ContentValues cv = new ContentValues();
//						cv.put(DBModle.TaskStack.TID, taskCv.getAsShort(DBModle.Task.TID));
//						cv.put(DBModle.TaskStack.Action, DBModle.TaskStack.Action_Http);						
//						cv.put(DBModle.TaskStack.StackType, "");
//						cv.put(DBModle.TaskStack.Level, DBModle.TaskStack.Level_Http);
//						cv.put(DBModle.TaskStack.IsRequest, DBModle.TaskStack.IsRequest_Y);
//						cv.put(DBModle.TaskStack.Description, "");
//						cv.put(DBModle.TaskStack.Data, "");
//						cv.put(DBModle.TaskStack.RequestURL, RequestURL);
//						cv.put(DBModle.TaskStack.Keys, Keys);
//						cv.put(DBModle.TaskStack.Vals, Values);
//						sqLiteManager.beginTransaction();
//						//插入堆栈表
//						long taskStackRt = sqLiteManager.Insert(DBModle.TASKSTACK, cv);
//						boolean refuseTask = true;
//						//如果是拒绝任务，就要删除本地task
//						if(FrontState.equals(DBModle.TaskLog.FrontState_REFUSE)){	
//							int delTask = sqLiteManager.Delete(DBModle.TASK, selection, selectionArgs); 
//							if( delTask == -1){
//								refuseTask = false;
//							}
//						}
//						if(taskStackRt > 0 && refuseTask){
//							sqLiteManager.setTransactionSuccessful();
//							Log.d(TAG, "operatorTask succ");
//							result = true;
//						}else{							
//							Log.e(TAG, "operatorTask fail");
//						}
//						sqLiteManager.endTransaction();
//					}	
//				}else{
//					Log.e(TAG, "operatorTask tasklog not find");
//				}				
//			}else{
//				Log.e(TAG, "operatorTask task not find");
//			}
		}
		
		return result;
	}
	
	/**
	 * 上传案件到服务器
	 * @param TID
	 * @param FrontState
	 * */
	public static boolean uploadCaseService(String TID,String FrontState){
		boolean result = false;
		String selection = DBModle.Task.TID + " = ? ";
		String[] selectionArgs = {TID};
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		ArrayList<ContentValues> taskCVList =  sqLiteManager.Select(DBModle.TASK, new String[]{"*"}, selection, selectionArgs);
		if(taskCVList.size() > 0){
			ArrayList<ContentValues> taskLogCVList =  sqLiteManager.Select(DBModle.TASKLOG, new String[]{"*"}, selection, selectionArgs);
			if(taskLogCVList.size() > 0){
				ContentValues taskCv = taskCVList.get(0); 
				String TaskJsonList = getTaskJsonList(taskCv,taskLogCVList);
				if(!TaskJsonList.equals("")){
					String AccessToken = UserManager.getInstance().getAccessToken();
					String RequestURL = HttpParams.CREATETASK;
					String Keys = "TaskJsonList|AccessToken";
					String Values = TaskJsonList+ "|" + AccessToken;
					ContentValues cv = new ContentValues();
					cv.put(DBModle.TaskStack.TID, taskCv.getAsShort(DBModle.Task.TID));
					cv.put(DBModle.TaskStack.Action, DBModle.TaskStack.Action_Http);						
					cv.put(DBModle.TaskStack.StackType, "");
					cv.put(DBModle.TaskStack.Level, DBModle.TaskStack.Level_Http);
					cv.put(DBModle.TaskStack.IsRequest, DBModle.TaskStack.IsRequest_Y);
					cv.put(DBModle.TaskStack.Description, "");
					cv.put(DBModle.TaskStack.Data, "");
					cv.put(DBModle.TaskStack.RequestURL, RequestURL);
					cv.put(DBModle.TaskStack.Keys, Keys);
					cv.put(DBModle.TaskStack.Vals, Values);
					sqLiteManager.beginTransaction();
					//插入堆栈表
					long taskStackRt = sqLiteManager.Insert(DBModle.TASKSTACK, cv);
					boolean refuseTask = true;
					//如果是拒绝任务，就要删除本地task
					if(FrontState.equals(DBModle.TaskLog.FrontState_REFUSE)){	
						int delTask = sqLiteManager.Delete(DBModle.TASK, selection, selectionArgs); 
						if( delTask == -1){
							refuseTask = false;
						}
					}
					if(taskStackRt > 0 && refuseTask){
						sqLiteManager.setTransactionSuccessful();
						Log.d(TAG, "operatorTask succ");
						result = true;
					}else{							
						Log.e(TAG, "operatorTask fail");
					}
					sqLiteManager.endTransaction();
				}	
			}else{
				Log.e(TAG, "operatorTask tasklog not find");
			}				
		}else{
			Log.e(TAG, "operatorTask task not find");
		}
		return result;		
	} 
	
	private static String getTaskJsonList(ContentValues taskCV,ArrayList<ContentValues> tasklogCVList){
		JSONArray jsonList = new JSONArray();
		for(int i = 0; i < tasklogCVList.size(); i++){
			try {
				ContentValues tasklogCV = tasklogCVList.get(i);
				JSONObject tasklog = new JSONObject();			
				tasklog.put(Task.CaseNo, taskCV.getAsString(Task.CaseNo));
				tasklog.put(Task.CarMark, taskCV.getAsString(Task.CarMark));
				tasklog.put(Task.CarOwner, taskCV.getAsString(Task.CarOwner));
				tasklog.put(Task.CarDriver, taskCV.getAsString(Task.CarDriver));
				tasklog.put(Task.Telephone, taskCV.getAsString(Task.Telephone));
				tasklog.put(Task.Address, taskCV.getAsString(Task.Address));
				tasklog.put(Task.FrontPrice, taskCV.getAsString(Task.FrontPrice));
				tasklog.put(Task.BackPrice, taskCV.getAsString(Task.BackPrice));
				tasklog.put(Task.FixedPrice, taskCV.getAsString(Task.FixedPrice));
				tasklog.put(Task.TaskType, taskCV.getAsString(Task.TaskType));
				tasklog.put(Task.FrontOperator, taskCV.getAsString(Task.FrontOperator));
				tasklog.put(Task.BackOperator, taskCV.getAsString(Task.BackOperator));
				tasklog.put(Task.BackState, taskCV.getAsString(Task.BackState));
				tasklog.put(Task.Watcher, taskCV.getAsString(Task.Watcher));
				tasklog.put(Task.AccidentTime, taskCV.getAsString(Task.AccidentTime));
				tasklog.put(Task.Memo, taskCV.getAsString(Task.Memo));
				tasklog.put(Task.GarageID, taskCV.getAsString(Task.GarageID));			
				tasklog.put(Task.CarTypeID, taskCV.getAsString(Task.CarTypeID));
				
				tasklog.put(Task.IsCooperation, taskCV.getAsString(Task.IsCooperation));
				tasklog.put(Task.CaseType, taskCV.getAsString(Task.CaseType));
				tasklog.put(Task.AutoGenerationNO, taskCV.getAsString(Task.AutoGenerationNO));
				tasklog.put(Task.CarType, taskCV.getAsString(Task.CarType));
				tasklog.put(Task.AccidentType, taskCV.getAsString(Task.AccidentType));
				tasklog.put(Task.InsuranceType, taskCV.getAsString(Task.InsuranceType));
				tasklog.put(Task.InsuranceID, taskCV.getAsString(Task.InsuranceID));
				tasklog.put(Task.InsuranceContactPeople, taskCV.getAsString(Task.InsuranceContactPeople));
				tasklog.put(Task.InsuranceContactTelephone, taskCV.getAsString(Task.InsuranceContactTelephone));
				tasklog.put(Task.PaymentAmount, taskCV.getAsString(Task.PaymentAmount));
				tasklog.put(Task.PaymentMethod, taskCV.getAsString(Task.PaymentMethod));
				tasklog.put(Task.IsRisk, taskCV.getAsInteger(Task.IsRisk));
				tasklog.put(Task.AccidentDescribe, taskCV.getAsString(Task.AccidentDescribe));
				tasklog.put(Task.InsuranceID, taskCV.getAsString(Task.InsuranceID));
				tasklog.put(Task.ThirdCar, taskCV.getAsString(Task.ThirdCar));
				tasklog.put(Task.AreaID, taskCV.getAsString(Task.AreaID));
				tasklog.put(Task.LinkCaseNo, taskCV.getAsString(Task.LinkCaseNo));

//				//记录的改变状态
				tasklog.put(TaskLog.FrontState, tasklogCV.getAsString(TaskLog.FrontState));				
				tasklog.put(TaskLog.StateUpdateTime, tasklogCV.getAsString(TaskLog.StateUpdateTime));
				tasklog.put(TaskLog.Longitude, tasklogCV.getAsString(TaskLog.Longitude));
				tasklog.put(TaskLog.Latitude, tasklogCV.getAsString(TaskLog.Latitude));	
								
				
				jsonList.put(tasklog);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try{					
					Log.d(TAG, "serialize fail taskCV:" + taskCV.toString() + " tasklogCV:" + tasklogCVList.get(i).toString());
				}catch (Exception e1) {
					Log.d(TAG, "serialize fail");
				}				
				return "";
			}			
		}
		return jsonList.toString();
	}
	
	/**
	 * 创建任务查勘任务
	 * @param TIDs 要删除的案件ID 多个用“,” 分隔
	 * @return int 删除成功几条
	 * */
	public static int deleteTask(String TIDs){
		int result = -1;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		try{
			String selection =  DBModle.Task.TID + " in("+ TIDs +") "; 
			result = sqLiteManager.Delete(DBModle.TASK, selection,null);
		}catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "create task fail" + e.getMessage());
		}			
		return result;
	}
	
	/**
	 * 更新案件状态
	 * @param TID 本地案件ID
	 * @param Longitude 经度
	 * @param Latitude 纬度
	 * @param stage
	 * 	stage,0,未分配
		stage,1,拒绝
		stage,3,接受
		stage,4,开始查勘(查勘员)(推修开始)
		stage,5,完成(推修完成)
		stage,7,调度错误
	 * */
	public static boolean updateTaskSate(String TID,String stage,String Longitude,String Latitude , String StateUpdateTime){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		try{			
			String selection = TaskLog.TID + " = ? and " + TaskLog.FrontState + " = ? ";
			ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.TASKLOG, new String[]{Task.TID},selection, new String[]{TID,stage});
			if(cvList.size() > 0){
				Log.e(TAG, "taskLog exist insert stop TID:" + TID + " stage:" + stage);
				return false;
			}			
			ContentValues taskcv = new ContentValues();
			taskcv.put(Task.FrontState, stage);
			String taskSelection = TaskLog.TID + " = " + TID;
			
			if(sqLiteManager.Update(DBModle.TASK, taskcv, taskSelection, new String[]{})> 0){
				ContentValues cv = new ContentValues();
				cv.put(TaskLog.TID, TID);
				cv.put(TaskLog.FrontState, stage);
				cv.put(TaskLog.StateUpdateTime, StateUpdateTime);
				cv.put(TaskLog.Longitude, Longitude);
				cv.put(TaskLog.Latitude, Latitude);
				if(sqLiteManager.Insert(DBModle.TASKLOG, cv) > 0){
					Log.d(TAG, "create tasklog succ TID:" + TID + " stage:" + stage);		
					result = true;
				}
			}else{
				Log.e(TAG, "update task FrontState fail TID:" + TID + " stage:" + stage);		
			}
		}catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "create tasklog fail TID:" + TID + " stage:" + stage + e.getMessage());
		}
		if(result){
			if(stage.equals(DBModle.TaskLog.FrontState_START) || stage.equals(DBModle.TaskLog.FrontState_ACCEPT)
					|| stage.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_NO_PICTURE)|| stage.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_WITH_PICTURE)){
				ContentValues cv = getTask(TID);
				String selection = DBModle.Task.TID + " = ? ";
				String[] selectionArgs = {TID};
				ArrayList<ContentValues> taskLogCVList =  sqLiteManager.Select(DBModle.TASKLOG, new String[]{"*"}, selection, selectionArgs);
				String TaskJsonList = getTaskJsonList(cv,taskLogCVList);
				
				//如果是开始任务 就提交并上传任务
				String Action = DBModle.TaskStack.Action_Http;
				String RequestURL = HttpParams.CREATETASK;
				String AccessToken = UserManager.getInstance().getAccessToken();
				String Keys = "TaskJsonList|AccessToken";
				String Values = TaskJsonList+"|"+AccessToken;
				String Level = DBModle.TaskStack.Level_Http;
				String IsRequest = DBModle.TaskStack.IsRequest_Y;
				String Description = "";
				String Data = "";
				result = addTaskStack(TID,Action,RequestURL,Keys,Values,Level,IsRequest,Description,Data);
			}
		}
		return result;
	}
	/**
	 * 更新本地数据库
	 * @param TID
	 * @param stage
	 * @return
	 */
	public static boolean updateTaskSate(String TID,String colum,String stage){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
			ContentValues taskcv = new ContentValues();
			taskcv.put(colum, stage);
			String taskSelection = TaskLog.TID + " = " + TID;
			
			if(sqLiteManager.Update(DBModle.TASK, taskcv, taskSelection, new String[]{})> 0){
					result = true;
			}else{
				result = false;
				Log.e(TAG, "update task isNew fail TID:" + TID + " stage:" + stage);		
			}
		return result;
	}
	
	
	
	public static boolean updateTaskSate(String TID,String stage,String Longitude,String Latitude){
		return updateTaskSate(TID,stage,Longitude,Latitude,G.getPhoneCurrentTime());
	}
	
	/**
	 * 修改推修任务完成时，上传任务列表
	 * @param TID
	 * @return
	 */
	public static boolean SupplyPictureState(String Longitude ,String Latitude , String CaseNo , String TID , String TaskType , String Operator ,String BackOperator , String BackState , String AccessToken ,String FrontState){
		String selection = DBModle.Task.TID + " = " + TID;
		ContentValues cv = new ContentValues();
		cv.put(DBModle.Task.StateUpdateTime, G.getPhoneCurrentTime());
		cv.put(DBModle.Task.BackOperator, BackOperator);
		cv.put(DBModle.Task.BackState, BackState);
		if(SQLiteManager.getInstance().Update(DBModle.TASK, cv, selection, new String[]{}) > 0){
			return operatorTask(TID,Longitude,Latitude,FrontState);
		}else{
			Log.e(TAG, "finishTask update EstPrice error");
			return false;
		}
		
				//额外数据
			/*	JSONObject jsonObject = new JSONObject();
				String UpdateFileds = "FrontState|StateUpdateTime|BackOperator|BackState";
				String UpdateValues = DBModle.TaskLog.FrontState_SUPPLY +"|" + G.getPhoneCurrentTime()+"|"+BackOperator+"|"+BackState;		
				try {
					jsonObject.put("UpdateFileds", UpdateFileds);
					jsonObject.put("UpdateValues", UpdateValues);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "json error:" + e.getMessage());
					return false;
				}
				
				
				String Action = DBModle.TaskStack.Action_Http;
				String RequestURL = HttpParams.CREATETASK;
				String Keys = "CaseNo|TaskType|Operator|AccessToken|FrontState|BackOperator|BackState";
				String Values = CaseNo+"|"+TaskType+"|"+Operator+"|"+AccessToken+"|"+DBModle.TaskLog.FrontState_SUPPLY+"|"+BackOperator+"|"+BackState;
				String Level = DBModle.TaskStack.Level_Http;
				String IsRequest = DBModle.TaskStack.IsRequest_Y;
				String Description = "";
				String Data = jsonObject.toString();
				
				return  addTaskStack(TID,Action,RequestURL,Keys,Values,Level,IsRequest,Description,Data);
		*/
	}
	
	
	
	/**
	 * 修改推修任务完成时，上传任务列表
	 * @param TID
	 * @return
	 */
	public static boolean repairUpdateState(String CaseNo , String TID , String TaskType , String Operator , String AccessToken){
		//额外数据
				JSONObject jsonObject = new JSONObject();
				String UpdateFileds = "FrontState|StateUpdateTime";
				String UpdateValues = DBModle.TaskLog.FrontState_FINISH +"|" + G.getPhoneCurrentTime();		
				try {
					jsonObject.put("UpdateFileds", UpdateFileds);
					jsonObject.put("UpdateValues", UpdateValues);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "json error:" + e.getMessage());
					return false;
				}
				
				
				String Action = DBModle.TaskStack.Action_Http;
				String RequestURL = HttpParams.UPDATETASK;
				String Keys = "CaseNo|TaskType|Operator|AccessToken|FrontState";
				String Values = CaseNo+"|"+TaskType+"|"+Operator+"|"+AccessToken+"|"+DBModle.TaskLog.FrontState_FINISH;
				String Level = DBModle.TaskStack.Level_Http;
				String IsRequest = DBModle.TaskStack.IsRequest_Y;
				String Description = "";
				String Data = jsonObject.toString();
				
				return  addTaskStack(TID,Action,RequestURL,Keys,Values,Level,IsRequest,Description,Data);
		
	}
	
	
	
	
	/**
	 * 添加一张照片到上传列表
	 * @param TID
	 * @param CaseNo 案件号
	 * @param FilePath 照片路径
	 * @param md5 照片
	 * @return 返回是否添加成功
	 * */
	public static boolean addPicture(String TID,String CaseNo,String FilePath){
		String Action = DBModle.TaskStack.Action_Upload;
		String AccessToken = UserManager.getInstance().getAccessToken();
		String RequestURL = HttpParams.UPLOADFILES;
		String Keys = DBModle.TaskStack.Keys_File +"|CaseNo|AccessToken"; //"|MD5";
		String Values = FilePath + "|" + CaseNo  +"|" + AccessToken ;//+ "|" + MD5;
//		String Keys = DBModle.TaskStack.Keys_File;
//		String Values = FilePath;
		String Level = DBModle.TaskStack.Level_Upload;
		String IsRequest = DBModle.TaskStack.IsRequest_Y;
		String Description = "";
		String Data = "";
		
		return addTaskStack(TID,Action,RequestURL,Keys,Values,Level,IsRequest,Description,Data);
	}
	
	/**
	 * 获取上传列表中，待上传的照片数
	 * @param TID
	 * @param IsRequest 上传状态
	 * @return
	 */
	public static ArrayList<ContentValues> getPicturesSum(String TID,String IsRequest){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();	
		String sql = "select * from TaskStack where TID = "+TID+" and IsRequest in ("+IsRequest+") and Keys = 'FilePath|CaseNo|AccessToken'";
		return sqLiteManager.rawQuery(sql,null);
	}
	
	/**
	 * 添加一个上传状态
	 * @param Action 参考 Action_Http Action_Upload Action_Download
	 * @param RequestURL 请求地址
	 * @param Keys 参数键"|"逗号分隔
	 * @param Values 参数值"|"逗号分隔
	 * @param Level 上传级别
	 * @param Description 描述
	 * @param Data 其他数据
	 * @return 是否添加成功
	 * */
	public static boolean addTaskStack(String TID,String Action,String RequestURL,String Keys,String Values,String Level,String IsRequest,String Description,String Data){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		try{	
			ContentValues cv = new ContentValues();
			cv.put(TaskStack.TID, TID);
			cv.put(TaskStack.Action, Action);
			cv.put(TaskStack.StackType, "0");
			cv.put(TaskStack.RequestURL, RequestURL);
			cv.put(TaskStack.Keys, Keys);
			cv.put(TaskStack.Vals, Values);
			cv.put(TaskStack.Level, Level);
			cv.put(TaskStack.IsRequest, IsRequest);
			cv.put(TaskStack.Description, Description);
			cv.put(TaskStack.Data, Data);

			if(sqLiteManager.Insert(DBModle.TASKSTACK, cv) > 0){
				Log.d(TAG, " addTaskStack succ");		
				result = true;
			}
		}catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "addTaskStack fail" + e.getMessage());
		}
		return result;
	}
	
	/**
	 * 获取车型列表
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ArrayList<ContentValues> getCarTypes(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.CARTYPE, new String[]{"*"},null,null);
		return cvList;
	}
	
	
	/**
	 * 获取修理厂列表
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ArrayList<ContentValues> getGarages(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.GARAGE, new String[]{"*"},null,null);
		return cvList;
	}
	
	
	/**
	 * 获取修理厂列表
	 * @param CarTypeID车型ID
	 * @return ArrayList<ContentValues> 
	 * 返回可以修这个车型的所有修理厂，并按金额上线倒序排序 MoneyLimit
	 * */
	public static ArrayList<ContentValues> getGarages(int Areaid,String CarTypeID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();	
//		String selection = Garage.CarType +" like '%" + CarTypeID + "%' and " +  Garage.AreaID +" like '%" + Areaid + "%' and UserName<>'' and ReachRate<1 ";
//		String orderBy  = Garage.ReachRate + " desc ";
//		return sqLiteManager.Select(DBModle.GARAGE, new String[]{"*"},selection,null,null,null, orderBy);
		
		String sql = "select Garage.* from Garage INNER JOIN Users ON Garage.UserName = Users.UserName where Garage.AreaID like '%" + Areaid + "%' and Garage.CarType like '%" + CarTypeID + "%' and " 
				+" Garage.UserName<>'' " + " and Garage.ReachRate < 1 and Users.UserState <> -1 "
				+" order by Garage.ReachRate desc ";
		
		return sqLiteManager.rawQuery(sql,null);

	}
	
	/**
	 * 获取修理厂列表       车行保单      取前5个
	 * @param AreaID  区域ID
	 * @param is4S  是否是4S店
	 * @param IsPartner 是否是合作单位
	 * @return ArrayList<ContentValues> 
	 * 返回可以修这个车型的所有修理厂，并按金额上线倒序排序 MoneyLimit     
	 * */
	public static ArrayList<ContentValues> getCooperationGarages(int AreaID,int is4S,int IsPartner){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();		
		String selection = "select Garage.* from Garage INNER JOIN Users ON Garage.UserName = Users.UserName where Garage.AreaID = " + AreaID + " and Garage.Is4s = " + is4S 
				+ " and Garage.IsPartner = " + IsPartner+" and Users.UserState <> -1 and Garage.UserName<>'' and  Garage.Email<>''"
				+" order by Garage.Is4s desc , Garage.ReachRate desc limit 5";
		
		return sqLiteManager.rawQuery(selection,null);
	}
	
	
	/**
	 * 获取修理厂列表       车行保单      取所有的
	 * @param AreaID  区域ID
	 * @param is4S  是否是4S店
	 * @param IsPartner 是否是合作单位
	 * @return ArrayList<ContentValues> 
	 * 返回可以修这个车型的所有修理厂，并按金额上线倒序排序 MoneyLimit
	 * */
	public static ArrayList<ContentValues> getAllCooperationGarages(int AreaID,int is4S,int IsPartner){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();		
		String selection = "select Garage.* from Garage INNER JOIN Users ON Garage.UserName = Users.UserName where Garage.AreaID = " + AreaID + " and Garage.Is4s = " + is4S + " and " +
				"Garage.IsPartner = " + IsPartner +" and Garage.UserName<>''" + " and Garage.ReachRate<1 and Users.UserState <> -1 "
				+" order by Garage.Is4s desc , Garage.ReachRate desc ";
		
		return sqLiteManager.rawQuery(selection,null);
	}
	
	
	/**
	 * 获取所有用户 按照最后登录时间倒叙排序
	 * @param AreaId
	 * @return ContentValues 返回用户
	 * */
	public static ArrayList<ContentValues> getUser(String AreaId , String UserClassID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection  = DBModle.Users.UserClass + " = ? and " + DBModle.Users.Email+" <>'' and "+DBModle.Users.UserState+" <> -1";
		String[] selectionArgs   = {UserClassID};
		return sqLiteManager.Select(DBModle.USERS, new String[]{"*"}, selection,selectionArgs);
	}
	
	
	/**
	 * 获取用户
	 * @param userName 用户名
	 * @return ContentValues 如有就返回内容，没有就返回null
	 * */
	public static ArrayList<ContentValues> getUsers(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();	
		String selection  = DBModle.Users.Email+" <>'' and "+DBModle.Users.UserState + " <> -1";
		return  sqLiteManager.Select(DBModle.USERS, new String[]{"*"}, selection, null);		
	}
	
	/**
	 * 获取所有用户
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ArrayList<ContentValues> getAllUsers(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.USERS, new String[]{"*"},null,null);
		return cvList;
	}
	
	
	/**
	 * 同步user表
	 * @param data 服务器返回的user数据
	 * @param UserName 当前登录用户
	 * @return 插入成功多少条数据
	 * @throws JSONException 
	 *  */
	public synchronized static int syncUsers(JSONArray data,String UserName){
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		//String selection = DBModle.Users.UserName + " != ?";
		int delCount = sqLiteManager.Delete(DBModle.USERS, null, null);
		Log.d(TAG, "sync user delete:"+delCount+" data" + " date:"+G.getPhoneCurrentTime());
		ContentValues cv = new ContentValues();	
		try {
			sqLiteManager.beginTransaction(); 
			for(int i = 0; i < data.length(); i++){
				cv.clear();
				JSONObject json = data.getJSONObject(i);						
				cv.put(DBModle.Users.UserName, json.getString("UserName"));
				cv.put(DBModle.Users.UserClass, json.getString("UserClass"));
				cv.put(DBModle.Users.AreaID, json.getString("AreaID"));
				cv.put(DBModle.Users.RealName, json.getString("RealName"));	
				cv.put(DBModle.Users.Email, json.getString("Email"));
				cv.put(DBModle.Users.UserState, json.getString("UserState"));
				if(sqLiteManager.Insert(DBModle.USERS, cv) > 0){
					result++;
				}
			}
			sqLiteManager.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sqLiteManager.endTransaction(); //处理完成
        }
		Log.d(TAG, "sync user insert:"+result+" data"+ " date:"+G.getPhoneCurrentTime());
		
		return result;
	}
	
	/**
	 * 同步CarType表
	 * @param data 服务器返回的user数据
	 * @return 插入成功多少条数据
	 * @throws JSONException 
	 *  */
	public synchronized static int syncCarType(JSONArray data) throws JSONException{
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		int delCount = sqLiteManager.Delete(DBModle.CARTYPE, null, null);
		Log.d(TAG, "sync Cartype delete:"+delCount+" data"+ " date:"+G.getPhoneCurrentTime());
		ContentValues cv = new ContentValues();
		try {
			sqLiteManager.beginTransaction(); 
			for(int i = 0; i < data.length(); i++){
				JSONObject json = data.getJSONObject(i);
				cv.clear();
				cv.put(DBModle.CarType.CarTypeID, json.getString("CarTypeID"));
				cv.put(DBModle.CarType.TypeName, json.getString("TypeName"));
				if(sqLiteManager.Insert(DBModle.CARTYPE, cv) > 0){
					result++;
				}
			}
			sqLiteManager.setTransactionSuccessful(); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sqLiteManager.endTransaction(); //处理完成
        }
		Log.d(TAG, "sync Cartype insert:"+result+" data"+ " date:"+G.getPhoneCurrentTime());
		
		return result;
	}
	
	
	/**
	 * 同步GetAreaList表
	 * @param data 服务器返回的user数据
	 * @return 插入成功多少条数据
	 * @throws JSONException 
	 *  */
	public synchronized static int syncArea(JSONArray data) throws JSONException{
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		int delCount = sqLiteManager.Delete(DBModle.AREAS, null, null);
		Log.d(TAG, "sync Area delete:" + delCount + " data"+ " date:"+G.getPhoneCurrentTime());
		ContentValues cv = new ContentValues();
		try {
			sqLiteManager.beginTransaction(); 
			for(int i = 0; i < data.length(); i++){
				JSONObject json = data.getJSONObject(i);
				cv.clear();
				cv.put(DBModle.Areas.AreaID, json.getString("AreaID"));
				cv.put(DBModle.Areas.AreaName, json.getString("AreaName"));
				cv.put(DBModle.Areas.AreaClass, json.getString("AreaClass"));
				cv.put(DBModle.Areas.ParentId, json.getString("ParentID"));
				cv.put(DBModle.Areas.AreaCode, json.getString("AreaCode"));
				if(sqLiteManager.Insert(DBModle.AREAS, cv) > 0){
					result++;
				}
			}
			sqLiteManager.setTransactionSuccessful(); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sqLiteManager.endTransaction(); //处理完成
        }
		Log.d(TAG, "sync Area insert:"+result+" data"+ " date:"+G.getPhoneCurrentTime());
		
		return result;
	}
	
	
	

	/**
	 * 同步GetInsuranceList表
	 * @param data 服务器返回的Insurance数据
	 * @return 插入成功多少条数据
	 * @throws JSONException 
	 *  */
	public synchronized static int syncInsurance(JSONArray data) throws JSONException{
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		int delCount = sqLiteManager.Delete(DBModle.INSURANCE, null, null);
		Log.d(TAG, "sync Insurance delete:" + delCount + " data"+ " date:"+G.getPhoneCurrentTime());
		ContentValues cv = new ContentValues();
		try {
			sqLiteManager.beginTransaction(); 
			for(int i = 0; i < data.length(); i++){
				JSONObject json = data.getJSONObject(i);
				cv.clear();
				cv.put(DBModle.Insurance.InsuranceID, json.getString("InsuranceID"));
				cv.put(DBModle.Insurance.InsuranceName, json.getString("InsuranceName"));
				cv.put(DBModle.Insurance.InsuranceLink, json.getString("InsuranceLink"));
				cv.put(DBModle.Insurance.UserNames, json.getString("UserNames"));
				if(sqLiteManager.Insert(DBModle.INSURANCE, cv) > 0){
					result++;
				}
			}
			sqLiteManager.setTransactionSuccessful(); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sqLiteManager.endTransaction(); //处理完成
        }
		Log.d(TAG, "sync Insurance insert:"+result+" data"+ " date:"+G.getPhoneCurrentTime());
		return result;
	}
	
	/**
	 * 获取Dict表
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ArrayList<ContentValues> getDict(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.DICT, new String[]{"*"},null,null);
		return cvList;
	}
	
	
	
	/**
	 * 同步Dict表
	 * @param data 服务器返回的Insurance数据
	 * @return 插入成功多少条数据
	 * @throws JSONException 
	 *  */
	public synchronized static int syncDict(JSONArray data) throws JSONException{
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		int delCount = sqLiteManager.Delete(DBModle.DICT, null, null);
		Log.d(TAG, "sync Dict delete:" + delCount + " data"+ " date:"+G.getPhoneCurrentTime());
		ContentValues cv = new ContentValues();
		try {
			sqLiteManager.beginTransaction(); 
			for(int i = 0; i < data.length(); i++){
				JSONObject json = data.getJSONObject(i);
				cv.clear();
				cv.put(DBModle.Dict.DictID, json.getString("DictID"));
				cv.put(DBModle.Dict.TableName, json.getString("TableName"));
				cv.put(DBModle.Dict.FieldName, json.getString("FieldName"));
				cv.put(DBModle.Dict.FieldValue, json.getString("FieldValue"));
				cv.put(DBModle.Dict.Description, json.getString("Description"));
				cv.put(DBModle.Dict.Memo, json.getString("Memo"));
				if(sqLiteManager.Insert(DBModle.DICT, cv) > 0){
					result++;
				}
			}
			sqLiteManager.setTransactionSuccessful(); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sqLiteManager.endTransaction(); //处理完成
        }
		Log.d(TAG, "sync Dict insert:"+result+" data"+ " date:"+G.getPhoneCurrentTime());
		return result;
	}
	
	
	/**
	 * 获取RoleDicts表
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ArrayList<ContentValues> getRoleDicts(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.ROLEDICTS, new String[]{"*"},null,null);
		return cvList;
	}
	
	/**
	 * 同步RoleDicts表
	 * @param data 服务器返回的Insurance数据
	 * @return 插入成功多少条数据
	 * @throws JSONException 
	 *  */
	public synchronized static int syncRoleDicts(JSONArray data) throws JSONException{
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		int delCount = sqLiteManager.Delete(DBModle.ROLEDICTS, null, null);
		Log.d(TAG, "sync RoleDicts delete:" + delCount + " data"+ " date:"+G.getPhoneCurrentTime());
		ContentValues cv = new ContentValues();
		try {
			sqLiteManager.beginTransaction(); 
			for(int i = 0; i < data.length(); i++){
				JSONObject json = data.getJSONObject(i);
				cv.clear();
				cv.put(DBModle.RoleDicts.RoleID, json.getString("RoleId"));
				cv.put(DBModle.RoleDicts.RoleType, json.getString("RoleType"));
				cv.put(DBModle.RoleDicts.RoleName, json.getString("RoleName"));
				if(sqLiteManager.Insert(DBModle.ROLEDICTS, cv) > 0){
					result++;
				}
			}
			sqLiteManager.setTransactionSuccessful(); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sqLiteManager.endTransaction(); //处理完成
        }
		Log.d(TAG, "sync RoleDicts insert:"+result+" data"+ " date:"+G.getPhoneCurrentTime());
		return result;
	}
	
	
	
	/**
	 * 同步Garage表
	 * @param data 服务器返回的user数据
	 * @return 插入成功多少条数据
	 *  */
	public synchronized static int syncGarage(JSONArray data){
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
				
		int delCount = sqLiteManager.Delete(DBModle.GARAGE, null, null);
		Log.d(TAG, "sync Garage delete:"+delCount+" data"+ " date:"+G.getPhoneCurrentTime());
		ContentValues cv = new ContentValues();
		try {
			sqLiteManager.beginTransaction(); 
			for(int i = 0; i < data.length(); i++){
				JSONObject json = data.getJSONObject(i);
				cv.clear();
				cv.put(DBModle.Garage.GarageID, json.getString("GarageID"));
				cv.put(DBModle.Garage.AreaID, json.getString("AreaID"));
				cv.put(DBModle.Garage.ShortName, json.getString("ShortName"));
				cv.put(DBModle.Garage.CarType, json.getString("CarType"));
				cv.put(DBModle.Garage.Telephone, json.getString("Telephone"));
				cv.put(DBModle.Garage.Address, json.getString("Address"));
				cv.put(DBModle.Garage.Latitude, json.getString("Latitude"));
				cv.put(DBModle.Garage.Longitude, json.getString("Longitude"));
				cv.put(DBModle.Garage.ImgPath, json.getString("ImgPath"));
				cv.put(DBModle.Garage.MoneyLimit, json.getString("MoneyLimit"));
				cv.put(DBModle.Garage.Memo, json.getString("Memo"));				
				cv.put(DBModle.Garage.UserName, json.getString("UserName"));
				cv.put(DBModle.Garage.Is4s, json.getString("Is4s"));
				cv.put(DBModle.Garage.IsPartner, json.getString("IsPartner"));
				cv.put(DBModle.Garage.IsSynthesize, json.getString("IsSynthesize"));
				cv.put(DBModle.Garage.MonthDone, json.getString("MonthDone"));
				cv.put(DBModle.Garage.ReachRate, json.getString("ReachRate"));
				cv.put(DBModle.Garage.FullName, json.getString("FullName"));
				if(sqLiteManager.Insert(DBModle.GARAGE, cv) > 0){
					result++;
				}
			}
			
			sqLiteManager.setTransactionSuccessful(); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			sqLiteManager.endTransaction(); //处理完成
        }
		Log.d(TAG, "sync Garage insert:"+result+" data"+ " date:"+G.getPhoneCurrentTime());
		
		
		return result;
	}
	
	/**
	 * 获取推修任务 么有CarType
	 * @param TID 
	 * @return ContentValues 返回任务
	 */
	public static ContentValues getCooperationGarageCase(String TID){		
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();		
		String selection = "select Task.*,Garage.ShortName,Garage.FullName from Task,CarType,Garage where Task.TID = ? and Task.GarageID = Garage.GarageID";
		String[] selectionArgs = {TID};
		ArrayList<ContentValues> cvList = sqLiteManager.rawQuery(selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	/**
	 * 获取推修任务
	 * @param TID 
	 * @return ContentValues 返回任务
	 */
	public static ContentValues getGarageCase(String TID){		
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();		
		String selection = "select Task.*,CarType.TypeName,Garage.ShortName,Garage.FullName from Task,CarType,Garage where Task.TID = ? and Task.CarTypeID = CarType.CarTypeID and Task.GarageID = Garage.GarageID";
		String[] selectionArgs = {TID};
		ArrayList<ContentValues> cvList = sqLiteManager.rawQuery(selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	
	/**
	 * 修改任务表
	 * @param TID
	 * @param values 需要修改的字段
	 * @return boolean true 修改成功 false 修改失败
	 */
	public static boolean updateTask(String TID,ContentValues values){
		SQLiteManager liteManager = SQLiteManager.getInstance();
		String taskSelection = Task.TID + " = " + TID;
		long update = liteManager.Update(DBModle.TASK, values, taskSelection, new String[]{});
		if(update==-1){
			return false;
		}
		return true;
	}
	
	/**
	 * 修改任务表
	 * @param CaseNo 报案号
	 * @param TaskType 任务类型
	 * @param values 需要修改的字段
	 * @return boolean true 修改成功 false 修改失败
	 */
	public static boolean updateTask(String CaseNo,String TaskType,ContentValues values){
		SQLiteManager liteManager = SQLiteManager.getInstance();
		String taskSelection = Task.CaseNo + " = ? and " + Task.TaskType + " = ? ";
		long update = liteManager.Update(DBModle.TASK, values, taskSelection, new String[]{CaseNo,TaskType});
		if(update==-1){
			return false;
		}
		return true;
	}
	
	/**
	 * 依据维修厂的ID获取维修厂信息
	 * 
	 * @param GarageID
	 * @return ContentValues
	 */
	public static ContentValues getGarage(String GarageID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection  = DBModle.Garage.GarageID + " = ?";
		String[] selectionArgs   = {GarageID};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.GARAGE, new String[]{"*"}, selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	/**
	 * 依据品牌的ID获取品牌信息
	 * 
	 * @param GarageID
	 * @return ContentValues
	 */
	public static ContentValues getCarType(String CarTypeID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection  = DBModle.CarType.CarTypeID + " = ?";
		String[] selectionArgs   = {CarTypeID};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.CARTYPE, new String[]{"*"}, selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	
	
	/**
	 * 获取区域列表
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ArrayList<ContentValues> getAreaList(){
//		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
//		String selection = DBModle.Areas.AreaClass + " = ?";
//		String[] selectionArgs = {"2"};
//		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.AREAS, new String[]{"*"},selection,selectionArgs);
//		return cvList;
		
		
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String sql = "SELECT DISTINCT Areas.* FROM Areas INNER JOIN Garage ON Areas.AreaID = Garage.AreaID GROUP BY AreaID";
		return sqLiteManager.rawQuery(sql,null);
	}
	
	/**
	 * 获取所有用户
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ArrayList<ContentValues> getArea(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.AREAS, new String[]{"*"},null,null);
		return cvList;
	}
	
	
	
	/**
	 * 依据区域ID 获取区域 信息
	 * @return ArrayList<ContentValues> 返回所有
	 * */
	public static ContentValues getArea(String AreaID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection  = DBModle.Areas.AreaID + " = ?";
		String[] selectionArgs   = {AreaID};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.AREAS, new String[]{"*"}, selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	/**
	 * 往短信任务表中
	 * 
	 * @param contentValues
	 * @param isRead
	 * @return
	 */
	public static boolean addMessages(ContentValues contentValues){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		int i = (int) sqLiteManager.Insert(DBModle.MESSAGE_SOURCES, contentValues);
		if(i ==-1){
			return false;
		}
		return true;
	}
	
	/**
	 * 获取短信任务表信息
	 * 
	 * @return ArrayList<ContentValues> 返回所有的短信任务
	 * 
	 */
	public static ArrayList<ContentValues> getMessageSources(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.MESSAGE_SOURCES, new String[]{"*"},null,null);
		return cvList;
	}
	
	/**
	 * 修改短信任务表
	 * @param SMSID
	 * @param values 需要修改的字段
	 * @return boolean true 修改成功 false 修改失败
	 */
	public static boolean updateMessageSources(String SMSID,ContentValues values){
		SQLiteManager liteManager = SQLiteManager.getInstance();
		String taskSelection = DBModle.MessageSources.SMSID + " = " + SMSID;
		long update = liteManager.Update(DBModle.MESSAGE_SOURCES, values, taskSelection, new String[]{});
		if(update==-1){
			return false;
		}
		return true;
	}
	
	/**
	 * 依据 修理厂的简称，查询出对应修理厂
	 * @param GarageShortName 修理厂简称
	 * @return
	 */
	public static ContentValues getGarageContentValues(String GarageShortName){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection  = DBModle.Garage.ShortName + " = ?";
		String[] selectionArgs   = {GarageShortName};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.GARAGE, new String[]{"*"}, selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	/**
	 * 解绑推送用户
	 * 
	 * @param UserID
	 * @param UserName
	 * @param AccessToken
	 * @return
	 */
	public static boolean SetUnBindPushUser(String UserName , String AccessToken){
		String TID = "";
		String Action = DBModle.TaskStack.Action_Http;
		String RequestURL = HttpParams.UNBINDPUSHUSER;
		String Keys = "UserName|AccessToken";
		String Values =UserName+"|"+AccessToken;
		String Level = DBModle.TaskStack.Level_Http;
		String IsRequest = DBModle.TaskStack.IsRequest_Y;
		String Description = "";
		String Data = "";
		return addTaskStack(TID, Action, RequestURL, Keys, Values, Level, IsRequest, Description, Data);
	}
	
	/**
	 * 获取上传任务列表
	 * 
	 * @return ArrayList<ContentValues>
	 */
	public static ArrayList<ContentValues> getTaskStack(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		ArrayList<ContentValues> list = sqLiteManager.Select(DBModle.TASKSTACK, null, null, null);
		return list;
	}
	
	/**
	 * 获取上传任务列表
	 * 
	 * @return ArrayList<ContentValues>
	 */
	public static ArrayList<ContentValues> getTaskStack(String isRequest){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection = DBModle.TaskStack.IsRequest + " in ("+isRequest+") ";
		String[] selectionArgs = new String[]{};
		ArrayList<ContentValues> list = sqLiteManager.Select(DBModle.TASKSTACK, new String[]{"*"}, selection, selectionArgs);
		return list;
	}
	
	
	/**
	 * 依据StackID  删除上传任务列表中的任务
	 * 
	 * @param StackID
	 * @return
	 */
	public static boolean deleteTaskStack(String StackID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection = DBModle.TaskStack.StackID + " = ? ";
		String[] selectionArgs = new String[]{StackID};
		int i = sqLiteManager.Delete(DBModle.TASKSTACK, selection, selectionArgs);
		if(i == -1){
			return false;
		}
		return true;
	}
	
	/**
	 * 依据StackID  IsRequest  删除上传任务列表中的任务
	 * 
	 * @param StackID
	 * @return
	 */
	public static boolean deleteTaskStack(String StackID,String IsRequest){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String sql = "DELETE FROM TaskStack where tid = '"+StackID+"' and isrequest = '"+IsRequest+"'";
		return sqLiteManager.ExecSQL(sql);
	}
	
	
	/**
	 * 依据 IsRequest 删除上传任务列表中的任务 当IsRequest 为上传完成状态时IsRequest = 2 时风险记录
	 * 
	 * @param StackID
	 * @return
	 */
	public static boolean deleteTaskStackByIsRequest(String[] IsRequest , String date){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String sql = "delete from TaskStack where IsRequest in ( "+IsRequest[0] +","+ IsRequest[1]+" ) and TID in (SELECT TID from TaskLog WHERE strftime('%Y-%m-%d' , StateUpdateTime) <= '"+date+"' AND FrontState = 5)";
		return sqLiteManager.ExecSQL(sql);
	}
	
	/**
	 * 修改上传任务表
	 * @param TID
	 * @param values 需要修改的字段
	 * @return boolean true 修改成功 false 修改失败
	 */
	public static boolean updateTaskStack(String StackID,ContentValues values){
		SQLiteManager liteManager = SQLiteManager.getInstance();
		String taskSelection = DBModle.TaskStack.StackID + " = " + StackID;
		long update = liteManager.Update(DBModle.TASKSTACK, values, taskSelection, new String[]{});
		if(update==-1){
			return false;
		}
		return true;
	}
	
	
	
	
	/**
	 * 获取未完成任务列表
	 * @param FrontOperator 前端操作员
	 * @param CaseNo 当前任务的案件号
	 * @return ArrayList<ContentValues> 返回所有任务
	 * */
	public static ArrayList<ContentValues> getNoFinishTasks(String FrontOperator , String CaseNo){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		
		String selection = DBModle.Task.FrontOperator + " = ? and "  + DBModle.Task.FrontState + " < ? and "+ DBModle.Task.FrontState +" >= ? and " +DBModle.Task.CaseNo + " <> ?" ;
		String[] selectionArgs = {FrontOperator, DBModle.TaskLog.FrontState_FINISH , DBModle.TaskLog.FrontState_START , CaseNo};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.TASK, new String[]{"*"}, selection,selectionArgs,null,null, DBModle.Task.TID +" desc");		
		
		return cvList;
	}
	
	/**
	 * 获取被关联的任务开始查勘的状态
	 * @param TID 
	 * @return
	 */
	public static ContentValues getTaskLogStart(String TID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection  = DBModle.TaskLog.TID + " = ? and " + DBModle.TaskLog.FrontState +" = ? ";
		String[] selectionArgs   = {TID , DBModle.TaskLog.FrontState_START};
		ArrayList<ContentValues> cvList = sqLiteManager.Select(DBModle.TASKLOG, new String[]{"*"}, selection,selectionArgs);
		ContentValues cv = null;
		if(cvList.size() > 0){
			cv = cvList.get(0);
		}
		return cv;
	}
	
	/**
	 * 获取保险公司列表
	 * @return
	 */
	public static ArrayList<ContentValues> getInsuranceList(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		return sqLiteManager.Select(DBModle.INSURANCE, new String[]{"*"}, null,null);
	}
	
	/**
	 * 依据InsuranceID 获取保险公司信息
	 */
	public static ContentValues getInsurance(String InsuranceID){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection = DBModle.Insurance.InsuranceID +" = ? ";
		String[] selectionArgs = {InsuranceID};
		ArrayList<ContentValues> cvList= sqLiteManager.Select(DBModle.INSURANCE, new String[]{"*"}, selection, selectionArgs);
		ContentValues cv=null;
		if(cvList.size()>0){
			cv= cvList.get(0);
		}
		return cv;
	}
	
	
	/**
	 * 依据Insurance名字  获取保险公司信息
	 * 
	 */
	public static ContentValues getInsuranceByName(String InsuranceName){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String selection = DBModle.Insurance.InsuranceName +" = ? ";
		String[] selectionArgs = {InsuranceName};
		ArrayList<ContentValues> cvList= sqLiteManager.Select(DBModle.INSURANCE, new String[]{"*"}, selection, selectionArgs);
		ContentValues cv=null;
		if(cvList.size()>0){
			cv= cvList.get(0);
		}
		return cv;
	}
	
	
	/**
	 * 删除TaskLog记录
	 * @param TID
	 * @param FrontState
	 * @return
	 */
	public static int deleteTaskLog(String TID,String FrontStates){
		int result = 0;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		try{
			String selection =  DBModle.TaskLog.TID + " = "+ TID +" and " +DBModle.TaskLog.FrontState + " in (" + FrontStates +")"; 
			result = sqLiteManager.Delete(DBModle.TASKLOG, selection,null);
		}catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "delete TaskLog fail" + e.getMessage());
		}			
		return result;
	}
	
	/**
	 * 查询TaskLog记录
	 * @param TID
	 * @param FrontState
	 * @return
	 */
	public static ArrayList<ContentValues> selectTaskLog(String TID,String FrontStates){
		ArrayList<ContentValues> result;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		try{
			String selection =  DBModle.TaskLog.TID + " = "+ TID +" and " +DBModle.TaskLog.FrontState + " in (" + FrontStates +")"; 
			result = sqLiteManager.Select(DBModle.TASKLOG, new String[]{"count(*)"}, selection, null);//(DBModle.TASKLOG, selection,null);
		}catch (Exception e) {
			// TODO: handle exception
			result = null;
			Log.d(TAG, "delete TaskLog fail" + e.getMessage());
		}			
		return result;
	}
	
	/**
	 * 江西5.0升级到5.8
	 * @return
	 */
	public static boolean updateV5_5up5_8(){
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String sql = "update Task  set linkcaseno = Task.caseno , ImgPath = '"+G.STORAGEPATH+"'||caseno||'/'||Task.TaskType||'/'";
		return sqLiteManager.ExecSQL(sql);
	}

	/**
	 * 
	 * @param datelength 删除保留多长时间外的案件
	 * @return
	 */
	public static boolean  deleteOverdueCase(String date){
		 SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String sql = "DELETE FROM Task WHERE TID IN (SELECT TID from TaskLog WHERE strftime('%Y-%m-%d' , StateUpdateTime) <= '"+date+"' AND FrontState = 5)";
		boolean result = sqLiteManager.ExecSQL(sql);
		return result;
	}
	
	/**
	 * 添加推送消息表
	 * */
	public static boolean addPushMessage(String Alert,String Sound,String Sender,String Receiver,String MessageType,String Data,String Platform,String CaseNo ,String TaskType,String IsRead,String UserName){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		try{	
			ContentValues cv = new ContentValues();
			cv.put(DBModle.PushMessage.Alert, Alert);
			cv.put(DBModle.PushMessage.Sound, Sound);
			cv.put(DBModle.PushMessage.Sender, Sender);
			cv.put(DBModle.PushMessage.Receiver, Receiver);
			cv.put(DBModle.PushMessage.MessageType, MessageType);
			cv.put(DBModle.PushMessage.Data, Data);
			cv.put(DBModle.PushMessage.Platform, Platform);
			cv.put(DBModle.PushMessage.CaseNo, CaseNo);
			cv.put(DBModle.PushMessage.TaskType, TaskType);
			cv.put(DBModle.PushMessage.IsRead, IsRead);
			cv.put(DBModle.PushMessage.PushTime, G.getPhoneCurrentTime());
			cv.put(DBModle.PushMessage.UserName, UserName);
			if(sqLiteManager.Insert(DBModle.PUSHMESSAGE, cv) > 0){
				Log.d(TAG, " addPushMessage succ");		
				result = true;
			}
		}catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "addPushMessage fail" + e.getMessage());
		}
		return result;
	}
	
	/**
	 * 查询PushMessage记录
	 * @return ArrayList<ContentValues>
	 */
	public static ArrayList<ContentValues> getPushMessage(String UserName){
		ArrayList<ContentValues> result;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		try{
			String selection  =  DBModle.PushMessage.UserName + " = ? ";
			String[] selectionArgs  = {UserName};
			String orderBy = DBModle.PushMessage.PushTime + " desc";
			result = sqLiteManager.Select(DBModle.PUSHMESSAGE,  new String[]{"*"}, selection, selectionArgs, null, null, orderBy);//(DBModle.TASKLOG, selection,null);
		}catch (Exception e) {
			// TODO: handle exception
			result = null;
		}			
		return result;
	}
	
	/**
	 * 
	 * @param datelength 删除保留多长时间外的案件
	 * @return
	 */
	public static boolean  deleteOverduePushmessage(String date){
		 SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		String sql = "DELETE FROM PushMessage WHERE  strftime('%Y-%m-%d' , PushTime) <= '"+date+"'";
		boolean result = sqLiteManager.ExecSQL(sql);
		return result;
	}
	
	
/**
 * 添加常用参数表
 * @param ParamsID 
 * @param ParamsType 
 * @param ParamsName
 * @param ParamsValue
 * @param Description
 * @param UpdateTime
 * @return
 */
	public static boolean addParams(String ParamsID,String ParamsType,String ParamsName,String ParamsValue,String Description,String UpdateTime){
		boolean result = false;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();
		try{	
			ContentValues cv = new ContentValues();
			cv.put(DBModle.Params.ParamsID, ParamsID);
			cv.put(DBModle.Params.ParamsType, ParamsType);
			cv.put(DBModle.Params.ParamsName, ParamsName);
			cv.put(DBModle.Params.ParamsValue, ParamsValue);
			cv.put(DBModle.Params.Description, Description);
			cv.put(DBModle.Params.UpdateTime, UpdateTime);
			if(sqLiteManager.Insert(DBModle.PARAMS, cv) > 0){
				Log.d(TAG, " addParams succ");		
				result = true;
			}
		}catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "addParams fail" + e.getMessage());
		}
		return result;
	}
	
	/**
	 * 添加常用参数表
	 * @param ParamsType 需要修改的表名
	 * @param UpdateTime 修改时间
	 * @return
	 */
	public static boolean updateParams(String ParamsType,String UpdateTime ){
		SQLiteManager liteManager = SQLiteManager.getInstance();
		ContentValues values = new ContentValues();
		values.put(DBModle.Params.UpdateTime, UpdateTime);
		String selection =DBModle.Params.ParamsType + " = ? ";
		String[] selectionArgs = {ParamsType};
		long update = liteManager.Update(DBModle.PARAMS, values, selection, selectionArgs);
		if(update==-1){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 获取添加常用参数表
	 * @param ParamsType 表名
	 * @return ArrayList<ContentValues>
	 */
	public static ContentValues getParams(String ParamsType){
		ContentValues values = null;
		SQLiteManager sqLiteManager = SQLiteManager.getInstance();				
		try{
			String selection  =  DBModle.Params.ParamsType + " = ? ";
			String[] selectionArgs  = {ParamsType};
			ArrayList<ContentValues> result = sqLiteManager.Select(DBModle.PARAMS,  new String[]{"*"}, selection, selectionArgs);
			if(!result.isEmpty()){
				values = result.get(0);
			}
		}catch (Exception e) {
			// TODO: handle exception
			values = null;
		}			
		return values;
	}
	
	
}
