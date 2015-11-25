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
	public static int errorCodeImportTargetFolderDoesNotExist = 2;
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
	public static int errorCodeCommandDoesNotExist = 15;
	
	private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd-HHmmSS");
	
	private static String commandImport = "import";
	
	// TODO: introduce and implement verify command (check imported folder and database consistency)
	// this could be called also CheckConsistency (but it is a bit long)
	
	// TODO: introduce command: fillmediatimestamp (this would analyze exif information etc. - update database information)
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 4) {
			MyLogger.displayActionMessage("Wrong usage. Command line parameters:\n<Command> <SourcePath> <DestinationPath> <Type>\nExample:\nPhotons /media/store /home/myUser/pictures jpg");
			return;
		}

		String command;
		String sourcePath;
		String destinationPath;
		String type;
		
		command = args[0].toLowerCase();
		sourcePath = args[1];
		destinationPath = args[2];
		type = args[3];

		if (!command.equals(commandImport)) {
			MyLogger.displayAndLogActionMessage("Command [%s] does not exist. Cannot proceed.", command);
			System.exit(errorCodeCommandDoesNotExist);
		}
		
		if (!FileUtil.folderExists(sourcePath)) {
			MyLogger.displayAndLogActionMessage("Import source folder [%s] does not exist. Cannot import.", sourcePath);
			System.exit(errorCodeImportSourceFolderDoesNotExist);
		}
		
		if (!FileUtil.folderExists(destinationPath)) {
			MyLogger.displayAndLogActionMessage("Import target folder [%s] does not exist. Cannot import.", destinationPath);
			System.exit(errorCodeImportTargetFolderDoesNotExist);
		}
		
		final Path logPath = Paths.get(destinationPath, String.format("actions_%s.txt", dateTimeFormatter.format(new Date())));
		try {
			MyLogger.setActionLogFile(logPath);
		} catch (IOException e) {
			System.err.println(String.format("Could not create logfile '%s'.", logPath));
			e.printStackTrace();
			System.exit(errorCodeFailedToCreateLogFile);
		}
		
		if (command.equals(commandImport)) {
			FileImporter fileImporter = new FileImporter(sourcePath, destinationPath, type);
			try {
				fileImporter.Import();
			} catch (IOException e) {
				MyLogger.displayAndLogException(e);
			}
		}

		MyLogger.displayAndLogActionMessage("Done");
	}
}
