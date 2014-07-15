package common;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;


/**
 * A common class for general file operations
 * @author emil
 *
 */
public class FileUtil {

	//public static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyyMMdd");
	
	/**
	 * This format will be used to generate subfolders based on Date objects
	 */
	public static SimpleDateFormat subfolderDateFormatter = new SimpleDateFormat("yyyy/MM/dd");  

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
	 * If the target file already exists, generates an alternate filename with a sequence number
	 * appended at the end of the filename.
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
			alternateFilePath = Paths.get(originalPath, String.format("%s (%d)%s", fileNameWithoutExtension, i, extension));
			i++;
		}
		
		return alternateFilePath;
	}
	
	/**
	 * Appends text to the file. Does not add new line!
	 * @param fileNameWithFullPath	The file to open for write/append
	 * @param text					The text to write into the text file
	 * @param append				True if the text should be appended to the file, false if overwrite requested
	 * TODO: this method is not tested yet
	 */
	public static void writeToFile(String fileNameWithFullPath, String text, boolean append) {
		BufferedWriter writer = null;
        try {
            File textFile = new File(fileNameWithFullPath);
            writer = new BufferedWriter(new FileWriter(textFile, append));
            writer.write(text);
        } catch (Exception e) {
    		MyLogger.displayException(e);
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
        		MyLogger.displayException(e);
            }
        }
    }
	
	/**
	 * TODO: document
	 * @param fileName
	 * @param algo
	 * @return
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
	 * TODO: document
	 * @param fileName
	 * @param algo
	 * @return
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
