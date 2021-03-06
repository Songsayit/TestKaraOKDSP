
package com.example.testkaraok.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.example.testkaraok.provider.GainContract.GainBaseColumns;

public final class RegSave implements GainBaseColumns {
    public static final long INVALID_ID = -1;


    private static final String[] QUERY_COLUMNS = {
            _ID,
            DATE,
            HOLDER_LIST_STR,
    };

    private static final int COLUMN_COUNT = QUERY_COLUMNS.length;

    public static ContentValues createContentValues(RegSave gain) {
        ContentValues values = new ContentValues(COLUMN_COUNT);
        if (gain.id != INVALID_ID) {
            values.put(GainContract.GainBaseColumns._ID, gain.id);
        }

        values.put(DATE, gain.date);
        values.put(HOLDER_LIST_STR, gain.holder_list_string);

        return values;
    }


    public static Uri getUri(long gainId) {
        return ContentUris.withAppendedId(CONTENT_URI, gainId);
    }

    public static long getId(Uri contentUri) {
        return ContentUris.parseId(contentUri);
    }

    public static RegSave getGainSave(ContentResolver contentResolver, String date) {
    	List<RegSave> gainSaves = getGainSaveList(contentResolver, DATE + "=" + convertToQuotedString(date));
       
    	if (gainSaves ==  null) {
    		return null;
    	}
    	
    	return gainSaves.get(0);
    }
    
    private static String convertToQuotedString(String string) {
		 return "\"" + string + "\"";
	}
    
    public static RegSave getGainSave(ContentResolver contentResolver, long gainId) {
        Cursor cursor = contentResolver.query(getUri(gainId), QUERY_COLUMNS, null, null, null);
        RegSave result = null;
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                result = new RegSave(cursor);
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static List<RegSave> getGainSaveList(ContentResolver contentResolver,
            String selection, String ... selectionArgs) {
        Cursor cursor  = contentResolver.query(CONTENT_URI, QUERY_COLUMNS,
                selection, selectionArgs, null);
        List<RegSave> result = new LinkedList<RegSave>();
        if (cursor == null) {
            return result;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(new RegSave(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public static RegSave addGainSave(ContentResolver contentResolver, RegSave gain) {
        ContentValues values = createContentValues(gain);
        Uri uri = contentResolver.insert(CONTENT_URI, values);
        gain.id = getId(uri);
        return gain;
    }

    public static boolean updateGainSave(ContentResolver contentResolver, RegSave gain) {
        if (gain.id == RegSave.INVALID_ID) return false;
        ContentValues values = createContentValues(gain);
        long rowsUpdated = contentResolver.update(getUri(gain.id), values, null, null);
        return rowsUpdated == 1;
    }

    public static boolean deleteGainSave(ContentResolver contentResolver, long gainId) {
        if (gainId == INVALID_ID) return false;
        int deletedRows = contentResolver.delete(getUri(gainId), "", null);
        return deletedRows == 1;
    }


    // Public fields
    // TODO: Refactor instance names
    public long id;
    public String date;
    public String holder_list_string;

    public RegSave() {
        this.id = INVALID_ID;
    }
    
    public RegSave(String date, String gainHolderString) {
    	 this.id = INVALID_ID;
    	 this.date = date;
    	 this.holder_list_string = gainHolderString;
    }

    public RegSave(Cursor c) {
        id = c.getLong(c.getColumnIndex(_ID));
        date = c.getString(c.getColumnIndex(DATE));
        holder_list_string = c.getString(c.getColumnIndex(HOLDER_LIST_STR));
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegSave)) return false;
        final RegSave other = (RegSave) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(id).hashCode();
    }

}
