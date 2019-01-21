package de.refactoringbot.services.main;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.springframework.util.ClassUtils;

public class FileServiceTest {

	@Test
	public void testGetAllJavaFiles() throws IOException {
		// arrange
		FileService fileService = new FileService();
		String repoFolderPath = System.getProperty("user.dir");

		// act
		List<String> allJavaFiles = fileService.getAllJavaFiles(repoFolderPath);

		// assert
		assertTrue(allJavaFiles.size() > 42);
		assertTrue(allJavaFiles.contains(getAbsoluteFilePathOfThis()));
	}

	@Test
	public void testFindJavaRoots() throws IOException {
		// arrange
		FileService fileService = new FileService();
		String repoFolderPath = System.getProperty("user.dir");
		List<String> allJavaFiles = fileService.getAllJavaFiles(repoFolderPath);

		// act
		List<String> javaRoots = fileService.findJavaRoots(allJavaFiles);
	
		// assert
		assertTrue(javaRoots.size() > 0);
		assertTrue(javaRoots.contains(getAbsoluteJavaRootPathOfThis()));
	}

	private String getAbsoluteFilePathOfThis() {
		String path = "src/test/java/" + ClassUtils.convertClassNameToResourcePath(FileServiceTest.class.getName())
				+ ".java";
		return new File(path).getAbsolutePath();
	}
	
	private String getAbsoluteJavaRootPathOfThis() {
		String path = System.getProperty("user.dir") + "/src/test/java";
		return new File(path).getAbsolutePath();
	}

}
