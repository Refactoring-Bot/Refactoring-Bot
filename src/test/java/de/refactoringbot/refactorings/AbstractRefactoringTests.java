package de.refactoringbot.refactorings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;


/**
 * Abstract class for refactoring test cases
 */
public abstract class AbstractRefactoringTests {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * Returns a temporary copy of the file with the given fileName in the resources folder
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected File getTempCopyOfResourcesFile(String fileName) throws IOException {
		File copy = folder.newFile();
		com.google.common.io.Files.copy(new File(getTestResourcesAbsolutePath() + "/" + fileName), copy);
		return copy;
	}
	
	/**
	 * Returns the line from the given file, stripped from whitespace
	 * @param file
	 * @param line
	 * @return
	 * @throws IOException
	 */
	protected String getStrippedContentFromFile(File file, int line) throws IOException {
		String result;
		try (Stream<String> lines = Files.lines(Paths.get(file.getAbsolutePath()))) {
		    result = lines.skip(line-1).findFirst().get();
		}
		return StringUtils.strip(result);
	}
	
	/**
	 * Returns the absolute path of the test resources folder
	 * @return
	 */
	private String getTestResourcesAbsolutePath() {
		File resourcesDirectory = new File("src/test/java/de/refactoringbot/resources");
		return resourcesDirectory.getAbsolutePath();
	}	

}
