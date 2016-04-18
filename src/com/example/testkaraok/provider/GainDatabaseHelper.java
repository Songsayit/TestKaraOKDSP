package com.example.testkaraok.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GainDatabaseHelper extends SQLiteOpenHelper {

	private final String TAG = "GainDatabaseHelper";
	
	private Context mContext;
	private static final int VERSION = 1;
	static final String DATABASE_NAME = "gain.db";

	
	public interface Tables {
		public static final String GAIN_SAVE = "gain_save";
		public static final String DSP_REG_SAVE = "dsp_reg_save";
	}
	
	public GainDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		mContext = context;
	}

	private void createSaveGain(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.GAIN_SAVE + " (" +
				GainContract.GainBaseColumns._ID + " INTEGER PRIMARY KEY," +
	            GainContract.GainBaseColumns.DATE + " TEXT, " +
	            GainContract.GainBaseColumns.HOLDER_LIST_STR + " TEXT);");
		Log.i(TAG, "SAVE_GAIN Table created");
	}
	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createSaveGain(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

}
