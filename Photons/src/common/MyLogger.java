package common;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Class for logging methods
 * @author emil
 * TODO: this class is not tested yet
 *
 */
public class MyLogger {

	//private static String logFileNameWithPath;
	private static SimpleDateFormat dateTimeLogFormatter = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.SSS");  
	//private static final Logger fileLogger=Logger.getLogger("Photons");
	
	private static String actionLogFile;
	//private static String logFile;
	
	public static void displayAndLogActionMessage(String message) {
		displayActionMessage(message);
		logAction(message);
	}
	
	public static void displayActionMessage(String message) {
		System.out.println(message);
	}

	public static void displayAndLogException(Exception e) {
		displayException(e);
		logAction(e.getMessage());
	}

	public static void displayException(Exception e) {
		e.printStackTrace();
	}
	
	private static void logAction(String action) {
		FileUtil.writeToFile(
				actionLogFile,
				String.format("%s - %s\n", dateTimeLogFormatter.format(new Date()), action),
				true);
    }
	
//	public static String getActionLogFile() {
//		return actionLogFile;
//	}
	
	public static void setActionLogFile(String actionLogFile) {
		MyLogger.actionLogFile = actionLogFile;
	}

	
//	public static String getLogFile() {
//		return logFile;
//	}
	
//	public static void setLogFile(String logFile) {
//		MyLogger.logFile = logFile;
//	}
}
