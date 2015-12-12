package modell;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import common.DatabaseUtil;
import common.MyLogger;
import ui.Photons;

/**
 * This class is responsible for the database storage of imported files 
 * @author emil
 * TODO: this class is not tested yet
 */
public class FileInfoDatabase {

	private static final String defaultDatabaseFileName = "fileInfo.sqlite";

	private static final String versionStringCurrent = "2.3.4";
	
	private static final String versionStringOld231 = "2.3.1";
	private static final String versionStringOld232 = "2.3.2";
	private static final String versionStringOld233 = "2.3.3";
	private static final String versionStringOld234 = "2.3.4";
	
	private static final String fieldNameId = "id";
	
	private static final String configTableName = "config";
	private static final String configTableFieldNameValue = "value";
	private static final String configTableFieldNameRecordLastModificationTime = "recordLastModificationTime";
	private static final String configCreateCommandSql = "CREATE TABLE IF NOT EXISTS " + configTableName + " " +
			"(" + fieldNameId + "								INTEGER		PRIMARY KEY, " +
			"key												TEXT		NOT NULL, " +
			configTableFieldNameValue + "						TEXT		NOT NULL, " +
			configTableFieldNameRecordLastModificationTime + "	INTEGER		NOT NULL, " +
			"deleted											INTEGER		DEFAULT 0)";
	private static final String configVersionInsertCommandSql = "INSERT INTO " + configTableName + " (key, " + configTableFieldNameValue + ", recordLastModificationTime) " +
			"VALUES ('version', '" + versionStringCurrent + "', %d)";
	//private static final String configVersionUpdateToCurrentCommandSql = "UPDATE " + configTableName + " SET " + configTableFieldNameValue + "='" + versionStringCurrent + "' WHERE key='version';"
	//		+ "UPDATE " + configTableName + " SET " + configTableFieldNameRecordLastModificationTime + "=%d;";
	private static final String configVersionUpdateToOldCommandSql = "UPDATE " + configTableName + " SET " + configTableFieldNameValue + "='%s' WHERE key='version';"
			+ "UPDATE " + configTableName + " SET " + configTableFieldNameRecordLastModificationTime + "=%d;";
	private static final String configVersionSelectCommandSql = "SELECT " + configTableFieldNameValue + ", recordLastModificationTime " +
			"FROM " + configTableName + " WHERE key = 'version' ORDER BY value ASC";

	private static final String fileInfoTableName = "fileinfo";
	private static final String fileInfoTableCreationCommandSql = "CREATE TABLE IF NOT EXISTS " + fileInfoTableName + " " +
			"(" + fieldNameId + "			INTEGER	PRIMARY KEY, " +
			"originalFileNameWithPath		TEXT	NOT NULL, " +
			"originalLength					INTEGER	NOT NULL, " +
			"originalHash					TEXT	NOT NULL, " +
			"originalLastModificationTime	INTEGER	NOT NULL, " +
			"subFolder						TEXT	NOT NULL, " +
			"fileName						TEXT	NOT NULL, " +
			"importEnabled					INTEGER	NOT NULL, " +
			"type							INTEGER	NOT NULL, " +
			"description					TEXT	NOT NULL, " +
			"recordLastModificationTime		INTEGER	NOT NULL, " +
			"deleted						INTEGER	DEFAULT 0)";
	private static final String fileInfoInsertCommandSql = "INSERT INTO " + fileInfoTableName + " " +
			"(originalFileNameWithPath, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	/**
	 * SELECT statement to get records from fileinfo table with the specified original hash and size values 
	 */
	private static final String selectFileInfoCommandSql = "SELECT " +
			fieldNameId + ", originalFileNameWithPath, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime, deleted " +
			"FROM " + fileInfoTableName + " WHERE originalHash=? AND originalLength=?";

	private static final String fileGroupTableName = "filegroup";
	private static final String fileGroupTableFieldNameDescription = "description";
	private static final String fileGroupTableCreationCommandSql = "CREATE TABLE IF NOT EXISTS " + fileGroupTableName + " " +
			"(" + fieldNameId + "					INTEGER	PRIMARY KEY, " +
			fileGroupTableFieldNameDescription + "	TEXT	NOT NULL, " +
			"recordLastModificationTime				INTEGER	NOT NULL, " +
			"deleted								INTEGER	DEFAULT 0)";
	private static final String fileGroupInsertCommandSql = "INSERT INTO " + fileGroupTableName + " " +
			"(" + fileGroupTableFieldNameDescription + ", recordLastModificationTime) " +
			"VALUES (?, ?)";
	private static final String selectFileGroupCommandSql = "SELECT " +
			fieldNameId + ", " + fileGroupTableFieldNameDescription + ", recordLastModificationTime, deleted " +
			"FROM " + fileGroupTableName + " WHERE " + fileGroupTableFieldNameDescription + "=?";

