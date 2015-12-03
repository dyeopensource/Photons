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

	private static final String versionString = "2.3.1";
	
	private static final String fieldNameId = "id";
	
	private static final String configTableName = "config";
	private static final String configTableFieldNameValue = "value";
	private static final String configCreateCommandSql = "CREATE TABLE IF NOT EXISTS " + configTableName + " " +
			"(" + fieldNameId + "			INTEGER		PRIMARY KEY, " +
			"key							TEXT		NOT NULL, " +
			configTableFieldNameValue + "	TEXT		NOT NULL, " +
			"recordLastModificationTime		INTEGER		NOT NULL, " +
			"deleted						INTEGER		DEFAULT 0)";
	private static final String configVersionInsertCommandSql = "INSERT INTO " + configTableName + " (key, " + configTableFieldNameValue + ", recordLastModificationTime) " +
			"VALUES ('version', '" + versionString + "', %d)";
	private static final String configVersionSelectCommandSql = "SELECT " + configTableFieldNameValue + ", recordLastModificationTime " +
			"FROM " + configTableName + " WHERE key = 'version' ORDER BY value ASC";

	private static final String fileInfoTableName = "fileinfo";
	private static final String fileInfoTableCreationCommandSql = "CREATE TABLE IF NOT EXISTS " + fileInfoTableName + " " +
			"(" + fieldNameId + "			INTEGER	PRIMARY KEY, " +
			"originalFileNameWithPath		TEXT	NOT NULL, " +
			"originalLength					INTEGER	NOT NULL, " +
			"originalHash					TEXT	NOT NULL, " +
			"originalLastModificationTime	INTEGER	NOT NULL, " +
			"mediaContentTimeStamp			INTEGER	NOT NULL, " +
			"userTimeStamp					INTEGER	NOT NULL, " +
			"subFolder						TEXT	NOT NULL, " +
			"fileName						TEXT	NOT NULL, " +
			"importEnabled					INTEGER	NOT NULL, " +
			"type							INTEGER	NOT NULL, " +
			"description					TEXT	NOT NULL, " +
			"recordLastModificationTime		INTEGER	NOT NULL, " +
			"deleted						INTEGER	DEFAULT 0)";
	private static final String fileInfoInsertCommandSql = "INSERT INTO " + fileInfoTableName + " " +
			"(originalFileNameWithPath, originalLength, originalHash, originalLastModificationTime, " +
			"mediaContentTimeStamp, userTimeStamp, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	/**
	 * SELECT statement to get records from fileinfo table with the specified original hash and size values 
	 */
	private static final String selectFileInfoCommandSql = "SELECT " +
			fieldNameId + ", originalFileNameWithPath, originalLength, originalHash, originalLastModificationTime, " +
			"mediaContentTimeStamp, userTimeStamp, " +
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
			"FROM " + fileGroupTableName + " WHERE description=?";

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
	    	
		    Connection connection = DriverManager.getConnection(this.connectionString);
		      
			String  version = "";
			try {
				Statement queryStatement = connection.createStatement();
				ResultSet resultSet = queryStatement.executeQuery(configVersionSelectCommandSql);
				while ( resultSet.next() ) {
					version = resultSet.getString(configTableFieldNameValue);
				}
				
				resultSet.close();
				queryStatement.close();

				if (version.equals(versionString)) {
					// OK
					MyLogger.displayAndLogDebugMessage("Database already exists with expected version: [version=%s] at [databaseFolder=%s].", version, this.databaseFolder);
				} else {
					MyLogger.displayAndLogErrorMessage(String.format("Unsupported database version: [version=%s] at [databaseFolder=%s].", version, this.databaseFolder));
					System.exit(Photons.errorCodeUnsupportedDatabaseVersion);
				}
			} catch ( Exception e ) {
				MyLogger.displayAndLogExceptionMessage(e, "Version check failed");
	
				Statement updateStatement = connection.createStatement();
				updateStatement.executeUpdate(configCreateCommandSql);
				updateStatement.executeUpdate(String.format(configVersionInsertCommandSql, DatabaseUtil.getLongTimeStampCurrent()));
				updateStatement.executeUpdate(fileInfoTableCreationCommandSql);
				updateStatement.executeUpdate(fileGroupTableCreationCommandSql);
				updateStatement.executeUpdate(fileGroupAssignmentTableCreationCommandSql);
			      
				updateStatement.close();
	
				MyLogger.displayAndLogDebugMessage("Created database successfully");
			}
			
		    connection.close();
	    } catch ( Exception e ) {
	    	MyLogger.displayAndLogExceptionMessage(e, "openOrCreateDatabase failed");
	    	System.exit(5);
	    }
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
			preparedInsertFileInfoStatement.setLong(5, fileInfo.getMediaContentTimestamp().getTime());
			preparedInsertFileInfoStatement.setLong(6, fileInfo.getUserTimestamp().getTime());
			preparedInsertFileInfoStatement.setString(7, fileInfo.getSubfolder());
			preparedInsertFileInfoStatement.setString(8, fileInfo.getFileName());
			preparedInsertFileInfoStatement.setString(9, DatabaseUtil.getStringFromBoolValue(fileInfo.getImportEnabled()));
			preparedInsertFileInfoStatement.setInt(10, fileInfo.getType());
			preparedInsertFileInfoStatement.setString(11, fileInfo.getDescription());
			preparedInsertFileInfoStatement.setLong(12, new Date().getTime());
			
			if (preparedInsertFileInfoStatement.executeUpdate() != 1) {
				MyLogger.displayAndLogErrorMessage("Failed to insert file information [OriginalFileNameWithPath=%s]", fileInfo.getOriginalFileNameWithPath());
				System.exit(Photons.errorCodeFailedToInsertFileInfoInformationIntoDatabase);
			}
			
			preparedInsertFileInfoStatement.close();

			storeFileGroupInformation(connection, fileInfo, null);
			
			//connection.commit(); // If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogExceptionMessage(e, "addFileImportedInfo failed");
			System.exit(Photons.errorCodeFailedToInsertFileIntoDatabase);
		}
	}
	
	private void storeFileGroupInformation(Connection connection, FileImportedInfo fileInfo, FileImportedInfo existingFileImportedInfo) throws SQLException {
		
		String description = fileInfo.getFilePath();
		
		long groupId = getFileGroupId(connection, description);

		if (groupId == DatabaseUtil.idNotSetValue) {
			// File group does not exist - inserting new
			PreparedStatement preparedInsertFileGroupStatement = connection.prepareStatement(fileGroupInsertCommandSql);
			preparedInsertFileGroupStatement.setString(1, description);
			preparedInsertFileGroupStatement.setLong(2, new Date().getTime());
			if (preparedInsertFileGroupStatement.executeUpdate() != 1) {
				MyLogger.displayAndLogErrorMessage("Failed to insert filegroup information [description=%s]", description);
				System.exit(Photons.errorCodeFailedToInsertFileGroupInformationIntoDatabase);
			}
			
			preparedInsertFileGroupStatement.close();
			
			groupId = getFileGroupId(connection, description);
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
		}
	}
	
	private long getFileGroupId(Connection connection, String description) throws SQLException {
		
		long groupId = DatabaseUtil.idNotSetValue;
		
		PreparedStatement fileGroupQueryStatement = connection.prepareStatement(selectFileGroupCommandSql);
		
		fileGroupQueryStatement.setString(1, description);
		
		ResultSet fileGroupQueryResultSet = fileGroupQueryStatement.executeQuery();
		while ( fileGroupQueryResultSet.next() ) {
			groupId = fileGroupQueryResultSet.getInt(fieldNameId);
		}
		
		fileGroupQueryResultSet.close();

		fileGroupQueryStatement.close();
		
		return groupId;
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
	
	public void addSourcePathInfo(FileImportedInfo fileImportedInfo, FileImportedInfo existingFileImportedInfo) {
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.connectionString);
			
			//connection.setAutoCommit(false); // If running multiple actions in a transaction
			
			storeFileGroupInformation(connection, fileImportedInfo, existingFileImportedInfo);
			
			//connection.commit(); // If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogExceptionMessage(e, "addSourcePathInfo failed");
			System.exit(Photons.errorCodeFailedToAddSourcePathInformationToDatabase);
		}
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
