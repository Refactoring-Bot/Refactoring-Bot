package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.StaticJavaParser;
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
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.RefactoringImpl;

/**
 * Refactoring to remove an unused method parameter
 */
public class RemoveMethodParameter implements RefactoringImpl {

	private static final Logger logger = LoggerFactory.getLogger(RemoveMethodParameter.class);

	/**
	 * List of method declarations which are related to the target method, i.e.
	 * related methods in child/parent/sibling classes. These methods need to be
	 * refactored.
	 */
	private List<MethodDeclaration> allRefactoringRelevantMethodDeclarations = new ArrayList<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws Exception {
		configureJavaParserForProject(issue);

		String parameterName = issue.getRefactorString();
		String issueFilePath = gitConfig.getRepoFolder() + File.separator + issue.getFilePath();
		MethodDeclaration targetMethod = findAndValidateTargetMethod(issue, issueFilePath, parameterName);
		ClassOrInterfaceDeclaration targetClass = RefactoringHelper.getClassOrInterfaceOfMethod(targetMethod);
		Set<String> qualifiedNamesOfRelatedClassesAndInterfaces = RefactoringHelper
				.findRelatedClassesAndInterfaces(issue.getAllJavaFiles(), targetClass, targetMethod);

		HashSet<String> javaFilesRelevantForRefactoring = findRelevantJavaFiles(issue, parameterName,
				targetMethod, qualifiedNamesOfRelatedClassesAndInterfaces);
		removeParameterFromRelatedMethodDeclarationsAndMethodCalls(javaFilesRelevantForRefactoring, targetMethod,
				parameterName);

		String targetMethodSignature = RefactoringHelper.getLocalMethodSignatureAsString(targetMethod);
		return "Removed parameter '" + parameterName + "' from method '" + targetMethodSignature + "'";
	}

	/**
	 * Tries to find the target method in the given class or interface. Checks if
	 * the found method itself could be refactored, without yet checking other code
	 * locations
	 * 
	 * @param issue
	 * @param filePath
	 * @param parameterToBeRemoved
	 * @return
	 * @throws BotRefactoringException
	 * @throws FileNotFoundException
	 */
	private MethodDeclaration findAndValidateTargetMethod(BotIssue issue, String filePath, String parameterToBeRemoved)
			throws BotRefactoringException, FileNotFoundException {
		List<ClassOrInterfaceDeclaration> classesAndInterfaces = RefactoringHelper
				.getAllClassesAndInterfacesFromFile(filePath);

		MethodDeclaration targetMethod = null;
		for (ClassOrInterfaceDeclaration classOrInterface : classesAndInterfaces) {
			for (MethodDeclaration currentMethod : classOrInterface.getMethods()) {
				if (RefactoringHelper.isMethodDeclarationAtLine(currentMethod, issue.getLine())) {
					targetMethod = currentMethod;
					break;
				}
			}
			if (targetMethod != null) {
				break;
			}
		}

		if (targetMethod == null) {
			throw new BotRefactoringException("Could not find a method declaration at the given line!");
		}
		validateMethodHasParameter(targetMethod, parameterToBeRemoved);
		validateParameterUnused(targetMethod, parameterToBeRemoved);
		return targetMethod;
	}

