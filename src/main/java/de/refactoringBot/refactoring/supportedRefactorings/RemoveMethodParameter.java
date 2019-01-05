package de.refactoringBot.refactoring.supportedRefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.exceptions.BotRefactoringException;
import de.refactoringBot.model.javaparser.ParserRefactoring;
import de.refactoringBot.refactoring.RefactoringHelper;
import de.refactoringBot.refactoring.RefactoringImpl;

public class RemoveMethodParameter extends RefactoringHelper implements RefactoringImpl {

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
		ParserRefactoring refactoring = new ParserRefactoring();

		// Init all java files path-list
		List<String> allJavaFiles = new ArrayList<>();

		// Init needed variables
		String issueFilePath = gitConfig.getRepoFolder() + "/" + issue.getFilePath();
		String globalMethodSignature = null;
		String localMethodSignature = null;
		Integer paramPosition = null;
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
				globalMethodSignature = getFullMethodSignature(method, issue.getLine(), issue.getRefactorString());
				localMethodSignature = getMethodDeclarationAsString(method, issue.getLine());
				// If it is
				if (globalMethodSignature != null) {
					// Set method and class signatures of the method
					methodToRefactor = method;
					paramPosition = getMethodParameterPosition(method, issue.getRefactorString());
					foundMethod = true;

					// Check if parameter is used inside the given method
					if (checkIfParameterUsed(method, issue.getRefactorString())) {
						throw new BotRefactoringException("Parameter '" + issue.getRefactorString()
								+ "' is used inside the method '" + globalMethodSignature + "'!");
					}

					// Add class to refactoring
					refactoring.addClass(currentClass.resolve().getQualifiedName());

					// Get all super classes
					List<ResolvedReferenceType> ancestors = null;
					try {
						ancestors = currentClass.resolve().getAllAncestors();
					} catch (UnsolvedSymbolException u) {
						ancestors = getAllAncestors(currentClass.resolve());
						refactoring.setWarning(
								" Refactored classes might extend/implement external project! Check if overriden method was NOT renamed!");
					} catch (InvalidPathException i) {
						throw new BotRefactoringException("Javaparser could not parse file: " + i.getMessage());
					} catch (Exception e) {
						throw new BotRefactoringException("Error while resolving superclasses occured!");
					}

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

		// Check if any method in AST-Tree uses parameter
		for (MethodDeclaration method : refactoring.getMethods()) {
			if (checkIfParameterUsed(method, issue.getRefactorString())) {
				throw new BotRefactoringException(
						"Parameter '" + issue.getRefactorString() + "' is used inside the method '"
								+ getFullMethodSignature(method) + "' which is a super/sub class of the given method!");
			}
		}

		// Remove parameter from all methods/method calls
		removeParameter(refactoring, issue.getRefactorString(), paramPosition);

		return "Removed method parameter '" + issue.getRefactorString() + "' of method '"
				+ methodToRefactor.getSignature() + "'";
	}

	/**
	 * This method removes the parameter from all methods or method calls inside the
	 * entire project.
	 * 
	 * @param refactoring
	 * @param paramName
	 * @throws FileNotFoundException
	 */
	private void removeParameter(ParserRefactoring refactoring, String paramName, Integer paramPosition)
			throws FileNotFoundException {

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
	 * This method returns the global signature of a method as a string.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @return
	 * @throws BotRefactoringException
	 */
	private String getFullMethodSignature(MethodDeclaration methodDeclaration, Integer position, String parameterName)
			throws BotRefactoringException {
		// If method is at the refactored position
		if (methodDeclaration.getName().getBegin().isPresent()) {
			if (position == methodDeclaration.getName().getBegin().get().line) {
				// Check if method has parameter
				if (!methodDeclaration.getParameterByName(parameterName).isPresent()) {
					throw new BotRefactoringException("Method '" + methodDeclaration.getSignature()
							+ "' does not have parameter '" + parameterName + "'!");
				}
				try {
					ResolvedMethodDeclaration resolvedMethod = methodDeclaration.resolve();
					return resolvedMethod.getQualifiedSignature();
				} catch (Exception e) {
					throw new BotRefactoringException("Method '" + methodDeclaration.getSignature().asString()
							+ "' can't be resolved. It might have parameters from external projects/libraries or method might be inside a class that extends a generic class! Error: "
							+ e);
				}
			}
		}
		return null;
	}

	/**
	 * This method checks if a parameter with a specific name is used inside a
	 * method.
	 * 
	 * @param method
	 * @param paramName
	 * @return used/notUsed
	 */
	private boolean checkIfParameterUsed(MethodDeclaration method, String paramName) {
		// Check if Method has body
		if (method.getBody().isPresent()) {
			// Get body
			BlockStmt methodBody = method.getBody().get();
			// Get all expressions from body
			List<NameExpr> expressions = methodBody.findAll(NameExpr.class);
			// Iterate expressions
			for (NameExpr expression : expressions) {
				// If expression name = param name
				if (expression.getNameAsString().equals(paramName)) {
					// Parameter used
					return true;
				}
			}
		}

		// Parameter not used
		return false;
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
	 * This method performs the removing of a specific parameter of the method.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @param parameterName
	 */
	private void performRemoveMethodParameter(MethodDeclaration methodDeclaration, String parameterName) {
		if (methodDeclaration.getParameterByName(parameterName).isPresent()) {
			methodDeclaration.getParameterByName(parameterName).get().remove();
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
