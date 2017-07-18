package com.whhcxw.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.readystatesoftware.viewbadger.BadgeView;
import com.whhcxw.MobileCheck.Main;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.global.G;
import com.whhcxw.ui.PictureProgressBar;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainCaseAdapter extends BaseAdapter {

	private final String TAG = this.getClass().getSimpleName();
	private Context mContext;
	private ArrayList<ContentValues> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;

	private String mUserClass;
	private ArrayList<ContentValues> mAllData;

	private ArrayList<Map<Object, String>> mPicture_ArrayList;

	private int droidGreen = -1;


	public MainCaseAdapter(Context mContext, ArrayList<ContentValues> mData, int mResourceID ,String userClass ,ArrayList<ContentValues> allData) {
		super();
		this.mContext = mContext;
		this.mData = mData;
		this.mInflater = LayoutInflater.from(mContext);
		this.mResourceID = mResourceID;
		this.mUserClass = userClass;
		this.mAllData = allData;
		if(!mUserClass.equals(Main.RECEPTIONIST)){
			setPicture();
		}

		droidGreen = Color.parseColor("#f57e14");
	}

	private void setPicture(){
		mPicture_ArrayList = new ArrayList<Map<Object,String>>();
		for(int i = 0 ;i<mData.size();i++){
			ContentValues contentValues = mData.get(i);
			/*String caseno= contentValues.getAsString(DBModle.Task.LinkCaseNo);
			String taskType = contentValues.getAsString(DBModle.Task.TaskType);
			if(mUserClass.equals(Main.SURVEY) && (!taskType.equals(mContext.getResources().getString(R.string.tasktype_diaodu)) || !taskType.equals(mContext.getResources().getString(R.string.tasktype_repeat)) || !taskType.equals(mContext.getResources().getString(R.string.tasktype_survey)))){
				for(ContentValues values:mAllData){
					if(values.get(DBModle.Task.TaskType).equals(mContext.getResources().getString(R.string.tasktype_survey)) && values.get(DBModle.Task.CaseNo).equals(contentValues.get(DBModle.Task.CaseNo))){
						taskType = mContext.getResources().getString(R.string.tasktype_survey);
					}else if(values.get(DBModle.Task.TaskType).equals(mContext.getResources().getString(R.string.tasktype_repeat)) && values.get(DBModle.Task.CaseNo).equals(contentValues.get(DBModle.Task.CaseNo))){
						taskType = mContext.getResources().getString(R.string.tasktype_repeat);
					}else if(values.get(DBModle.Task.TaskType).equals(mContext.getResources().getString(R.string.tasktype_diaodu)) && values.get(DBModle.Task.CaseNo).equals(contentValues.get(DBModle.Task.CaseNo))){
						taskType = mContext.getResources().getString(R.string.tasktype_diaodu);
					} 				
				}
			}
			String mPath_str = G.STORAGEPATH + caseno+"/"+taskType+"/";  */
			String mPath_str="";
			try {
				mPath_str = contentValues.getAsString(DBModle.Task.ImgPath);
			} catch (Exception e) {
				// TODO: handle exception
				mPath_str = G.STORAGEPATH + contentValues.getAsString(DBModle.Task.CaseNo)+"/"+mContext.getResources().getString(R.string.tasktype_survey)+"/";
			}
			if(mPath_str.contains(mContext.getResources().getString(R.string.tasktype_danger))||mPath_str.contains(mContext.getResources().getString(R.string.tasktype_large))||mPath_str.contains(mContext.getResources().getString(R.string.tasktype_recommend))){
				mPath_str = G.STORAGEPATH + contentValues.getAsString(DBModle.Task.CaseNo)+"/"+mContext.getResources().getString(R.string.tasktype_survey)+"/";
			}
			
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

	public void updateData( ArrayList<ContentValues> _data){
		mData =  _data;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "MainCaseAdapter getView position:"+position);
		ViewHolder holder = null;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(mResourceID, null);
			holder.car_text=(TextView)convertView.findViewById(R.id.car);
			holder.time_text=(TextView)convertView.findViewById(R.id.time);
			holder.address_text=(TextView)convertView.findViewById(R.id.address);
			holder.report_text=(TextView)convertView.findViewById(R.id.report);
			holder.imageView = (ImageView)convertView.findViewById(R.id.image);

			holder.taskType = (ImageView)convertView.findViewById(R.id.taskType);
			holder.newmessage = (ImageView)convertView.findViewById(R.id.newmessage);

//			holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressbar);
			holder.pictureProgressBar =(PictureProgressBar)convertView.findViewById(R.id.pictureProgressBar);

			holder.number = (TextView)convertView.findViewById(R.id.number);
			holder.badgeView = new BadgeView(mContext,holder.number);
			holder.badgeView.setBadgeBackgroundColor(droidGreen);
			holder.badgeView.setTextColor(Color.WHITE);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}

		ContentValues map = (ContentValues) mData.get(position);


		//推送，新案件
		if(map.getAsBoolean(DBModle.Task.IsNew)){
			holder.newmessage.setVisibility(View.VISIBLE);
		}else {
			holder.newmessage.setVisibility(View.GONE);
		}


		String map_caseno = map.get(DBModle.Task.CaseNo).toString();

		holder.car_text.setText(map.get(DBModle.Task.CarMark).toString());
		holder.time_text.setText(map.get(DBModle.Task.AccidentTime).toString());
		holder.address_text.setText(map.get(DBModle.Task.Address).toString());
		holder.report_text.setText(map_caseno);
		String taskType = map.getAsString(DBModle.Task.TaskType); 
		String frontState = map.getAsString(DBModle.Task.FrontState);
		if(taskType.equals(mContext.getResources().getString(R.string.tasktype_survey))){
			if(frontState.equals(DBModle.TaskLog.FrontState_NO_ASSIGN)){
				holder.taskType.setImageResource(R.drawable.tasktype_survey_noaccept);
			}else if (frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
				holder.taskType.setImageResource(R.drawable.tasktype_survey_finish2);
			}else if(frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_WITH_PICTURE) ||frontState.equals(DBModle.TaskLog.FrontState_CLEAR_CASE_NO_PICTURE)){
				holder.taskType.setImageResource(R.drawable.tasktype_remove_finish2);
			} else {
				holder.taskType.setImageResource(R.drawable.tasktype_survey);
			}
		}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_large))){
			if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
				holder.taskType.setImageResource(R.drawable.tasktype_large_finish2);
			}else {
				holder.taskType.setImageResource(R.drawable.tasktype_large);
			}


		}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_danger))){
			if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
				holder.taskType.setImageResource(R.drawable.tasktype_danger_finish2);
			}else {
				holder.taskType.setImageResource(R.drawable.tasktype_danger);
			}


		}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_recommend))){
			if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
				holder.taskType.setImageResource(R.drawable.tasktype_recommend_finish2);
			}else {
				holder.taskType.setImageResource(R.drawable.tasktype_recommend);
			}

		}else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_repeat))){
			if(frontState.equals(DBModle.TaskLog.FrontState_NO_ASSIGN)){
				holder.taskType.setImageResource(R.drawable.tasktype_repeat_noaccept);
			} else if(frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
				holder.taskType.setImageResource(R.drawable.tasktype_repeat_noaccept_finish2);
			} else {
				holder.taskType.setImageResource(R.drawable.tasktype_repeat);
			}
		}
		else if(taskType.equals(mContext.getResources().getString(R.string.tasktype_diaodu))){
			if(frontState.equals(DBModle.TaskLog.FrontState_NO_ASSIGN)){
				holder.taskType.setImageResource(R.drawable.tasktype_survey_noaccept);
			}else if (frontState.equals(DBModle.TaskLog.FrontState_FINISH)){
				holder.taskType.setImageResource(R.drawable.tasktype_survey_finish2);
			} else {
				holder.taskType.setImageResource(R.drawable.tasktype_survey);
			}
		}

		if(!mUserClass.equals(Main.SURVEY)){
			holder.taskType.setVisibility(View.GONE);
		}

		holder.imageView.setImageResource(R.drawable.picback);

		int pictureSum = -1;
		if(mUserClass.equals(Main.RECEPTIONIST)){
			holder.imageView.setVisibility(View.GONE);
		}else {

			String mPath_str = mPicture_ArrayList.get(position).get("path");
			//设置默认图片
			File file = new File(mPath_str);
			if(file.exists()){
				File[] files = file.listFiles();
				if(files.length !=0){
					pictureSum = files.length;

					BitmapFactory.Options options = new BitmapFactory.Options();

					options.inJustDecodeBounds = true;
					options.inSampleSize = G.computeInitialSampleSize(options, -1, 20 * 20);
					options.inJustDecodeBounds = false;
					options.inSampleSize  = 20;
					String files0 = files[0].getAbsolutePath();					
					Log.d(TAG, "filename:"+files0);
					try{
						Bitmap bitmap = BitmapFactory.decodeFile(files0, options);							
						holder.imageView.setImageBitmap(bitmap);
						Log.d(TAG, "holder.imageView set files:"+files0+" position:" +position);
					}catch (Exception e) {
						// TODO: handle exception
						Log.e(TAG, "decodeBitmap files:"+files0+" error:"+ e.getMessage());
						holder.imageView.setImageResource(R.drawable.picback);
					}
				}
			}
		}

		
		String carMark = map.get(DBModle.Task.CarMark).toString();
		if(pictureSum == -1){
			holder.badgeView.hide();			
//			holder.progressBar.setVisibility(View.GONE);
			holder.pictureProgressBar.setVisibility(View.GONE);
		}else {
			holder.badgeView.show();
			holder.badgeView.setText(pictureSum+"");			
//			String TID = map.getAsString(DBModle.Task.TID);
//			ArrayList<ContentValues> pictureList = DBOperator.getPicturesSum(TID);
//
//			holder.progressBar.setMax(pictureSum);
//			holder.progressBar.setProgress(pictureSum - pictureList.size());
//			Log.d(TAG, "MainCaseAdapter getView progressBar show maxNum:"+pictureSum +" upload:" + (pictureSum - pictureList.size()));
			 
			
			String TID = map.getAsString(DBModle.Task.TID);
			ArrayList<ContentValues> pictureList = DBOperator.getPicturesSum(TID,"1");
			int noUpload = pictureList.size();
			holder.pictureProgressBar.setProgress(noUpload);
			if(pictureList.isEmpty()){
				holder.pictureProgressBar.setVisibility(View.GONE);
			}else{
				holder.pictureProgressBar.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}


	private class ViewHolder{
		public TextView car_text;
		public TextView time_text;
		public TextView address_text;
		public TextView report_text;
		public ImageView imageView;
		public ImageView taskType;
		public ImageView newmessage;
		public BadgeView badgeView;
		public TextView number;
//		public ProgressBar progressBar;
		
		public PictureProgressBar pictureProgressBar;
	}

}
