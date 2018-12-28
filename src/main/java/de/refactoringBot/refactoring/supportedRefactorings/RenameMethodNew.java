package de.refactoringBot.refactoring.supportedRefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.exceptions.BotRefactoringException;
import de.refactoringBot.model.javaparser.ParserRefactoringNew;
import de.refactoringBot.refactoring.RefactoringImpl;

/**
 * This refactoring class is used for renaming methods inside a java project.
 *
 * @author Stefan Basaric
 */
@Component
public class RenameMethodNew implements RefactoringImpl {

	/**
	 * This method performs the refactoring and returns the a commit message.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws Exception
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws Exception {

		// Init Refactorings
		ParserRefactoringNew refactoring = new ParserRefactoringNew();

		// Init all java files path-list
		List<String> allJavaFiles = new ArrayList<>();

		// Init needed variables
		String issueFilePath = gitConfig.getRepoFolder() + "/" + issue.getFilePath();
		String globalMethodSignature = null;
		String localMethodSignature = null;
		String oldMethodName = null;
		MethodDeclaration methodToRefactor = null;

		// Get root folder of project
		File dir = new File(gitConfig.getRepoFolder());

		// Get paths to all java files of the project
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			if (file.getCanonicalPath().endsWith(".java")) {
				allJavaFiles.add(file.getCanonicalPath());
			}
		}

		List<String> javaRoots = findJavaRoots(allJavaFiles, gitConfig.getRepoFolder());

		// Configure solver for the project
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		// Add java-roots
		for (String javaRoot : javaRoots) {
			typeSolver.add(new JavaParserTypeSolver(javaRoot));
		}
		typeSolver.add(new ReflectionTypeSolver());
		JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser.getStaticConfiguration().setSymbolResolver(javaSymbolSolver);

		// Read file
		FileInputStream methodPath = new FileInputStream(issueFilePath);
		CompilationUnit renameMethodUnit = LexicalPreservingPrinter.setup(JavaParser.parse(methodPath));

		// Get all Classes
		List<ClassOrInterfaceDeclaration> classes = renameMethodUnit.findAll(ClassOrInterfaceDeclaration.class);
		boolean foundMethod = false;

		// Iterate all Classes
		for (ClassOrInterfaceDeclaration currentClass : classes) {
			// Get all methods
			List<MethodDeclaration> methods = currentClass.getMethods();
			// Search methods
			for (MethodDeclaration method : methods) {
				// check if method = desired method
				globalMethodSignature = getFullMethodSignature(method, issue.getLine());
				localMethodSignature = getMethodDeclarationAsString(method, issue.getLine());
				oldMethodName = method.getNameAsString();
				// If it is
				if (globalMethodSignature != null) {
					// Set method and class signatures of the method
					methodToRefactor = method;
					foundMethod = true;

					// Add class to refactoring
					refactoring.addClass(currentClass.resolve().getQualifiedName());

					// Get all super classes
					List<ResolvedReferenceType> ancestors = currentClass.resolve().getAllAncestors();

					// Add all super classes to All-To-Refactor classes
					for (ResolvedReferenceType ancestor : ancestors) {
						if (!refactoring.getClasses().contains(ancestor.getQualifiedName())
								&& !ancestor.getQualifiedName().equals("java.lang.Object")) {
							refactoring.addClass(ancestor.getQualifiedName());
						}
					}
					break;
				}
			}
			// If method found
			if (foundMethod) {
				break;
			}
		}

		// If refactor-method not found
		if (methodToRefactor == null) {
			throw new BotRefactoringException("Could not find specified method! Automated refactoring failed.");
		}

		// Add all Subclasses and their Superclasses to AST-Tree
		while (true) {
			int before = refactoring.getClasses().size();
			refactoring = addSubClasses(refactoring, allJavaFiles);
			int after = refactoring.getClasses().size();
			// Break if all classes found
			if (before == after) {
				break;
			}
		}

		// Find all Methods and Method-Calls for renaming
		refactoring = findMethods(refactoring, allJavaFiles, localMethodSignature);
		refactoring = findMethodCalls(refactoring, allJavaFiles);

		// Rename method declarations and their calls
		renameFindings(refactoring, issue.getRefactorString());

		return "Renamed method '" + oldMethodName + "' to '" + issue.getRefactorString() + "'";
	}
	
	/**
	 * This method scanns all java files for classes that could be subclasses in our
	 * AST-Tree.
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return refactoring
	 * @throws FileNotFoundException
	 */
	private ParserRefactoringNew addSubClasses(ParserRefactoringNew refactoring, List<String> allJavaFiles)
			throws FileNotFoundException {

		// Search all Java-Files
		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

			// Get all Classes
			List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);

