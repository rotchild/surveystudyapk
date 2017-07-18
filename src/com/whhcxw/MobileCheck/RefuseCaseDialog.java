package com.whhcxw.MobileCheck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.whhcxw.MobileCheck.data.DBOperator;
import com.whhcxw.MobileCheck.service.UploadWork;
import com.whhcxw.location.BaiduLocation;
import com.whhcxw.theme.BaseActivity;

public class RefuseCaseDialog extends BaseActivity {
	private final String TAG = this.getClass().getName();
	private Context mContext;
	private Button mSure_Button;
	
	private String mTID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.refusecasedialog);
		mContext = this;
		initView();
	}
	
	public void initView(){
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mTID = bundle.getString("TID");
		String alert = bundle.getString("alert");
		mSure_Button = (Button)this. findViewById(R.id.button);
		mSure_Button.setOnClickListener(mSureButtonOnClickListener);
		TextView contentView = (TextView)this.findViewById(R.id.content);
		contentView.setText(alert);
	}
	
	OnClickListener mSureButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
					boolean b = DBOperator.refuseTask(mTID, BaiduLocation.instance().LOCAITON_LONITUDE, BaiduLocation.instance().LOCAITON_LATITUDE);
					if(b){
						UploadWork.sendDataChangeBroadcast(mContext);		
						
						Intent mIntent = new Intent(Main.NEWTASKTRENDS); 
			            mIntent.putExtra("yaner", mContext.getString(R.string.yuandong)); 
			            mContext.sendBroadcast(mIntent);
			            
						finish();
					}else {
						Dialog.positiveDialog(mContext, mContext.getResources().getString(R.string.casemanage_dialog_refuse_fail),new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								finish();
							}
						});
					}
				}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME){
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
