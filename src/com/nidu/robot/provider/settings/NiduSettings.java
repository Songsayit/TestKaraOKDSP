package com.nidu.robot.provider.settings;

import java.util.HashSet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.AndroidException;
import android.util.Log;

/**
 * 通过操作这个类来操作nidu数据库.
 * @author Song
 *
 */
public class NiduSettings {
	/**
	 * - Private call() method on SettingsProvider to read from 'robot' table.
	 */
	public static final String CALL_METHOD_GET_ROBOT = "GET_robot";

	public static final String EXTRA_AUTHORITIES = "authorities";

	public static final String EXTRA_INPUT_METHOD_ID = "input_method_id";

	private static final String JID_RESOURCE_PREFIX = "android";

	public static final String AUTHORITY = "nidu_settings"; //这个需要改. 要不然就跟系统的一样了.这个是provider的authority

	private static final String TAG = "NiduSettings";
	private static final boolean LOCAL_LOGV = true;

	public static class SettingNotFoundException extends AndroidException {
		public SettingNotFoundException(String msg) {
			super(msg);
		}
	}

	/**
	 * Common base for tables of name/value settings.
	 */

	public static class NameValueTable {
		
		public static final String NAME = "name";
		public static final String VALUE = "value";

		protected static boolean putString(ContentResolver resolver, Uri uri,
				String name, String value) {
			// The database will take care of replacing duplicates.
			try {
				ContentValues values = new ContentValues();
				values.put(NAME, name);
				values.put(VALUE, value);
				resolver.insert(uri, values);
				return true;
			} catch (SQLException e) {
				Log.w(TAG, "Can't set key " + name + " in " + uri, e);
				return false;
			}
		}

		public static Uri getUriFor(Uri uri, String name) {
			return Uri.withAppendedPath(uri, name);
		}
	}

	// Thread-safe.
	private static class NameValueCache {
		private final Uri mUri;

		private static final String[] SELECT_VALUE = new String[] { NameValueTable.VALUE };
		private static final String NAME_EQ_PLACEHOLDER =  NameValueTable.NAME + "=?";

		// The method we'll call (or null, to not use) on the provider
		// for the fast path of retrieving settings.
		private final String mCallCommand;

		public NameValueCache(Uri uri, String callCommand) {
			mUri = uri;
			mCallCommand = callCommand;
		}

