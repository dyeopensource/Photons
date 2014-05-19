package common;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


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
	
	private static void logAction(String action) {
		BufferedWriter writer = null;
        try {
            File logFile = new File(actionLogFile);
            writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(String.format("%s - %s\n", dateTimeLogFormatter.format(new Date()), action));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
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
