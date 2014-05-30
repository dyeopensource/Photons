/**
 * 
 */
package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Date;

import modell.FileToImportInfo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author emil
 *
 */
public class FileToImportInfoTest {

	private static final String testDataFolderPathString = "testData";
	private final String testFileName = "testFileToImportInfo_constructor.jpg";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link modell.FileToImportInfo#FileToImportInfo(java.nio.file.Path)}.
	 * @throws Exception If the file does not exist or is not accessible
	 */
	@Test(expected = IOException.class)
	public final void testFileToImportInfoPath_notExistingFile_throwsException() throws Exception {
		FileToImportInfo fileToImportInfo = new FileToImportInfo(Paths.get(".", testDataFolderPathString, "notexistingFile.txt"));
		if (fileToImportInfo != null) {
			fail("Constructor succeeded for not existing file.");
		}
	}

	/**
	 * Test method for {@link modell.FileToImportInfo#FileToImportInfo(java.nio.file.Path)}.
	 * @throws Exception 
	 */
	@Test
	public final void testFileToImportInfoPath_existingFile_succeeds() throws Exception {
		FileToImportInfo fileToImportInfo = new FileToImportInfo(Paths.get(".", testDataFolderPathString, testFileName));
		TestGetters(fileToImportInfo);
	}

	/**
	 * Test method for {@link modell.FileToImportInfo#FileToImportInfo(java.lang.String, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test(expected = IOException.class)
	public final void testFileToImportInfoStringString_notExistingFile_throwsException() throws Exception {
		FileToImportInfo fileToImportInfo = new FileToImportInfo(Paths.get(".", testDataFolderPathString).toString(), "notexistingFile.txt");
		if (fileToImportInfo != null) {
			fail("Constructor succeeded for not existing file.");
		}
	}

	/**
	 * Test method for {@link modell.FileToImportInfo#FileToImportInfo(java.lang.String, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public final void testFileToImportInfoStringString_existingFile_succeeds() throws Exception {
		FileToImportInfo fileToImportInfo = new FileToImportInfo(Paths.get(".", testDataFolderPathString).toString(), testFileName);
		TestGetters(fileToImportInfo);
	}

	private void TestGetters(FileToImportInfo fileToImportInfo) throws IOException {
		String pathString = fileToImportInfo.getPath();
		String referencePathString = Paths.get(Paths.get("").toString(), testDataFolderPathString).toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
		if (!pathString.equals(referencePathString)) {
			fail(String.format("Wrong path: [%s]. Should be [%s].", pathString, referencePathString));
		}

		String fileName = fileToImportInfo.getFileName();
		if (!fileName.equals(testFileName)) {
			fail(String.format("Wrong fileName: [%s]. Should be [%s].", fileName, testFileName));
		}
		
		long length = fileToImportInfo.getLength();
		long referenceLength = 6130;
		if (length != referenceLength) {
			fail(String.format("Wrong file length: [%s]. Should be [%d].", length, referenceLength));
		}
		
		String fileContentHash = fileToImportInfo.getHash();
		String referenceHash = "994ca7616d83e21ec41cde5a2936fe8e06749207843f14e559a2f928fe1f6611";
		if (!fileContentHash.equals(referenceHash)) {
			fail(String.format("Wrong hash string: [%s]. Should be [%s].", fileContentHash, referenceHash));
		}
		
		Date lastModificationTime = fileToImportInfo.getLastModificationTime();
		Date referenceTime = new Date(1401104496000L);
		if (!lastModificationTime.equals(referenceTime)) {
			fail(String.format("Wrong lastModificationTime: [%s]. Should be [%s].", lastModificationTime, referenceTime));
		}
	}
}
