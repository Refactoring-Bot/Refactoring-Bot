package de.refactoringbot.testutils;

import java.io.File;

public class TestUtils {

	public static final String TEST_FOLDER_PATH = "src/test/java/";
	
	/**
	 * @return absolute path of <code>src/test/java/</code> directory in this
	 *         repository
	 */
	public static String getAbsolutePathOfTestsFolder() {
		return new File(TEST_FOLDER_PATH).getAbsolutePath();
	}
	
}
