package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
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
 * Refactoring to rename a method
 */
@Component
public class RenameMethod implements RefactoringImpl {

	private static final Logger logger = LoggerFactory.getLogger(RenameMethod.class);

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

		String newMethodName = issue.getRefactorString();
		String issueFilePath = gitConfig.getRepoFolder() + File.separator + issue.getFilePath();
		MethodDeclaration targetMethod = findAndValidateTargetMethod(issue, issueFilePath, newMethodName);
		ClassOrInterfaceDeclaration targetClass = RefactoringHelper.getMethodParentNodeAsClassOrInterface(targetMethod);
		Set<String> qualifiedNamesOfRelatedClassesAndInterfaces = RefactoringHelper
				.findQualifiedNamesOfRelatedClassesAndInterfaces(issue.getAllJavaFiles(), targetClass);

		HashSet<String> javaFilesRelevantForRefactoring = findJavaFilesRelevantForRefactoring(issue, newMethodName,
				targetMethod, qualifiedNamesOfRelatedClassesAndInterfaces);
		renameMethodInRelatedMethodDeclarationsAndMethodCalls(javaFilesRelevantForRefactoring, newMethodName);

		String oldMethodName = targetMethod.getNameAsString();
		return "Renamed method '" + oldMethodName + "' to '" + newMethodName + "'.";
	}

	/**
	 * Tries to find the target method in the given class or interface. Checks if
	 * the found method itself could be refactored, without yet checking other code
	 * locations
	 * 
	 * @param issue
	 * @param filePath
	 * @param newMethodName
	 * @return
	 * @throws BotRefactoringException
	 * @throws FileNotFoundException
	 */
	private MethodDeclaration findAndValidateTargetMethod(BotIssue issue, String filePath, String newMethodName)
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
			throw new BotRefactoringException("Could not find specified method! Automated refactoring failed.");
		}

		String oldMethodName = targetMethod.getNameAsString();
		if (oldMethodName.equals(newMethodName)) {
			throw new BotRefactoringException("New method name must differ from the current one!");
		}

		return targetMethod;
	}

	/**
	 * Finds all Java files that are relevant for refactoring, i.e. those that
	 * contain method declarations or method calls that refer to the target method.
	 * Validates if the related declarations and calls can be refactored.
	 * 
	 * @param issue
	 * @param newMethodName
	 * @param targetMethod
	 * @param qualifiedNamesOfRelatedClassesAndInterfaces
	 * @return
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private HashSet<String> findJavaFilesRelevantForRefactoring(BotIssue issue, String newMethodName,
			MethodDeclaration targetMethod, Set<String> qualifiedNamesOfRelatedClassesAndInterfaces)
			throws FileNotFoundException, BotRefactoringException {
		HashSet<String> javaFilesRelevantForRefactoring = new HashSet<>();
		String postRefactoringSignature = getPostRefactoringSignature(targetMethod, newMethodName);

		for (String currentFilePath : issue.getAllJavaFiles()) {
			List<ClassOrInterfaceDeclaration> classesAndInterfacesInCurrentFile = RefactoringHelper
					.getAllClassesAndInterfacesFromFile(currentFilePath);

			// search for files containing relevant method declarations
			for (ClassOrInterfaceDeclaration currentClassOrInterface : classesAndInterfacesInCurrentFile) {
				if (isRelatedToTargetClass(currentClassOrInterface, qualifiedNamesOfRelatedClassesAndInterfaces)) {
					List<MethodDeclaration> methodDeclarationsInCurrentClass = currentClassOrInterface
							.findAll(MethodDeclaration.class);
					for (MethodDeclaration methodDeclaration : methodDeclarationsInCurrentClass) {
						boolean localMethodSignatureIsEqual = RefactoringHelper
								.getLocalMethodSignatureAsString(methodDeclaration)
								.equals(RefactoringHelper.getLocalMethodSignatureAsString(targetMethod));
						if (localMethodSignatureIsEqual) {
							validateClassOrInterfaceNotContainsPostRefactoringSignatureAlready(currentClassOrInterface,
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
	 * @param newMethodName
	 * @return signature of the given method declaration after renaming would have
	 *         been performed
	 */
	private String getPostRefactoringSignature(MethodDeclaration methodDeclaration, String newMethodName) {
		MethodDeclaration copy = methodDeclaration.clone();
		renameMethod(copy, newMethodName);
		return RefactoringHelper.getLocalMethodSignatureAsString(copy);
	}

	/**
	 * Check if post refactoring signature already exists in given class or
	 * interface
	 * 
	 * @param currentClassOrInterface
	 * @param postRefactoringSignature
	 * @throws BotRefactoringException
	 */
	private void validateClassOrInterfaceNotContainsPostRefactoringSignatureAlready(
			ClassOrInterfaceDeclaration classOrInterface, String postRefactoringSignature)
			throws BotRefactoringException {
		if (RefactoringHelper.isLocalMethodSignaturePresentInClassOrInterface(classOrInterface,
				postRefactoringSignature)) {
			throw new BotRefactoringException(
					"Renaming of method would result in method signature already present in class or interface '"
							+ classOrInterface.getNameAsString() + "'.");
		}
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

	/**
	 * Rename all relevant method declarations and method calls in the given java
	 * files
	 * 
	 * @param javaFilesRelevantForRefactoring
	 * @param newMethodName
	 * @throws FileNotFoundException
	 */
	private void renameMethodInRelatedMethodDeclarationsAndMethodCalls(HashSet<String> javaFilesRelevantForRefactoring,
			String newMethodName) throws FileNotFoundException {
		for (String currentFilePath : javaFilesRelevantForRefactoring) {
			FileInputStream is = new FileInputStream(currentFilePath);
			CompilationUnit cu = LexicalPreservingPrinter.setup(JavaParser.parse(is));

			List<MethodDeclaration> methodDeclarationsInCurrentFile = cu.findAll(MethodDeclaration.class);
			List<MethodCallExpr> methodCallsInCurrentFile = cu.findAll(MethodCallExpr.class);

			// rename all target method calls
			for (MethodCallExpr fileMethodCall : methodCallsInCurrentFile) {
				if (isTargetMethodCall(fileMethodCall)) {
					renameMethodCall(fileMethodCall, newMethodName);
				}
			}

			// rename all relevant method declarations
			for (MethodDeclaration fileMethod : methodDeclarationsInCurrentFile) {
				if (allRefactoringRelevantMethodDeclarations.contains(fileMethod)) {
					renameMethod(fileMethod, newMethodName);
				}
			}

			PrintWriter out = new PrintWriter(currentFilePath);
			out.println(LexicalPreservingPrinter.print(cu));
			out.close();
		}
	}

	/**
	 * Renames the given method to the given new method name
	 * 
	 * @param methodDeclaration
	 * @param newMethodName
	 */
	private void renameMethod(MethodDeclaration methodDeclaration, String newMethodName) {
		methodDeclaration.setName(newMethodName);
	}

	/**
	 * Renames the given method call to the given method name
	 * 
	 * @param methodCall
	 * @param newMethodName
	 */
	private void renameMethodCall(MethodCallExpr methodCall, String newMethodName) {
		methodCall.setName(newMethodName);
	}

	private void configureJavaParserForProject(BotIssue issue) {
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		for (String javaRoot : issue.getJavaRoots()) {
			typeSolver.add(new JavaParserTypeSolver(javaRoot));
		}
		typeSolver.add(new ReflectionTypeSolver());
		JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser.getStaticConfiguration().setSymbolResolver(javaSymbolSolver);
	}

}
