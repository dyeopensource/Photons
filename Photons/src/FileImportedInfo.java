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
	
	private FileType type;
	private String description;
	
	private Date recordLastModificationTime;

	public static enum FileType {
		unknown,
		jpg
	}

	public FileImportedInfo(FileToImportInfo originalFileInfo) {
		this.originalPath = originalFileInfo.getPath();
		this.originalFileName = originalFileInfo.getFileName();

		this.originalLength = originalFileInfo.getLength();
		this.originalHash = originalFileInfo.getHash();
		this.originalLastModificationTime = originalFileInfo.getLastModificationTime();
		
		this.subFolder = FileUtil.subfolderDateFormatter.format(this.originalLastModificationTime);
		this.fileName = originalFileInfo.getFileName();
		
		this.importEnabled = true;
		
		if (this.originalFileName.toLowerCase().endsWith(".jpg")) {
			this.type = FileType.jpg;
		}
		else {
			this.type = FileType.unknown;
		}
		
		this.description = "";
	}

	public String getOriginalPath() {
		return originalPath;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public long getOriginalLength() {
		return this.originalLength;
	}

	public String getOriginalHash() {
		return originalHash;
	}

	public Date getOriginalLastModificationTime() {
		return originalLastModificationTime;
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

	public FileType getType() {
		return type;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getRecordLastModificationTime() {
		return recordLastModificationTime;
	}

	public void setRecordLastModificationTime(Date recordLastModificationTime) {
		this.recordLastModificationTime = recordLastModificationTime;
	}
}
