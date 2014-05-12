import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


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
	public void test_GetAlternateFileName() {
		Path tempFile = null;
		try {
			try {
				tempFile = Files.createTempFile("Photons_test_GetAlternateFileName", ".txt");
			} catch (IOException e) {
				e.printStackTrace();
				fail("Could not create temp file.");
			}
			
			Path newFilePath = FileUtil.GetAlternateFileName(tempFile);
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} finally {
			try {
				if (tempFile != null) {
					Files.delete(tempFile);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