	private static final String fileGroupAssignmentTableName = "filegroupassignment";
	private static final String fileGroupAssignmentTableCreationCommandSql = "CREATE TABLE IF NOT EXISTS " + fileGroupAssignmentTableName + " " +
			"(" + fieldNameId + "			INTEGER	PRIMARY KEY, " +
			"groupid						INTEGER	NOT NULL, " +
			"fileid							INTEGER	NOT NULL, " +
			"recordLastModificationTime		INTEGER	NOT NULL, " +
			"deleted						INTEGER	DEFAULT 0)";
	private static final String fileGroupAssignmentInsertCommandSql = "INSERT INTO " + fileGroupAssignmentTableName + " " +
			"(groupid, fileid, recordLastModificationTime) " +
			"VALUES (?, ?, ?)";
	private static final String selectFileGroupAssignmentCommandSql = "SELECT " +
			fieldNameId + ", groupid, fileid, recordLastModificationTime, deleted " +
			"FROM " + fileGroupAssignmentTableName + " WHERE groupid=? AND fileid=?";

	private static final String fileTypeTableName = "filetype";
	private static final String fileTypeTableFieldNameExtension = "extension";
	private static final String fileTypeTableCreationCommandSql = "CREATE TABLE IF NOT EXISTS " + fileTypeTableName + " " +
			"(" + fieldNameId + "					INTEGER	PRIMARY KEY, " +
			fileTypeTableFieldNameExtension + 	"	TEXT	NOT NULL, " +
			"recordLastModificationTime				INTEGER	NOT NULL, " +
			"deleted								INTEGER	DEFAULT 0)";
	private static final String fileTypeInsertCommandSql = "INSERT INTO " + fileTypeTableName + " " +
			"(" + fileTypeTableFieldNameExtension + ", recordLastModificationTime) " +
			"VALUES (?, ?)";
	private static final String selectFileTypeCommandSql = "SELECT " +
			fieldNameId + ", " + fileTypeTableFieldNameExtension + ", recordLastModificationTime, deleted " +
			"FROM " + fileTypeTableName + " WHERE " + fileTypeTableFieldNameExtension + "=?";
	
	private Path databaseFolder;
	private Path databaseFilePath;
	private String connectionString;


	public FileInfoDatabase(Path databaseFolder) {
		setPrivateData(databaseFolder, defaultDatabaseFileName);
	}

	private void setPrivateData(Path databaseFolder, String fileName) {
		this.databaseFolder = databaseFolder;
		this.databaseFilePath = Paths.get(databaseFolder.toString(), fileName);
		this.connectionString = String.format("jdbc:sqlite:%s", this.databaseFilePath);
	}
	
