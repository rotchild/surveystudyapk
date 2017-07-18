package com.whhcxw.adapter;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

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

public class PushMessageAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ContentValues> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;
	
	
	
	public PushMessageAdapter(Context mContext, ArrayList<ContentValues> mData, int mResourceID) {
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
			holder.alert=(TextView)convertView.findViewById(R.id.alert);
			holder.date=(TextView)convertView.findViewById(R.id.date);
			holder.CarMark=(TextView)convertView.findViewById(R.id.carmark);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String messageType = mData.get(position).getAsString(DBModle.PushMessage.MessageType);
		if(messageType.equals(DBModle.PushMessage.DiaoDu) ||messageType.equals(DBModle.PushMessage.ShouLi) ){
			try {
				JSONObject object = new JSONObject(mData.get(position).getAsString(DBModle.PushMessage.Data));
				String CarMark = object.getString(DBModle.Task.CarMark);
				holder.CarMark.setText(CarMark);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		String alert = mData.get(position).getAsString(DBModle.PushMessage.Alert);
		if(alert.length()>15){
			alert = alert.substring(0, 15);
			alert = alert +"……";
		}
		holder.alert.setText(alert);
		holder.date.setText(mData.get(position).getAsString(DBModle.PushMessage.PushTime));
		return convertView;
	}
	
	private class ViewHolder{
		public TextView alert;
		public TextView date;
		public TextView CarMark;
	}

}
