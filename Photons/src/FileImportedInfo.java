import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


public class FileImportedInfo {
	private String originalPath;
	private String originalFileName;
	
	private long originalLength;
	private String originalHash;
	private Date originalLastModificationTime;
	
	private String subFolder;
	private String fileName;
	
	private Boolean importEnabled;
	
	private String type;
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
		this.originalLastModificationTime = originalFileInfo.getLastModificationTime();
		
		this.subFolder = FileUtil.subfolderDateFormatter.format(this.originalLastModificationTime);
		this.fileName = originalFileInfo.getFileName();
		
		this.importEnabled = false;
		
		if (this.originalFileName.toLowerCase().endsWith(".jpg")) {
			this.type = "jpg";
		}
		else {
			this.type = "?";
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
			fileImportedInfo.setType(resultSet.getString("type"));
			fileImportedInfo.setDescription(resultSet.getString("description"));
			fileImportedInfo.setRecordLastModificationTime(new Date(resultSet.getLong("recordLastModificationTime")));
			fileImportedInfo.setDeleted(DatabaseUtil.getBooleanFromStringValue(resultSet.getString("deleted")));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return originalLastModificationTime;
	}

	private void setOriginalLastModificationTime(Date originalLastModificationTime) {
		this.originalLastModificationTime = originalLastModificationTime;
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

	public String getType() {
		return type;
	}

	private void setType(String type) {
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
}
