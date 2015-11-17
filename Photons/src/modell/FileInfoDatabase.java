package modell;

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


public class FileInfoDatabase {

	private static final String defaultDatabaseFileName = "fileInfo.sqlite";

	private static final String versionString = "2.1";
	
	private static final String configTableName = "config";
	private static final String configCreateCommandSql = "CREATE TABLE " + configTableName + " " +
			"(id							INTEGER		PRIMARY KEY, " +
			"key							TEXT		NOT NULL, " +
			"value							TEXT		NOT NULL, " +
			"recordLastModificationTime		INTEGER		NOT NULL, " +
			"deleted						INTEGER		DEFAULT 0)";
	private static final String configVersionInsertCommandSql = "INSERT INTO " + configTableName + " (key, value, recordLastModificationTime) " +
			"VALUES ('version', '" + versionString + "', %d)";
	private static final String configVersionSelectCommandSql = "SELECT value, recordLastModificationTime " +
			"FROM " + configTableName + " WHERE key = 'version' ORDER BY value ASC";

	private static final String fileInfoTableName = "fileinfo";
	private static final String fileInfoTableCreationCommandSql = "CREATE TABLE " + fileInfoTableName + " " +
			"(id							INTEGER	PRIMARY KEY, " +
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
			"id, originalFileNameWithPath, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime, deleted " +
			"FROM " + fileInfoTableName + " WHERE originalHash=? AND originalLength=?";

	private static final String fileGroupTableName = "filegroup";
	private static final String fileGroupTableCreationCommandSql = "CREATE TABLE " + fileGroupTableName + " " +
			"(id							INTEGER	PRIMARY KEY, " +
			"description					TEXT	NOT NULL, " +
			"recordLastModificationTime		INTEGER	NOT NULL, " +
			"deleted						INTEGER	DEFAULT 0)";
	private static final String fileGroupInsertCommandSql = "INSERT INTO " + fileGroupTableName + " " +
			"(description, recordLastModificationTime) " +
			"VALUES (?, ?)";
	private static final String selectFileGroupCommandSql = "SELECT " +
			"id, description, recordLastModificationTime, deleted " +
			"FROM " + fileGroupTableName + " WHERE description=?";
	
	private Path databaseFolder;
	private Path databaseFilePath;
	private String connectionString;


	public FileInfoDatabase(Path databaseFolder) {
		setPrivateData(databaseFolder, defaultDatabaseFileName);
	}

	public FileInfoDatabase(Path databaseFolder, String fileName) {
		setPrivateData(databaseFolder, fileName);
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
				MyLogger.displayAndLogActionMessage("Path does not exist. Creating folder [%s]...", this.databaseFolder);
	    		Files.createDirectories(this.databaseFolder);
	    	}
	    	
		    Connection connection = DriverManager.getConnection(this.connectionString);
		      
			String  version = "";
			try {
				Statement queryStatement = connection.createStatement();
				ResultSet resultSet = queryStatement.executeQuery(configVersionSelectCommandSql);
				while ( resultSet.next() ) {
					version = resultSet.getString("value");
				}
				resultSet.close();
				queryStatement.close();

				if (version.equals(versionString)) {
					// OK
					MyLogger.displayAndLogActionMessage("Database already exists with expected version: [version=%s] at [databaseFolder=%s].", version, this.databaseFolder);
				} else {
					MyLogger.displayAndLogActionMessage(String.format("Unsupported database version: [version=%s] at [databaseFolder=%s].", version, this.databaseFolder));
					System.exit(4);
				}
			} catch ( Exception e ) {
				MyLogger.displayException(e);
	
				Statement updateStatement = connection.createStatement();
				updateStatement.executeUpdate(configCreateCommandSql);
				updateStatement.executeUpdate(String.format(configVersionInsertCommandSql, DatabaseUtil.getLongTimeStampCurrent()));
				updateStatement.executeUpdate(fileInfoTableCreationCommandSql);
				updateStatement.executeUpdate(fileGroupTableCreationCommandSql);
			      
				updateStatement.close();
	
				MyLogger.displayAndLogActionMessage("Created database successfully");
			}
			
