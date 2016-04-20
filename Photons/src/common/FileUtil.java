package common;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import ui.Photons;


/**
 * A common class for general file operations
 * @author emil
 */
public class FileUtil {

	//public static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyyMMdd");
	
	private static final String sourceFolderUUIDFileName = "photons.uuid";
	private static final String photonsPathPrefix = "photons://";
	private static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	private static final Boolean isWindowsMachine = System.getProperty("os.name", "generic").toLowerCase().startsWith("windows"); 

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
	 * Gets a "path" (with slash as folder separator) with an UUID.
	 * The UUID is read from the photons.uuid file which is located in the device's root folder.
	 * If the file does not exist, then the method tries to create it and writes a random UUID into it.
	 * If the file does not exist and the method could not create it, the empty UUID is used.
	 * @param originalPath The path to generate the path for
	 * @return The String containing the UUID prefixed "path"
	 */
	public static String getPathWithUUID(Path originalPath) {
		
		// TODO: determine the path of the photons.uuid file: get path to device root
		
		UUID uuid = emptyUUID;
		
		Path absolutePath = Paths.get(originalPath.toString()).toAbsolutePath().normalize();
		String absolutePathString = absolutePath.toString();

		String nonRootPart = originalPath.toString(); // To be sure that it contains something meaningful
		if (isWindowsMachine) {
			Path root = absolutePath.getRoot();
			uuid = getUUIDFromPath(root); 
			nonRootPart = absolutePathString.substring(root.toString().length(), absolutePathString.length()).replace(File.separator, "/");
		} else {
			// TODO: determine device root path
		}
		
		return photonsPathPrefix + uuid.toString() + "/" + nonRootPart;
	}
	
	public static Path getResolvedPath(String pathToResolve) {
		
		String pathString = pathToResolve;

		// Removing final folder separator:
		if (pathToResolve.endsWith("\\") || pathToResolve.endsWith("/")) {
			pathString = pathToResolve.substring(0, pathToResolve.length() - 1);
		}

		Path resolvedPath = Paths.get(pathString);
		
		try {
			resolvedPath = resolvedPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
			
			// TODO: it would be nice to resolve soft and hard links to the original path
			
			// TODO: resolve original path if a drive letter was assigned to a folder on a Windows machine
		} catch (IOException e) {
			MyLogger.displayAndLogExceptionMessage(e, "Failed to get real path from [pathString=%s]", pathString);
		}
		
		return resolvedPath;
	}
	
	private static UUID getUUIDFromPath(Path uuidFileFolderPath) {
		
		UUID uuid = emptyUUID;

		Path uuidFilePath = Paths.get(uuidFileFolderPath.toString(), sourceFolderUUIDFileName); 
		if (!Files.exists(uuidFilePath)) {
			BufferedWriter writer = null;
			try {
			File sourceFolderUUIDFile = uuidFilePath.toFile();
			writer = new BufferedWriter(new FileWriter(sourceFolderUUIDFile));
			UUID newUUID = UUID.randomUUID();
            writer.write(newUUID.toString());
            //this.sourceFolderUUID = newUUID; // This will be read from the file
			} catch (Exception e) {
				MyLogger.displayAndLogExceptionMessage(e, "Failed to write new UUID to file [uuidFilePath=%s]", uuidFilePath);
	            e.printStackTrace();
	        } finally {
	            try {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) {
	            }
	        }
		}
		
		if (Files.exists(uuidFilePath)) {
			try {
				List<String> sourceFolderUUIDFileContent = Files.readAllLines(uuidFilePath);
				if (sourceFolderUUIDFileContent.size() > 0) {
					uuid = UUID.fromString(sourceFolderUUIDFileContent.get(0));
				}
			} catch (IOException e) {
				MyLogger.displayAndLogExceptionMessage(e, "Failed to read content from file [uuidFilePath=%s]", uuidFilePath);
				System.exit(Photons.errorCodeSourceFolderUUIDReadingError);
			}
		}
		
		return uuid;
	}
	
	/**
	 * 
	 * @param uuidFolderPath The folder where the photons.uuid file is supposed to be (usually the source folder)
	 * @param path
	 * @return A string containing a device based uniform path in style 'uuid://{UUID}/{path}'
	 */
	public static String getDeviceBasedUniformPath(String uuidFolderPath, String path) {
		UUID uuid = emptyUUID;
		
		// TODO: check if photons.uuid file exists at uuidFolderPath at the path
		// If yes: read content - if it is a UUID, take it
		// If not, (re)create photons.uuid with a new UUID as content
		// If creation fails, UUID is empty UUID
		// return photons://{UUID}/{relativePath}
		
		Path absolutePath = Paths.get(path).toAbsolutePath().normalize();
		Path root = absolutePath.getRoot();
		String absolutePathString = absolutePath.toString();
		String nonRootPart = absolutePathString.substring(root.toString().length(), absolutePathString.length()).replace(File.separator, "/");
		
		if (!isWindowsMachine) {
			// Resolve mount point etc.
		}
		
		return photonsPathPrefix + uuid.toString() + "/" + nonRootPart;
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
		
		int i = 2;
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
