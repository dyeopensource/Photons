package modell;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;



import common.DatabaseUtil;
import common.FileUtil;
import common.MyLogger;

/**
 * Information about an imported file
 * @author emil
 *
 */
public class FileImportedInfo {
	private String originalPath;
	private String originalFileName;
	
	private long length;
	private String hash;
	
	private Date fileLastModificationTime;
	private Date mediaContentTimeStamp;
	private Date userTimeStamp;
	
	private String subFolder;
	private String fileName;
	
	private Boolean importEnabled;
	
	private int type;
	private String description;
	
	private long id;
	private Date recordLastModificationTime;
	private boolean deleted;

	public FileImportedInfo(FileToImportInfo originalFileInfo) {
		this.id = 0;
		
		// TODO: next 2 fields should be moved to another table where we collect all the paths for a file (if found several times)
		// This is useful for "event" information
		this.originalPath = originalFileInfo.getPath();
		this.originalFileName = originalFileInfo.getFileName();

		this.length = originalFileInfo.getLength();
		this.hash = originalFileInfo.getHash();
		this.fileLastModificationTime = originalFileInfo.getLastModificationTime();
		this.subFolder = FileUtil.subfolderDateFormatter.format(this.fileLastModificationTime);
		this.fileName = originalFileInfo.getFileName();
		
		this.importEnabled = false;
		
		if (this.originalFileName.toLowerCase().endsWith(".jpg")) {
			this.type = 1;
		}
		else {
			this.type = 0;
		}
		
		this.description = "";
		
		this.deleted = false;
	}
	
	private FileImportedInfo() {
		
	}
	
	public static FileImportedInfo getFileImportedInfoFromDatabase(ResultSet resultSet) {
		FileImportedInfo fileImportedInfo = new FileImportedInfo();
		try {
			fileImportedInfo.setId(resultSet.getLong("id"));
			fileImportedInfo.setOriginalPath(resultSet.getString("originalPath"));
			fileImportedInfo.setOriginalFileName(resultSet.getString("originalFileName"));
			fileImportedInfo.setLength(resultSet.getLong("originalLength"));
			fileImportedInfo.setHash(resultSet.getString("originalHash"));
			fileImportedInfo.setLastModificationTime(new Date(resultSet.getLong("originalLastModificationTime")));
			fileImportedInfo.setSubfolder(resultSet.getString("subFolder"));
			fileImportedInfo.setFileName(resultSet.getString("fileName"));
			fileImportedInfo.setImportEnabled(DatabaseUtil.getBooleanFromStringValue(resultSet.getString("importEnabled")));
			fileImportedInfo.setType(resultSet.getInt("type"));
			fileImportedInfo.setDescription(resultSet.getString("description"));
			fileImportedInfo.setRecordLastModificationTime(new Date(resultSet.getLong("recordLastModificationTime")));
			fileImportedInfo.setDeleted(DatabaseUtil.getBooleanFromStringValue(resultSet.getString("deleted")));
			
		} catch (SQLException e) {
			MyLogger.displayException(e);
			return null;
		}
		
		return fileImportedInfo;
	}

	public Path getCurrentRelativePathWithFileName() {
		return Paths.get(this.subFolder,  this.fileName);
	}
	
	public String getOriginalPath() {
		return this.originalPath;
	}

	private void setOriginalPath(String originalPath) {
		this.originalPath = originalPath;
	}

	public String getOriginalFileName() {
		return this.originalFileName;
	}

	private void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	/**
	 * Gets the file's original full filename with path
	 * @return The file's original full filename with path
	 */
	public String getOriginalFileNameWithPath() {
		return Paths.get(this.originalPath,  this.originalFileName).toString();
	}
	
	public long getLength() {
		return this.length;
	}
	
	private void setLength(long length) {
		this.length = length;
	}

	public String getHash() {
		return this.hash;
	}

	private void setHash(String hash) {
		this.hash = hash;
	}

	public Date getLastModificationTime() {
		return fileLastModificationTime;
	}

	private void setLastModificationTime(Date lastModificationTime) {
		this.fileLastModificationTime = lastModificationTime;
	}

	public String getSubfolder() {
		return this.subFolder;
	}

	public void setSubfolder(String subFolder) {
		this.subFolder = subFolder;
	}

	public Boolean getImportEnabled() {
		return this.importEnabled;
	}

	public void setImportEnabled(Boolean importEnabled) {
		this.importEnabled = importEnabled;
	}

	public int getType() {
		return this.type;
	}

	private void setType(int type) {
		this.type = type;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getId() {
		return this.id;
	}

	private void setId(long id) {
		this.id = id;
	}
	
	public Date getRecordLastModificationTime() {
		return recordLastModificationTime;
	}

	public void setRecordLastModificationTime(Date recordLastModificationTime) {
		this.recordLastModificationTime = recordLastModificationTime;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Date getMediaContentTimestamp() {
		return mediaContentTimeStamp;
	}
	
	public void setMediaContentTimestamp(Date mediaContentTimestamp) {
		this.mediaContentTimeStamp = mediaContentTimestamp;
	}

	public Date getUserTimestamp() {
		return this.userTimeStamp;
	}
	
	public void setUserTimestamp(Date userTimeStamp) {
		this.userTimeStamp = userTimeStamp;
	}
}
