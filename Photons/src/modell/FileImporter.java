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
import ui.Photons;

/**
 * This class is responsible for importing files from a folder 
 * @author emil
 * TODO: this class is not tested yet
 */
public class FileImporter {

	/**
	 * This is the source folder of the images to be imported. All subdirectories will be scanned.
	 */
	private Path importSourcePath;
	
	/**
	 * The target folder of the import action. Imported files will be stored in the subfolders of this folder.
	 * The database storing file info will also be located here
	 */
	private Path importTargetPath;
	
	/**
	 * The lower case file extension which should be used for importing files
	 */
	private String fileExtensionToImportLowerCase;
	
	private FileInfoDatabase fileInfoDatabase;
	
	public FileImporter(String pathToImportFrom, String pathToImportTo, String fileExtensionToImport) {
		this.importSourcePath = Paths.get(pathToImportFrom);
		this.importTargetPath = Paths.get(pathToImportTo);
		this.fileExtensionToImportLowerCase = fileExtensionToImport.toLowerCase();
		
		this.fileInfoDatabase = new FileInfoDatabase(this.importTargetPath);
	}
	
	public void Import() throws IOException {
		MyLogger.displayAndLogActionMessage("Importing files from [%s] to [%s]", this.importSourcePath, this.importTargetPath);
		this.fileInfoDatabase.openOrCreateDatabase();
		
		Files.walkFileTree(this.importSourcePath,
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
		
		if (!fileExtensionFits(file)) {
			return;
		}

		MyLogger.displayAndLogActionMessage("Importing file [%s]...", file);
		try {
			
			// Data based on the source file
			FileToImportInfo fileToImportInfo = new FileToImportInfo(file);
			
			// Creating data for database, based on the original file data
			FileImportedInfo fileImportedInfo = new FileImportedInfo(fileToImportInfo);
			
			Path targetFolder = Paths.get(importTargetPath.toString(), fileImportedInfo.getSubfolder());
			Path targetPath = Paths.get(targetFolder.toString(), fileImportedInfo.getFileName());

			// Actions here:
			// Check if the file is already imported.
			// If there is a record in the database with the same hash and size, it means the file was already imported.
			// (If there are not only one, but more records with the same hash and size, this shows a bug in the import process.)
			// If the imported file does not exist, then maybe it should be reimported.
			// But maybe it was removed intentionally:
			// In this case the ImportEnabled flag is set to false and the file should not be imported again.
			// Bit if the flag is true, the file should be copied again.
			Boolean addNewFileInfo = false;
			Boolean copyFile = false;
			FileImportedInfo existingFileImportedInfo = null;
			// Retrieving imported file info
			existingFileImportedInfo = fileInfoDatabase.getFileImportedInfo(
					fileImportedInfo.getHash(),
					fileImportedInfo.getLength());
			// Checking if the imported file exists
			if (existingFileImportedInfo == null) {
				addNewFileInfo = true;
				copyFile = true;
			} else {
				// File was already imported
				MyLogger.displayActionMessage("FileInfo in the database with the same hash and size already exists.", targetPath);
				MyLogger.displayAndLogActionMessage("MATCH: DB: [%s] Import: [%s]", Paths.get(importTargetPath.toString(), existingFileImportedInfo.getSubfolder(), existingFileImportedInfo.getFileName()), file);
				// Checking if imported file exists
				if (Files.exists(Paths.get(importTargetPath.toString(), existingFileImportedInfo.getCurrentRelativePathWithFileName().toString()))) {
					// Imported file exists: checking if length and hash is correct (sanity check). If not - report an error
					FileToImportInfo existingFileToImportInfo = new FileToImportInfo(targetPath);
					if (existingFileToImportInfo.getLength() != existingFileImportedInfo.getLength()) {
						// Length mismatch
						MyLogger.displayAndLogActionMessage("File length mismatch. Length in database: [%d]. Real file lentgh: [%d].", existingFileImportedInfo.getLength(), existingFileToImportInfo.getLength());
						System.exit(Photons.errorCodeLengthMismatch);
					}
					
					if (!existingFileToImportInfo.getHash().equals(existingFileImportedInfo.getHash())) {
						// Hash mismatch
						MyLogger.displayAndLogActionMessage("File hash mismatch. Hash in database: [%s]. Real file hash: [%s].", existingFileImportedInfo.getHash(), existingFileToImportInfo.getHash());
						System.exit(Photons.errorCodeHashMismatch);
					}
				} else {
					// Imported file does not exist
					if (!existingFileImportedInfo.getImportEnabled()) {
						// ImportEnabled flag is set to false - it was deleted intentionally, no reimport
						MyLogger.displayAndLogActionMessage("Skipping...");
						return;
					}
					
					copyFile = true;
					MyLogger.displayAndLogActionMessage("Reimporting...");
				}
			}
			
			if (copyFile) {
				if (Files.exists(targetPath)) {
					// This case can happen. E.g. if the picture was saved and resized to another location with the same name.
					Path oldTargetPath = targetPath;
					targetPath = FileUtil.getAlternateFileName(oldTargetPath);
					MyLogger.displayAndLogActionMessage("WARNING: Target file already exists [%s]. Generated new file name: [%s].", oldTargetPath, targetPath);
					
					fileImportedInfo.setFileName(targetPath.getFileName().toString());
				}
				
				// File copy
				CopyFileToTargetPathWithVerification(file, fileImportedInfo, targetFolder, targetPath);
			}
			
			// Saving file information into database
			if (addNewFileInfo) {
				fileInfoDatabase.addFileImportedInfo(importTargetPath.toString(), fileImportedInfo);
				
				// Verifying database insertion
				FileImportedInfo createdFileImportedInfo = fileInfoDatabase.getFileImportedInfo(
						fileImportedInfo.getHash(),
						fileImportedInfo.getLength());
				if (createdFileImportedInfo == null) {
					MyLogger.displayAndLogActionMessage("ERROR: error during addition of file info to database. [%s]", file);
					System.exit(Photons.errorCodeFileInsertionVerificationFailed);
				}
			} else {
				fileInfoDatabase.addSourcePathInfo(fileImportedInfo, existingFileImportedInfo);
			}
			
			// Success
			MyLogger.displayAndLogActionMessage("File imported from: [%s] to [%s].", file, targetPath);
		} catch (Exception e) {
			MyLogger.displayAndLogActionMessage("ERROR: Failed to import file [%s].", file);
			MyLogger.displayAndLogException(e);
		}
	}
	
	/**
	 * Checks if the file has the expected extension 
	 * @param file The file to be checked
	 * @return True if the file has the expected extension (ignoring case), false otherwise
	 */
	private Boolean fileExtensionFits(Path file) {
		
		if (file.toString().toLowerCase().endsWith(fileExtensionToImportLowerCase)) {
			return true;
		} else {
			MyLogger.displayActionMessage("Ignoring file because of file type mismatch [%s].", file);
		}

		return false;
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
		
		MyLogger.displayAndLogActionMessage("Copy verified, success.");
	}
	
	private Boolean fileCopySuccess(
			Path file,
			FileImportedInfo fileImportedInfo,
			Path targetPath) throws Exception {
		
		if (fileImportedInfo.getLength() != Files.size(targetPath)) {
			MyLogger.displayAndLogActionMessage("ERROR: error during copying file from: [%s] to [%s]. File length difference.", file, targetPath);
			return false;
		}
		
		if (!fileImportedInfo.getHash().equals(FileUtil.getFileContentHash(targetPath.toString()))) {
			MyLogger.displayAndLogActionMessage("ERROR: error during copying file from: [%s] to [%s]. File content hash difference.", file, targetPath);
			return false;
		}
		
		return true;
	}
}
