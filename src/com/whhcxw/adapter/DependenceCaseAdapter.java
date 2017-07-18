package com.whhcxw.adapter;

import java.util.ArrayList;
import com.whhcxw.MobileCheck.data.DBModle;
import com.whhcxw.MobileCheck.R;
import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DependenceCaseAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ContentValues> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;
	
	
	public DependenceCaseAdapter(Context mContext, ArrayList<ContentValues> mData, int mResourceID) {
		super();
		this.mContext = mContext;
		this.mData = mData;
		this.mInflater = LayoutInflater.from(mContext);
		this.mResourceID = mResourceID;
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
			
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		ContentValues contentValues = (ContentValues) mData.get(position);
		
		holder.car_text.setText(contentValues.get(DBModle.Task.CarMark).toString());
		holder.time_text.setText(contentValues.get(DBModle.Task.AccidentTime).toString());
		holder.address_text.setText(contentValues.get(DBModle.Task.Address).toString());
		holder.report_text.setText(contentValues.get(DBModle.Task.CaseNo).toString());

		return convertView;
	}
	
	private class ViewHolder{
		public TextView car_text;
		public TextView time_text;
		public TextView address_text;
		public TextView report_text;
	}

}
