package common;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	private static SimpleDateFormat dateTimeLogFormatter = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.SSS");  
	
	private static Path logPath;
	
	public static void displayAndLogActionMessage(String format, Object... args) {
		String message = String.format(format, args);
		displayMessage(message);
		logMessage(message);
	}
	
	public static void displayActionMessage(String format, Object... args) {
		String message = String.format(format, args);
		displayMessage(message);
	}

	public static void displayAndLogException(Exception e) {
		displayException(e);
		logMessage(e.getMessage());
	}

	public static void displayException(Exception e) {
		//displayActionMessage("%s: %s", e.getClass().getName(), e.getMessage());
		e.printStackTrace();
	}

	public static void setActionLogFile(Path logPath) throws IOException {
		Path logFolder = logPath.getParent();
		if (!Files.exists(logFolder)) {
			Files.createDirectories(logFolder);
		}
		MyLogger.logPath = logPath;
	}

	private static void displayMessage(String message) {
		System.out.println(message);
	}

	private static void logMessage(String message) {
		BufferedWriter writer = null;
		try {
		    File textFile = logPath.toFile();
		    writer = new BufferedWriter(new FileWriter(textFile, true));
		    writer.write(String.format("%s - %s\n", dateTimeLogFormatter.format(new Date()), message));
		} catch (Exception e) {
			displayException(e);
		} finally {
		    try {
		        // Close the writer regardless of what happens...
		        writer.close();
		    } catch (Exception e) {
				displayException(e);
		    }
		}
	}
}
