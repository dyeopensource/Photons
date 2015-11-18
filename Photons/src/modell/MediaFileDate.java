package modell;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;

public class MediaFileDate {
	
	/**
	 * The last modification date of the original file
	 */
	private Date originalFileLastModificationTime;
	
	/**
	 * The date when the media was taken (EXIF information)
	 */
	private Date mediaContentTimeStamp;
	
	/**
	 * Custom timestamp set by the user.
	 * Maybe neither the file modification date, nor the media content information
	 * about the date are correct.
	 * In this case the user can set the expected timestamp.
	 */
	private Date userTimeStamp;

	public MediaFileDate(Path filePath) {

		File file = new File(filePath.toString());
		this.originalFileLastModificationTime = new Date(file.lastModified());

		// TODO: get mediaContentTimestamp based on file content (type necessary etc.)
		this.mediaContentTimeStamp = this.originalFileLastModificationTime;
		
		this.userTimeStamp = this.originalFileLastModificationTime;
	}

	public Date getUserTimeStamp() {
		return userTimeStamp;
	}

	public void setUserTimeStamp(Date userTimeStamp) {
		this.userTimeStamp = userTimeStamp;
	}

	public Date getMediaContentTimeStamp() {
		return mediaContentTimeStamp;
	}

	public void setMediaContentTimeStamp(Date mediaContentTimeStamp) {
		this.mediaContentTimeStamp = mediaContentTimeStamp;
	}

	public Date getOriginalFileLastModificationTime() {
		return originalFileLastModificationTime;
	}

	public void setOriginalFileLastModificationTime(Date originalFileLastModificationTime) {
		this.originalFileLastModificationTime = originalFileLastModificationTime;
	}
}
