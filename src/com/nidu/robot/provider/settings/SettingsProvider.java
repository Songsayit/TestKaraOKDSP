package com.nidu.robot.provider.settings;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

public class SettingsProvider extends ContentProvider {
	private static final String TAG = "SettingsProvider";
	private static final boolean LOCAL_LOGV = false;

	private static final String TABLE_ROBOT = SettingsDatabaseHelper.Tables.ROBOT;// 我们的机器人

	private static final String[] COLUMN_VALUE = new String[] { NiduSettings.NameValueTable.VALUE };

    // Cache for settings, access-ordered for acting as LRU.
    // Guarded by themselves.
    private static final int MAX_CACHE_ENTRIES = 200;
    private static final SettingsCache sRobotCache = new SettingsCache(TABLE_ROBOT);

    // The count of how many known (handled by SettingsProvider)
    // database mutations are currently being handled.  Used by
    // sFileObserver to not reload the database when it's ourselves
    // modifying it.
    private static final AtomicInteger sKnownMutationsInFlight = new AtomicInteger(0);

    // Over this size we don't reject loading or saving settings but
    // we do consider them broken/malicious and don't keep them in
    // memory at least:
    private static final int MAX_CACHE_ENTRY_SIZE = 500;

    private static final Bundle NULL_SETTING = new Bundle();// = Bundle.forPair("value", null);

    // Used as a sentinel value in an instance equality XmppSenderController when we
    // want to cache the existence of a key, but not store its value.
    private static final Bundle TOO_LARGE_TO_CACHE_MARKER = new Bundle();// = Bundle.forPair("_dummy", null);

    protected SettingsDatabaseHelper mOpenHelper;

	/**
	 * Decode a content URL into the table, projection, and arguments used to
	 * access the corresponding database rows.
	 */
	private static class SqlArguments {
		public String table;
		public final String where;
		public final String[] args;

		/** Operate on existing rows. */
		SqlArguments(Uri url, String where, String[] args) {
			if (url.getPathSegments().size() == 1) {
				// of the form content://settings/secure, arbitrary where clause
				this.table = url.getPathSegments().get(0);
				if (!SettingsDatabaseHelper.isValidTable(this.table)) {
					throw new IllegalArgumentException("Bad root path: "
							+ this.table);
				}
				this.where = where;
				this.args = args;
			} else if (url.getPathSegments().size() != 2) {
				throw new IllegalArgumentException("Invalid URI: " + url);
			} else if (!TextUtils.isEmpty(where)) {
				throw new UnsupportedOperationException("WHERE clause not supported: " + url);
			} else {
				// of the form content://settings/secure/element_name, no where
				// clause
				this.table = url.getPathSegments().get(0);
				if (!SettingsDatabaseHelper.isValidTable(this.table)) {
					throw new IllegalArgumentException("Bad root path: "
							+ this.table);
				}

				if (TABLE_ROBOT.equals(this.table)) {
					this.where = NiduSettings.NameValueTable.NAME + "=?";
					final String name = url.getPathSegments().get(1);
					this.args = new String[] { name };
				} else {
					// of the form content://bookmarks/19
					this.where = "_id=" + ContentUris.parseId(url);
					this.args = null;
				}
			}
		}

		/** Insert new rows (no where clause allowed). */
		SqlArguments(Uri url) {
			if (url.getPathSegments().size() == 1) {
				this.table = url.getPathSegments().get(0);
				if (!SettingsDatabaseHelper.isValidTable(this.table)) {
					throw new IllegalArgumentException("Bad root path: "
							+ this.table);
				}
				this.where = null;
				this.args = null;
			} else {
				throw new IllegalArgumentException("Invalid URI: " + url);
			}
		}
	}

	/**
	 * Get the content URI of a row added to a table.
	 * 
	 * @param tableUri
	 *            of the entire table
	 * @param values
	 *            found in the row
	 * @param rowId
	 *            of the row
	 * @return the content URI for this particular row
	 */
	private Uri getUriFor(Uri tableUri, ContentValues values, long rowId) {
		if (tableUri.getPathSegments().size() != 1) {
			throw new IllegalArgumentException("Invalid URI: " + tableUri);
		}
		String table = tableUri.getPathSegments().get(0);
		if (TABLE_ROBOT.equals(table)) {
			String name = values.getAsString(NiduSettings.NameValueTable.NAME);
			return Uri.withAppendedPath(tableUri, name);
		} else {
			return ContentUris.withAppendedId(tableUri, rowId);
		}
	}

