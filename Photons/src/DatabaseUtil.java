import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.SQLException;
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
	
	private static final String versionInfoInsertCommand1 = "INSERT INTO config (key, value, recordLastModificationTime) " +
			"VALUES ('version', '" + versionString + "', ";
	private static final String versionInfoInsertCommand2 = ")";
	
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
	
	public static void openOrCreateDatabase(String folderPath) {
	    try {
	      Class.forName("org.sqlite.JDBC");
	      String connectionString = "jdbc:sqlite:" + Paths.get(folderPath, databaseFileName).toString();
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
				  System.out.println("Database already exists with expected version: [" + version + "].");
			  } else {
				  throw new Exception("Unsupported database version: [" + version + "].");
			  }
		  } catch ( Exception e ) {
			  System.out.println(e.getClass().getName() + ": " + e.getMessage() + ". Maybe the database did not exist.");

			  Statement updateStatement = connection.createStatement();
			  updateStatement.executeUpdate(configTableCreationCommand);
			  updateStatement.executeUpdate(versionInfoInsertCommand1 + new Date().getTime() + versionInfoInsertCommand2);
			  updateStatement.executeUpdate(fileTableCreationCommand);
		      
			  updateStatement.close();

			  System.out.println("Created database successfully");
		  }
	      
	      connection.close();
	      
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
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
	    
		String connectionString = "jdbc:sqlite:" + Paths.get(importFolderPath, databaseFileName).toString();
		Connection connection;
		try {
			connection = DriverManager.getConnection(connectionString);
			
			String insertCommand = "INSERT INTO fileinfo " +
					"(originalPath, originalFileName, originalLength, originalHash, originalLastModificationTime, " +
					"subFolder, fileName, importEnabled, type, description, recordLastModificationTime) " +
					"VALUES (" +
					"'" +	fileInfo.getOriginalPath() + "', " +
					"'" +	fileInfo.getOriginalFileName() + "', " +
							fileInfo.getOriginalLength() + ", " +
					"'" +	fileInfo.getOriginalHash() + "', " +
							fileInfo.getOriginalLastModificationTime().getTime() + ", " +
					"'" +	fileInfo.getSubfolder() + "', " +
					"'" +	fileInfo.getFileName() + "', " +
							(fileInfo.getImportEnabled() ? "1" : "0") + ", " +
					"'" +	fileInfo.getType() + "', " +
					"'" +	fileInfo.getDescription() + "', " +
							new Date().getTime() +
					")";
			
			  Statement insertStatement = connection.createStatement();
			  insertStatement.executeUpdate(insertCommand);
			  insertStatement.close();
			
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	private static void runSqlCommand(Connection connection, String command) throws SQLException {
//		  Statement stmt = connection.createStatement();
//	      stmt.executeUpdate(command);
//	      stmt.close();
//	}
}
