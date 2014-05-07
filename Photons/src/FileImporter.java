import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;


public class FileImporter {
	
	// This is the source folder of the images to be imported. All subdirectories will be scanned.
	private Path pathToImportFrom;
	
	// The target folder of the import action. Imported files will be stored in the subdirectories of this folder.
	// The database storing file infos will also be located here
	private Path pathToImportTo;
	
	private String fileExtensionToImport;
	
	public FileImporter(String pathToImportFrom, String pathToImportTo, String fileExtensionToImport) {
		this.pathToImportFrom = Paths.get(pathToImportFrom);
		this.pathToImportTo = Paths.get(pathToImportTo);
		this.fileExtensionToImport = fileExtensionToImport;
	}
	
	public void Import() throws IOException {

		System.out.println("Importing files from [" + this.pathToImportFrom.toString() + "] to [" + this.pathToImportTo.toString() + "]");

		// Next example with walkFileTree originates from http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileVisitor.html
		Files.walkFileTree(this.pathToImportFrom, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
			new SimpleFileVisitor<Path>() {
//	             @Override
//	             public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
//	             {
//	            	 // TODO: implement. This version is only a copy from the original website
//	                 Path targetdir = pathToImportTo.resolve(pathToImportFrom.relativize(dir));
//	                 try {
//	                     Files.copy(dir, targetdir);
//	                 } catch (FileAlreadyExistsException e) {
//	                      if (!Files.isDirectory(targetdir))
//	                          throw e;
//	                 }
//	                 return FileVisitResult.CONTINUE;
//	             }
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					System.out.println("Importing file [" + file.toString() + "]...");
					
					if (file.toString().toLowerCase().endsWith(fileExtensionToImport)) {
						FileToImportInfo fileToImportInfo = null;
						try {
							fileToImportInfo = new FileToImportInfo(file);
							FileImportedInfo fileImportedInfo = new FileImportedInfo(fileToImportInfo);
							
							Path targetFolder = Paths.get(pathToImportTo.toString(), fileImportedInfo.getSubfolder());
							Path targetPath = Paths.get(targetFolder.toString(), fileImportedInfo.getFileName());

							// TODO: check existence in database
							if (Files.exists(targetPath)) {
								System.out.println("Target file already exists [" + targetPath.toString() + "]. Skipping copy.");
							} else {
								System.out.println("Copying file from [" + file.toString() + "] to [" + targetPath.toString() + "]");
								if (Files.exists(targetFolder)) {
									System.out.println("Target folder already exists [" + targetFolder.toString() + "].");
								} else {
									Files.createDirectories(targetFolder);
								}
								
								Files.copy(file, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
								DatabaseUtil.saveFileImportedInfo(pathToImportTo.toString(), fileImportedInfo);
							}
						} catch (Exception e) {
							System.out.println("ERROR: Failed to import file [" + file.toString() + "].");
							e.printStackTrace();
						}
						
					} else {
						System.out.println("Ignoring file because of file type mismatch [" + file.toString() + "].");
					}

					return FileVisitResult.CONTINUE;
				}
			});
		}
}
