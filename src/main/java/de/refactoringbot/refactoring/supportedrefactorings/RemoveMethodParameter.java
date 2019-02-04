package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.InvalidPathException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.javaparser.ParserRefactoring;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.RefactoringImpl;

public class RemoveMethodParameter implements RefactoringImpl {

	private static final Logger logger = LoggerFactory.getLogger(RemoveMethodParameter.class);

	// -
	// -
	// Sketch of new implementation
	// -
	// -

	public String performRefactoringN(BotIssue issue, GitConfiguration gitConfig) throws Exception {
		configureJavaParserForProject(issue);

		String parameterName = issue.getRefactorString();
		String issueFilePath = gitConfig.getRepoFolder() + File.separator + issue.getFilePath();
		ClassOrInterfaceDeclaration targetClassOrInterface = getFirstClassOrIntefaceDeclarationFromFile(issueFilePath);
		MethodDeclaration targetMethod = findAndValidateTargetMethod(issue, parameterName, targetClassOrInterface);

		HashSet<String> javaFilesRelevantForRefactoring = findJavaFilesRelevantForRefactoring(issue, parameterName,
				targetMethod);
		removeParameterFromRelatedMethodDeclarationsAndMethodCalls(javaFilesRelevantForRefactoring, targetMethod,
				parameterName);

		String targetMethodSignature = RefactoringHelper.getMethodSignatureAsString(targetMethod);
		return "Removed method parameter '" + parameterName + "' of method '" + targetMethodSignature + "'";
	}

	/**
	 * Tries to find the target method in the given class or interface. Checks if
	 * the found method itself could be refactored, without yet checking other code
	 * locations
	 * 
	 * @param issue
	 * @param parameterToBeRemoved
	 * @param targetClassOrInterface
	 * @return
	 * @throws BotRefactoringException
	 */
	private MethodDeclaration findAndValidateTargetMethod(BotIssue issue, String parameterToBeRemoved,
			ClassOrInterfaceDeclaration targetClassOrInterface) throws BotRefactoringException {
		MethodDeclaration targetMethod = null;
		for (MethodDeclaration currentMethod : targetClassOrInterface.getMethods()) {
			if (RefactoringHelper.isMethodDeclarationAtLine(currentMethod, issue.getLine())) {
				targetMethod = currentMethod;
				break;
			}
		}

		if (targetMethod == null) {
			throw new BotRefactoringException("Could not find specified method! Automated refactoring failed.");
		}
		validateMethodHasParameter(targetMethod, parameterToBeRemoved);
		validateParameterUnused(targetMethod, parameterToBeRemoved);
		return targetMethod;
	}