	/**
	 * Opens or creates the database for file import - does version check if already exists
	 */
	public void openOrCreateDatabase() {
	    try {
			DatabaseUtil.CheckSQLite();

			if (!Files.exists(this.databaseFolder)) {
				MyLogger.displayAndLogWarningMessage("Path does not exist. Creating folder [%s]...", this.databaseFolder);
	    		Files.createDirectories(this.databaseFolder);
	    	}
	    	
			Boolean databaseAlreadyExists = Files.exists(this.databaseFilePath) && !Files.isDirectory(this.databaseFilePath);
		    Connection connection = DriverManager.getConnection(this.connectionString);
			if (databaseAlreadyExists) {
				String  databaseVersion = "";
				try {
					Statement queryStatement = connection.createStatement();
					ResultSet resultSet = queryStatement.executeQuery(configVersionSelectCommandSql);
					while ( resultSet.next() ) {
						databaseVersion = resultSet.getString(configTableFieldNameValue);
					}
					
					resultSet.close();
					queryStatement.close();

					if (databaseVersion.equals(FileInfoDatabase.versionStringCurrent)) {
						// OK
						MyLogger.displayAndLogDebugMessage("Database already exists with expected version: [databaseVersion=%s] at [databaseFolder=%s].", databaseVersion, this.databaseFolder);
					} else if (databaseVersion.equals(FileInfoDatabase.versionStringOld231)) {
						upgradeFrom231to232(connection);
						upgradeFrom232to233(connection);
						upgradeFrom233to234(connection);
					} else if (databaseVersion.equals(FileInfoDatabase.versionStringOld232)) {
						upgradeFrom232to233(connection);
						upgradeFrom233to234(connection);
					} else if (databaseVersion.equals(FileInfoDatabase.versionStringOld233)) {
						upgradeFrom233to234(connection);
					} else {
						MyLogger.displayAndLogErrorMessage(String.format("Unsupported database version: [databaseVersion=%s] at [databaseFolder=%s].", databaseVersion, this.databaseFolder));
						System.exit(Photons.errorCodeUnsupportedDatabaseVersion);
					}
				} catch ( Exception e ) {
					MyLogger.displayAndLogExceptionMessage(e, "Version check failed for database [this.databaseFilePath=%s]", this.databaseFilePath);
			    	System.exit(Photons.errorCodeDatabaseVersionCheckFailure);
				}
			} else {
				Statement sqlStatement = connection.createStatement();
				sqlStatement.executeUpdate(configCreateCommandSql);
				sqlStatement.executeUpdate(String.format(configVersionInsertCommandSql, DatabaseUtil.getLongTimeStampCurrent()));
				sqlStatement.executeUpdate(fileInfoTableCreationCommandSql);
				sqlStatement.executeUpdate(fileGroupTableCreationCommandSql);
				sqlStatement.executeUpdate(fileGroupAssignmentTableCreationCommandSql);
				sqlStatement.executeUpdate(fileTypeTableCreationCommandSql);
			      
				sqlStatement.close();
	
				MyLogger.displayAndLogDebugMessage("Created database successfully");
			}
			
		    connection.close();
	    } catch ( Exception e ) {
	    	MyLogger.displayAndLogExceptionMessage(e, "openOrCreateDatabase failed");
	    	System.exit(Photons.errorCodeFailedToOpenOrCreateDatabase);
	    }
	}

	private void upgradeFrom231to232(Connection connection) throws SQLException {
		// Upgrading from 2.3.1 to 2.3.2
		MyLogger.displayAndLogDebugMessage("Database already exists with version: [versionStringOld231=%s] at [databaseFolder=%s]. Upgrading to [versionStringOld232=%s]", FileInfoDatabase.versionStringOld231, this.databaseFolder, FileInfoDatabase.versionStringOld232);

		// Removing mediaContentTimeStamp field from fileinfo table:
		Statement sqlStatement = connection.createStatement();
		sqlStatement.executeUpdate("create table fileinfo1 as select id, originalFileNameWithPath, originalLength, originalHash, originalLastModificationTime, userTimeStamp, subFolder, fileName, importEnabled, type, description, recordLastModificationTime, deleted from fileinfo;"
		+ " drop table fileinfo;"
		+ " alter table fileinfo1 rename to fileinfo;");
	      
		sqlStatement.executeUpdate(String.format(configVersionUpdateToOldCommandSql, FileInfoDatabase.versionStringOld232, DatabaseUtil.getLongTimeStampCurrent()));

		sqlStatement.close();

		MyLogger.displayAndLogInformationMessage("Updated database from [versionStringOld231=%s] to [versionStringOld232=%s]", FileInfoDatabase.versionStringOld231, FileInfoDatabase.versionStringOld232);
	}

	private void upgradeFrom232to233(Connection connection) throws SQLException {
		// Upgrading from 2.3.2 to 2.3.3
		MyLogger.displayAndLogDebugMessage("Database already exists with version: [versionStringOld232=%s] at [databaseFolder=%s]. Upgrading to [versionStringOld233=%s]", FileInfoDatabase.versionStringOld232, this.databaseFolder, FileInfoDatabase.versionStringOld233);

		// Removing userTimeStamp field from fileinfo table:
		Statement sqlStatement = connection.createStatement();
		sqlStatement.executeUpdate("create table fileinfo1 as select id, originalFileNameWithPath, originalLength, originalHash, originalLastModificationTime, subFolder, fileName, importEnabled, type, description, recordLastModificationTime, deleted from fileinfo;"
		+ " drop table fileinfo;"
		+ " alter table fileinfo1 rename to fileinfo;");
	      
		sqlStatement.executeUpdate(String.format(configVersionUpdateToOldCommandSql, FileInfoDatabase.versionStringOld233, DatabaseUtil.getLongTimeStampCurrent()));

		sqlStatement.close();

		MyLogger.displayAndLogInformationMessage("Updated database from [versionStringOld232=%s] to [versionStringOld233=%s]", FileInfoDatabase.versionStringOld232, FileInfoDatabase.versionStringOld233);
	}

