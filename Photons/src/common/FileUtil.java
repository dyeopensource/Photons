package common;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;


/**
 * A common class for general file operations
 * @author emil
 */
public class FileUtil {

	//public static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyyMMdd");
	
	/**
	 * This format will be used to generate subfolders based on Date objects
	 */
	public static SimpleDateFormat subfolderDateFormatter = new SimpleDateFormat("yyyy/MM/dd");  

	/**
	 * Checks if a folder exists or not
	 * @param path The path to the folder
	 * @return True if the folder exists, false if it does not exist or it the path points to a file, not to a folder
	 */
	public static Boolean folderExists(String path) {
		return (Files.exists(Paths.get(path)) && Files.isDirectory(Paths.get(path)));
	}

	/**
	 * Gets an SHA-256 hash string of the content of the file
	 * @param fileName		The name of the file to hash with full path
	 * @return				A string containing the hash string
	 * @throws Exception
	 */
	public static String getFileContentHash(String fileName) throws Exception {
		return getChecksum(fileName, "SHA-256");
	}

	/**
	 * If the target file already exists, generates an alternate filename with a sequence number appended at the end of the filename.
	 * @param originalTargetPath	The full path and filename of the original file
	 * @return						A filename with full path which does not exist, but is in the same folder as the original
	 */
	public static Path getAlternateFileName(Path originalTargetPath) {
		String originalPath = originalTargetPath.getParent().toString();
		String originalFileName = originalTargetPath.getFileName().toString();
		String fileNameWithoutExtension = originalFileName;
		String extension = "";
		int extensionSeparator = originalFileName.lastIndexOf('.');
		if (extensionSeparator > 0) {
			fileNameWithoutExtension = originalFileName.substring(0, extensionSeparator);
			extension = "." + originalFileName.substring(extensionSeparator + 1);
		}
		
		int i = 1;
		Path alternateFilePath = originalTargetPath;
		while (Files.exists(alternateFilePath)) {
			alternateFilePath = Paths.get(originalPath, String.format("%s(%d)%s", fileNameWithoutExtension, i, extension));
			i++;
		}
		
		return alternateFilePath;
	}
	
	/**
	 * Gets the checksum as string of the content of the file by the specified algorithm
	 * @param fileName The filename with path of the file for which the content checksum is calculated
	 * @param algo The name of the algorithm which is used for calculating the checksum
	 * @return The calculated checksum as hexadecimal string
	 * @throws Exception
	 * @link Source: http://techiejoms.blogspot.hu/2013/06/getting-checksum-or-hash-value-of-file.html
	 */
	private static String getChecksum(String fileName, String algo) throws Exception {  
	      byte[] b = createChecksum(fileName, algo);  
	      String result = "";  
	      for (int i=0; i < b.length; i++) {  
	           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );  
	      }  
	      return result;  
	}
	
	/**
	 * Gets the checksum of the content of the file by the specified algorithm
	 * @param fileName The filename with path of the file for which the content checksum is calculated 
	 * @param algo The name of the algorithm which is used for calculating the checksum
	 * @return The calculated checksum
	 * @throws Exception
	 * @link Source: http://techiejoms.blogspot.hu/2013/06/getting-checksum-or-hash-value-of-file.html
	 */
	private static byte[] createChecksum(String fileName, String algo) throws Exception{  
	      InputStream fis = new FileInputStream(fileName);  
	      byte[] buffer = new byte[1024];  
	      MessageDigest complete = MessageDigest.getInstance(algo); //One of the following "SHA-1", "SHA-256", "SHA-384", and "SHA-512"  
	      int numRead;  
	      do {  
	           numRead = fis.read(buffer);  
	           if (numRead > 0) {  
	                complete.update(buffer, 0, numRead);  
	           }  
	      } while (numRead != -1);  
	      fis.close();  
	      return complete.digest();  
	} 
}
