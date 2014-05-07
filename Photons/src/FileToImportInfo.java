import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;


public class FileToImportInfo {
	
	private String path;
	private String fileName;
	
	private String fileNameWithPath;
	
	private long length;
	private String hash;
	private Date lastModificationTime;
	
	
	public FileToImportInfo(Path path) throws Exception {
		this.path = path.getParent().toString();
		this.fileName = path.getFileName().toString();
		
		this.fileNameWithPath = path.toString();
		
		File thisFile = new File(this.fileNameWithPath);
		this.length = thisFile.length();
		
		this.lastModificationTime = new Date(thisFile.lastModified());
		this.hash = FileUtil.getChecksum(this.fileNameWithPath, "SHA-256");
	}
	
	public FileToImportInfo(String path, String fileName) throws Exception {
		this.path = path;
		this.fileName = fileName;
		
		this.fileNameWithPath = Paths.get(this.path, this.fileName).toString();
		
		File thisFile = new File(this.fileNameWithPath);
		this.length = thisFile.length();
		
		this.lastModificationTime = new Date(thisFile.lastModified());
		this.hash = FileUtil.getChecksum(this.fileNameWithPath, "MD5");
	}

	public String getPath() {
		return path;
	}

	public String getFileName() {
		return fileName;
	}

	public long getLength() {
		return this.length;
	}

	public String getHash() {
		return hash;
	}

	public Date getLastModificationTime() {
		return lastModificationTime;
	}
}
