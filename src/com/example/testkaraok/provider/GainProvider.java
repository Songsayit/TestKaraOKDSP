package com.example.testkaraok.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class GainProvider extends ContentProvider {

	private final String TAG = "GainProvider";
	private final boolean DEBUG = false;

	private GainDatabaseHelper mOpenHelper;
	private static final int GAIN_SAVE = 10;
	private static final int GAIN_SAVE_ID = 11;

	private static final UriMatcher sURLMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURLMatcher.addURI(GainContract.AUTHORITY,
				GainDatabaseHelper.Tables.GAIN_SAVE, GAIN_SAVE);
		sURLMatcher.addURI(GainContract.AUTHORITY,
				GainDatabaseHelper.Tables.GAIN_SAVE + "/*", GAIN_SAVE_ID);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mOpenHelper = new GainDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		// Generate the body of the query
		int match = sURLMatcher.match(uri);

		switch (match) {
		case GAIN_SAVE:
			qb.setTables(GainDatabaseHelper.Tables.GAIN_SAVE);
			break;
		case GAIN_SAVE_ID:
			qb.setTables(GainDatabaseHelper.Tables.GAIN_SAVE);
			qb.appendWhere(GainContract.GainBaseColumns._ID + "=");
            qb.appendWhere(uri.getLastPathSegment());
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);

		if (ret == null) {
			Log.e(TAG, "GAIN_SAVE.query: failed");
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		}


		return ret;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		int match = sURLMatcher.match(uri);
		switch (match) {
		case GAIN_SAVE:
			return "vnd.android.cursor.dir/"
					+ GainDatabaseHelper.Tables.GAIN_SAVE;
		case GAIN_SAVE_ID:
			return "vnd.android.cursor.item/"
					+ GainDatabaseHelper.Tables.GAIN_SAVE;
			
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		String id;
		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (sURLMatcher.match(uri)) {
		case GAIN_SAVE_ID:

			id = uri.getLastPathSegment();
            count = db.update(GainDatabaseHelper.Tables.GAIN_SAVE, values,
                    GainContract.GainBaseColumns._ID + "=" + id,
                    null);
			break;
			
		default: {
			throw new UnsupportedOperationException("Cannot update URL: " + uri);
		}
		}

		if (DEBUG)
			Log.e(TAG, "*** notifyChange() id: " + id + " url " + uri);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// TODO Auto-generated method stub
		long rowId;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (sURLMatcher.match(uri)) {
		case GAIN_SAVE:
			rowId = db.insert(GainDatabaseHelper.Tables.GAIN_SAVE, null,
					initialValues);
			break;
		default:
			throw new IllegalArgumentException("Cannot insert from URL: " + uri);
		}

		Uri uriResult = ContentUris.withAppendedId(GainContract.GainBaseColumns.CONTENT_URI, rowId);
	    getContext().getContentResolver().notifyChange(uriResult, null);
		return uriResult;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// TODO Auto-generated method stub

		int count;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String primaryKey;
		 switch (sURLMatcher.match(uri)) {
         case GAIN_SAVE:
             count = db.delete(GainDatabaseHelper.Tables.GAIN_SAVE, where, whereArgs);
             break;
         case GAIN_SAVE_ID:
        	 primaryKey = uri.getLastPathSegment();
        	 if (TextUtils.isEmpty(where)) {
                 where = GainContract.GainBaseColumns._ID + "=" + primaryKey;
             } else {
                 where = GainContract.GainBaseColumns._ID + "=" + primaryKey +
                         " AND (" + where + ")";
             }
             count = db.delete(GainDatabaseHelper.Tables.GAIN_SAVE, where, whereArgs);
             break;
             
         default:
             throw new IllegalArgumentException("Cannot delete from URL: " + uri);
     }

     getContext().getContentResolver().notifyChange(uri, null);
     return count;
	}
	
}
