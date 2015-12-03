package common;

import java.util.Date;

/**
 * Class for general database methods
 * @author emil
 *
 */
public class DatabaseUtil {
	
	/**
	 * A special value for an ID if it is not set
	 */
	public static long idNotSetValue = -1;
	
	/**
	 * Gets a single character string ("0"/"1") from a boolean value
	 * @param value The boolean value to convert to String
	 * @return A single character string ("0"/"1") from a boolean value
	 */
	public static String getStringFromBoolValue(boolean value) {
		return value ? "1" : "0";
	}
	
	/**
	 * Gets a boolean value from a string value stored in the database
	 * @param value	The string (retrieved from the database) to convert to boolean (0/1)
	 * @return		A boolean value as a result of the string parsed
	 */
	public static boolean getBooleanFromStringValue(String value) {
		return value.equals("1");
	}

	/**
	 * Gets the current date as long
	 * @return	The current date as long
	 */
	public static long getLongTimeStampCurrent() {
		return new Date().getTime();
	}
	
	/**
	 * Checks if SQLite JDBC is present or not.
	 * If not, logs and exits the application.
	 */
	public static void CheckSQLite() {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			MyLogger.displayAndLogExceptionMessage(e, "JDBC not found.");
			System.exit(1);
		}
	}
}
