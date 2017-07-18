package com.whhcxw.MobileCheck.mail;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class EmailManager {

	private Context context;
	
	private Intent intent;
	
	public EmailManager(Context context){
		this.context  = context;
		initEmail();
	}
	
	private void initEmail(){
		intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
	}
	
	/**
	 * 添加主题
	 * @param subject
	 */
	public void addSubject(String subject){
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
	}
	
	
	/**
	 * 添加收件人(可添加多位联系人)
	 * @param receivers
	 */
	public void addEmailTO(String... receivers){
		intent.putExtra(Intent.EXTRA_EMAIL, receivers);
	}
	
	/**
	 * 添加抄送人
	 * @param cc
	 */
	public void addEmailCC(String...cc){
		intent.putExtra(Intent.EXTRA_CC, cc);
	}
	
	/**
	 * 添加密送人
	 * @param bcc
	 */
	public void addEmailBCC(String...bcc){
		intent.putExtra(Intent.EXTRA_BCC, bcc);
	}
	
	/**
	 * 添加邮件内容
	 * @param content
	 */
	public void addEmailContent(String content){
		intent.putExtra(Intent.EXTRA_TEXT, content);
	}
	
	/**
	 * 添加照片附件
	 * @param imageUris 所有附件的uri集合
	 */
	public void addEmailPhotos(ArrayList<Uri> imageUris){
		intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
		intent.setType("image/*");
	}
	
	/**
	 * 把文件集合转换为uri集合
	 * @param files
	 * @return
	 */
	public static ArrayList<Uri> convertFilesToUris(ArrayList<File> files){
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (int i = 0; i < files.size(); i++) {
			uris.add(Uri.fromFile(files.get(i)));
		}
		
		return uris;
		
	}
	/**
	 * 把String的文件集合转换为uri集合
	 * @param files
	 * @return
	 */
	public static ArrayList<Uri> convertStringsToUris(ArrayList<String> files){
		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (int i = 0; i < files.size(); i++) {
			File f = new File(files.get(i));
			uris.add(Uri.fromFile(f));
		}
		
		return uris;
	}
	
	/**
	 * 发送邮件
	 */
	public void sendEmail(){
		if (intent != null) {
//			intent.setType("message/rfc882");
			Intent.createChooser(intent, "Choose Email Client");
//			context.startActivity(intent);
			((Activity)context).startActivityForResult(intent, 0);
		}
	}
	
	
}
