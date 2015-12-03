package common;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Class for logging methods
 * @author emil
 * TODO: this class is not tested yet
 *
 */
public class MyLogger {
	
	public static final int logLevelDebug = 1;
	public static final int logLevelInformation = 2;
	public static final int logLevelWarning = 3;
	public static final int logLevelError = 4;
	
	private static final SimpleDateFormat dateTimeLogFormatter = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.SSS");  
	
	private static Path logPath;
	
	private static int logLevel = MyLogger.logLevelInformation;

	public static void setLogLevel(int logLevel) {
		MyLogger.logLevel = logLevel;
	}

	public static void setLogFile(Path logPath) throws IOException {
		Path logFolder = logPath.getParent();
		if (!Files.exists(logFolder)) {
			Files.createDirectories(logFolder);
		}
		
		MyLogger.logPath = logPath;
	}
	
	public static void displayAndLogDebugMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelDebug) {
			MyLogger.displayDebugMessage(format, args);
			MyLogger.logMessage(String.format("DEBUG: " + format, args));
		}
	}
	
	public static void displayDebugMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelDebug) {
			MyLogger.displayMessage(format, args);
		}
	}
	
	public static void displayAndLogInformationMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelInformation) {
			MyLogger.displayInformationMessage(format, args);
			MyLogger.logMessage(String.format("INFO:  " + format, args));
		}
	}
	
	public static void displayInformationMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelInformation) {
			String message = String.format(format, args);
			MyLogger.displayMessage(message);
		}
	}
	
	public static void displayAndLogWarningMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelWarning) {
			MyLogger.displayWarningMessage(format, args);
			MyLogger.logMessage(String.format("WARN:  " + format, args));
		}
	}

	public static void displayWarningMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelWarning) {
			MyLogger.displayMessage(String.format("WARN:  " + format, args));
		}
	}

	public static void displayAndLogExceptionMessage(Exception e, String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelError) {
			MyLogger.displayAndLogErrorMessage(format, args);
			MyLogger.displayAndLogException(e);
		}
	}
	
	public static void displayExceptionMessage(Exception e, String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelError) {
			MyLogger.displayErrorMessage(format, args);
			MyLogger.displayException(e);
		}
	}
	
	public static void displayAndLogErrorMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelError) {
			MyLogger.displayErrorMessage(format, args);
			MyLogger.logMessage(String.format("ERROR: " + format, args));
		}
	}
	
	public static void displayErrorMessage(String format, Object... args) {
		if (MyLogger.logLevel <= MyLogger.logLevelError) {
			MyLogger.displayMessage(String.format("ERROR: " + format, args));
		}
	}
	
	private static void displayMessage(String format, Object... args) {
		String message = String.format(format, args);
		MyLogger.displayMessage(message);
	}

	private static void displayAndLogException(Exception e) {
		displayException(e);
		logException(e);
	}

	private static void displayException(Exception e) {
		e.printStackTrace();
	}
	
	private static void displayMessage(String message) {
		System.out.println(message);
	}

	private static void logMessage(String message) {
		BufferedWriter writer = null;
		try {
		    File logFile = logPath.toFile();
		    writer = new BufferedWriter(new FileWriter(logFile, true));
		    writer.write(String.format("%s - %s\n", dateTimeLogFormatter.format(new Date()), message));
		} catch (Exception ex) {
			displayException(ex);
		} finally {
		    try {
		        // Close the writer regardless of what happens...
		        writer.close();
		    } catch (Exception ex) {
				displayException(ex);
		    }
		}
	}

	private static void logException(Exception e) {
	    File logFile = logPath.toFile();
	    PrintStream ps = null;
		try {
			ps = new PrintStream(logFile);
			e.printStackTrace(ps);
		} catch (Exception ex) {
			displayException(e);
		} finally {
			if (ps != null) {
				ps.close();	
			}
		}
	}
}
