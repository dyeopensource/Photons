import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;


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
			"VALUES ('%s', '%s', %d, '%s', %d, '%s', '%s', '%s', '%s', '%s', %d)";
	
	private static final String selectFileInfoCommand = "SELECT " +
			"id, originalPath, originalFileName, originalLength, originalHash, originalLastModificationTime, " +
			"subFolder, fileName, importEnabled, type, description, recordLastModificationTime, deleted " +
			"FROM fileinfo WHERE originalHash='%s' AND originalLength=%d";
	
	public static void openOrCreateDatabase(String folderPath) {
	    try {
	      Class.forName("org.sqlite.JDBC");
	      String connectionString = String.format("jdbc:sqlite:%s", Paths.get(folderPath, databaseFileName));
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
				  MyLogger.displayActionMessage(String.format("Database already exists with expected version: [%s].", version));
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

			  MyLogger.displayActionMessage("Created database successfully");
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
			
			String insertCommand = String.format(fileInfoInsertCommand,
					fileInfo.getOriginalPath(),
					fileInfo.getOriginalFileName(),
					fileInfo.getOriginalLength(),
					fileInfo.getOriginalHash(),
					fileInfo.getOriginalLastModificationTime().getTime(),
					fileInfo.getSubfolder(),
					fileInfo.getFileName(),
					getStringFromBoolValue(fileInfo.getImportEnabled()),
					fileInfo.getType(),
					fileInfo.getDescription(),
					new Date().getTime());
			
			  Statement insertStatement = connection.createStatement();
			  insertStatement.executeUpdate(insertCommand);
			  insertStatement.close();
			
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
			
			String queryCommand = String.format(selectFileInfoCommand,
					originalFileContentHash,
					originalLength);
			
			  Statement queryStatement = connection.createStatement();
			  ResultSet resultSet = queryStatement.executeQuery(queryCommand);
			  boolean fileImportedInfoWasAlreadyRetrieved = false;
		      while ( resultSet.next() ) {
		    	 if (fileImportedInfoWasAlreadyRetrieved) {
		    		 // TODO: duplicate element found (same hash and length), what to do now????
		    	 }
		    	 fileImportedInfo = FileImportedInfo.getFileImportedInfoFromDatabase(resultSet);
		    	 fileImportedInfoWasAlreadyRetrieved = true;
		      }
		      resultSet.close();
			  queryStatement.close();
			
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
}
