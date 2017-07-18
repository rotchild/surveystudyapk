package com.whhcxw.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.global.G;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainDeleteCaseAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ContentValues> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;
	
	private String mUserClass;
	private ArrayList<ContentValues> mAllData;
	private ArrayList<Map<Object, String>> mPicture_ArrayList;
	
	public MainDeleteCaseAdapter(Context mContext, ArrayList<ContentValues> mData, int mResourceID ,String userClass,ArrayList<ContentValues> allData) {
		super();
		this.mContext = mContext;
		this.mData = mData;
		this.mInflater = LayoutInflater.from(mContext);
		this.mResourceID = mResourceID;
		this.mUserClass = userClass;
		this.mAllData = allData;
		setPicture();
	}

	
	private void setPicture(){
		mPicture_ArrayList = new ArrayList<Map<Object,String>>();
		for(int i = 0 ;i<mData.size();i++){
			ContentValues contentValues = mData.get(i);
			/*String caseno= contentValues.getAsString(DBModle.Task.LinkCaseNo);
			String taskType = contentValues.getAsString(DBModle.Task.TaskType);
			if(mUserClass.equals(Main.SURVEY) && (!taskType.equals(mContext.getResources().getString(R.string.tasktype_repeat)) || !taskType.equals(mContext.getResources().getString(R.string.tasktype_survey)))){
				for(ContentValues values:mAllData){
					if(values.get(DBModle.Task.TaskType).equals(mContext.getResources().getString(R.string.tasktype_survey)) && values.get(DBModle.Task.CaseNo).equals(contentValues.get(DBModle.Task.CaseNo))){
						taskType = mContext.getResources().getString(R.string.tasktype_survey);
					}else if(values.get(DBModle.Task.TaskType).equals(mContext.getResources().getString(R.string.tasktype_repeat)) && values.get(DBModle.Task.CaseNo).equals(contentValues.get(DBModle.Task.CaseNo))){
						taskType = mContext.getResources().getString(R.string.tasktype_repeat);
					}
				}
			}
			String mPath_str = G.STORAGEPATH + caseno+"/"+taskType+"/";*/
			String mPath_str = contentValues.getAsString(DBModle.Task.ImgPath);
			Map<Object, String> picturePath = new HashMap<Object,String>();
			picturePath.put("path" , mPath_str);
			mPicture_ArrayList.add(picturePath);
		}
	}
		
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
	
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(mResourceID, null);
			holder.car_text=(TextView)convertView.findViewById(R.id.car);
			holder.time_text=(TextView)convertView.findViewById(R.id.time);
			holder.address_text=(TextView)convertView.findViewById(R.id.address);
			holder.report_text=(TextView)convertView.findViewById(R.id.report);
//			holder.state_text=(TextView)convertView.findViewById(R.id.state);
			holder.imageView = (ImageView)convertView.findViewById(R.id.imageview);
			
			holder.picture = (ImageView)convertView.findViewById(R.id.image);
			holder.taskType = (ImageView)convertView.findViewById(R.id.taskType);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		
		ContentValues contentValues = (ContentValues) mData.get(position);
				
			boolean b = (Boolean) contentValues.get("check");
			if(b){
				holder.imageView.setImageResource(R.drawable.checkbox_btn_off);
			}else {
				holder.imageView.setImageResource(R.drawable.checkbox_btn_on);
			}
		
			holder.car_text.setText(contentValues.get(DBModle.Task.CarMark).toString());
			holder.time_text.setText(contentValues.get(DBModle.Task.AccidentTime).toString());
			holder.address_text.setText(contentValues.get(DBModle.Task.Address).toString());
			holder.report_text.setText(contentValues.get(DBModle.Task.CaseNo).toString());
//			holder.state_text.setText(contentValues.get(DBModle.Task.TaskType).toString());
	
			
			String taskType = contentValues.getAsString(DBModle.Task.TaskType); 
			String frontState = contentValues.getAsString(DBModle.Task.FrontState); 
			if(taskType.equals(mContext.getResources().getString(R.string.tasktype_survey))){
				if(frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_WITH_PICTURE) ||frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_NO_PICTURE)){
					holder.taskType.setImageResource(R.drawable.tasktype_remove_finish2);
				} else {
					holder.taskType.setImageResource(R.drawable.tasktype_survey_finish2);
				}
			}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_large))){
				holder.taskType.setImageResource(R.drawable.tasktype_large_finish2);
			}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_danger))){
				holder.taskType.setImageResource(R.drawable.tasktype_danger_finish2);
			}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_recommend))){
				holder.taskType.setImageResource(R.drawable.tasktype_recommend_finish2);
			}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_repeat))){
				holder.taskType.setImageResource(R.drawable.tasktype_repeat_noaccept_finish2);
			}
			
			String mPath_str = mPicture_ArrayList.get(position).get("path");
			File file = new File(mPath_str);
			if(file.exists()){
				File[] files = file.listFiles();
				if(files.length !=0){
					BitmapFactory.Options options = new BitmapFactory.Options();
//					options.inJustDecodeBounds = true;
//					options.inSampleSize = G.computeInitialSampleSize(options, -1, 80 * 70);
//					options.inJustDecodeBounds = false;
					options.inSampleSize = 20;
					Bitmap bitmap = BitmapFactory.decodeFile(files[0].getAbsolutePath(), options);	
					holder.picture.setImageBitmap(bitmap);
				}else{
					holder.picture.setImageResource(R.drawable.picback);
				}
			}else{
				holder.picture.setImageResource(R.drawable.picback);
			}
		return convertView;
	}
	
	private class ViewHolder{
		public TextView car_text;
		public TextView time_text;
		public TextView address_text;
		public TextView report_text;
//		public TextView state_text;
		public ImageView imageView;
		public ImageView picture;
		public ImageView taskType;
	}

}