	/**
	 * Send a notification when a particular content URI changes. Modify the
	 * system property used to communicate the version of this table, for tables
	 * which have such a property. (The NiduSettings contract class uses these to
	 * provide client-side caches.)
	 * 
	 * @param uri
	 *            to send notifications for
	 */
	private void sendNotify(Uri uri) {
		
		// Now send the notification through the content framework.

		String notify = uri.getQueryParameter("notify");
		if (notify == null || "true".equals(notify)) {
			final long oldId = Binder.clearCallingIdentity();
			try {
				getContext().getContentResolver().notifyChange(uri, null);
			} finally {
				Binder.restoreCallingIdentity(oldId);
			}
			if (LOCAL_LOGV)
				Log.v(TAG, "notifying url: " + uri);
		} else {
			if (LOCAL_LOGV)
				Log.v(TAG, "notification suppressed: " + uri);
		}
	}

	// FileObserver for external modifications to the database file.
    // Note that this is for platform developers only with
    // userdebug/eng builds who should be able to tinker with the
    // sqlite database out from under the SettingsProvider, which is
    // normally the exclusive owner of the database.  But we keep this
    // enabled all the time to minimize development-vs-user
    // differences in testing.
    private static SettingsFileObserver sObserverInstance;
    private class SettingsFileObserver extends FileObserver {
        private final AtomicBoolean mIsDirty = new AtomicBoolean(false);
        private final String mPath;

        public SettingsFileObserver(String path) {
            super(path, FileObserver.CLOSE_WRITE |
                  FileObserver.CREATE | FileObserver.DELETE |
                  FileObserver.MOVED_TO | FileObserver.MODIFY);
            mPath = path;
        }

        public void onEvent(int event, String path) {
            int modsInFlight = sKnownMutationsInFlight.get();
            if (modsInFlight > 0) {
                // our own modification.
                return;
            }
            Log.d(TAG, "external modification to " + mPath + "; event=" + event);
            if (!mIsDirty.compareAndSet(false, true)) {
                // already handled. (we get a few update events
                // during an sqlite write)
                return;
            }
            Log.d(TAG, "updating our caches for " + mPath);
            fullyPopulateCaches();
            mIsDirty.set(false);
        }
    }
    
    
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		
		NULL_SETTING.putString(NiduSettings.NameValueTable.VALUE, null);
		TOO_LARGE_TO_CACHE_MARKER.putString("_dummy", null);
		
		mOpenHelper = new SettingsDatabaseHelper(getContext());
		
		if (!ensureAndroidIdIsSet()) {
            return false;
        }

