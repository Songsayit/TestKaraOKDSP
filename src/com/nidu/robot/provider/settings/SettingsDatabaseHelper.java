package com.nidu.robot.provider.settings;

import java.util.HashSet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class SettingsDatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "SettingsProvider";
	private static final String DATABASE_NAME = "settings.db";

	private static final int DATABASE_VERSION = 2;// nidu2.0
	private Context mContext;

	private static final HashSet<String> mValidTables = new HashSet<String>();

	public interface Tables {
		public static final String ROBOT = "robot";// 我们的机器人
	}

	public interface TableBaseColumes extends BaseColumns {
		public static final String NAME = NiduSettings.NameValueTable.NAME;
		public static final String VALUE = NiduSettings.NameValueTable.VALUE;
	}

	static {
		mValidTables.add(Tables.ROBOT);
	}

	public SettingsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	public static boolean isValidTable(String name) {
		return mValidTables.contains(name);
	}

	private void createRobotTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.ROBOT + " (" + TableBaseColumes._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + TableBaseColumes.NAME
				+ " TEXT UNIQUE ON CONFLICT REPLACE," + TableBaseColumes.VALUE
				+ " TEXT" + ");");
		db.execSQL("CREATE INDEX robotIndex1 ON " + Tables.ROBOT + " ("
				+ TableBaseColumes.NAME + ");");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		createRobotTable(db);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}


}
