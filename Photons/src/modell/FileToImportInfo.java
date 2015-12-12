package modell;

import java.io.File;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import common.FileUtil;
import common.MyLogger;

/**
 * Represents the important information about a file to be imported.
 * @author emil
 * TODO: this class is not tested yet
 */
public class FileToImportInfo {
	
	private String fileName;
	private String filePath;
	private String fileNameWithPath;
	
	private long length;
	private String hash;
	private Date lastModificationTime;
	
	/**
	 * Class constructor from Path object of the file.
	 * @param path	The Path object of the file which will be imported 
	 * @throws Exception
	 */
	public FileToImportInfo(Path path) throws Exception {
		
		Path realPath = path.toRealPath(LinkOption.NOFOLLOW_LINKS);
		this.filePath = realPath.getParent().toString();
		setFileData(realPath);
		
		MyLogger.displayAndLogDebugMessage("Calculated info for [%s]", realPath);
	}
	
	/**
	 * Class constructor from the path and name of the file
	 * @param path		The String containing the path to the file (can be relative or absolute)
	 * @param fileName	The String containing the name of the file
	 * @throws Exception
	 */
	public FileToImportInfo(String path, String fileName) throws Exception {
		Path realPath = Paths.get(path, fileName).toRealPath(LinkOption.NOFOLLOW_LINKS);
		setFileData(realPath);
	}
	
	/**
	 * Sets length, last modification time and hash of the file
	 * @param realPath		The real full path with filename.
	 * @throws Exception
	 */
	private void setFileData(Path realPath) throws Exception {
		this.fileNameWithPath = realPath.toString();
		this.fileName = realPath.getFileName().toString();

		File thisFile = new File(this.fileNameWithPath);
		
		this.length = thisFile.length();
		this.lastModificationTime = new Date(thisFile.lastModified());
		this.hash = FileUtil.getFileContentHash(this.fileNameWithPath);
	}

	/**
	 * Gets the name of the file.
	 * @return	The String containing the name of the file.
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Gets the name of the file with full path
	 * @return	The String containing the name of the file with full path
	 */
	public String getFileNameWithPath() {
		return this.fileNameWithPath;
	}

	/**
	 * Gets the path of the file
	 * @return The String containing the full path of the file
	 */
	public String getFilePath() {
		return this.filePath;
	}
	
	/**
	 * Gets the length of the file in bytes.
	 * @return	The length of the file in bytes
	 */
	public long getLength() {
		return this.length;
	}

	/**
	 * Gets the hash string of the file. Currently SHA-256.
	 * @return	The String containing the hash calculated from the content of the file. (SHA-256)
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * Gets the last modification time of the file
	 * @return	Date of the last modification time of the file.
	 */
	public Date getLastModificationTime() {
		return this.lastModificationTime;
	}
}
