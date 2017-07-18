package com.whhcxw.MobileCheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.adapter.DependenceCaseAdapter;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.pushservice.PushManager;
import com.whhcxw.theme.BaseActivity;
import com.whhcxw.ui.Titlebar2;

public class DependenceCase extends BaseActivity {

	private final String TAG = this.getClass().getName();
	
	private Context mContext;
	private Titlebar2 titlebar;
	private ListView mListView;
	private String mUserName;
	private String mTID;
	private ContentValues mCase_ContentValues;
//	private ContentValues mLink_values;

	public static final int LINKCASE = 988;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MobileCheckApplocation.activityInitFlag == 0)
			finish();
		setContentView(R.layout.main_dependencecase);
		mContext = this;
		initTitle();
	}

	public void initTitle() {
		initView();
		titlebar = (Titlebar2) findViewById(R.id.titlebar);
		titlebar.showLeft();
		titlebar.setLeftBackImagesRes(R.drawable.ui_titile_return);
//		titlebar.setLeftBackground(mContext.getResources().getDrawable(
//				R.drawable.ui_titlebar_bg_selector));
		titlebar.setCenterText(R.string.app_name);
	}

	public void initView() {
		Intent intent = getIntent();
		mTID = intent.getExtras().getString("TID");
		mCase_ContentValues = DBOperator.getTask(mTID);

		mUserName = UserManager.getInstance().getUserName();
		mListView = (ListView) this.findViewById(R.id.list);
		View nocaseLayout = (View) this.findViewById(R.id.nocase_layout);
		ArrayList<ContentValues> data = initData();
		if (data.size() == 0) {
			nocaseLayout.setVisibility(View.VISIBLE);
		}
		DependenceCaseAdapter adapter = new DependenceCaseAdapter(mContext, data, R.layout.main_dependence_list);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(mListViewOnClickListener);
	}

	/** 初始化数据 */
	public ArrayList<ContentValues> initData() {
		String caseNo = mCase_ContentValues.getAsString(DBModle.Task.CaseNo);
		ArrayList<ContentValues> arrayList = DBOperator.getNoFinishTasks( mUserName, caseNo);
		return arrayList;
	}

	OnItemClickListener mListViewOnClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			final DependenceCaseAdapter adapter = (DependenceCaseAdapter) arg0.getAdapter();

			final ContentValues mLink_values = (ContentValues) adapter.getItem(arg2);

			Dialog.negativeAndPositiveDialog(mContext,getString(R.string.dependenceCase_dialog),
					new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if(copyLocationCase(mLink_values)){
								Intent intent = new Intent();
								setResult(LINKCASE, intent);
								finish();
							}else{
								Dialog.negativeDialog(mContext, getString(R.string.dependenceCase_already_fail));
							}
							
						}
					});
		}
	};

	/** 拷贝本地案件 */
	public boolean copyLocationCase(ContentValues linkcaseValues) {
		if(mCase_ContentValues.getAsString(DBModle.Task.LinkCaseNo).equals(linkcaseValues.getAsString(DBModle.Task.LinkCaseNo))){
			return false;
		}
		
		String frontState = mCase_ContentValues.getAsString(DBModle.Task.FrontState);
		ContentValues taskLogValues =DBOperator.getTaskLogStart(linkcaseValues.getAsString(DBModle.Task.TID));
		
		ContentValues values = new ContentValues();
		values.put(DBModle.Task.LinkCaseNo, linkcaseValues.getAsString(DBModle.Task.LinkCaseNo));
		values.put(DBModle.Task.ImgPath, linkcaseValues.getAsString(DBModle.Task.ImgPath));
		values.put(DBModle.Task.AccidentTime, PushManager.getCurrentTime(taskLogValues.getAsString(DBModle.TaskLog.StateUpdateTime)));	
		if(!frontState.equals(DBModle.TaskLog.FrontState_START)){
			values.put(DBModle.Task.FrontState, DBModle.TaskLog.FrontState_START);	
		}
		
		
		if(DBOperator.updateTask(mTID, values)){
			String Longitude = linkcaseValues.getAsString(DBModle.Task.Longitude);
			String Latitude = linkcaseValues.getAsString(DBModle.Task.Latitude); 
			
			String startTime = taskLogValues.getAsString(DBModle.TaskLog.StateUpdateTime); 			
			
			if(frontState.equals(DBModle.TaskLog.FrontState_START)){
				if(DBOperator.uploadCaseService(mTID,linkcaseValues.getAsString(DBModle.Task.FrontState))){
					String fromFile = mCase_ContentValues.getAsString(DBModle.Task.ImgPath); 
					if(!fromFile.equals("")){
						String toFile = linkcaseValues.getAsString(DBModle.Task.ImgPath);
						if(copyPicture(fromFile,toFile,linkcaseValues)){
							Log.d(TAG, "copyPicture success!");
						}else {
							Dialog.negativeDialog(mContext,getString(R.string.dependenceCase_fail));
							Log.e(TAG, "copyPicture fail!");
							return false;
						}
					}
					UploadWork.sendDataChangeBroadcast(mContext);
				} else {
					Dialog.negativeDialog(mContext,getString(R.string.dependenceCase_already_fail));
				}
			} else {
				if(DBOperator.updateTaskSate(mTID, DBModle.TaskLog.FrontState_START,Longitude,Latitude,startTime)){
						
						UploadWork.sendDataChangeBroadcast(mContext);
					} else {
						Dialog.negativeDialog(mContext,getString(R.string.dependenceCase_already_fail));
					}
			}
			
			
			
		} else {
			Dialog.negativeDialog(mContext,getString(R.string.dependenceCase_fail));
		}
		
		return true;
		
	}

	/** 上传到达现场的时间，即拍第一张照片的时间 */