		 // Watch for external modifications to the database file,
        // keeping our cache in sync.
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        db.enableWriteAheadLogging();
        sObserverInstance = new SettingsFileObserver(db.getPath());
        sObserverInstance.startWatching();
        startAsyncCachePopulation();
        return true;
	}
	
	private void startAsyncCachePopulation() {
        new Thread("populate-settings-caches") {
            public void run() {
                fullyPopulateCaches();
            }
        }.start();
    }

    private void fullyPopulateCaches() {
        fullyPopulateCache(TABLE_ROBOT, sRobotCache);
    }

    // Slurp all values (if sane in number & size) into cache.
    private void fullyPopulateCache(String table, SettingsCache cache) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = db.query(
            table,
            new String[] { NiduSettings.NameValueTable.NAME, NiduSettings.NameValueTable.VALUE },
            null, null, null, null, null,
            "" + (MAX_CACHE_ENTRIES + 1) /* limit */);
        try {
            synchronized (cache) {
                cache.clear();
                cache.setFullyMatchesDisk(true);  // optimistic
                int rows = 0;
                while (c.moveToNext()) {
                    rows++;
                    String name = c.getString(0);
                    String value = c.getString(1);
                    cache.populate(name, value);
                }
                if (rows > MAX_CACHE_ENTRIES) {
                    // Somewhat redundant, as removeEldestEntry() will
                    // have already done this, but to be explicit:
                    cache.setFullyMatchesDisk(false);
                    Log.d(TAG, "row count exceeds max cache entries for table " + table);
                }
                Log.d(TAG, "cache for settings table '" + table + "' rows=" + rows + "; fullycached=" +
                      cache.fullyMatchesDisk());
            }
        } finally {
            c.close();
        }
    }

    private boolean ensureAndroidIdIsSet() {
        final Cursor c = query(NiduSettings.Robot.CONTENT_URI,
                new String[] { NiduSettings.NameValueTable.VALUE },
                NiduSettings.NameValueTable.NAME + "=?",
                new String[] { NiduSettings.Robot.ANDROID_ID }, null);
        try {
            final String value = c.moveToNext() ? c.getString(0) : null;
            if (value == null) {
                final SecureRandom random = new SecureRandom();
                final String newAndroidIdValue = Long.toHexString(random.nextLong());
                Log.d(TAG, "Generated and saved new ANDROID_ID [" + newAndroidIdValue + "]");
                final ContentValues values = new ContentValues();
                values.put(NiduSettings.NameValueTable.NAME, NiduSettings.Robot.ANDROID_ID);
                values.put(NiduSettings.NameValueTable.VALUE, newAndroidIdValue);
                final Uri uri = insert(NiduSettings.Robot.CONTENT_URI, values);
                if (uri == null) {
                    return false;
                }
            }
            return true;
        } finally {
            c.close();
        }
    }
    
    /**
     * Fast path that avoids the use of chatty remoted Cursors.
     */
    @Override
    public Bundle call(String method, String request, Bundle args) {
        if (NiduSettings.CALL_METHOD_GET_ROBOT.equals(method)) {
            return lookupValue(TABLE_ROBOT, sRobotCache, request);
        }
        return null;
    }
    
    // Looks up value 'key' in 'table' and returns either a single-pair Bundle,
    // possibly with a null value, or null on failure.
    private Bundle lookupValue(String table, SettingsCache cache, String key) {
        synchronized (cache) {
            Bundle value = cache.get(key);
            if (value != null) {
                if (value != TOO_LARGE_TO_CACHE_MARKER) {
                    return value;
                }
                // else we fall through and read the value from disk
            } else if (cache.fullyMatchesDisk()) {
                // Fast path (very common).  Don't even try touch disk
                // if we know we've slurped it all in.  Trying to
                // touch the disk would mean waiting for yaffs2 to
                // give us access, which could takes hundreds of
                // milliseconds.  And we're very likely being called
                // from somebody's UI thread...
                return NULL_SETTING;
            }
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(table, COLUMN_VALUE, "name=?", new String[]{key},
                              null, null, null, null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cache.putIfAbsent(key, cursor.getString(0));
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "settings lookup error", e);
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
        cache.putIfAbsent(key, null);
        return NULL_SETTING;
    }
    
	@Override
	public Cursor query(Uri url, String[] select, String where, String[] whereArgs, String sort) {
		// TODO Auto-generated method stub
		
		SqlArguments args = new SqlArguments(url, where, whereArgs);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);
        
        Cursor ret = qb.query(db, select, args.where, args.args, null, null, sort);
        ret.setNotificationUri(getContext().getContentResolver(), url);
        
		return ret;
	}
	
	@Override
	public String getType(Uri url) {
		// TODO Auto-generated method stub
		  // If SqlArguments supplies a where clause, then it must be an item
        // (because we aren't supplying our own where clause).
        SqlArguments args = new SqlArguments(url, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(uri);
		
		SettingsCache cache = SettingsCache.forTable(args.table);
		sKnownMutationsInFlight.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (db.insert(args.table, null, values[i]) < 0) return 0;
                SettingsCache.populate(cache, values[i]);
                if (LOCAL_LOGV) Log.v(TAG, args.table + " <- " + values[i]);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            sKnownMutationsInFlight.decrementAndGet();
        }

        sendNotify(uri);
        return values.length;
	}


	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(url);
		
		String name = initialValues.getAsString(NiduSettings.NameValueTable.NAME);
		SettingsCache cache = SettingsCache.forTable(args.table);
	    String value = initialValues.getAsString(NiduSettings.NameValueTable.VALUE);
        if (SettingsCache.isRedundantSetValue(cache, name, value)) {
            return Uri.withAppendedPath(url, name);
        }
        
        sKnownMutationsInFlight.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, initialValues);

        sKnownMutationsInFlight.decrementAndGet();
        if (rowId <= 0) return null;

        SettingsCache.populate(cache, initialValues);  // before we notify

        if (LOCAL_LOGV) Log.v(TAG, args.table + " <- " + initialValues);
        url = getUriFor(url, initialValues, rowId);
        sendNotify(url);
        return url;
	}
    
    
	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(url, where, whereArgs);

        sKnownMutationsInFlight.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        sKnownMutationsInFlight.decrementAndGet();
        if (count > 0) {
            SettingsCache.invalidate(args.table);  // before we notify
            sendNotify(url);
        }
        startAsyncCachePopulation();
        if (LOCAL_LOGV) Log.v(TAG, args.table + ": " + count + " row(s) deleted");
        return count;
	}

	@Override
	public int update(Uri url, ContentValues initialValues, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		SqlArguments args = new SqlArguments(url, where, whereArgs);

        sKnownMutationsInFlight.incrementAndGet();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, initialValues, args.where, args.args);
        sKnownMutationsInFlight.decrementAndGet();
        if (count > 0) {
            SettingsCache.invalidate(args.table);  // before we notify
            sendNotify(url);
        }
        startAsyncCachePopulation();
        if (LOCAL_LOGV) Log.v(TAG, args.table + ": " + count + " row(s) <- " + initialValues);
        return count;
	}

	/**
     * In-memory LRU Cache of system and secure settings, along with
     * associated helper functions to keep cache coherent with the
     * database.
     */
    private static final class SettingsCache extends LinkedHashMap<String, Bundle> {

        private final String mCacheName;
        private boolean mCacheFullyMatchesDisk = false;  // has the whole database slurped.

        public SettingsCache(String name) {
            super(MAX_CACHE_ENTRIES, 0.75f /* load factor */, true /* access ordered */);
            mCacheName = name;
        }

        /**
         * Is the whole database table slurped into this cache?
         */
        public boolean fullyMatchesDisk() {
            synchronized (this) {
                return mCacheFullyMatchesDisk;
            }
        }

        public void setFullyMatchesDisk(boolean value) {
            synchronized (this) {
                mCacheFullyMatchesDisk = value;
            }
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            if (size() <= MAX_CACHE_ENTRIES) {
                return false;
            }
            synchronized (this) {
                mCacheFullyMatchesDisk = false;
            }
            return true;
        }

        /**
         * Atomic cache population, conditional on size of value and if
         * we lost a race.
         *
         * @returns a Bundle to send back to the client from call(), even
         *     if we lost the race.
         */
        public Bundle putIfAbsent(String key, String value) {
        	
            Bundle bundle ;
            if  (value == null) {
            	bundle = NULL_SETTING;
            } else {
            	bundle = new Bundle();
            	bundle.putString(NiduSettings.NameValueTable.VALUE, value);
            }
            if (value == null || value.length() <= MAX_CACHE_ENTRY_SIZE) {
                synchronized (this) {
                    if (!containsKey(key)) {
                        put(key, bundle);
                    }
                }
            }
            return bundle;
        }

        public static SettingsCache forTable(String tableName) {
            if (TABLE_ROBOT.equals(tableName)) {
                return SettingsProvider.sRobotCache;
            }
            return null;
        }

        /**
         * Populates a key in a given (possibly-null) cache.
         */
        public static void populate(SettingsCache cache, ContentValues contentValues) {
            if (cache == null) {
                return;
            }
            String name = contentValues.getAsString(NiduSettings.NameValueTable.NAME);
            if (name == null) {
                Log.w(TAG, "null name populating settings cache.");
                return;
            }
            String value = contentValues.getAsString(NiduSettings.NameValueTable.VALUE);
            cache.populate(name, value);
        }

        public void populate(String name, String value) {
            synchronized (this) {
                if (value == null || value.length() <= MAX_CACHE_ENTRY_SIZE) {
                	
                	Bundle bundle = new Bundle();
                	bundle.putString(NiduSettings.NameValueTable.VALUE, value);
                	
                    put(name, bundle);
                } else {
                    put(name, TOO_LARGE_TO_CACHE_MARKER);
                }
            }
        }

        /**
         * Used for wiping a whole cache on deletes when we're not
         * sure what exactly was deleted or changed.
         */
        public static void invalidate(String tableName) {
            SettingsCache cache = SettingsCache.forTable(tableName);
            if (cache == null) {
                return;
            }
            synchronized (cache) {
                cache.clear();
                cache.mCacheFullyMatchesDisk = false;
            }
        }

        /**
         * For suppressing duplicate/redundant settings inserts early,
         * checking our cache first (but without faulting it in),
         * before going to sqlite with the mutation.
         */
        public static boolean isRedundantSetValue(SettingsCache cache, String name, String value) {
            if (cache == null) return false;
            synchronized (cache) {
                Bundle bundle = cache.get(name);
                if (bundle == null) return false;
                String oldValue = bundle.getString(NiduSettings.NameValueTable.VALUE);
                if (oldValue == null && value == null) return true;
                if ((oldValue == null) != (value == null)) return false;
                return oldValue.equals(value);
            }
        }
    }
    
}
