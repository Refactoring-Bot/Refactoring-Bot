package de.refactoringbot.refactorings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.util.ClassUtils;

/**
 * Abstract class for refactoring test cases
 */
public abstract class AbstractRefactoringTests {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * Returns the test data class for a specific refactoring test class
	 * 
	 * @return
	 */
	public abstract Class<?> getTestResourcesClass();

	/**
	 * Returns a temporary copy of the test resources file of the current
	 * refactoring test class
	 * 
	 * @return
	 * @throws IOException
	 */
	protected File getTempCopyOfTestResourcesFile() throws IOException {
		File copy = folder.newFile();
		com.google.common.io.Files.copy(getTestResourcesFile(), copy);
		return copy;
	}

	/**
	 * Returns the line from the given file, stripped from whitespace
	 * 
	 * @param file
	 * @param line
	 * @return
	 * @throws IOException
	 */
	protected String getStrippedContentFromFile(File file, int line) throws IOException {
		String result;
		try (Stream<String> lines = Files.lines(Paths.get(file.getAbsolutePath()))) {
			result = lines.skip(line - 1).findFirst().get();
		}
		return StringUtils.strip(result);
	}

	/**
	 * Returns the test resource file that was determined based on the test
	 * resources class
	 * 
	 * @return
	 */
	private File getTestResourcesFile() {
		String pathToTestResources = "src/test/java/"
				+ ClassUtils.convertClassNameToResourcePath(getTestResourcesClass().getName()) + ".java";
		return new File(pathToTestResources);
	}

}
