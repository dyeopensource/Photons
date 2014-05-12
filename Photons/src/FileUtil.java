import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;


public class FileUtil {

	//public static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyyMMdd");  
	public static SimpleDateFormat subfolderDateFormatter = new SimpleDateFormat("yyyy/MM/dd");  

	public static String getFileContentHash(String fileName) throws Exception {
		return getChecksum(fileName, "SHA-256");
	}

	public static Path GetAlternateFileName(Path originalTargetPath) {
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
	
	// Source: http://techiejoms.blogspot.hu/2013/06/getting-checksum-or-hash-value-of-file.html
	private static String getChecksum(String fileName, String algo) throws Exception {  
	      byte[] b = createChecksum(fileName, algo);  
	      String result = "";  
	      for (int i=0; i < b.length; i++) {  
	           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );  
	      }  
	      return result;  
	}
	
	// Source: http://techiejoms.blogspot.hu/2013/06/getting-checksum-or-hash-value-of-file.html
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