		public String getString(ContentResolver cr, String name) {

			// Try the fast path first, not using query(). If this
			// fails (alternate NiduSettings provider that doesn't support
			// this interface?) then we fall back to the query/table
			// interface.
			if (mCallCommand != null) {
				try {
					 Bundle b = cr.call(mUri, mCallCommand, name, null);
					if (b != null) {
						String value = b.getString(NameValueTable.VALUE);// b.getPairValue();
						return value;
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}

			Cursor c = null;
			try {
				c = cr.query(mUri, SELECT_VALUE, NAME_EQ_PLACEHOLDER,
						new String[] { name }, null);
				if (c == null) {
					Log.w(TAG, "Can't get key " + name + " from " + mUri);
					return null;
				}

				String value = c.moveToNext() ? c.getString(0) : null;
				if (LOCAL_LOGV) {
					Log.v(TAG, "cache miss [" + mUri.getLastPathSegment()
							+ "]: " + name + " = "
							+ (value == null ? "(null)" : value));
				}
				return value;
			} finally {
				if (c != null)
					c.close();
			}
		}
	}

	/**
	 * Robot settings, containing miscellaneous system preferences. This table
	 * holds simple name/value pairs. There are convenience functions for
	 * accessing individual settings entries.
	 */
	public static final class Robot extends NameValueTable {
		// Populated lazily, guarded by class object:
		private static NameValueCache sNameValueCache = null;
		
		private static final HashSet<String> MOVED_TO_CLIENT;
		static {
			MOVED_TO_CLIENT = new HashSet<String>(30);
		}

		/**
		 * Look up a name in the database.
		 * 
		 * @param resolver
		 *            to access the database with
		 * @param name
		 *            to look up in the table
		 * @return the corresponding value, or null if not present
		 */
		public synchronized static String getString(ContentResolver resolver,
				String name) {
			if (sNameValueCache == null) {
				sNameValueCache = new NameValueCache(CONTENT_URI,
						CALL_METHOD_GET_ROBOT);
			}
			return sNameValueCache.getString(resolver, name);
		}

		/**
		 * Store a name/value pair into the database.
		 * 
		 * @param resolver
		 *            to access the database with
		 * @param name
		 *            to store
		 * @param value
		 *            to associate with the name
		 * @return true if the value was set, false on database errors
		 */
		public static boolean putString(ContentResolver resolver, String name,
				String value) {
			if (MOVED_TO_CLIENT.contains(name)) {
				Log.w(TAG,
						"Setting "
								+ name
								+ " has moved from android.provider.Settings.System"
								+ " to android.provider.Settings.Secure, value is unchanged.");
				return false;
			}
			return putString(resolver, CONTENT_URI, name, value);
		}

		/**
		 * Construct the content URI for a particular name/value pair, useful
		 * for monitoring changes with a ContentObserver.
		 * 
		 * @param name
		 *            to look up in the table
		 * @return the corresponding content URI, or null if not present
		 */
		public static Uri getUriFor(String name) {
			return getUriFor(CONTENT_URI, name);
		}

		/**
		 * Convenience function for retrieving a single system settings value as
		 * an integer. Note that internally setting values are always stored as
		 * strings; this function converts the string to an integer for you. The
		 * default value will be returned if the setting is not defined or not
		 * an integer.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to retrieve.
		 * @param def
		 *            Value to return if the setting is not defined.
		 * 
		 * @return The setting's current value, or 'def' if it is not defined or
		 *         not a valid integer.
		 */
		public static int getInt(ContentResolver cr, String name, int def) {
			String v = getString(cr, name);
			try {
				return v != null ? Integer.parseInt(v) : def;
			} catch (NumberFormatException e) {
				return def;
			}
		}

		/**
		 * Convenience function for retrieving a single system settings value as
		 * an integer. Note that internally setting values are always stored as
		 * strings; this function converts the string to an integer for you.
		 * <p>
		 * This version does not take a default value. If the setting has not
		 * been set, or the string value is not a number, it throws
		 * {@link SettingNotFoundException}.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to retrieve.
		 * 
		 * @throws SettingNotFoundException
		 *             Thrown if a setting by the given name can't be found or
		 *             the setting value is not an integer.
		 * 
		 * @return The setting's current value.
		 */
		public static int getInt(ContentResolver cr, String name)
				throws SettingNotFoundException {
			String v = getString(cr, name);
			try {
				return Integer.parseInt(v);
			} catch (NumberFormatException e) {
				throw new SettingNotFoundException(name);
			}
		}

		/**
		 * Convenience function for updating a single settings value as an
		 * integer. This will either create a new entry in the table if the
		 * given name does not exist, or modify the value of the existing row
		 * with that name. Note that internally setting values are always stored
		 * as strings, so this function converts the given value to a string
		 * before storing it.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to modify.
		 * @param value
		 *            The new value for the setting.
		 * @return true if the value was set, false on database errors
		 */
		public static boolean putInt(ContentResolver cr, String name, int value) {
			return putString(cr, name, Integer.toString(value));
		}

		/**
		 * Convenience function for retrieving a single system settings value as
		 * a {@code long}. Note that internally setting values are always stored
		 * as strings; this function converts the string to a {@code long} for
		 * you. The default value will be returned if the setting is not defined
		 * or not a {@code long}.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to retrieve.
		 * @param def
		 *            Value to return if the setting is not defined.
		 * 
		 * @return The setting's current value, or 'def' if it is not defined or
		 *         not a valid {@code long}.
		 */
		public static long getLong(ContentResolver cr, String name, long def) {
			String valString = getString(cr, name);
			long value;
			try {
				value = valString != null ? Long.parseLong(valString) : def;
			} catch (NumberFormatException e) {
				value = def;
			}
			return value;
		}

		/**
		 * Convenience function for retrieving a single system settings value as
		 * a {@code long}. Note that internally setting values are always stored
		 * as strings; this function converts the string to a {@code long} for
		 * you.
		 * <p>
		 * This version does not take a default value. If the setting has not
		 * been set, or the string value is not a number, it throws
		 * {@link SettingNotFoundException}.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to retrieve.
		 * 
		 * @return The setting's current value.
		 * @throws SettingNotFoundException
		 *             Thrown if a setting by the given name can't be found or
		 *             the setting value is not an integer.
		 */
		public static long getLong(ContentResolver cr, String name)
				throws SettingNotFoundException {
			String valString = getString(cr, name);
			try {
				return Long.parseLong(valString);
			} catch (NumberFormatException e) {
				throw new SettingNotFoundException(name);
			}
		}

		/**
		 * Convenience function for updating a single settings value as a long
		 * integer. This will either create a new entry in the table if the
		 * given name does not exist, or modify the value of the existing row
		 * with that name. Note that internally setting values are always stored
		 * as strings, so this function converts the given value to a string
		 * before storing it.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to modify.
		 * @param value
		 *            The new value for the setting.
		 * @return true if the value was set, false on database errors
		 */
		public static boolean putLong(ContentResolver cr, String name,
				long value) {
			return putString(cr, name, Long.toString(value));
		}

		/**
		 * Convenience function for retrieving a single system settings value as
		 * a floating point number. Note that internally setting values are
		 * always stored as strings; this function converts the string to an
		 * float for you. The default value will be returned if the setting is
		 * not defined or not a valid float.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to retrieve.
		 * @param def
		 *            Value to return if the setting is not defined.
		 * 
		 * @return The setting's current value, or 'def' if it is not defined or
		 *         not a valid float.
		 */
		public static float getFloat(ContentResolver cr, String name, float def) {
			String v = getString(cr, name);
			try {
				return v != null ? Float.parseFloat(v) : def;
			} catch (NumberFormatException e) {
				return def;
			}
		}

		/**
		 * Convenience function for retrieving a single system settings value as
		 * a float. Note that internally setting values are always stored as
		 * strings; this function converts the string to a float for you.
		 * <p>
		 * This version does not take a default value. If the setting has not
		 * been set, or the string value is not a number, it throws
		 * {@link SettingNotFoundException}.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to retrieve.
		 * 
		 * @throws SettingNotFoundException
		 *             Thrown if a setting by the given name can't be found or
		 *             the setting value is not a float.
		 * 
		 * @return The setting's current value.
		 */
		public static float getFloat(ContentResolver cr, String name)
				throws SettingNotFoundException {
			String v = getString(cr, name);
			if (v == null) {
				throw new SettingNotFoundException(name);
			}
			try {
				return Float.parseFloat(v);
			} catch (NumberFormatException e) {
				throw new SettingNotFoundException(name);
			}
		}

		/**
		 * Convenience function for updating a single settings value as a
		 * floating point number. This will either create a new entry in the
		 * table if the given name does not exist, or modify the value of the
		 * existing row with that name. Note that internally setting values are
		 * always stored as strings, so this function converts the given value
		 * to a string before storing it.
		 * 
		 * @param cr
		 *            The ContentResolver to access.
		 * @param name
		 *            The name of the setting to modify.
		 * @param value
		 *            The new value for the setting.
		 * @return true if the value was set, false on database errors
		 */
		public static boolean putFloat(ContentResolver cr, String name,
				float value) {
			return putString(cr, name, Float.toString(value));
		}

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/robot");

		 /**
         * A 64-bit number (as a hex string) that is randomly
         * generated on the device's first boot and should remain
         * constant for the lifetime of the device.  (The value may
         * change if a factory reset is performed on the device.)
         */
        public static final String ANDROID_ID = "android_id";
        
        
        
	}//robot

}
