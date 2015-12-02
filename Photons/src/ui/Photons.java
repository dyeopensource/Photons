package ui;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import modell.FileImporter;
import common.FileUtil;
import common.MyLogger;


public class Photons {
	
	public static int errorCodeImportSourceFolderDoesNotExist = 1;
	public static int errorCodeImportDestinationFolderDoesNotExist = 2;
	public static int errorCodeFailedToCreateLogFile = 3;
	public static int errorCodeLengthMismatch = 4;
	public static int errorCodeHashMismatch = 5;
	public static int errorCodeFailedToInsertFileIntoDatabase = 6;
	public static int errorCodeFailedToAddSourcePathInformationToDatabase = 7;
	public static int errorCodeFailedToGetFileInformationFromDatabase = 8;
	public static int errorCodeUnsupportedDatabaseVersion = 9;
	public static int errorCodeFailedToInsertFileGroupInformationIntoDatabase = 10;
	public static int errorCodeFailedToInsertFileGroupAssignmentInformationIntoDatabase = 11;
	public static int errorCodeDuplicateImportedFile = 12;
	public static int errorCodeFileInsertionVerificationFailed = 13;
	public static int errorCodeFailedToInsertFileInfoInformationIntoDatabase = 14;
	public static int errorCodeCommandOrOptionDoesNotExist = 15;
	public static int errorCodeWrongUsage = 16;
	
	private static int actionShowUsage = 0;
	private static int actionShowUI = 1;
	private static int actionImport = 2;
	private static int actionVerify = 3;
	private static int actionCheck = 4;
	
	private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd-HHmmSS");
	
	private static String commandImport = "import";
	private static String commandVerify = "verify";
	private static String commandCheck = "check";
	
	private static String argSource = "source";
	private static String argDestination = "destination";
	private static String argTypes = "types";
	
	private static String usage = "\n"
			+ "Usage:\n"
			+ "\n"
			+ "java -jar photons.jar [CommandLineParamaters]\n"
			+ "\n"
			+ "Command line parameters:\n"
			+ "\n"
			+ "import /source=<SourcePath> /destination=<DestinationPath> /types=<Types>\n"
			+ "verify /source=<SourcePath> /destination=<DestinationPath> /types=<Type>\n"
			+ "check /destination=<DestinationPath>\n"
			+ "\n"
			+ "Example:\n"
			+ "\n"
			+ "Importing jpg files:\n"
			+ "java -jar photons.jar import /source=/media/store /destination=/home/myUser/pictures /types=jpg\n"
			+ "Verifying that all jpg files were imported correctly:\n"
			+ "java -jar photons.jar verify /source=/media/store /destination=/home/myUser/pictures /types=jpg\n"
			+ "Checking consistency of the target path (e.g. after making a copy of it):\n"
			+ "java -jar photons.jar check /destination=/home/myUser/pictures\n"
			;

	private static String command = null;
	private static String sourcePath = null;
	private static String destinationPath = null;
	private static String[] types = null;
	
	private static int action = actionShowUsage;
	private static int errorCode = 0;
	
	// TODO: introduce and implement verify command (check imported folder and database consistency)
	// this could be called also CheckConsistency (but it is a bit long)
	
	// TODO: introduce command: fillmediatimestamp (this would analyze exif information etc. - update database information)
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		InterpretCommandLineArguments(args);
		
		if (Photons.action == Photons.actionShowUsage) {
			MyLogger.displayActionMessage(usage);
			System.exit(errorCode);
		}
		
		final Path logPath = Paths.get(Photons.destinationPath, String.format("actions_%s.txt", dateTimeFormatter.format(new Date())));
		try {
			MyLogger.setActionLogFile(logPath);
		} catch (IOException e) {
			System.err.println(String.format("ERROR: Could not create logfile '%s'.", logPath));
			e.printStackTrace();
			System.exit(errorCodeFailedToCreateLogFile);
		}
		
