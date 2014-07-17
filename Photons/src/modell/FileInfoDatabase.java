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

	private static final String versionString = "1.1";
	private static final String configCreateCommandSql = "CREATE TABLE config " +
			"(id							INTEGER		PRIMARY KEY, " +
			"key							TEXT		NOT NULL, " +
			"value							TEXT		NOT NULL, " +
			"recordLastModificationTime		INTEGER		NOT NULL, " +
			"deleted						INTEGER		DEFAULT 0)";
	private static final String configVersionInsertCommandSql = "INSERT INTO config (key, value, recordLastModificationTime) " +
			"VALUES ('version', '" + versionString + "', %d)";
	private static final String configVersionSelectCommandSql = "SELECT value, recordLastModificationTime " +
			"FROM config WHERE key = 'version' ORDER BY value ASC";

	private static final String fileTableCreationCommandSql = "CREATE TABLE fileinfo " +
			"(id							INTEGER	PRIMARY KEY, " +
			"originalPath					TEXT	NOT NULL, " +
			"originalFileName				TEXT	NOT NULL, " +
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
	private static final String fileInfoInsertCommandSql = "INSERT INTO fileinfo " +
			"(originalPath, originalFileName, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String selectFileInfoCommandSql = "SELECT " +
			"id, originalPath, originalFileName, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime, deleted " +
			"FROM fileinfo WHERE originalHash=? AND originalLength=?";
	
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

				if (version.equals("1.0")) {
					throw new Exception(String.format("Unsupported outdated database version: [%s].", version));
				}
				else if (version.equals(versionString)) {
					// OK
					MyLogger.displayAndLogActionMessage("Database already exists with expected version: [%s].", version);
				} else {
					throw new Exception(String.format("Unsupported database version: [%s].", version));
				}
			} catch ( Exception e ) {
				MyLogger.displayException(e);
	
				Statement updateStatement = connection.createStatement();
				updateStatement.executeUpdate(configCreateCommandSql);
				updateStatement.executeUpdate(String.format(configVersionInsertCommandSql, DatabaseUtil.getLongTimeStampCurrent()));
				updateStatement.executeUpdate(fileTableCreationCommandSql);
			      
				updateStatement.close();
	
				MyLogger.displayAndLogActionMessage("Created database successfully");
			}
		    connection.close();
	    } catch ( Exception e ) {
	    	MyLogger.displayAndLogException(e);
	    	System.exit(0);
	    }
	}

	public void saveFileImportedInfo(String importFolderPath, FileImportedInfo fileInfo) {
		DatabaseUtil.CheckSQLite();
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.connectionString);
			
			//connection.setAutoCommit(false); If running multiple actions in a transaction
			
			PreparedStatement preparedInsertStatement = connection.prepareStatement(fileInfoInsertCommandSql);
			
			preparedInsertStatement.setString(1, fileInfo.getOriginalPath());
			preparedInsertStatement.setString(2, fileInfo.getOriginalFileName());
			preparedInsertStatement.setLong(3, fileInfo.getOriginalLength());
			preparedInsertStatement.setString(4, fileInfo.getOriginalHash());
			preparedInsertStatement.setLong(5, fileInfo.getOriginalLastModificationTime().getTime());
			preparedInsertStatement.setString(6, fileInfo.getSubfolder());
			preparedInsertStatement.setString(7, fileInfo.getFileName());
			preparedInsertStatement.setString(8, DatabaseUtil.getStringFromBoolValue(fileInfo.getImportEnabled()));
			preparedInsertStatement.setInt(9, fileInfo.getType());
			preparedInsertStatement.setString(10, fileInfo.getDescription());
			preparedInsertStatement.setLong(11, new Date().getTime());
			
			preparedInsertStatement.executeUpdate();
			
			preparedInsertStatement.close();
			
			//connection.commit(); If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			MyLogger.displayAndLogException(e);
			System.exit(0);
		}
	}
	
	public FileImportedInfo getFileImportedInfo(
			String importFolderPath,
			String originalFileContentHash,
			long originalLength) {
		DatabaseUtil.CheckSQLite();
		Connection connection;
		FileImportedInfo fileImportedInfo = null;
		try {
			connection = DriverManager.getConnection(this.connectionString);

			PreparedStatement preparedQueryStatement = connection.prepareStatement(selectFileInfoCommandSql);
			
			preparedQueryStatement.setString(1, originalFileContentHash);
			preparedQueryStatement.setLong(2, originalLength);
			
			ResultSet resultSet = preparedQueryStatement.executeQuery();
			
			boolean fileImportedInfoWasAlreadyRetrieved = false;
		    while ( resultSet.next() ) {
		    	if (fileImportedInfoWasAlreadyRetrieved) {
		    		// TODO: duplicate element found (same hash and length), what to do now????
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
