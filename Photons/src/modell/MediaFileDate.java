package modell;

import java.nio.file.Path;
import java.util.Date;

public class MediaFileDate {
	private Date originalFileLastModificationTime;
	private Date mediaContentTimestamp;
	private Date utcTimestamp;

	public MediaFileDate(Path filePath) {
		// TODO: implement, maybe requires database structure change
		
		// TODO: get originalFileLastModificationTime from file
		// TODO: get mediaContentTimestamp based on file content (type necessary etc.)
		// TODO: UTC? how to handle? maybe timezone info is a necessary argument, how does Date type store it?
	}
}
