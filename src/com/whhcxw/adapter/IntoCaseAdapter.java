package com.whhcxw.adapter;

import java.util.ArrayList;
import com.whhcxw.MobileCheck.R;

import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IntoCaseAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private Context mContext;
	private ArrayList<ContentValues> mData;
	private LayoutInflater mInflater;
	private int mResourceID=-1;
	
	
	
	public IntoCaseAdapter(Context mContext, ArrayList<ContentValues> mData, int mResourceID) {
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
	
	public void updata(ArrayList<ContentValues> mData){
		this.mData = mData;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if(convertView==null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(mResourceID, null);
			holder.phone=(TextView)convertView.findViewById(R.id.phone);
			holder.time=(TextView)convertView.findViewById(R.id.time);
			holder.mess=(TextView)convertView.findViewById(R.id.mess);
			holder.imageView=(ImageView)convertView.findViewById(R.id.imageview);
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
			
			holder.phone.setText(contentValues.get("phone").toString());
			holder.time.setText(contentValues.get("time").toString());
			holder.mess.setText(contentValues.get("mess").toString());
	
		return convertView;
	}
	
	private class ViewHolder{
		public TextView phone;
		public TextView time;
		public TextView mess;
		public ImageView imageView;
	}

}
