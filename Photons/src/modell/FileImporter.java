package modell;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
	private String[] fileExtensionsToImportLowerCase;
	
	private FileInfoDatabase fileInfoDatabase;
	
	private Path lastFilePath = null;
	
	private Boolean verificationSucceeded;
	
	public FileImporter(String pathToImportFrom, String pathToImportTo, String[] fileExtensionsToImport) {
		this.importSourcePath = Paths.get(pathToImportFrom);
		this.importTargetPath = Paths.get(pathToImportTo);
		this.fileExtensionsToImportLowerCase = new String[fileExtensionsToImport.length];
		for(int i = 0; i < fileExtensionsToImport.length; i++) {
			this.fileExtensionsToImportLowerCase[i] = fileExtensionsToImport[i].toLowerCase();
		}
		
		this.fileInfoDatabase = new FileInfoDatabase(this.importTargetPath);
	}
	
	public void Import() throws IOException {
		MyLogger.displayAndLogInformationMessage("Importing files from [%s] to [%s]", this.importSourcePath, this.importTargetPath);
		
		this.fileInfoDatabase.openOrCreateDatabase();
		
		Files.walkFileTree(this.importSourcePath,
			new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
				{
					FileImporter.this.visitFileForImport(file);
					return FileVisitResult.CONTINUE;
				}

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
					MyLogger.displayAndLogExceptionMessage(e, "Failed to import file [%s].", file);
                    return FileVisitResult.SKIP_SUBTREE;
                }
			});
		}

	public Boolean Verify() throws IOException {
		MyLogger.displayAndLogInformationMessage("Verifying import from [%s] to [%s]", this.importSourcePath, this.importTargetPath);
		
		verificationSucceeded = true;
		
		this.fileInfoDatabase.openOrCreateDatabase();
		
		Files.walkFileTree(this.importSourcePath,
			new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
				{
					FileImporter.this.visitFileForVerification(file);
					return FileVisitResult.CONTINUE;
				}

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
					MyLogger.displayAndLogExceptionMessage(e, "Failed to verify file [%s].", file);
                    return FileVisitResult.SKIP_SUBTREE;
                }
			});
		
		return verificationSucceeded;
		
		}

	/**
	 * Checks if a file is subject for importing (extension fits) and if it should be imported,
	 * then checks if it was already imported and tries to import / reimport the file if necessary.
	 *  
	 * @param file The file checked
	 */
	private void visitFileForImport(Path file) {

		if (!fileExtensionFits(file)) {
			return;
		}
		
		// Display the progress
		//MyLogger.displayAndLogActionMessage("Importing file [%s]...", file);
		try {
			Path filePath = file.toRealPath(LinkOption.NOFOLLOW_LINKS).getParent();
			if (!filePath.equals(lastFilePath)) {
				lastFilePath = filePath;
				MyLogger.displayInformationMessage("Importing from [%s]...", lastFilePath);
			}
		} catch (Exception e) {
			MyLogger.displayAndLogExceptionMessage(e, "Failed to import files in [%s].", file);
			// TODO: should we exit?
		}

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
				Path importedFilePath = Paths.get(importTargetPath.toString(), existingFileImportedInfo.getCurrentRelativePathWithFileName().toString());
				MyLogger.displayAndLogInformationMessage("MATCH: DB: [%s] Import: [%s]", importedFilePath, file);
				// Checking if imported file exists
				if (!Files.exists(importedFilePath)) {
					// Imported file does not exist
					if (!existingFileImportedInfo.getImportEnabled()) {
						// ImportEnabled flag is set to false - it was deleted intentionally, no reimport
						MyLogger.displayAndLogDebugMessage("Skipping...");
						return;
					}
					
					copyFile = true;
					MyLogger.displayAndLogDebugMessage("Reimporting...");
				}
			}
			
			if (copyFile) {
				if (Files.exists(targetPath)) {
					// This case can happen. E.g. if the picture was saved and resized to another location with the same name.
					Path oldTargetPath = targetPath;
					targetPath = FileUtil.getAlternateFileName(oldTargetPath);
					MyLogger.displayAndLogWarningMessage("Target file already exists [%s]. Generated new file name: [%s].", oldTargetPath, targetPath);
					
					fileImportedInfo.setFileName(targetPath.getFileName().toString());
				}
				
				// File copy
				CopyFileToTargetPathWithVerification(file, fileImportedInfo, targetFolder, targetPath);
			}
			
			// Saving file information into database
			Boolean groupInfoWasAdded = false;
			if (addNewFileInfo) {
				fileInfoDatabase.addFileImportedInfo(importTargetPath.toString(), fileImportedInfo);
				
				// Verifying database insertion
				FileImportedInfo createdFileImportedInfo = fileInfoDatabase.getFileImportedInfo(
						fileImportedInfo.getHash(),
						fileImportedInfo.getLength());
				if (createdFileImportedInfo == null) {
					MyLogger.displayAndLogErrorMessage("Error during addition of file info to database. [%s]", file);
					System.exit(Photons.errorCodeFileInsertionVerificationFailed);
				}
			} else {
				groupInfoWasAdded = fileInfoDatabase.addSourcePathInfo(fileImportedInfo, existingFileImportedInfo);
			}
			
			// Success
			if (copyFile || addNewFileInfo || groupInfoWasAdded) {
				MyLogger.displayAndLogInformationMessage("File imported from: [%s] to [%s].", file, targetPath);
			}
		} catch (Exception e) {
			MyLogger.displayAndLogExceptionMessage(e, "Failed to import file [%s].", file);
			// TODO: should we exit?
		}
	}

	/**
	 * Checks if a file is subject for importing (extension fits) and if it was imported correctly, i.e.
	 * - File information is found with the same length and hash
	 * - If the importEnabled flag is set to true, then check:
	 * - if the file exists, has the same (original) length and hash as the one to be imported (the same as in the database)
	 * - there is a group for the verified file's source path and the imported file is assigned to it 
	 * @param file The file checked
	 */
	private void visitFileForVerification(Path file) {

		Boolean success = true;
		if (!fileExtensionFits(file)) {
			return;
		}
		
		try {
			Path filePath = file.toRealPath(LinkOption.NOFOLLOW_LINKS).getParent();
			if (!filePath.equals(lastFilePath)) {
				lastFilePath = filePath;
				MyLogger.displayInformationMessage("Verifying files in [%s]...", lastFilePath);
			}
		} catch (Exception e) {
			MyLogger.displayAndLogExceptionMessage(e, "Failed to verify files in folder [%s].", file);
			// TODO: should we exit?
		}

		try {
			// Data based on the source file
			FileToImportInfo fileToImportInfo = new FileToImportInfo(file);
			
			// Creating data for database, based on the original file data
			FileImportedInfo fileImportedInfo = new FileImportedInfo(fileToImportInfo);
			
			FileImportedInfo existingFileImportedInfo = null;
			// Retrieving imported file info
			existingFileImportedInfo = fileInfoDatabase.getFileImportedInfo(
					fileImportedInfo.getHash(),
					fileImportedInfo.getLength());
			// Checking if the imported file exists
			if (existingFileImportedInfo == null) {
				MyLogger.displayAndLogErrorMessage("File does not exist in database with [length=%d] [hash=%s]", fileToImportInfo.getLength(), fileToImportInfo.getHash());
				success = false;
			} else {
				// File was already imported
				Path importedFilePath = Paths.get(importTargetPath.toString(), existingFileImportedInfo.getCurrentRelativePathWithFileName().toString());
				MyLogger.displayAndLogInformationMessage("MATCH: DB: [%s] Import: [%s]", importedFilePath, file);
				// Checking if imported file exists
				if (!Files.exists(importedFilePath)) {
					// Imported file does not exist
					if (!existingFileImportedInfo.getImportEnabled()) {
						// ImportEnabled flag is set to false - it was deleted intentionally, no reimport
						MyLogger.displayAndLogDebugMessage("Skipping...");
						return;
					} else {
						MyLogger.displayAndLogErrorMessage("File does not exist in database [path=%s]", importedFilePath);
						success = false;
					}
				} else {
					// Check file against stored data
					FileToImportInfo importedFile = new FileToImportInfo(importedFilePath);
					if (importedFile.getLength() != fileToImportInfo.getLength()) {
						MyLogger.displayAndLogErrorMessage("Length mismatch. Source file [length=%d] Imported file [length=%d]", importedFile.getLength(), fileToImportInfo.getLength());
						success = false;
					}
					
					if (!importedFile.getHash().equals(fileToImportInfo.getHash())) {
						MyLogger.displayAndLogErrorMessage("Hash mismatch. Source file [hash=%s] Imported file [hash=%s]", importedFile.getHash(), fileToImportInfo.getHash());
						success = false;
					}
				}
			}
			
			// Checking group information in the database
			if (!fileInfoDatabase.verifySourcePathInfo(fileToImportInfo, existingFileImportedInfo)) {
				MyLogger.displayAndLogErrorMessage("Source path check failure. [file=%s]", file);
				success = false;
			}
			
			// Success
			MyLogger.displayAndLogDebugMessage("File verified: [%s]", file);
		} catch (Exception e) {
			MyLogger.displayAndLogExceptionMessage(e, "Failed to verify file [%s].", file);
			// TODO: should we exit?
			success = false;
		}
		
		if (!success) {
			verificationSucceeded = false;
		}
	}
	
	/**
	 * Checks if the file has the expected extension 
	 * @param file The file to be checked
	 * @return True if the file has the expected extension (ignoring case), false otherwise
	 */
	private Boolean fileExtensionFits(Path file) {
		
		for (int i = 0; i < this.fileExtensionsToImportLowerCase.length; i++) {
			if (file.toString().toLowerCase().endsWith(this.fileExtensionsToImportLowerCase[i])) {
				return true;
			}
		}

		//MyLogger.displayActionMessage("Ignoring file because of file type mismatch [%s].", file);

		return false;
	}
	
	private void CopyFileToTargetPathWithVerification(
			Path file,
			FileImportedInfo fileImportedInfo,
			Path targetFolder,
			Path targetPath) throws Exception {
		
		MyLogger.displayDebugMessage("Copying file from [%s] to [%s]", file, targetPath);
		if (!Files.exists(targetFolder)) {
			Files.createDirectories(targetFolder);
		}
		
		Files.copy(file, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
		
		// Verification of file copy:
		if (!fileCopySuccess(file, fileImportedInfo, targetPath)) {
			return;
		}
		
		MyLogger.displayAndLogDebugMessage("Copy verified, success.");
	}
	
	private Boolean fileCopySuccess(
			Path file,
			FileImportedInfo fileImportedInfo,
			Path targetPath) throws Exception {
		
		if (fileImportedInfo.getLength() != Files.size(targetPath)) {
			MyLogger.displayAndLogErrorMessage("Error during copying file from: [%s] to [%s]. File length difference.", file, targetPath);
			return false;
		}
		
		if (!fileImportedInfo.getHash().equals(FileUtil.getFileContentHash(targetPath.toString()))) {
			MyLogger.displayAndLogErrorMessage("Error during copying file from: [%s] to [%s]. File content hash difference.", file, targetPath);
			return false;
		}
		
		return true;
	}
}
