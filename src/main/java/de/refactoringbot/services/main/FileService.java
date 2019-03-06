package de.refactoringbot.services.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**
 * This class has methods that work with Files and Folders of Java-Projects.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class FileService {

	private static final Logger logger = LoggerFactory.getLogger(FileService.class);

	/**
	 * This method returns all Javafile-Paths of a project from a configuration.
	 * 
	 * @param repoFolderPath
	 * @return allJavaFiles
	 * @throws IOException
	 */
	public List<String> getAllJavaFiles(String repoFolderPath) throws IOException {
		// Init all java files path-list
		List<String> allJavaFiles = new ArrayList<>();

		// Get root folder of project
		File dir = new File(repoFolderPath);

		// Get paths to all java files of the project
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			if (file.getCanonicalPath().endsWith(".java")) {
				allJavaFiles.add(file.getCanonicalPath());
			}
		}
		return allJavaFiles;
	}

	/**
	 * This method returns all root-folders of java files (like the src folder or
	 * the src/main/java folder from maven projects)
	 * 
	 * @param allJavaFiles
	 * @return javaRoots
	 * @throws FileNotFoundException
	 */
	public List<String> findJavaRoots(List<String> allJavaFiles) throws FileNotFoundException {

		// Init roots list
		List<String> javaRoots = new ArrayList<>();

		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);

			CompilationUnit compilationUnit;

			try {
				compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				continue;
			}

			// Get all Classes
			List<PackageDeclaration> packageDeclarations = compilationUnit.findAll(PackageDeclaration.class);

			// If javafile has no package
			if (packageDeclarations.isEmpty()) {
				// Get javafile
				File rootlessFile = new File(javaFile);
				// Add parent of file to root
				if (!javaRoots.contains(rootlessFile.getParentFile().getAbsoluteFile().getAbsolutePath())) {
					javaRoots.add(rootlessFile.getParentFile().getAbsolutePath());
				}
			} else {
				// Only 1 package declaration for each file
				PackageDeclaration packageDeclaration = packageDeclarations.get(0);
				String rootPackage;

				if (packageDeclaration.getNameAsString().split("\\.").length == 1) {
					rootPackage = packageDeclaration.getNameAsString();
				} else {
					rootPackage = packageDeclaration.getNameAsString().split("\\.")[0];
				}

				// Get javafile
				File currentFile = new File(javaFile);

				// Until finding the root package
				while (currentFile.getParentFile() != null && !currentFile.isDirectory()
						|| !currentFile.getName().equals(rootPackage)) {
					if (currentFile.getParentFile() != null) {
						currentFile = currentFile.getParentFile();
					} else {
						break;
					}
				}

				// Add parent of rootPackage as java root
				if (currentFile.getParentFile() != null) {
					if (!javaRoots.contains(currentFile.getParentFile().getAbsoluteFile().getAbsolutePath())) {
						javaRoots.add(currentFile.getParentFile().getAbsolutePath());
					}
				}
			}

		}

		return javaRoots;
	}
}
