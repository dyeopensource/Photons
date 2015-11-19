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
	
	private static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd-HHmmSS");  
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			MyLogger.displayActionMessage("Wrong usage. Command line parameters:\n<SourcePath> <DestinationPath> <Type>\nExample:\nPhotons /media/store /home/myUser/pictures jpg");
			return;
		}

		String sourcePath;
		String destinationPath;
		String type;
		
		sourcePath = args[0];
		destinationPath = args[1];
		type = args[2];

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
		
		FileImporter fileImporter = new FileImporter(sourcePath, destinationPath, type);
		try {
			fileImporter.Import();
		} catch (IOException e) {
			MyLogger.displayAndLogException(e);
		}

		MyLogger.displayAndLogActionMessage("Done");
	}
}
