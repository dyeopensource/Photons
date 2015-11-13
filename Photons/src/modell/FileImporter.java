package modell;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import common.FileUtil;
import common.MyLogger;


public class FileImporter {
	// This is the source folder of the images to be imported. All subdirectories will be scanned.
	private Path pathToImportFrom;
	
	// The target folder of the import action. Imported files will be stored in the subdirectories of this folder.
	// The database storing file infos will also be located here
	private Path pathToImportTo;
	
	private String fileExtensionToImport;
	
	private FileInfoDatabase fileInfoDatabase;
	
	public FileImporter(String pathToImportFrom, String pathToImportTo, String fileExtensionToImport) {
		this.pathToImportFrom = Paths.get(pathToImportFrom);
		this.pathToImportTo = Paths.get(pathToImportTo);
		this.fileExtensionToImport = fileExtensionToImport;
		
		this.fileInfoDatabase = new FileInfoDatabase(this.pathToImportTo);
	}
	
	public void Import() throws IOException {
		MyLogger.displayAndLogActionMessage("Importing files from [%s] to [%s]", this.pathToImportFrom, this.pathToImportTo);
		this.fileInfoDatabase.openOrCreateDatabase();
		
		Files.walkFileTree(this.pathToImportFrom,
			new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					FileImporter.this.visitFile(file);
					return FileVisitResult.CONTINUE;
				}

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
					MyLogger.displayAndLogActionMessage("ERROR: Failed to import file [%s].", file);
					MyLogger.displayAndLogException(e);
                    return FileVisitResult.SKIP_SUBTREE;
                }
			});
		}

	/**
	 * Checks if a file is subject for importing (extension fits) and if it should be imported,
	 * then checks if it was already imported and tries to import / reimport the file if necessary.
	 *  
	 * @param file The file checked
	 */
	private void visitFile(
			Path file) {
		
		if (!fileShouldBeImported(file)) {
			return;
		}

		MyLogger.displayAndLogActionMessage("Importing file [%s]...", file);
		try {
			FileToImportInfo fileToImportInfo = new FileToImportInfo(file);
			FileImportedInfo fileImportedInfo = new FileImportedInfo(fileToImportInfo);
			
			Path targetFolder = Paths.get(pathToImportTo.toString(), fileImportedInfo.getSubfolder());
			Path targetPath = Paths.get(targetFolder.toString(), fileImportedInfo.getFileName());

			// TODO: move already imported check to a method / function
			
			// Actions here:
			// Check if the file is already imported.
			// If there is a record in the database with the same hash and size, it means the file was already imported.
			// (If there are not only one, but more records with the same hash and size, this shows a bug in the import process.)
			// If the imported file does not exist, then maybe it should be reimported.
			// But maybe it was removed intentionally:
			// In this case the ImportEnabled flag is set to false and the file should not be imported again.
			// Bit if the flag is true, the file should be copied again.
			
			FileImportedInfo existingFileImportedInfo = null;
			existingFileImportedInfo = fileInfoDatabase.getFileImportedInfo(
					fileImportedInfo.getOriginalHash(),
					fileImportedInfo.getOriginalLength());
			if (existingFileImportedInfo != null) {
				MyLogger.displayActionMessage("FileInfo in the database with the same hash and size already exists.", targetPath);
				MyLogger.displayAndLogActionMessage("MATCH: DB: [%s] Import: [%s]", Paths.get(pathToImportTo.toString(), existingFileImportedInfo.getSubfolder(), existingFileImportedInfo.getFileName()), file);
				if (!existingFileImportedInfo.getImportEnabled()) {
					MyLogger.displayAndLogActionMessage("Skipping...");
					return;
				}
				
				MyLogger.displayAndLogActionMessage("Reimporting...");
			}
			
			if (Files.exists(targetPath)) {
				// TODO: maybe length and hash check would be nice here
				// This case can happen. E.g. if the picture was saved and resized
				// to another location with the same name.
				// But at least a warning should be logged to be able to check later.
				Path oldTargetPath = targetPath;
				targetPath = FileUtil.getAlternateFileName(oldTargetPath);
				MyLogger.displayAndLogActionMessage("WARNING: Target file already exists [%s]. Generated new file name: [%s].", oldTargetPath, targetPath);
				
				fileImportedInfo.setFileName(targetPath.getFileName().toString());
			}
			
			// File copy
			CopyFileToTargetPathWithVerification(file, fileImportedInfo, targetFolder, targetPath);
			
			// Saving file information into database
			fileInfoDatabase.saveFileImportedInfo(pathToImportTo.toString(), fileImportedInfo);
			FileImportedInfo createdFileImportedInfo = fileInfoDatabase.getFileImportedInfo(
					fileImportedInfo.getOriginalHash(),
					fileImportedInfo.getOriginalLength());
			if (createdFileImportedInfo == null) {
				// TODO: how to retry?
				MyLogger.displayAndLogActionMessage("ERROR: error during database insert.");
				return;
			}
			
			// Success
			MyLogger.displayAndLogActionMessage("File imported from: [%s] to [%s].", file, targetPath);
		} catch (Exception e) {
			MyLogger.displayAndLogActionMessage("ERROR: Failed to import file [%s].", file);
			MyLogger.displayAndLogException(e);
		}
	}
	
	private Boolean fileShouldBeImported(Path file) {
		
		if (!file.toString().toLowerCase().endsWith(fileExtensionToImport)) {
			MyLogger.displayActionMessage("Ignoring file because of file type mismatch [%s].", file);
			return false;
		}
		
		return true;
	}
	
	private void CopyFileToTargetPathWithVerification(
			Path file,
			FileImportedInfo fileImportedInfo,
			Path targetFolder,
			Path targetPath) throws Exception {
		
		MyLogger.displayActionMessage("Copying file from [%s] to [%s]", file, targetPath);
		if (!Files.exists(targetFolder)) {
			Files.createDirectories(targetFolder);
		}
		
		Files.copy(file, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
		
		// Verification of file copy:
		if (!fileCopySuccess(file, fileImportedInfo, targetPath)) {
			return;
		}
	}
	
	private Boolean fileCopySuccess(
			Path file,
			FileImportedInfo fileImportedInfo,
			Path targetPath) throws Exception {
		
		if (fileImportedInfo.getOriginalLength() != Files.size(targetPath)) {
			MyLogger.displayAndLogActionMessage("ERROR: error during copying file from: [%s] to [%s]. File length difference.", file, targetPath);
			return false;
		}
		
		if (!fileImportedInfo.getOriginalHash().equals(FileUtil.getFileContentHash(targetPath.toString()))) {
			MyLogger.displayAndLogActionMessage("ERROR: error during copying file from: [%s] to [%s]. File content hash difference.", file, targetPath);
			return false;
		}
		
		return true;
	}
}
