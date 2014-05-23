package common;

import java.util.Date;

public class DatabaseUtil {
	public static String getStringFromBoolValue(Boolean value) {
		return value ? "1" : "0";
	}
	
	public static Boolean getBooleanFromStringValue(String value) {
		return value.equals("1");
	}

	public static long getLongTimeStampCurrent() {
		return new Date().getTime();
	}
	
	public static void CheckSQLite() {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO: improve logging
			e.printStackTrace();
			System.exit(0);
		}
	}
}
