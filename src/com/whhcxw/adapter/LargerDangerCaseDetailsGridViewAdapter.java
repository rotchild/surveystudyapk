package com.whhcxw.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.whhcxw.MobileCheck.data.FileDownload;
import com.whhcxw.MobileCheck.net.HttpParams;
import com.whhcxw.MobileCheck.R;
import com.whhcxw.global.G;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class LargerDangerCaseDetailsGridViewAdapter extends BaseAdapter {

	private final String TAG = this.getClass().getSimpleName();
	private Context mContext;
	
	private List<HashMap<String, String>> mData;
	@SuppressWarnings("unused")
	private String mUserClass;
	private String mDownPath;
	public LargerDangerCaseDetailsGridViewAdapter(Context _c,List<HashMap<String, String>> _mData,String _userClass , String _mDownPath) {
		this.mContext = _c;
		this.mData = _mData;
		this.mUserClass = _userClass;
		this.mDownPath = _mDownPath;
	}

	
	public void nitifyData(List<HashMap<String, String>> _data){
		mData = _data;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder =null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.largerdangergridview, null);
			holder.imageView = (ImageView)convertView.findViewById(R.id.picture);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		HashMap<String, String> map = mData.get(position);
		String imagrPath = map.get("survey_path");
		if(!imagrPath.equals("")){
			holder.imageView.setImageBitmap(getBitmap(imagrPath));
		}else{
			//设置默认图片
			holder.imageView.setImageResource(R.drawable.picback);
			//开始下载图片
			String serverPath = map.get("server_path"); 			
			String url = HttpParams.BSASURL + URLEncoder.encode(serverPath);
			String imageName = serverPath.substring(serverPath.indexOf("/") + 1, serverPath.length());
			FileDownload.download(url, new DownloadBinaryHttpResponseHandler(mDownPath, holder.imageView, imageName,map));
		}			
		return convertView;
	}
	
	private class ViewHolder{
		public ImageView imageView;
	}


	public Bitmap getBitmap(String path, int size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = size;		
		Bitmap bmp = BitmapFactory.decodeFile(path, options);
		return bmp;
	}
	
	public Bitmap getBitmap(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = G.computeInitialSampleSize(options, -1, 120 * 120);
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);	
		return bitmap;
	}
	
	
	
	/**下载照片*/
    private class DownloadBinaryHttpResponseHandler extends BinaryHttpResponseHandler{
    	private String _path = "";
    	private ImageView _view;
    	private String _picName;
    	private HashMap<String, String> _map;
    	public DownloadBinaryHttpResponseHandler(String path,ImageView view ,String picName,HashMap<String, String> map){
    		_path = path;
    		_view = view;
    		_picName = picName;
    		_map = map;
    	} 
    	
		@Override
		public void onSuccess(int statusCode, byte[] binaryData) {
			// TODO Auto-generated method stub
			try {
				String[] picname = _picName.split("/");
				String imagePath = _path + picname[1];
				File fileDirectory = new File(_path);
				if(!fileDirectory.exists()){
					fileDirectory.mkdirs();
				}
				File file = new File(imagePath);
				if(!file.exists()){
					file.createNewFile();
					FileOutputStream fos = new FileOutputStream(imagePath);
					fos.write(binaryData, 0, binaryData.length);
					fos.close();
				}
				//把流转换成为图片。
				Bitmap bitmap = getBitmap(imagePath);
				_view.setImageBitmap(bitmap);
				_map.put("survey_path", imagePath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d(TAG, "DownloadBinaryHttpResponseHandler save pic error :"+ e.getMessage());
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onFailure(Throwable error, byte[] binaryData) {
			// TODO Auto-generated method stub
			super.onFailure(error, binaryData);
			Log.d(TAG, "DownloadBinaryHttpResponseHandler save pic error :"+ error.getMessage());
		}

		
	};

	
}
