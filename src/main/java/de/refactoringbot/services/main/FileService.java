package de.refactoringbot.services.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	 * This method returns all root folders' absolute paths of the given java files
	 * (like the src folder or the src/main/java folder of maven projects)
	 * 
	 * @param allJavaFiles
	 * @return javaRoots
	 * @throws FileNotFoundException
	 */
	public List<String> findJavaRoots(List<String> allJavaFiles) throws FileNotFoundException {
		Set<String> javaRoots = new HashSet<>();

		for (String javaFile : allJavaFiles) {
			FileInputStream filepath = new FileInputStream(javaFile);

			CompilationUnit compilationUnit;
                        
			try {
				compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(filepath));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				continue;
			}

			File file = new File(javaFile);
			List<PackageDeclaration> packageDeclarations = compilationUnit.findAll(PackageDeclaration.class);
			if (!packageDeclarations.isEmpty()) {
				// current java file should contain exactly one package declaration
				PackageDeclaration packageDeclaration = packageDeclarations.get(0);
				String rootPackage = packageDeclaration.getNameAsString().split("\\.")[0];
				String javaRoot = file.getAbsolutePath()
						.split(Pattern.quote(File.separator) + rootPackage + Pattern.quote(File.separator))[0];

                                // If we have a made-up package name the path will not exist, so we don't add it
                                if (Files.exists(Paths.get(javaRoot))) {
                                    javaRoots.add(javaRoot);
                                }
			}
		}
		return new ArrayList<>(javaRoots);
	}
}
