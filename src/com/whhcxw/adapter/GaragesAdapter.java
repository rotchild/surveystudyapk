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

public class GaragesAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ContentValues> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;
	
	
	
	public GaragesAdapter(Context mContext, ArrayList<ContentValues> mData, int mResourceID) {
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
			holder.garages_name=(TextView)convertView.findViewById(R.id.garages_name);
			holder.garages_phone=(TextView)convertView.findViewById(R.id.garages_phone);
			holder.garages_address=(TextView)convertView.findViewById(R.id.garages_address);
			holder.imageview = (ImageView)convertView.findViewById(R.id.imageview);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ContentValues map = (ContentValues) mData.get(position);
		String garagename = map.getAsString(DBModle.Garage.ShortName);
		if(garagename.equals("")){
			garagename=map.getAsString(DBModle.Garage.FullName);;
		}
		holder.garages_name.setText(garagename);
		holder.garages_phone.setText(map.get(DBModle.Garage.Telephone).toString());
		holder.garages_address.setText(map.get(DBModle.Garage.Address).toString());
		
		
		boolean check = (Boolean) map.get("check");
		
		if(check){
			holder.imageview.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.checkbox_btn_on));
		}else{
			holder.imageview.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.checkbox_btn_off));
		}
		return convertView;
	}
	
	private class ViewHolder{
		public TextView garages_name;
		public TextView garages_phone;
		public TextView garages_address;
		public ImageView imageview;
		
	}

}
