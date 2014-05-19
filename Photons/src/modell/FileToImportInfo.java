package modell;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import common.FileUtil;


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
		setFileData();
	}
	
	public FileToImportInfo(String path, String fileName) throws Exception {
		this.path = path;
		this.fileName = fileName;
		this.fileNameWithPath = Paths.get(this.path, this.fileName).toString();
		setFileData();
	}
	
	private void setFileData() throws Exception {
		File thisFile = new File(this.fileNameWithPath);
		this.length = thisFile.length();
		this.lastModificationTime = new Date(thisFile.lastModified());
		this.hash = FileUtil.getFileContentHash(this.fileNameWithPath);
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