			// Search all Classes
			for (ClassOrInterfaceDeclaration currentClass : classes) {
				List<String> classAncestors = new ArrayList<String>();
				boolean isSubClass = false;

				// Get all Super-Classes
				List<ResolvedReferenceType> ancestors = currentClass.resolve().getAllAncestors();

				for (ResolvedReferenceType ancestor : ancestors) {
					// Collect all Signatures of Super-Classes
					if (!ancestor.getQualifiedName().equals("java.lang.Object")) {
						classAncestors.add(ancestor.getQualifiedName());
					}

					// If Super-Class belongs to AST-Tree
					if (refactoring.getClasses().contains(ancestor.getQualifiedName())
							&& !refactoring.getClasses().contains(currentClass.resolve().getQualifiedName())) {
						// Add class to AST-Tree
						refactoring.getClasses().add(currentClass.resolve().getQualifiedName());
						isSubClass = true;
					}
				}

				// Add all Super-Classes of Class to AST-Tree
				if (isSubClass) {
					for (String classAncestor : classAncestors) {
						if (!refactoring.getClasses().contains(classAncestor)) {
							refactoring.getClasses().add(classAncestor);
						}
					}
				}
			}
		}
		
		return refactoring;
	}

	/**
	 * This method scanns all java files for method calls that need to be renamed.
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return
	 * @throws FileNotFoundException
	 */
	private ParserRefactoringNew findMethods(ParserRefactoringNew refactoring, List<String> allJavaFiles,
			String methodSignature) throws FileNotFoundException {

		// Iterate all Java-Files
		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

			// Get all Classes
			List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
			// Iterate all Classes
			for (ClassOrInterfaceDeclaration currentClass : classes) {

				// If class is sub/or superclass
				if (refactoring.getClasses().contains(currentClass.resolve().getQualifiedName())) {
					// Get all methods
					List<MethodDeclaration> methods = currentClass.getMethods();
					// Search methods
					for (MethodDeclaration method : methods) {
						if (method.getSignature().asString().equals(methodSignature)) {
							refactoring.addMethod(method);
							refactoring.addMethodSignature(getFullMethodSignature(method));
							if (!refactoring.getJavaFiles().contains(javaFile)) {
								refactoring.addJavaFile(javaFile);
							}
						}
					}
				}
			}
		}

		return refactoring;
	}

	/**
	 * This method scanns all java files for methods that need to be renamed.
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return
	 * @throws FileNotFoundException
	 */
	private ParserRefactoringNew findMethodCalls(ParserRefactoringNew refactoring, List<String> allJavaFiles)
			throws FileNotFoundException {

		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

			List<MethodCallExpr> methodCalls = compilationUnit.findAll(MethodCallExpr.class);

			for (MethodCallExpr methodCall : methodCalls) {
				ResolvedMethodDeclaration calledMethod = methodCall.resolve();
				if (refactoring.getMethodSignatures().contains(calledMethod.getQualifiedSignature())) {
					refactoring.addMethodCall(methodCall);

					if (!refactoring.getJavaFiles().contains(javaFile)) {
						refactoring.addJavaFile(javaFile);
					}
				}
			}

		}

		return refactoring;
	}

	/**
	 * This method returns all root-folders of java files (like the src folder or
	 * the src/main/java folder from maven projects)
	 * 
	 * @param allJavaFiles
	 * @param repoFolder
	 * @return javaRoots
	 * @throws FileNotFoundException
	 */
	public List<String> findJavaRoots(List<String> allJavaFiles, String repoFolder) throws FileNotFoundException {

		// Init roots list
		List<String> javaRoots = new ArrayList<>();

		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

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
				String rootPackage = null;

				if (packageDeclaration.getNameAsString().split("\\.").length == 1) {
					rootPackage = packageDeclaration.getNameAsString();
				} else {
					rootPackage = packageDeclaration.getNameAsString().split("\\.")[0];
				}

				// Get javafile
				File currentFile = new File(javaFile);

				// Until finding the root package
				while (!currentFile.isDirectory() || !currentFile.getName().equals(rootPackage)) {
					currentFile = currentFile.getParentFile();
				}

				// Add parent of rootPackage as java root
				if (!javaRoots.contains(currentFile.getParentFile().getAbsoluteFile().getAbsolutePath())) {
					javaRoots.add(currentFile.getParentFile().getAbsolutePath());
				}
			}

		}

		return javaRoots;
	}

	/**
	 * This method renames all findings of method declarations and method calls
	 * inside the java project.
	 * 
	 * @param allRefactorings
	 * @param allJavaFiles
	 * @param newName
	 * @throws FileNotFoundException
	 */
	private void renameFindings(ParserRefactoringNew refactoring, String newName) throws FileNotFoundException {

		for (String javaFile : refactoring.getJavaFiles()) {
			// Create compilation unit
			FileInputStream methodPath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(methodPath));

			// Get all Methods and MethodCalls of File
			List<MethodDeclaration> fileMethods = compilationUnit.findAll(MethodDeclaration.class);
			List<MethodCallExpr> fileMethodCalls = compilationUnit.findAll(MethodCallExpr.class);

			// Rename all Methods
			for (MethodDeclaration fileMethod : fileMethods) {
				if (refactoring.getMethods().contains(fileMethod)) {
					performRenameMethod(fileMethod, newName);
				}
			}

			// Rename all Method-Calls
			for (MethodCallExpr fileMethodCall : fileMethodCalls) {
				if (refactoring.getMethodCalls().contains(fileMethodCall)) {
					performRenameMethodCall(fileMethodCall, newName);
				}
			}

			// Save changes to file
			PrintWriter out = new PrintWriter(javaFile);
			out.println(LexicalPreservingPrinter.print(compilationUnit));
			out.close();
		}
	}

	/**
	 * This method returns the global signature of a method as a string.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @return
	 */
	private String getFullMethodSignature(MethodDeclaration methodDeclaration, Integer position) {
		// If method is at the refactored position
		if (position == methodDeclaration.getName().getBegin().get().line) {
			ResolvedMethodDeclaration resolvedMethod = methodDeclaration.resolve();
			return resolvedMethod.getQualifiedSignature();
		}
		return null;
	}

	/**
	 * This method returns the global signature of a method as a string.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @return
	 */
	private String getFullMethodSignature(MethodDeclaration methodDeclaration) {
		ResolvedMethodDeclaration resolvedMethod = methodDeclaration.resolve();
		return resolvedMethod.getQualifiedSignature();
	}

	/**
	 * This method returns the local signature of a method as a string.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @return
	 */
	private String getMethodDeclarationAsString(MethodDeclaration methodDeclaration, Integer position) {
		// If method is at the refactored position
		if (position == methodDeclaration.getName().getBegin().get().line) {
			return methodDeclaration.getSignature().asString();
		}
		return null;
	}

	/**
	 * This method performs the renaming of the method inside the local class.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @param newName
	 */
	private void performRenameMethod(MethodDeclaration methodDeclaration, String newName) {
		methodDeclaration.setName(newName);
	}

	/**
	 * This method performs the renaming of a method call.
	 * 
	 * @param methodCall
	 * @param newName
	 */
	private void performRenameMethodCall(MethodCallExpr methodCall, String newName) {
		methodCall.setName(newName);
	}
}