		    connection.close();
	    } catch ( Exception e ) {
	    	MyLogger.displayAndLogException(e);
	    	System.exit(5);
	    }
	}

	/**
	 * Inserts a new file record into the database
	 * @param importTargetPath The target folder where the file was copied 
	 * @param fileInfo The object containing data to be inserted into the database
	 */
	public void addFileImportedInfo(String importTargetPath, FileImportedInfo fileInfo) {
		DatabaseUtil.CheckSQLite();
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.connectionString);
			
			connection.setAutoCommit(false); // If running multiple actions in a transaction
			
			PreparedStatement preparedInsertFileInfoStatement = connection.prepareStatement(fileInfoInsertCommandSql);
			
			preparedInsertFileInfoStatement.setString(1, fileInfo.getOriginalFileNameWithPath());
			preparedInsertFileInfoStatement.setLong(2, fileInfo.getLength());
			preparedInsertFileInfoStatement.setString(3, fileInfo.getHash());
			preparedInsertFileInfoStatement.setLong(4, fileInfo.getLastModificationTime().getTime());
			preparedInsertFileInfoStatement.setString(5, fileInfo.getSubfolder());
			preparedInsertFileInfoStatement.setString(6, fileInfo.getFileName());
			preparedInsertFileInfoStatement.setString(7, DatabaseUtil.getStringFromBoolValue(fileInfo.getImportEnabled()));
			preparedInsertFileInfoStatement.setInt(8, fileInfo.getType());
			preparedInsertFileInfoStatement.setString(9, fileInfo.getDescription());
			preparedInsertFileInfoStatement.setLong(10, new Date().getTime());
			
			if (preparedInsertFileInfoStatement.executeUpdate() != 1) {
				// TODO: error handling: failed to insert record
			}
			
			preparedInsertFileInfoStatement.close();

			// TODO: note that fileInfo has no ID yet - it should be queried after insertion
			storeFileGroupInformation(connection, fileInfo);
			
			connection.commit(); // If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogException(e);
			System.exit(0); // TODO: use predefinded exit codes
		}
	}
	
	private void storeFileGroupInformation(Connection connection, FileImportedInfo fileInfo) throws SQLException {
		// TODO: implement
		
		// TODO: File group information storage could be moved to a separate method
		// Steps:
		// 1. Check if group already exists; If not, insert new.
		// 2. Insert association of file with group.
		PreparedStatement preparedInsertFileGroupStatement = connection.prepareStatement(fileGroupInsertCommandSql);
		
		preparedInsertFileGroupStatement.setString(1, fileInfo.getFilePath());
		preparedInsertFileGroupStatement.setLong(2, new Date().getTime());
		
		preparedInsertFileGroupStatement.executeUpdate();
		
		preparedInsertFileGroupStatement.close();
	}
	
	public void addSourcePathInfo(FileToImportInfo fileToImportInfo, FileToImportInfo existingFileToImportInfo) {
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.connectionString);
			
			connection.setAutoCommit(false); // If running multiple actions in a transaction
			
			// TODO: implement
			storeFileGroupInformation(connection, );
			
			connection.commit(); // If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogException(e);
			System.exit(0); // TODO: use predefinded exit codes
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
		    		MyLogger.displayAndLogActionMessage("ERROR: Duplicate imported file found. [Hash=%s] [Length=%d] [File=%s]", originalFileContentHash, originalFileLength, fileImportedInfo.getOriginalFileNameWithPath());
		    		// TODO: duplicate element found (same hash and length), what to do now???? Exit?
		    	}
		    	
		    	fileImportedInfo = FileImportedInfo.getFileImportedInfoFromDatabase(resultSet);
		    	fileImportedInfoWasAlreadyRetrieved = true;
		    }
		    
		    resultSet.close();

		    preparedQueryStatement.close();
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogException(e);
			System.exit(0);
		}
		
		return fileImportedInfo;
	}
	
	
}
