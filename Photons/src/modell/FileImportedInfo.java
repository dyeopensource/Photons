package modell;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;



import common.DatabaseUtil;
import common.FileUtil;
import common.MyLogger;


public class FileImportedInfo {
	private String originalPath;
	private String originalFileName;
	
	private long originalLength;
	private String originalHash;
	
	// TODO: create field for storing time-zone information
	// TODO: use it: private MediaFileDate date;
	
	private Date originalFileLastModificationTime;
	private Date mediaContentTimestamp;
	private Date utcTimestamp;
	
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
		this.originalPath = originalFileInfo.getPath();
		this.originalFileName = originalFileInfo.getFileName();

		this.originalLength = originalFileInfo.getLength();
		this.originalHash = originalFileInfo.getHash();
		this.originalFileLastModificationTime = originalFileInfo.getLastModificationTime();
		this.subFolder = FileUtil.subfolderDateFormatter.format(this.originalFileLastModificationTime);
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
			fileImportedInfo.setOriginalLength(resultSet.getLong("originalLength"));
			fileImportedInfo.setOriginalHash(resultSet.getString("originalHash"));
			fileImportedInfo.setOriginalLastModificationTime(new Date(resultSet.getLong("originalLastModificationTime")));
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

	public String getOriginalPath() {
		return originalPath;
	}

	private void setOriginalPath(String originalPath) {
		this.originalPath = originalPath;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	private void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public long getOriginalLength() {
		return this.originalLength;
	}
	
	private void setOriginalLength(long originalLength) {
		this.originalLength = originalLength;
	}

	public String getOriginalHash() {
		return originalHash;
	}

	private void setOriginalHash(String originalHash) {
		this.originalHash = originalHash;
	}

	public Date getOriginalLastModificationTime() {
		return originalFileLastModificationTime;
	}

	private void setOriginalLastModificationTime(Date originalLastModificationTime) {
		this.originalFileLastModificationTime = originalLastModificationTime;
	}

	public String getSubfolder() {
		return subFolder;
	}

	public void setSubfolder(String subFolder) {
		this.subFolder = subFolder;
	}

	public Boolean getImportEnabled() {
		return importEnabled;
	}

	public void setImportEnabled(Boolean importEnabled) {
		this.importEnabled = importEnabled;
	}

	public int getType() {
		return type;
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
		return description;
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
		return mediaContentTimestamp;
	}
	

	public void setMediaContentTimestamp(Date mediaContentTimestamp) {
		this.mediaContentTimestamp = mediaContentTimestamp;
	}

	public Date getUtcTimestamp() {
		return utcTimestamp;
	}

	public void setUtcTimestamp(Date utcTimestamp) {
		this.utcTimestamp = utcTimestamp;
	}
}
