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
 * TODO: this class is not tested yet
 */
public class FileImportedInfo {
	
	private String originalFileNameWithPath;
	private String originalFilePath;
	
	private long length;
	private String hash;
	
	private Date fileLastModificationTime;
	
	private String subFolder;
	private String fileName;
	
	private Boolean importEnabled;
	
	private long type;
	private String description;
	
	private long id;
	private Date recordLastModificationTime;
	private boolean deleted;

	public FileImportedInfo(FileToImportInfo originalFileInfo, long fileTypeId) {
		this.id = DatabaseUtil.idNotSetValue;
		
		this.originalFileNameWithPath = originalFileInfo.getFileNameWithPath();
		this.originalFilePath = originalFileInfo.getFilePath();

		this.length = originalFileInfo.getLength();
		this.hash = originalFileInfo.getHash();
		this.fileLastModificationTime = originalFileInfo.getLastModificationTime();
		this.subFolder = FileUtil.subfolderDateFormatter.format(this.fileLastModificationTime);
		this.fileName = originalFileInfo.getFileName();
		
		this.importEnabled = true;
		
		this.type = fileTypeId;
		
		this.description = "";
		
		this.deleted = false;
	}
	
	private FileImportedInfo() {
		
	}
	
	public static FileImportedInfo getFileImportedInfoFromDatabase(ResultSet resultSet) {
		FileImportedInfo fileImportedInfo = new FileImportedInfo();
		try {
			fileImportedInfo.setId(resultSet.getLong("id"));
			fileImportedInfo.setOriginalFileNameWithPath(resultSet.getString("originalFileNameWithPath"));
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
			MyLogger.displayAndLogExceptionMessage(e, "getFileImportedInfoFromDatabase failed");
			return null;
		}
		
		return fileImportedInfo;
	}

	public Path getCurrentRelativePathWithFileName() {
		return Paths.get(this.subFolder,  this.fileName);
	}

	/**
	 * Gets the file's original full filename with path
	 * @return The file's original full filename with path
	 */
	public String getOriginalFileNameWithPath() {
		return this.originalFileNameWithPath;
	}

	public String getOriginalFilePath() {
		return this.originalFilePath;
	}
	
	public void setOriginalFileNameWithPath(String originalFileName) {
		this.originalFileNameWithPath = originalFileName;
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
		return this.fileLastModificationTime;
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

	public long getType() {
		return this.type;
	}

	private void setType(long type) {
		this.type = type;
	}

	public String getFileName() {
		return this.fileName;
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
}