		if (Photons.action == Photons.actionImport) {
			FileImporter fileImporter = new FileImporter(Photons.sourcePath, Photons.destinationPath, Photons.types);
			try {
				fileImporter.Import();
			} catch (IOException e) {
				MyLogger.displayAndLogException(e);
			}
		} else if (Photons.action == Photons.actionVerify) {
			// TODO: implement
			MyLogger.displayAndLogActionMessage("ERROR: Command [%s] is not implemented yet.", Photons.command);
			Photons.errorCode = errorCodeCommandOrOptionDoesNotExist;
		} else if (Photons.action == Photons.actionCheck) {
			// TODO: implement
			MyLogger.displayAndLogActionMessage("ERROR: Command [%s] is not implemented yet.", Photons.command);
			Photons.errorCode = errorCodeCommandOrOptionDoesNotExist;
		}
		
		if (Photons.errorCode != 0) {
			System.exit(Photons.errorCode);
		}

		MyLogger.displayAndLogActionMessage("Done");
	}
	
	/**
	 * Interprets the command line arguments and sets the appropriate fields
	 * @param args The command line arguments
	 */
	private static void InterpretCommandLineArguments(String[] args) {
		
		if (args.length == 0) {
			// This will show the UI. Not implemented yet
			Photons.action = Photons.actionShowUI;
		} else {
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				String lowerCaseArg = arg.toLowerCase();
				if (lowerCaseArg.equals(Photons.commandImport)) {
					Photons.action = Photons.actionImport;
				} else if (lowerCaseArg.equals(Photons.commandVerify)) {
					Photons.action = Photons.actionVerify;
				} else if (lowerCaseArg.equals(Photons.commandCheck)) {
					Photons.action = Photons.actionCheck;
				} else if (lowerCaseArg.startsWith("/")) {
					int equationSignIndex = arg.indexOf("=");
					if (equationSignIndex > 0) {
						String valueName = lowerCaseArg.substring(1, equationSignIndex);
						String value = arg.substring(equationSignIndex + 1, arg.length());
						if (valueName.equals(argSource)) {
							Photons.sourcePath = value;
						} else if (valueName.equals(argDestination)) {
							Photons.destinationPath = value;
						} else if (valueName.equals(argTypes)) {
							Photons.types = value.split(",");
						}
					}
				} else {
					MyLogger.displayActionMessage("ERROR: Command or option [%s] does not exist. Cannot proceed.", lowerCaseArg);
					errorCode = errorCodeCommandOrOptionDoesNotExist;
				}
			}
			
			if (Photons.action == Photons.actionImport) {
				CheckSourcePath();
				CheckDestinationPath();
				CheckTypes();
			} else if (Photons.action == Photons.actionVerify) {
				CheckSourcePath();
				CheckDestinationPath();
				CheckTypes();
			} else if (Photons.action == Photons.actionCheck) {
				CheckDestinationPath();
			}
		}
	}
	
	private static void CheckSourcePath() {
		if (Photons.sourcePath == null) {
			MyLogger.displayAndLogActionMessage("ERROR: Source path not specified.");
			Photons.action = Photons.actionShowUsage;
		} else if (!FileUtil.folderExists(sourcePath)) {
			MyLogger.displayAndLogActionMessage("ERROR: Source folder [%s] does not exist. Cannot %s.", Photons.sourcePath, Photons.command);
			System.exit(errorCodeImportSourceFolderDoesNotExist);
		}
	}
	
	private static void CheckDestinationPath() {
		if (Photons.destinationPath == null) {
			MyLogger.displayAndLogActionMessage("ERROR: Destination path not specified.");
			Photons.action = Photons.actionShowUsage;
		} else if (Photons.destinationPath != null && !FileUtil.folderExists(Photons.destinationPath)) {
			MyLogger.displayAndLogActionMessage("ERROR: Destination folder [%s] does not exist. Cannot %s.", Photons.destinationPath, Photons.command);
			System.exit(errorCodeImportDestinationFolderDoesNotExist);
		}
	}
	
	private static void CheckTypes() {
		if (Photons.types == null) {
			MyLogger.displayAndLogActionMessage("ERROR: Types not specified.");
			Photons.action = Photons.actionShowUsage;
		}
	}
}
