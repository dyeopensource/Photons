package tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import common.DatabaseUtil;

/**
 * Tests for the common.DatabaseUtil class
 * @author emil
 */
public class DatabaseUtilTest {

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
	public void testIdNotSetValue() {
		if (DatabaseUtil.idNotSetValue != -1) {
			fail(String.format("Unexpected value for DatabaseUtil.idNotSetValue. Sould be [%d], but was [%d].", -1, DatabaseUtil.idNotSetValue));
		}
	}
	
	@Test
	public void testGetStringFromBoolValue_FromFalse0_Succeeds() {
		Boolean input = false;
		String result = DatabaseUtil.getStringFromBoolValue(input); 
		if (!result.equals("0")) {
			fail(String.format("Wrong String value from Boolean '%s': [%s]", input, result));
		}
	}

	@Test
	public void testGetStringFromBoolValue_FromTrue1_Succeeds() {
		Boolean input = true;
		String result = DatabaseUtil.getStringFromBoolValue(input); 
		if (!result.equals("1")) {
			fail(String.format("Wrong String value from Boolean '%s': [%s]", input, result));
		}
	}

	@Test
	public void testGetBooleanFromStringValue_From0False_Succeeds() {
		String input = "0";
		Boolean result = DatabaseUtil.getBooleanFromStringValue(input); 
		if (!result.equals(false)) {
			fail(String.format("Wrong Boolean value from String '%s': [%s]", input, result));
		}
	}

	@Test
	public void testGetBooleanFromStringValue_From1True_Succeeds() {
		String input = "1";
		Boolean result = DatabaseUtil.getBooleanFromStringValue(input); 
		if (!result.equals(true)) {
			fail(String.format("Wrong Boolean value from String '%s': [%s]", input, result));
		}
	}

	@Test
	public void testGetLongTimeStampCurrent() {
		fail("Not yet implemented");
	}

	@Test
	public void testCheckSQLite() {
		fail("Not yet implemented");
	}
}
