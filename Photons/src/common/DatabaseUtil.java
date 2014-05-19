package common;

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

import modell.FileImportedInfo;


public class DatabaseUtil {

	public static final String databaseFileName = "fileInfos.sqlite";

	private static final String versionString = "1.0";
	
	private static final String configTableCreationCommand = "CREATE TABLE config " +
			"(id							INTEGER		PRIMARY KEY, " +
			"key							TEXT		NOT NULL, " +
			"value							TEXT		NOT NULL, " +
			"recordLastModificationTime		INTEGER		NOT NULL, " +
			"deleted						INTEGER		DEFAULT 0)";
	
	private static final String versionInfoInsertCommand = "INSERT INTO config (key, value, recordLastModificationTime) " +
			"VALUES ('version', '" + versionString + "', %d)";
	
	private static final String versionInfoQueryCommand = "SELECT value FROM config WHERE key='version';";
	
	private static final String fileTableCreationCommand = "CREATE TABLE fileinfo " +
			"(id							INTEGER	PRIMARY KEY, " +
			"originalPath					TEXT	NOT NULL, " +
			"originalFileName				TEXT	NOT NULL, " +
			"originalLength					INTEGER	NOT NULL, " +
			"originalHash					TEXT	NOT NULL, " +
			"originalLastModificationTime	INTEGER	NOT NULL, " +
			"subFolder						TEXT	NOT NULL, " +
			"fileName						TEXT	NOT NULL, " +
			"importEnabled					INTEGER	NOT NULL, " +
			"type							TEXT	NOT NULL, " +
			"description					TEXT	NOT NULL, " +
			"recordLastModificationTime		INTEGER	NOT NULL, " +
			"deleted						INTEGER	DEFAULT 0)";
	
	private static final String fileInfoInsertCommand = "INSERT INTO fileinfo " +
			"(originalPath, originalFileName, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static final String selectFileInfoCommand = "SELECT " +
			"id, originalPath, originalFileName, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime, deleted " +
			"FROM fileinfo WHERE originalHash=? AND originalLength=?";
	
	public static void openOrCreateDatabase(String folderPathString) {
	    try {
	    	Path folderPath = Paths.get(folderPathString);
	    	if (!Files.exists(folderPath)) {
				MyLogger.displayAndLogActionMessage(String.format("Path does not exist. Creating folder [%s]...", folderPathString));
	    		Files.createDirectories(folderPath);
	    	}
	    	
	    	Class.forName("org.sqlite.JDBC");
		    String connectionString = String.format("jdbc:sqlite:%s", Paths.get(folderPathString, databaseFileName));
		    Connection connection = DriverManager.getConnection(connectionString);
		      
			String  version = "";
			try {
				Statement queryStatement = connection.createStatement();
				ResultSet resultSet = queryStatement.executeQuery(versionInfoQueryCommand);
				while ( resultSet.next() ) {
					version = resultSet.getString("value");
				}
				resultSet.close();
				queryStatement.close();
				  
				if (version.equals("1.0")) {
					// OK
					MyLogger.displayAndLogActionMessage(String.format("Database already exists with expected version: [%s].", version));
				} else {
					throw new Exception(String.format("Unsupported database version: [%s].", version));
				}
			} catch ( Exception e ) {
				MyLogger.displayActionMessage(String.format("%s: %s", e.getClass().getName(), e.getMessage()));
	
				Statement updateStatement = connection.createStatement();
				updateStatement.executeUpdate(configTableCreationCommand);
				updateStatement.executeUpdate(String.format(versionInfoInsertCommand, new Date().getTime()));
				updateStatement.executeUpdate(fileTableCreationCommand);
			      
				updateStatement.close();
	
				MyLogger.displayAndLogActionMessage("Created database successfully");
			}
		      
		    connection.close();
		      
	    } catch ( Exception e ) {
	    	System.err.println(String.format("%s: %s", e.getClass().getName(), e.getMessage()));
	    	System.exit(0);
	    }
	}
	
	public static void saveFileImportedInfo(String importFolderPath, FileImportedInfo fileInfo) {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	    
		String connectionString = String.format("jdbc:sqlite:%s", Paths.get(importFolderPath, databaseFileName));
		Connection connection;
		try {
			connection = DriverManager.getConnection(connectionString);
			
			//connection.setAutoCommit(false); If running multiple actions in a transaction
			
			PreparedStatement preparedInsertStatement = connection.prepareStatement(fileInfoInsertCommand);
			
			preparedInsertStatement.setString(1, fileInfo.getOriginalPath());
			preparedInsertStatement.setString(2, fileInfo.getOriginalFileName());
			preparedInsertStatement.setLong(3, fileInfo.getOriginalLength());
			preparedInsertStatement.setString(4, fileInfo.getOriginalHash());
			preparedInsertStatement.setLong(5, fileInfo.getOriginalLastModificationTime().getTime());
			preparedInsertStatement.setString(6, fileInfo.getSubfolder());
			preparedInsertStatement.setString(7, fileInfo.getFileName());
			preparedInsertStatement.setString(8, getStringFromBoolValue(fileInfo.getImportEnabled()));
			preparedInsertStatement.setString(9, fileInfo.getType());
			preparedInsertStatement.setString(10, fileInfo.getDescription());
			preparedInsertStatement.setLong(11, new Date().getTime());
			
			preparedInsertStatement.executeUpdate();
			
			preparedInsertStatement.close();
			
			//connection.commit(); If a transaction is necessary
			
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static FileImportedInfo getFileImportedInfo(
			String importFolderPath,
			String originalFileContentHash,
			long originalLength) {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	    
		String connectionString = String.format("jdbc:sqlite:%s", Paths.get(importFolderPath, databaseFileName));
		Connection connection;
		FileImportedInfo fileImportedInfo = null;
		try {
			connection = DriverManager.getConnection(connectionString);

			PreparedStatement preparedQueryStatement = connection.prepareStatement(selectFileInfoCommand);
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		return fileImportedInfo;
	}
	
	public static String getStringFromBoolValue(Boolean value) {
		return value ? "1" : "0";
	}
	
	public static Boolean getBooleanFromStringValue(String value) {
		return value.equals("1");
	}

	public static long getLongTimeStampCurrent() {
		return new Date().getTime();
	}
	
	public static void CheckSQLite() {
	    try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO: imnprove logging
			e.printStackTrace();
			System.exit(0);
		}
	}
}
