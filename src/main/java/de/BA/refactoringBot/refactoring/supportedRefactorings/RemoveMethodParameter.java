package de.BA.refactoringBot.refactoring.supportedRefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.BA.refactoringBot.configuration.BotConfiguration;
import de.BA.refactoringBot.model.botIssue.BotIssue;
import de.BA.refactoringBot.model.configuration.GitConfiguration;
import de.BA.refactoringBot.model.javaparser.ParserRefactoring;
import de.BA.refactoringBot.model.javaparser.ParserRefactoringCollection;

/**
 * This refactoring class is used for removing unused parameters of methods of a
 * java project.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class RemoveMethodParameter {

	@Autowired
	BotConfiguration botConfig;

	/**
	 * This method performs the refactoring and returns the a commit message.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws IOException
	 */
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws IOException {

		// Init Refactorings
		ParserRefactoringCollection allRefactorings = new ParserRefactoringCollection();

		// Init all java files path-list
		List<String> allJavaFiles = new ArrayList<String>();

		// Init needed variables
		String issueFilePath = botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId() + "/"
				+ issue.getFilePath();
		String globalMethodSignature = null;
		String localMethodSignature = null;
		String methodClassSignature = null;
		String oldMethodName = null;
		Integer paramPosition = null;
		MethodDeclaration methodToRefactor = null;

		// Get root folder of project
		File dir = new File(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId() + "/"
				+ gitConfig.getProjectRootFolder());

		// Get paths to all java files of the project
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			if (file.getCanonicalPath().endsWith(".java")) {
				allJavaFiles.add(file.getCanonicalPath());
			}
		}

		// Configure solver for the project
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		typeSolver.add(new JavaParserTypeSolver(botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId()
				+ "/" + gitConfig.getProjectRootFolder() + "/src"));
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
				globalMethodSignature = getFullMethodSignature(method, issue.getLine(), issue.getRefactorString());
				localMethodSignature = getMethodDeclarationAsString(method, issue.getLine());
				oldMethodName = method.getNameAsString();
				// If it is
				if (globalMethodSignature != null) {
					// Set method and class signatures of the method
					methodToRefactor = method;
					paramPosition = getMethodParameterPosition(method, issue.getRefactorString());
					methodClassSignature = currentClass.resolve().getQualifiedName();
					foundMethod = true;
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
			return null;
		}

		// Add class to the TO-DO list
		allRefactorings.addToDoClass(methodClassSignature);

		// Create super tree recursively
		allRefactorings = getSuperTree(allRefactorings, allJavaFiles, issueFilePath);

		// Add sub tree recursively
		while (true) {

			// Count classes before adding subclasses
			int beforeSubtree = allRefactorings.getDoneClasses().size();

			// Add subclasses
			for (String javaFile : allJavaFiles) {
				allRefactorings = addSubTree(allRefactorings, allJavaFiles, javaFile);
			}

			// Count classes after adding subclasses
			int afterSubtree = allRefactorings.getDoneClasses().size();

			// If no more new subclasses found
			if (beforeSubtree == afterSubtree) {
				break;
			}
		}

		// Create refactoring objects for every class
		for (String classSignature : allRefactorings.getDoneClasses()) {
			for (String javaFile : allJavaFiles) {
				createRefactoringObjects(allRefactorings, localMethodSignature, classSignature, javaFile, allJavaFiles);
			}
		}

		removeParameter(allRefactorings, allJavaFiles, issue.getRefactorString(), paramPosition);

		return "Removed method parameter '" + issue.getRefactorString() + "' of method '" + oldMethodName + "'";
	}

	/**
	 * This method renames all findings of method declarations and method calls
	 * inside the java project.
	 * 
	 * @param allRefactorings
	 * @param allJavaFiles
	 * @param parameterName
	 * @param paramPosition
	 * @throws FileNotFoundException
	 */
	private void removeParameter(ParserRefactoringCollection allRefactorings, List<String> allJavaFiles,
			String parameterName, Integer paramPosition) throws FileNotFoundException {

		// Iterate all java files
		for (String javaFile : allJavaFiles) {
			// Create compilation unit
			FileInputStream methodPath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(methodPath));

			// for each refactoring
			for (ParserRefactoring refactoring : allRefactorings.getRefactoring()) {

				// if refactoring = method declaration
				if (refactoring.getMethod() != null) {
					List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);
					// Search all methods
					for (MethodDeclaration method : methods) {
						// If methods match
						if (method.equals(refactoring.getMethod())) {
							performRemoveMethodParameter(method, parameterName);
						}
					}
				}

				// If refactoring = method call
				if (refactoring.getMethodCall() != null) {
					List<MethodCallExpr> methodCalls = compilationUnit.findAll(MethodCallExpr.class);
					// Iterate method calls that need refactoring
					for (MethodCallExpr refExpr : refactoring.getMethodCall()) {
						// For each method call inside the file
						for (MethodCallExpr expr : methodCalls) {
							// If method calls match
							if (expr.equals(refExpr)) {
								performRemoveMethodCallParameter(expr, paramPosition);
							}
						}
					}
				}
			}

			// Save changes to file
			PrintWriter out = new PrintWriter(javaFile);
			out.println(LexicalPreservingPrinter.print(compilationUnit));
			out.close();
		}
	}

	/**
	 * This method gathers the super class tree from the class in which the method
	 * that needs to be refactored sits in.
	 * 
	 * @param allRefactorings
	 * @param allJavaFiles
	 * @param currentJavaFile
	 * @return
	 * @throws FileNotFoundException
	 */
	private ParserRefactoringCollection getSuperTree(ParserRefactoringCollection allRefactorings,
			List<String> allJavaFiles, String currentJavaFile) throws FileNotFoundException {

		// Init variable
		String classSignature = null;

		// parse a file
		FileInputStream filepath = new FileInputStream(currentJavaFile);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

		// Get all Classes
		List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);

		// Search all Classes
		for (ClassOrInterfaceDeclaration currentClass : classes) {
			// Check if class in todo list and not in done list
			if (allRefactorings.getToDoClasses().contains(currentClass.resolve().getQualifiedName())
					&& !allRefactorings.getDoneClasses().contains(currentClass.resolve().getQualifiedName())) {
				classSignature = currentClass.resolve().getQualifiedName();
				// Check all implements + extends classes/interfaces
				NodeList<ClassOrInterfaceType> impl = currentClass.getImplementedTypes();
				NodeList<ClassOrInterfaceType> ext = currentClass.getExtendedTypes();
				// Add all implements signatures to list
				for (int i = 0; i < impl.size(); i++) {
					allRefactorings.addToDoClass(impl.get(i).resolve().getQualifiedName());
				}
				// Add all extends signatures to list
				for (int i = 0; i < ext.size(); i++) {
					allRefactorings.addToDoClass(ext.get(i).resolve().getQualifiedName());
				}
			}
		}

		// If class not super of previous class
		if (classSignature == null) {
			return allRefactorings;
		}

		// Mark class as done
		allRefactorings.addDoneClass(classSignature);
		allRefactorings.removeToDoClass(classSignature);

		// If super class tree finished
		if (allRefactorings.getToDoClasses().isEmpty()) {
			return allRefactorings;
		}

		// Recursively build super class tree
		for (String javaFile : allJavaFiles) {
			allRefactorings = getSuperTree(allRefactorings, allJavaFiles, javaFile);
		}

		return allRefactorings;
	}

	/**
	 * This method adds all subclasses to the already created super class tree.
	 * 
	 * @param allRefactorings
	 * @param allJavaFiles
	 * @param currentJavaFile
	 * @return
	 * @throws FileNotFoundException
	 */
	private ParserRefactoringCollection addSubTree(ParserRefactoringCollection allRefactorings,
			List<String> allJavaFiles, String currentJavaFile) throws FileNotFoundException {
		// parse a file
		FileInputStream filepath = new FileInputStream(currentJavaFile);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

		// Get all classes
		List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);

		// Search all classes
		for (ClassOrInterfaceDeclaration currentClass : classes) {
			// Check if class already marked as done
			if (!allRefactorings.getDoneClasses().contains(currentClass.resolve().getQualifiedName())) {
				// Check all implements + extends classes/interfaces
				NodeList<ClassOrInterfaceType> impl = currentClass.getImplementedTypes();
				NodeList<ClassOrInterfaceType> ext = currentClass.getExtendedTypes();
				// If class implements of one of the done classes
				for (int i = 0; i < impl.size(); i++) {
					if (allRefactorings.getDoneClasses().contains(impl.get(i).resolve().getQualifiedName())) {
						allRefactorings.addToDoClass(currentClass.resolve().getQualifiedName());
					}
				}
				// If class extends of one of the done classes
				for (int i = 0; i < ext.size(); i++) {
					if (allRefactorings.getDoneClasses().contains(ext.get(i).resolve().getQualifiedName())) {
						allRefactorings.addToDoClass(currentClass.resolve().getQualifiedName());
					}
				}
			}
		}

		// Recursively build super class tree
		for (String javaFile : allJavaFiles) {
			allRefactorings = getSuperTree(allRefactorings, allJavaFiles, javaFile);
		}

		return allRefactorings;
	}

	/**
	 * This method creates refactoring objects to rename a method inside a specific
	 * class and to rename all method calls for that specific method inside the
	 * specific class.
	 * 
	 * @param allRefactorings
	 * @param methodSignature
	 * @param classSignature
	 * @param javaFile
	 * @param allJavaFiles
	 * @return
	 * @throws FileNotFoundException
	 */
	private ParserRefactoringCollection createRefactoringObjects(ParserRefactoringCollection allRefactorings,
			String methodSignature, String classSignature, String javaFile, List<String> allJavaFiles)
			throws FileNotFoundException {

		// parse a file
		FileInputStream filepath = new FileInputStream(javaFile);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

		// Get all classes
		List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);

		// Search all classes
		for (ClassOrInterfaceDeclaration currentClass : classes) {
			if (classSignature.equals(currentClass.resolve().getQualifiedName())) {
				// Get all methods
				List<MethodDeclaration> methods = currentClass.getMethods();
				// Search methods
				for (MethodDeclaration method : methods) {
					// If method found
					if (methodSignature.equals(getMethodDeclarationAsString(method))) {
						// Get global Signature of method
						String globalMethodSignature = getFullMethodSignature(method);

						// Check all java files and create refactorings for method calls
						for (String file : allJavaFiles) {
							allRefactorings = createRefactoringsForMethodCalls(allRefactorings, file,
									globalMethodSignature);
						}

						// Create refactoring for method
						ParserRefactoring methodRefactoring = new ParserRefactoring();
						methodRefactoring.setJavaFile(javaFile);
						methodRefactoring.setMethod(method);
						methodRefactoring.setUnit(compilationUnit);
						allRefactorings.getRefactoring().add(methodRefactoring);
					}
				}
			}
		}

		return allRefactorings;
	}

	/**
	 * This method reads a java file, performs a refactor and saves the changes to
	 * the file.
	 * 
	 * @param list
	 * 
	 * @param javafile
	 * @param methodSignature
	 * @throws FileNotFoundException
	 */
	private ParserRefactoringCollection createRefactoringsForMethodCalls(ParserRefactoringCollection refactorings,
			String javafile, String methodSignature) throws FileNotFoundException {
		// parse a file
		FileInputStream callMethodPath = new FileInputStream(javafile);
		CompilationUnit renameMethodCallUnit = LexicalPreservingPrinter.setup(JavaParser.parse(callMethodPath));

		List<MethodCallExpr> methodCalls = renameMethodCallUnit.findAll(MethodCallExpr.class);

		// Create refactoring
		ParserRefactoring refactoring = new ParserRefactoring();
		List<MethodCallExpr> validCalls = new ArrayList<MethodCallExpr>();

		// Rename all suitable method calls
		for (MethodCallExpr methodCall : methodCalls) {
			// Check if call invokes the refactoring method
			if (checkMethodCall(methodCall, methodSignature) != null) {
				validCalls.add(checkMethodCall(methodCall, methodSignature));
			}
		}

		// Fill refactoring with data
		refactoring.setJavaFile(javafile);
		refactoring.setUnit(renameMethodCallUnit);

		// If no valid call found
		if (validCalls.isEmpty()) {
			return refactorings;
		}

		refactoring.setMethodCall(validCalls);
		refactorings.addRefactoring(refactoring);

		return refactorings;
	}

	/**
	 * This method renames all suitable method calls with the help of the method
	 * signature of the renamed method.
	 * 
	 * @param methodCall
	 * @param newName
	 */
	private MethodCallExpr checkMethodCall(MethodCallExpr methodCall, String globalMethodSignature) {
		// Resolve method call
		ResolvedMethodDeclaration calledMethod = methodCall.resolve();
		// If call belongs to the refactored method
		if (calledMethod.getQualifiedSignature().equals(globalMethodSignature)) {
			// return methodcall
			return methodCall;
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
	private String getFullMethodSignature(MethodDeclaration methodDeclaration, Integer position, String parameterName) {
		// If method is at the refactored position and parameter exists
		if (position == methodDeclaration.getName().getBegin().get().line
				&& methodDeclaration.getParameterByName(parameterName).isPresent()) {
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
	 * This method returns the position of the method parameter.
	 * 
	 * @param methodDeclaration
	 * @param parameterName
	 * @return position
	 */
	private Integer getMethodParameterPosition(MethodDeclaration methodDeclaration, String parameterName) {
		// Get parameters of method
		NodeList<Parameter> parameters = methodDeclaration.getParameters();
		// Search them
		for (int i = 0; i < parameters.size(); i++) {
			// If parameter found (needs to be since it was checked first)
			if (parameters.get(i).getName().asString().equals(parameterName)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * This method returns the local signature of a method as a string.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @return
	 */
	private String getMethodDeclarationAsString(MethodDeclaration methodDeclaration) {
		return methodDeclaration.getSignature().asString();
	}

	/**
	 * This method performs the removing of a specific parameter of the method.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @param parameterName
	 */
	private void performRemoveMethodParameter(MethodDeclaration methodDeclaration, String parameterName) {
		methodDeclaration.getParameterByName(parameterName).get().remove();
	}

	/**
	 * This method performs the renaming of a method call.
	 * 
	 * @param methodCall
	 * @param paramPosition
	 */
	private void performRemoveMethodCallParameter(MethodCallExpr methodCall, Integer paramPosition) {
		methodCall.getArgument(paramPosition).remove();
	}
}