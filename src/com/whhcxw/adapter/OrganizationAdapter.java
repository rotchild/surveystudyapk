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
import android.widget.ImageView;
import android.widget.TextView;

public class OrganizationAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ContentValues> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;
	
	
	
	public OrganizationAdapter(Context mContext, ArrayList<ContentValues> mData, int mResourceID) {
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
			holder.organization_view=(TextView)convertView.findViewById(R.id.organization_view);
			holder.imageview=(ImageView)convertView.findViewById(R.id.imageview);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ContentValues values = (ContentValues) mData.get(position);
		holder.organization_view.setText(values.get(DBModle.Areas.AreaName).toString());
		
		boolean check = (Boolean) values.get("check");
		
		if(check){
			holder.imageview.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.checkbox_btn_on));
		}else{
			holder.imageview.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.checkbox_btn_off));
		}
		
	
		return convertView;
	}
	
	
	private class ViewHolder{
		public TextView organization_view;
		public ImageView imageview;
		
	}

}
