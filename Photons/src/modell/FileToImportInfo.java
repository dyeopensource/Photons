package modell;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import common.FileUtil;

/**
 * Represents the important information about a file to be imported.
 * @author emil
 *
 */
public class FileToImportInfo {
	
	private String path;
	private String fileName;
	
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
		this.path = path.getParent().toString();
		this.fileName = path.getFileName().toString();
		this.fileNameWithPath = path.toString();
		setFileData();
	}
	
	/**
	 * Class constructor from the path and name of the file
	 * @param path		The String containing the path to the file
	 * @param fileName	The String containing the name of the file
	 * @throws Exception
	 */
	public FileToImportInfo(String path, String fileName) throws Exception {
		this.path = path;
		this.fileName = fileName;
		this.fileNameWithPath = Paths.get(this.path, this.fileName).toString();
		setFileData();
	}
	
	/**
	 * Sets length, last modification time and hash of the file
	 * @throws Exception
	 */
	private void setFileData() throws Exception {
		File thisFile = new File(this.fileNameWithPath);
		this.length = thisFile.length();
		this.lastModificationTime = new Date(thisFile.lastModified());
		this.hash = FileUtil.getFileContentHash(this.fileNameWithPath);
	}

	/**
	 * Gets the path to the file.
	 * @return	The String containing the path to the file.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets the name of the file.
	 * @return	The String containing the name of the file.
	 */
	public String getFileName() {
		return fileName;
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
		return hash;
	}

	/**
	 * Gets the last modification time of the file
	 * @return	Date of the last modification time of the file.
	 */
	public Date getLastModificationTime() {
		return lastModificationTime;
	}
}
