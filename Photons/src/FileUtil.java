import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;


public class FileUtil {

	public static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");  
	
	// Source: http://techiejoms.blogspot.hu/2013/06/getting-checksum-or-hash-value-of-file.html
	public static String getChecksum(String filename, String algo) throws Exception {  
	      byte[] b = createChecksum(filename, algo);  
	      String result = "";  
	      for (int i=0; i < b.length; i++) {  
	           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );  
	      }  
	      return result;  
	}
	
	// Source: http://techiejoms.blogspot.hu/2013/06/getting-checksum-or-hash-value-of-file.html
	private static byte[] createChecksum(String filename, String algo) throws Exception{  
	      InputStream fis = new FileInputStream(filename);  
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