	/**
	 * Validates that the given parameter is present in the method signature
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
			String qualifiedMethodName = RefactoringHelper.getQualifiedMethodSignatureAsString(methodDeclaration);
			throw new BotRefactoringException("Parameter '" + parameterName + "' is used in method '"
					+ qualifiedMethodName + "' and therefore cannot be removed automatically.");
		}
	}

	/**
	 * Finds all Java files that are relevant for refactoring, i.e. those that
	 * contain method declarations or method calls that refer to the target method.
	 * Validates if the related declarations and calls can be refactored.
	 * 
	 * @param issue
	 * @param parameterToBeRemoved
	 * @param targetMethod
	 * @param qualifiedNamesOfRelatedClassesAndInterfaces
	 * @return
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private HashSet<String> findRelevantJavaFiles(BotIssue issue, String parameterToBeRemoved,
			MethodDeclaration targetMethod, Set<String> qualifiedNamesOfRelatedClassesAndInterfaces)
			throws FileNotFoundException, BotRefactoringException {
		HashSet<String> javaFilesRelevantForRefactoring = new HashSet<>();
		String postRefactoringSignature = getPostRefactoringSignature(targetMethod, parameterToBeRemoved);

		for (String currentFilePath : issue.getAllJavaFiles()) {
			List<ClassOrInterfaceDeclaration> classesAndInterfacesInCurrentFile = RefactoringHelper
					.getAllClassesAndInterfacesFromFile(currentFilePath);

			// search for files containing relevant method declarations
			for (ClassOrInterfaceDeclaration currentClassOrInterface : classesAndInterfacesInCurrentFile) {
				if (isRelatedToTargetClass(currentClassOrInterface, qualifiedNamesOfRelatedClassesAndInterfaces)) {
					List<MethodDeclaration> methodDeclarationsInCurrentClass = currentClassOrInterface.getMethods();
					for (MethodDeclaration methodDeclaration : methodDeclarationsInCurrentClass) {
						boolean localMethodSignatureIsEqual = RefactoringHelper
								.getLocalMethodSignatureAsString(methodDeclaration)
								.equals(RefactoringHelper.getLocalMethodSignatureAsString(targetMethod));
						if (localMethodSignatureIsEqual) {
							validateParameterUnused(methodDeclaration, parameterToBeRemoved);
							validatePostRefactoringSignatureNotAlreadyExists(currentClassOrInterface,
									postRefactoringSignature);
							javaFilesRelevantForRefactoring.add(currentFilePath);
							allRefactoringRelevantMethodDeclarations.add(methodDeclaration);
							break;
						}
					}
				}
			}
		}

		// search for files containing relevant method calls
		// we had to first find all relevant target methods in order to find all method
		// calls that need to be refactored. This is why we need to iterate a second
		// time through all files
		for (String currentFilePath : issue.getAllJavaFiles()) {
			if (javaFilesRelevantForRefactoring.contains(currentFilePath)) {
				continue;
			}
			List<ClassOrInterfaceDeclaration> classesAndInterfacesInCurrentFile = RefactoringHelper
					.getAllClassesAndInterfacesFromFile(currentFilePath);
			if (containsTargetMethodCall(classesAndInterfacesInCurrentFile)) {
				javaFilesRelevantForRefactoring.add(currentFilePath);
			}
		}

		return javaFilesRelevantForRefactoring;
	}

	/**
	 * @param methodDeclaration
	 * @param parameterName
	 * @return signature of the given method declaration after parameter would have
	 *         been removed
	 */
	private String getPostRefactoringSignature(MethodDeclaration methodDeclaration, String parameterName) {
		MethodDeclaration copy = methodDeclaration.clone();
		removeMethodParameter(copy, parameterName);
		return RefactoringHelper.getLocalMethodSignatureAsString(copy);
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

	/**
	 * Check if post refactoring signature already exists in given class or
	 * interface
	 * 
	 * @param currentClassOrInterface
	 * @param postRefactoringSignature
	 * @throws BotRefactoringException
	 */
	private void validatePostRefactoringSignatureNotAlreadyExists(ClassOrInterfaceDeclaration currentClassOrInterface,
			String postRefactoringSignature) throws BotRefactoringException {
		if (RefactoringHelper.isLocalMethodSignatureInClassOrInterface(currentClassOrInterface,
				postRefactoringSignature)) {
			throw new BotRefactoringException(
					"Removal of parameter would result in a method signature that is already present inside the class or interface '"
							+ currentClassOrInterface.getNameAsString() + "'.");
		}
	}

	/**
	 * Removes the parameter from all relevant method declarations and method calls
	 * in the given java files
	 * 
	 * @param javaFilesRelevantForRefactoring
	 * @param issueFilePath
	 * @param targetMethod
	 * @param parameterName
	 * @throws FileNotFoundException
	 */
	private void removeParameterFromRelatedMethodDeclarationsAndMethodCalls(
			HashSet<String> javaFilesRelevantForRefactoring, MethodDeclaration targetMethod, String parameterName)
			throws FileNotFoundException {
		Integer parameterIndex = getMethodParameterIndex(targetMethod, parameterName);

		for (String currentFilePath : javaFilesRelevantForRefactoring) {
			FileInputStream is = new FileInputStream(currentFilePath);
			CompilationUnit cu = LexicalPreservingPrinter.setup(StaticJavaParser.parse(is));

			List<MethodDeclaration> methodDeclarationsInCurrentFile = cu.findAll(MethodDeclaration.class);
			List<MethodCallExpr> methodCallsInCurrentFile = cu.findAll(MethodCallExpr.class);

			// remove argument from all target method calls
			for (MethodCallExpr fileMethodCall : methodCallsInCurrentFile) {
				if (isTargetMethodCall(fileMethodCall)) {
					removeMethodCallArgument(fileMethodCall, parameterIndex);
				}
			}

			// remove parameter from all relevant method declarations
			for (MethodDeclaration fileMethod : methodDeclarationsInCurrentFile) {
				if (allRefactoringRelevantMethodDeclarations.contains(fileMethod)) {
					removeMethodParameter(fileMethod, parameterName);
					removeParameterFromJavadoc(fileMethod, parameterName);
				}
			}

			PrintWriter out = new PrintWriter(currentFilePath);
			out.println(LexicalPreservingPrinter.print(cu));
			out.close();
		}
	}

	/**
	 * @param methodDeclaration
	 * @param parameterName
	 * @return index of the parameter with the given name, or null if not found
	 */
	private Integer getMethodParameterIndex(MethodDeclaration methodDeclaration, String parameterName) {
		NodeList<Parameter> parameters = methodDeclaration.getParameters();
		for (int i = 0; i < parameters.size(); i++) {
			if (parameters.get(i).getName().asString().equals(parameterName)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * @param classesAndInterfaces
	 * @return true if given classes and interfaces contain at least one call
	 *         expression to the given target method, false otherwise
	 */
	private boolean containsTargetMethodCall(List<ClassOrInterfaceDeclaration> classesAndInterfaces) {
		for (ClassOrInterfaceDeclaration classOrInterface : classesAndInterfaces) {
			List<MethodCallExpr> methodCalls = classOrInterface.findAll(MethodCallExpr.class);
			for (MethodCallExpr methodCall : methodCalls) {
				if (isTargetMethodCall(methodCall)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param methodCall
	 * @return true if given method call is related to target method, false
	 *         otherwise
	 */
	private boolean isTargetMethodCall(MethodCallExpr methodCall) {
		for (MethodDeclaration targetMethod : allRefactoringRelevantMethodDeclarations) {
			String qualifiedMethodSignatureOfResolvedMethodCall = null;
			String qualifiedMethodSignatureOfTargetMethod = null;
			try {
				qualifiedMethodSignatureOfResolvedMethodCall = methodCall.resolve().getQualifiedSignature();
				qualifiedMethodSignatureOfTargetMethod = RefactoringHelper
						.getQualifiedMethodSignatureAsString(targetMethod);
			} catch (Exception e) {
				logger.error(e.getMessage());
				// TODO could be the case that an external dependency could not be resolved. In
				// such case it is fine to return false. However, it is an issue if a method
				// call that needs to be refactored can not be resolved.
				// see also RefactoringHelper.getQualifiedMethodSignatureAsString
				return false;
			}

			if (qualifiedMethodSignatureOfTargetMethod.equals(qualifiedMethodSignatureOfResolvedMethodCall)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes the argument at the given position from the given method call
	 * 
	 * @param methodCall
	 * @param position
	 */
	private void removeMethodCallArgument(MethodCallExpr methodCall, Integer position) {
		methodCall.getArgument(position).remove();
	}

	/**
	 * Removes the parameter with the given name from the given method declaration
	 * 
	 * @param methodDeclaration
	 * @param parameterName
	 */
	private void removeMethodParameter(MethodDeclaration methodDeclaration, String parameterName) {
		Optional<Parameter> parameterToBeRemoved = methodDeclaration.getParameterByName(parameterName);
		if (parameterToBeRemoved.isPresent()) {
			parameterToBeRemoved.get().remove();
		}
	}

	/**
	 * Removes the parameter with the given name from the Javadoc of the given
	 * method declaration
	 * 
	 * @param methodDeclaration
	 * @param paramName
	 */
	private void removeParameterFromJavadoc(MethodDeclaration methodDeclaration, String paramName) {
		Optional<Javadoc> javadoc = methodDeclaration.getJavadoc();
		if (javadoc.isPresent()) {
			Javadoc methodJavadoc = javadoc.get();
			List<JavadocBlockTag> javadocTags = methodJavadoc.getBlockTags();

			for (Iterator<JavadocBlockTag> it = javadocTags.iterator(); it.hasNext();) {
				JavadocBlockTag javadocTag = it.next();
				Optional<String> javadocTagName = javadocTag.getName();
				boolean isEqualParamName = javadocTagName.isPresent() && javadocTagName.get().equals(paramName);
				boolean isParamType = javadocTag.getType().equals(JavadocBlockTag.Type.PARAM);
				if (isParamType && isEqualParamName) {
					it.remove();
				}
			}

			// create new Javadoc with remaining Javadoc tags because there is no way to
			// remove individual tags directly
			Javadoc newJavadoc = new Javadoc(methodJavadoc.getDescription());
			for (JavadocBlockTag blockTag : javadocTags) {
				newJavadoc.addBlockTag(blockTag);
			}

			methodDeclaration.setJavadocComment(newJavadoc);
		}
	}

	/**
	 * 
	 * @param candidate
	 * @return true if candidate is reachable via the inheritance hierarchy
	 *         (ancestor, descendant, sibling, ...)
	 */
	private boolean isRelatedToTargetClass(ClassOrInterfaceDeclaration candidate,
			Set<String> qualifiedNamesOfRelatedClassesAndInterfaces) {
		String qualifiedNameOfCandidate = candidate.resolve().getQualifiedName();
		return qualifiedNamesOfRelatedClassesAndInterfaces.contains(qualifiedNameOfCandidate);
	}

	private void configureJavaParserForProject(BotIssue issue) {
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		for (String javaRoot : issue.getJavaRoots()) {
			typeSolver.add(new JavaParserTypeSolver(javaRoot));
		}
		typeSolver.add(new ReflectionTypeSolver());
		JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
		StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
	}
}