	/**
	 * Finds all Java files that are relevant for refactoring, i.e. those that
	 * contain method declarations or method calls that refer to the target method.
	 * Validates if the related declarations and calls can be refactored.
	 * 
	 * @param issue
	 * @param parameterToBeRemoved
	 * @param targetMethod
	 * @return
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private HashSet<String> findJavaFilesRelevantForRefactoring(BotIssue issue, String parameterToBeRemoved,
			MethodDeclaration targetMethod) throws FileNotFoundException, BotRefactoringException {
		HashSet<String> javaFilesRelevantForRefactoring = new HashSet<>();
		String postRefactoringSignature = getPostRefactoringSignature(targetMethod, parameterToBeRemoved);

		for (String currentFilePath : issue.getAllJavaFiles()) {
			List<ClassOrInterfaceDeclaration> classesAndInterfacesInCurrentFile = getAllClassOrInterfaceDeclarationsFromFile(
					currentFilePath);
			boolean isClassRelatedToTargetClass = (isDescendantOfTargetClass() && isAncestorOfTargetClass());
			if (isClassRelatedToTargetClass) {
				List<MethodDeclaration> relatedMethodDeclarations = RefactoringHelper
						.findAllMethodDeclarationsWithEqualMethodSignature(classesAndInterfacesInCurrentFile,
								RefactoringHelper.getMethodSignatureAsString(targetMethod));
				boolean containsMethodDeclarationWithEqualSignatureAsTargetMethod = !relatedMethodDeclarations
						.isEmpty();
				if (containsMethodDeclarationWithEqualSignatureAsTargetMethod) {
					validateParameterUnusedInRelatedMethods(relatedMethodDeclarations, parameterToBeRemoved);
					validateFileNotContainsPostRefactoringSignatureAlready(currentFilePath, postRefactoringSignature);
					javaFilesRelevantForRefactoring.add(currentFilePath);
					continue;
				}
			}
			if (containsTargetMethodCall(classesAndInterfacesInCurrentFile, targetMethod)) {
				javaFilesRelevantForRefactoring.add(currentFilePath);
			}
		}
		return javaFilesRelevantForRefactoring;
	}

	private String getPostRefactoringSignature(MethodDeclaration methodDeclaration, String parameterName) {
		// TODO
		return "";
	}
	
	/**
	 * Check if post refactoring signature already exists in given file
	 * 
	 * @param filePath
	 * @param postRefactoringSignature
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private void validateFileNotContainsPostRefactoringSignatureAlready(String filePath,
			String postRefactoringSignature) throws FileNotFoundException, BotRefactoringException {
		if (RefactoringHelper.isMethodSignaturePresentInFile(filePath, postRefactoringSignature)) {
			throw new BotRefactoringException(
					"Removal of parameter would result in method signature already present in file '" + filePath
							+ "'.");
		}
	}

	/**
	 * Removes the parameter from all relevant method declarations and method calls
	 * in the given java files
	 * 
	 * @param javaFilesRelevantForRefactoring
	 * @param targetMethod
	 * @param parameterName
	 * @throws FileNotFoundException
	 */
	private void removeParameterFromRelatedMethodDeclarationsAndMethodCalls(
			HashSet<String> javaFilesRelevantForRefactoring, MethodDeclaration targetMethod, String parameterName)
			throws FileNotFoundException {
		Integer parameterPosition = getMethodParameterPosition(targetMethod, parameterName);
		for (String currentFilePath : javaFilesRelevantForRefactoring) {
			FileInputStream is = new FileInputStream(currentFilePath);
			CompilationUnit cu = LexicalPreservingPrinter.setup(JavaParser.parse(is));

			List<MethodDeclaration> methodDeclarations = cu.findAll(MethodDeclaration.class);
			List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class);

			// remove parameter from all relevant method declarations
			boolean isClassRelatedToTargetClass = (isDescendantOfTargetClass() && isAncestorOfTargetClass());
			boolean isTargetClass = true; // TODO
			if (isClassRelatedToTargetClass || isTargetClass) {
				for (MethodDeclaration fileMethod : methodDeclarations) {
					if (RefactoringHelper.getMethodSignatureAsString(fileMethod)
							.equals(RefactoringHelper.getMethodSignatureAsString(targetMethod))) {
						performRemoveMethodParameter(fileMethod, parameterName);
						removeParamFromJavadoc(fileMethod, parameterName);
					}
				}
			}

			// remove argument from all target method calls
			for (MethodCallExpr fileMethodCall : methodCalls) {
				if (isTargetMethodCall(fileMethodCall, targetMethod)) {
					performRemoveMethodCallParameter(fileMethodCall, parameterPosition);
				}
			}

			PrintWriter out = new PrintWriter(currentFilePath);
			out.println(LexicalPreservingPrinter.print(cu));
			out.close();
		}
	}

	private boolean isDescendantOfTargetClass() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isAncestorOfTargetClass() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param classesAndInterfaces
	 * @param targetMethod
	 * @return true if given classes and interfaces contain at least one call
	 *         expression to the given target method, false otherwise
	 */
	private boolean containsTargetMethodCall(List<ClassOrInterfaceDeclaration> classesAndInterfaces,
			MethodDeclaration targetMethod) {
		for (ClassOrInterfaceDeclaration classOrInterface : classesAndInterfaces) {
			List<MethodCallExpr> methodCalls = classOrInterface.findAll(MethodCallExpr.class);
			for (MethodCallExpr methodCall : methodCalls) {
				if (isTargetMethodCall(methodCall, targetMethod)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param methodCall
	 * @param targetMethod
	 * @return true if given method call is related to target method, false
	 *         otherwise
	 */
	private boolean isTargetMethodCall(MethodCallExpr methodCall, MethodDeclaration targetMethod) {
		ResolvedMethodDeclaration calledMethod = null;
		try {
			calledMethod = methodCall.resolve();
		} catch (Exception e) {
			logger.debug(e.getMessage());
			return false;
		}

		return RefactoringHelper.getQualifiedMethodSignatureAsString(targetMethod)
				.equals(calledMethod.getQualifiedSignature());
	}

	// -
	// -
	// End sketch of new implementation
	// -
	// -

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
		configureJavaParserForProject(issue);

		ParserRefactoring refactoring = new ParserRefactoring();
		MethodDeclaration methodToRefactor = null;
		String parameterToBeRemoved = issue.getRefactorString();

		String issueFilePath = gitConfig.getRepoFolder() + File.separator + issue.getFilePath();
		ClassOrInterfaceDeclaration classOrInterfaceWithCodeSmell = getFirstClassOrIntefaceDeclarationFromFile(
				issueFilePath);

		// find method to refactor in the class with code smell
		for (MethodDeclaration currentMethod : classOrInterfaceWithCodeSmell.getMethods()) {
			if (RefactoringHelper.isMethodDeclarationAtLine(currentMethod, issue.getLine())) {
				methodToRefactor = currentMethod;
				break;
			}
		}

		// validate found method
		if (methodToRefactor == null) {
			throw new BotRefactoringException("Could not find specified method! Automated refactoring failed.");
		}
		validateMethodHasParameter(methodToRefactor, parameterToBeRemoved);
		validateParameterUnused(methodToRefactor, parameterToBeRemoved);

		// find all classes potentially relevant for refactoring
		refactoring.addClass(classOrInterfaceWithCodeSmell.resolve().getQualifiedName());
		findAndAddSuperClassesToParserRefactoring(refactoring, classOrInterfaceWithCodeSmell);

		// add all sub classes and their super classes and repeat until no more classes
		// added. Sub classes might have sub classes again, which is why we need the
		// loop
		while (true) {
			int before = refactoring.getClasses().size();
			refactoring = RefactoringHelper.addSubClasses(refactoring, issue.getAllJavaFiles());
			int after = refactoring.getClasses().size();
			// break if all classes found
			if (before == after) {
				break;
			}
		}

		// find all methods and method calls for renaming
		String localMethodSignature = RefactoringHelper.getMethodSignatureAsString(methodToRefactor);
		refactoring = RefactoringHelper.findAndAddMethods(refactoring, issue.getAllJavaFiles(), localMethodSignature);
		refactoring = RefactoringHelper.findAndAddMethodCalls(refactoring, issue.getAllJavaFiles());

		// validate related methods
		validateParameterUnusedInRelatedMethods(refactoring.getMethods(), parameterToBeRemoved);
		validateNoDuplicatedMethodSignatureExistAfterRefactoring(refactoring, parameterToBeRemoved);

		Integer paramPosition = getMethodParameterPosition(methodToRefactor, parameterToBeRemoved);
		removeParameterFromAllRelatedMethodsAndMethodCalls(refactoring, parameterToBeRemoved, paramPosition);

		return "Removed method parameter '" + parameterToBeRemoved + "' of method '" + methodToRefactor.getSignature()
				+ "'";
	}

	/**
	 * Validates that no duplicated method signatures exist after refactoring. This
	 * could be the case, for example, if methods were overloaded before
	 * refactoring.
	 * 
	 * @param refactoring
	 * @param parameterToBeRemoved
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private void validateNoDuplicatedMethodSignatureExistAfterRefactoring(ParserRefactoring refactoring,
			String parameterToBeRemoved) throws FileNotFoundException, BotRefactoringException {
		String postRefactoringSignature = getPostRefactoringSignature(refactoring, parameterToBeRemoved);
		RefactoringHelper.checkForDuplicatedMethodSignatures(refactoring.getJavaFiles(), postRefactoringSignature);
	}

	private void findAndAddSuperClassesToParserRefactoring(ParserRefactoring refactoring,
			ClassOrInterfaceDeclaration classOrInterfaceWithCodeSmell) throws BotRefactoringException {
		List<ResolvedReferenceType> ancestors = null;
		try {
			ancestors = classOrInterfaceWithCodeSmell.resolve().getAllAncestors();
		} catch (UnsolvedSymbolException u) {
			ancestors = RefactoringHelper.getAllAncestors(classOrInterfaceWithCodeSmell.resolve());
			refactoring.setWarning(
					" Refactored classes might extend/implement external project! Check if overriden method was NOT renamed!");
		} catch (InvalidPathException i) {
			throw new BotRefactoringException("Javaparser could not parse file: " + i.getMessage());
		} catch (Exception e) {
			throw new BotRefactoringException("Error while resolving superclasses occured!");
		}

		for (ResolvedReferenceType ancestor : ancestors) {
			if (!refactoring.getClasses().contains(ancestor.getQualifiedName())
					&& !ancestor.getQualifiedName().equals("java.lang.Object")) {
				refactoring.addClass(ancestor.getQualifiedName());
			}
		}
	}

	private void configureJavaParserForProject(BotIssue issue) {
		// Configure solver for the project
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		// Add java-roots
		for (String javaRoot : issue.getJavaRoots()) {
			typeSolver.add(new JavaParserTypeSolver(javaRoot));
		}
		typeSolver.add(new ReflectionTypeSolver());
		JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser.getStaticConfiguration().setSymbolResolver(javaSymbolSolver);
	}

	private ClassOrInterfaceDeclaration getFirstClassOrIntefaceDeclarationFromFile(String filePath)
			throws BotRefactoringException, FileNotFoundException {
		FileInputStream is = new FileInputStream(filePath);
		CompilationUnit cu = LexicalPreservingPrinter.setup(JavaParser.parse(is));

		Optional<ClassOrInterfaceDeclaration> classOrInterface = cu.findFirst(ClassOrInterfaceDeclaration.class);
		if (classOrInterface.isPresent()) {
			return classOrInterface.get();
		} else {
			throw new BotRefactoringException(
					"File path probably not pointing to a file with class or interface in it.");
		}
	}

	private List<ClassOrInterfaceDeclaration> getAllClassOrInterfaceDeclarationsFromFile(String filePath)
			throws FileNotFoundException {
		FileInputStream is = new FileInputStream(filePath);
		CompilationUnit cu = LexicalPreservingPrinter.setup(JavaParser.parse(is));

		return cu.findAll(ClassOrInterfaceDeclaration.class);
	}

	/**
	 * 
	 * @param methodDeclaration
	 * @param parameterName
	 * @throws BotRefactoringException
	 */
	private void validateMethodHasParameter(MethodDeclaration methodDeclaration, String parameterName)
			throws BotRefactoringException {
		if (!methodDeclaration.getParameterByName(parameterName).isPresent()) {
			throw new BotRefactoringException("Method '" + methodDeclaration.getSignature()
					+ "' does not have parameter '" + parameterName + "'!");
		}
	}

	/**
	 * Validates that the given parameter is not used inside the given method
	 * 
	 * @param methodDeclaration
	 * @param parameterName
	 * @throws BotRefactoringException
	 */
	private void validateParameterUnused(MethodDeclaration methodDeclaration, String parameterName)
			throws BotRefactoringException {
		if (isParameterUsed(methodDeclaration, parameterName)) {
			String qualifiedMethodName = getQualifiedMethodSignatureAsString(methodDeclaration);
			throw new BotRefactoringException(
					"Parameter '" + parameterName + "' is used inside the method '" + qualifiedMethodName + "'!");
		}
	}

	/**
	 * Validates that the given parameter is not used inside one of the given
	 * methods of related classes (e.g. sub classes)
	 * 
	 * @param methodDeclarations
	 * @param parameterToBeRemoved
	 * @throws BotRefactoringException
	 */
	private void validateParameterUnusedInRelatedMethods(List<MethodDeclaration> methodDeclarations,
			String parameterToBeRemoved) throws BotRefactoringException {
		for (MethodDeclaration methodDeclaration : methodDeclarations) {
			if (isParameterUsed(methodDeclaration, parameterToBeRemoved)) {
				throw new BotRefactoringException("Parameter '" + parameterToBeRemoved + "' is used inside the method '"
						+ RefactoringHelper.getQualifiedMethodSignatureAsString(methodDeclaration)
						+ "' which is a super/sub class of the given method!");
			}
		}
	}

	/**
	 * @param method
	 * @param paramName
	 * @return true if given parameter name is present in given method, false
	 *         otherwise
	 */
	private boolean isParameterUsed(MethodDeclaration method, String paramName) {
		Optional<BlockStmt> methodBody = method.getBody();
		if (methodBody.isPresent()) {
			List<NameExpr> expressions = methodBody.get().findAll(NameExpr.class);
			for (NameExpr expression : expressions) {
				if (expression.getNameAsString().equals(paramName)) {
					return true;
				}
			}
		}

		return false;
	}

	private String getQualifiedMethodSignatureAsString(MethodDeclaration methodDeclaration)
			throws BotRefactoringException {
		try {
			ResolvedMethodDeclaration resolvedMethod = methodDeclaration.resolve();
			return resolvedMethod.getQualifiedSignature();
		} catch (Exception e) {
			throw new BotRefactoringException("Method '" + methodDeclaration.getSignature().asString()
					+ "' can't be resolved. It might have parameters from external projects/libraries or method might be inside a class that extends a generic class! Error: "
					+ e);
		}
	}

	/**
	 * This method gets the Method signature that our methods will have without the
	 * parameter.
	 * 
	 * @param refactoring
	 * @param paramName
	 * @return signature
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private String getPostRefactoringSignature(ParserRefactoring refactoring, String paramName)
			throws FileNotFoundException, BotRefactoringException {
		for (String javaFile : refactoring.getJavaFiles()) {
			// Create compilation unit
			FileInputStream methodPath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(methodPath));

			// Get all Methods and MethodCalls of File
			List<MethodDeclaration> fileMethods = compilationUnit.findAll(MethodDeclaration.class);

			// Rename all Methods
			for (MethodDeclaration fileMethod : fileMethods) {
				if (refactoring.getMethods().contains(fileMethod)) {
					performRemoveMethodParameter(fileMethod, paramName);
					return RefactoringHelper.getMethodSignatureAsString(fileMethod);
				}
			}

		}
		throw new BotRefactoringException("Error reading methods that need their parameter removed!");
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
	 * This method removes the parameter from all methods or method calls inside the
	 * entire project.
	 * 
	 * @param refactoring
	 * @param paramName
	 * @throws FileNotFoundException
	 */
	private void removeParameterFromAllRelatedMethodsAndMethodCalls(ParserRefactoring refactoring, String paramName,
			Integer paramPosition) throws FileNotFoundException {

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
					performRemoveMethodParameter(fileMethod, paramName);
					removeParamFromJavadoc(fileMethod, paramName);
				}
			}

			// Rename all Method-Calls
			for (MethodCallExpr fileMethodCall : fileMethodCalls) {
				if (refactoring.getMethodCalls().contains(fileMethodCall)) {
					performRemoveMethodCallParameter(fileMethodCall, paramPosition);
				}
			}

			// Save changes to file
			PrintWriter out = new PrintWriter(javaFile);
			out.println(LexicalPreservingPrinter.print(compilationUnit));
			out.close();
		}
	}

	/**
	 * Removes the parameter with the given name from the given method declaration
	 * 
	 * @param methodDeclaration
	 * @param parameterName
	 */
	private void performRemoveMethodParameter(MethodDeclaration methodDeclaration, String parameterName) {
		Optional<Parameter> parameterToBeRemoved = methodDeclaration.getParameterByName(parameterName);
		if (parameterToBeRemoved.isPresent()) {
			parameterToBeRemoved.get().remove();
		}
	}

	/**
	 * Removes the parameter with the given name from the javadoc of the given
	 * method declaration
	 * 
	 * @param methodDeclaration
	 * @param paramName
	 */
	private void removeParamFromJavadoc(MethodDeclaration methodDeclaration, String paramName) {
		Optional<Javadoc> javadoc = methodDeclaration.getJavadoc();
		if (javadoc.isPresent()) {
			Javadoc methodJavadoc = javadoc.get();
			List<JavadocBlockTag> javadocTags = methodJavadoc.getBlockTags();
			List<JavadocBlockTag> tagsToDelete = new LinkedList<>();

			// find and remove all tags equal to paramName
			for (JavadocBlockTag javadocTag : javadocTags) {
				if (javadocTag.getTagName().equals("param") && javadocTag.getName().isPresent()
						&& javadocTag.getName().get().equals(paramName)) {
					tagsToDelete.add(javadocTag);
				}
			}

			for (JavadocBlockTag javadocTag : tagsToDelete) {
				javadocTags.remove(javadocTag);
			}

			// create new javadoc with remaining javadoc tags because there is no way to
			// remove individual tags directly
			Javadoc newJavadoc = new Javadoc(methodJavadoc.getDescription());
			for (JavadocBlockTag blockTag : javadocTags) {
				newJavadoc.addBlockTag(blockTag);
			}

			methodDeclaration.setJavadocComment(newJavadoc);
		}
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
