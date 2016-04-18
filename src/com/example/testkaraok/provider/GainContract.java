package com.example.testkaraok.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class GainContract {

	public static final String AUTHORITY = "com.example.testkaraok.provider";

	private GainContract(){}
	
	public interface GainBaseColumns extends BaseColumns {
		 public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/gain_save");
		//唯一标识码.
		public static final String DATE = "date";
		//设备昵称
		public static final String HOLDER_LIST_STR = "holder_list_string";
		
	}
	
	
	
}
