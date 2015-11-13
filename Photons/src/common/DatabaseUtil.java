package common;

import java.util.Date;

/**
 * Class for general database methods
 * @author emil
 *
 */
public class DatabaseUtil {
	
	/**
	 * Gets a single character string (0/1) from a boolean value
	 * @param value
	 * @return
	 */
	public static String getStringFromBoolValue(boolean value) {
		return value ? "1" : "0";
	}
	
	/**
	 * Gets a boolean value from a string value stored in the database
	 * @param value	The string from the database to convert to boolean (0/1)
	 * @return		A boolean value as a result of the string parsed
	 */
	public static boolean getBooleanFromStringValue(String value) {
		return value.equals("1");
	}

	/**
	 * Gets the current date as long
	 * @return	The current date as long
	 * TODO: this method is not tested
	 */
	public static long getLongTimeStampCurrent() {
		return new Date().getTime();
	}
	
	/**
	 * Checks if SQLite JDBC is present or not.
	 * If not, logs and exits the application.
	 * TODO: this method is not tested yet
	 */
	public static void CheckSQLite() {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			MyLogger.displayAndLogException(e);
			System.exit(1);
		}
	}
}