	private void upgradeFrom233to234(Connection connection) throws SQLException {
		// Upgrading from 2.3.3 to 2.3.4
		MyLogger.displayAndLogDebugMessage("Database already exists with version: [versionStringOld233=%s] at [databaseFolder=%s]. Upgrading to [versionStringOld234=%s]", FileInfoDatabase.versionStringOld233, this.databaseFolder, FileInfoDatabase.versionStringOld234);

		// Creating filetype table
		Statement sqlStatement = connection.createStatement();
		sqlStatement.executeUpdate(fileTypeTableCreationCommandSql);

		sqlStatement.close();
		
		// Inserting JPG type with ID 1
		String jpgTypeName = "jpg";
		long jpgTypeId = getOrCreateFileTypeId(connection, jpgTypeName);
		if (jpgTypeId != 1) {
			MyLogger.displayAndLogErrorMessage("id of filetype [type=%s] is not 1.", jpgTypeName);
			System.exit(Photons.errorCodeFailedToInsertFileTypeInformationIntoDatabase);
		}

		MyLogger.displayAndLogInformationMessage("Updated database from [versionStringOld233=%s] to [versionStringOld234=%s]", FileInfoDatabase.versionStringOld233, FileInfoDatabase.versionStringOld234);
	}
	
	/**
	 * Inserts a new file record into the database
	 * @param importTargetPath The target folder where the file was copied 
	 * @param fileInfo The object containing data to be inserted into the database
	 * @throws FileNotFoundException 
	 */
	public void addFileImportedInfo(String importTargetPath, FileImportedInfo fileInfo) {
		DatabaseUtil.CheckSQLite();
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.connectionString);
			
			//connection.setAutoCommit(false); // If running multiple actions in a transaction
			
			PreparedStatement preparedInsertFileInfoStatement = connection.prepareStatement(fileInfoInsertCommandSql);
			
			preparedInsertFileInfoStatement.setString(1, fileInfo.getOriginalFileNameWithPath());
			preparedInsertFileInfoStatement.setLong(2, fileInfo.getLength());
			preparedInsertFileInfoStatement.setString(3, fileInfo.getHash());
			preparedInsertFileInfoStatement.setLong(4, fileInfo.getLastModificationTime().getTime());
			preparedInsertFileInfoStatement.setString(5, fileInfo.getSubfolder());
			preparedInsertFileInfoStatement.setString(6, fileInfo.getFileName());
			preparedInsertFileInfoStatement.setString(7, DatabaseUtil.getStringFromBoolValue(fileInfo.getImportEnabled()));
			preparedInsertFileInfoStatement.setLong(8, fileInfo.getType());
			preparedInsertFileInfoStatement.setString(9, fileInfo.getDescription());
			preparedInsertFileInfoStatement.setLong(10, new Date().getTime());
			
			if (preparedInsertFileInfoStatement.executeUpdate() != 1) {
				MyLogger.displayAndLogErrorMessage("Failed to insert file information [OriginalFileNameWithPath=%s]", fileInfo.getOriginalFileNameWithPath());
				System.exit(Photons.errorCodeFailedToInsertFileInfoInformationIntoDatabase);
			}
			
			preparedInsertFileInfoStatement.close();

			// Note: return value is not checked for storeFileGroupInformation 
			storeFileGroupInformation(connection, fileInfo, null);
			
