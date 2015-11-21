package tests;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import common.FileUtil;

/**
 * Tests for the common.FileUtil class
 * @author emil
 *
 */
public class FileUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFolderExists() throws IOException {
		Path tempFolderPath = null;
		try {
			tempFolderPath = Files.createTempDirectory("FileUtilTest");
			if (!FileUtil.folderExists(tempFolderPath.toString())) {
				fail("Failed to report existing folder.");
			}
		} finally {
			Files.deleteIfExists(tempFolderPath);
		}

		if (tempFolderPath == null || FileUtil.folderExists(tempFolderPath.toString())) {
			fail("Deleted folder reported as existing.");
		}
	}
	
	@Test
	public void testGetAlternateFileName_GeneratesFileNameCorrectly_Succeeds() {
		Path tempFile = null;
		try {
			try {
				tempFile = Files.createTempFile("Photons_test_GetAlternateFileName", ".txt");
			} catch (IOException e) {
				e.printStackTrace();
				fail("Could not create temp file.");
			}
			
			Path newFilePath = FileUtil.getAlternateFileName(tempFile);
			boolean fileExists = false;
			try {
				if (Files.exists(newFilePath)) {
					fileExists = true;
					fail("New file already exists.");
				}
			} finally {
				if (fileExists) {
					try {
						Files.delete(newFilePath);
					} catch (IOException e) {
						e.printStackTrace();
						fail(String.format("Failed to delete new file [%s]", newFilePath));
					}
				}
			}
		} finally {
			try {
				if (tempFile != null) {
					Files.delete(tempFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
				fail(String.format("Failed to delete temporary file [%s]", tempFile));
			}
		}
	}

	@Test
	public void testGetFileContentHash_CalculatesHashCorrectly_Succeeds() {
		Path tempFile = null;
		try {
			try {
				tempFile = Files.createTempFile("Photons_test_GetAlternateFileName", ".txt");
			} catch (IOException e) {
				e.printStackTrace();
				fail("Could not create temp file.");
			}
			
			// Write something into the file
			BufferedWriter writer = null;
			try {
	            writer = new BufferedWriter(new FileWriter(tempFile.toString(), false));
	            writer.write("Test string...");
			} catch (Exception e) {
				e.printStackTrace();
				fail(String.format("Failed to write to file [%s].", tempFile));
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
						fail("Failed to close file writer.");
					}
				}
			}

			// Check hash
			try {
				String hash = FileUtil.getFileContentHash(tempFile.toString());
				
				// Reference hash checked at: http://www.md5calc.com/
				if (!hash.equals("247a74b52cfe95532fd652242d94ac0029f8dda1346a8cf0cc0874e274b3a2a0")) {
					fail(String.format("Incorrect hash string retrieved: [%s].", hash));
				}
			} catch (Exception e) {
				e.printStackTrace();
				fail("Failed to get hash string.");
			}
			
		} finally {
			try {
				if (tempFile != null) {
					Files.delete(tempFile);
				}
			} catch (IOException e) {
				e.printStackTrace();
				fail(String.format("Failed to delete temporary file [%s]", tempFile));
			}
		}
	}
}