/*	public boolean updateTime() {
		String linkTID = mLink_values.getAsString(DBModle.Task.TID);
		ContentValues linkStartValues = DBOperator.getTaskLogStart(linkTID);
		if (DBOperator.updateTaskSate(mTID, DBModle.TaskLog.FrontState_START,
				linkStartValues.getAsString(DBModle.TaskLog.Longitude),
				linkStartValues.getAsString(DBModle.TaskLog.Latitude),
				linkStartValues.getAsString(DBModle.TaskLog.StateUpdateTime))) {
			return true;
		}
		return false;
	}*/

	private boolean copyPicture(String fromFile, String toFile,ContentValues linkCaseValues) {
		if(mCase_ContentValues.getAsString(DBModle.Task.LinkCaseNo).equals(linkCaseValues.getAsString(DBModle.Task.LinkCaseNo))){
			return true;
		}
		// 要复制的文件目录
		File[] currentFiles;
		File root = new File(fromFile);
		// 如同判断SD卡是否存在或者文件是否存在
		// 如果不存在则 return出去
		if (!root.exists()) {
			return false;
		}
		// 如果存在则获取当前目录下的全部文件 填充数组
		currentFiles = root.listFiles();

		// 目标目录
		File targetDir = new File(toFile);
		// 创建目录
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		// 遍历要复制该目录下的全部文件
		for (int i = 0; i < currentFiles.length; i++) {
			if (currentFiles[i].isDirectory())// 如果当前项为子目录 进行递归
			{
				copyPicture(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/",linkCaseValues);

			} else// 如果当前项为文件则进行文件拷贝
			{
				CopySdcardFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName());
				
				Log.d(TAG, "copy pirct:"+i);
				
				
			String pictureName = currentFiles[i].getName();
			/*	
				MD5 md5 = new MD5();
				String fileMa5 = "";
				try {
					fileMa5 = md5.getFileMD5String(new File(toFile + pictureName));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "文件找不到 :"+e);
				}*/
				
				String mLink_TID = linkCaseValues.getAsString(DBModle.Task.TID);
				String mLink_CaseNo =linkCaseValues.getAsString(DBModle.Task.CaseNo);
				
				//添加上传照片
				boolean bb =DBOperator.addPicture(mLink_TID,mLink_CaseNo, toFile + pictureName);
				if(bb){							
					UploadWork.sendDataChangeBroadcast(mContext);							
					Log.d(TAG, "拷贝照片   插入照片成功 :");
				}else{
					Log.e(TAG, "拷贝照片   插入照片失败 :");
				}
			}
		}
		return true;
	}

	// 文件拷贝
	// 要复制的目录下的所有非子目录(文件夹)文件拷贝
	private boolean CopySdcardFile(String fromFile, String toFile) {
		try {
			InputStream fosfrom = new FileInputStream(fromFile);
			OutputStream fosto = new FileOutputStream(toFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			fosfrom.close();
			fosto.close();
			return true;

		} catch (Exception ex) {
			return false;
		}
	}

}