			//connection.commit(); // If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogExceptionMessage(e, "addFileImportedInfo failed");
			System.exit(Photons.errorCodeFailedToInsertFileIntoDatabase);
		}
	}
	
	private Boolean storeFileGroupInformation(Connection connection, FileImportedInfo fileInfo, FileImportedInfo existingFileImportedInfo) throws SQLException {
		
		Boolean databaseWasChanged = false;
		
		String groupName = fileInfo.getOriginalFilePath();
		
		long groupId = getFileGroupId(connection, groupName);

		if (groupId == DatabaseUtil.idNotSetValue) {
			// File group does not exist - inserting new
			PreparedStatement preparedInsertFileGroupStatement = connection.prepareStatement(fileGroupInsertCommandSql);
			preparedInsertFileGroupStatement.setString(1, groupName);
			preparedInsertFileGroupStatement.setLong(2, new Date().getTime());
			if (preparedInsertFileGroupStatement.executeUpdate() != 1) {
				MyLogger.displayAndLogErrorMessage("Failed to insert filegroup information [description=%s]", groupName);
				System.exit(Photons.errorCodeFailedToInsertFileGroupInformationIntoDatabase);
			}
			
			preparedInsertFileGroupStatement.close();
			
			databaseWasChanged = true;
			
			groupId = getFileGroupId(connection, groupName);
		}

		long fileId = DatabaseUtil.idNotSetValue;
		if (existingFileImportedInfo == null) {
			existingFileImportedInfo = getFileImportedInfo(fileInfo.getHash(), fileInfo.getLength());
		}
		
		fileId = existingFileImportedInfo.getId();
		
		if (getFileGroupAssignmentId(connection, groupId, fileId) == DatabaseUtil.idNotSetValue) {
			PreparedStatement preparedInsertFileGroupAssignmentStatement = connection.prepareStatement(fileGroupAssignmentInsertCommandSql);
			preparedInsertFileGroupAssignmentStatement.setLong(1, groupId);
			preparedInsertFileGroupAssignmentStatement.setLong(2, fileId);
			preparedInsertFileGroupAssignmentStatement.setLong(3, new Date().getTime());
			if (preparedInsertFileGroupAssignmentStatement.executeUpdate() != 1) {
				MyLogger.displayAndLogErrorMessage("Failed to insert filegroup assignment information [groupId=%d] [fileId=%d]", groupId, fileId);
				System.exit(Photons.errorCodeFailedToInsertFileGroupAssignmentInformationIntoDatabase);
			}
			
			preparedInsertFileGroupAssignmentStatement.close();
			
			databaseWasChanged = true;
		}
		
		return databaseWasChanged;
	}
	
	private long getFileGroupId(Connection connection, String description) throws SQLException {
		
		long groupId = DatabaseUtil.idNotSetValue;
		
		PreparedStatement fileGroupQueryStatement = connection.prepareStatement(selectFileGroupCommandSql);
		
		fileGroupQueryStatement.setString(1, description);
		
		ResultSet fileGroupQueryResultSet = fileGroupQueryStatement.executeQuery();
		if ( fileGroupQueryResultSet.next() ) {
			groupId = fileGroupQueryResultSet.getInt(fieldNameId);
		}
		
		fileGroupQueryResultSet.close();

		fileGroupQueryStatement.close();
		
		return groupId;
	}
	
	private long getFileTypeId(Connection connection, String type) throws SQLException {
		
		long fileTypeId = DatabaseUtil.idNotSetValue;
		
		PreparedStatement fileTypeQueryStatement = connection.prepareStatement(selectFileTypeCommandSql);
		
		fileTypeQueryStatement.setString(1, type);
		
		ResultSet fileTypeQueryResultSet = fileTypeQueryStatement.executeQuery();
		if ( fileTypeQueryResultSet.next() ) {
			fileTypeId = fileTypeQueryResultSet.getInt(fieldNameId);
		}
		
		fileTypeQueryResultSet.close();

		fileTypeQueryStatement.close();
		
		return fileTypeId;
	}
	
	public long getOrCreateFileTypeId(String type) {
		
		long fileTypeId = DatabaseUtil.idNotSetValue;
		try {
			
		    Connection connection = DriverManager.getConnection(this.connectionString);
			
		    fileTypeId = getOrCreateFileTypeId(connection, type);
		    
		    connection.close();
		} catch (Exception e) {
			MyLogger.displayAndLogExceptionMessage(e, "Error when getting ID for file type [%s]", type);
			System.exit(Photons.errorCodeFailedToInsertFileTypeInformationIntoDatabase);
		}
	    
	    return fileTypeId;
	}
		
	private long getOrCreateFileTypeId(Connection connection, String type) throws SQLException {
		
		long fileTypeId = getFileTypeId(connection, type);
		
		if (fileTypeId == DatabaseUtil.idNotSetValue) {
			// File type does not exist - inserting new
			PreparedStatement preparedInsertFileTypeStatement = connection.prepareStatement(fileTypeInsertCommandSql);
			preparedInsertFileTypeStatement.setString(1, type);
			preparedInsertFileTypeStatement.setLong(2, new Date().getTime());
			if (preparedInsertFileTypeStatement.executeUpdate() != 1) {
				MyLogger.displayAndLogErrorMessage("Failed to insert filetype information [type=%s]", type);
				System.exit(Photons.errorCodeFailedToInsertFileTypeInformationIntoDatabase);
			}
			
			fileTypeId = getFileTypeId(connection, type);
		}
		
		return fileTypeId;
	}
	
	private long getFileGroupAssignmentId(Connection connection, long groupId, long fileId) throws SQLException {
		
		long assignmentId = DatabaseUtil.idNotSetValue;
		
		PreparedStatement fileGroupAssignmentQueryStatement = connection.prepareStatement(selectFileGroupAssignmentCommandSql);
		
		fileGroupAssignmentQueryStatement.setLong(1, groupId);
		fileGroupAssignmentQueryStatement.setLong(2, fileId);
		
		ResultSet fileGroupAssignmentQueryResultSet = fileGroupAssignmentQueryStatement.executeQuery();
		while ( fileGroupAssignmentQueryResultSet.next() ) {
			assignmentId = fileGroupAssignmentQueryResultSet.getLong(fieldNameId);
		}
		
		fileGroupAssignmentQueryResultSet.close();

		fileGroupAssignmentQueryStatement.close();
		
		return assignmentId;
	}
	
	public Boolean addSourcePathInfo(FileImportedInfo fileImportedInfo, FileImportedInfo existingFileImportedInfo) {
		Boolean databaseWasChanged = false;
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.connectionString);
			
			//connection.setAutoCommit(false); // If running multiple actions in a transaction
			
			databaseWasChanged = storeFileGroupInformation(connection, fileImportedInfo, existingFileImportedInfo);
			
			//connection.commit(); // If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogExceptionMessage(e, "addSourcePathInfo failed");
			System.exit(Photons.errorCodeFailedToAddSourcePathInformationToDatabase);
		}
		
		return databaseWasChanged;
	}
	
	public Boolean verifySourcePathInfo(FileToImportInfo fileToImportInfo, FileImportedInfo existingFileImportedInfo) {
		Boolean groupExistsInDatabase = false;
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.connectionString);
			
			//connection.setAutoCommit(false); // If running multiple actions in a transaction
			
			String groupName = fileToImportInfo.getFilePath();
			
			long groupId = getFileGroupId(connection, groupName);

			groupExistsInDatabase = (groupId != DatabaseUtil.idNotSetValue);
			
			//connection.commit(); // If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogExceptionMessage(e, "addSourcePathInfo failed");
			System.exit(Photons.errorCodeFailedToAddSourcePathInformationToDatabase);
		}
		
		return groupExistsInDatabase;
	}
	
	/**
	 * Gets an already imported file info with the specified hash and length.
	 * @param originalFileContentHash
	 * @param originalFileLength
	 * @return An already imported file info with the specified hash and length, or null if not found
	 */
	public FileImportedInfo getFileImportedInfo(
			String originalFileContentHash,
			long originalFileLength) {
		
		DatabaseUtil.CheckSQLite();
		Connection connection;
		FileImportedInfo fileImportedInfo = null;
		try {
			connection = DriverManager.getConnection(this.connectionString);

			PreparedStatement preparedQueryStatement = connection.prepareStatement(selectFileInfoCommandSql);
			
			preparedQueryStatement.setString(1, originalFileContentHash);
			preparedQueryStatement.setLong(2, originalFileLength);
			
			ResultSet resultSet = preparedQueryStatement.executeQuery();
			
			boolean fileImportedInfoWasAlreadyRetrieved = false;
		    while ( resultSet.next() ) {
		    	if (fileImportedInfoWasAlreadyRetrieved) {
		    		MyLogger.displayAndLogErrorMessage("Duplicate imported file found. [Hash=%s] [Length=%d] [File=%s]", originalFileContentHash, originalFileLength, fileImportedInfo.getOriginalFileNameWithPath());
					System.exit(Photons.errorCodeDuplicateImportedFile);
		    	}
		    	
		    	fileImportedInfo = FileImportedInfo.getFileImportedInfoFromDatabase(resultSet);
		    	fileImportedInfoWasAlreadyRetrieved = true;
		    }
		    
		    resultSet.close();

		    preparedQueryStatement.close();
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogExceptionMessage(e, "getFileImportedInfo failed");
			System.exit(Photons.errorCodeFailedToGetFileInformationFromDatabase);
		}
		
		return fileImportedInfo;
	}
}
