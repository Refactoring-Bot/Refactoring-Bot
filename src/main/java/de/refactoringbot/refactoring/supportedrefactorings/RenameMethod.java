package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.InvalidPathException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
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

/**
 * This refactoring class is used for renaming methods inside a java project.
 *
 * @author Stefan Basaric
 */
@Component
public class RenameMethod implements RefactoringImpl {

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

		// Init needed variables
		String issueFilePath = gitConfig.getRepoFolder() + "/" + issue.getFilePath();
		String globalMethodSignature = null;
		String localMethodSignature = null;
		String oldMethodName = null;
		MethodDeclaration methodToRefactor = null;

		// Configure solver for the project
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		// Add java-roots
		for (String javaRoot : issue.getJavaRoots()) {
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
				localMethodSignature = RefactoringHelper.isMethodDeclarationAtLine(method, issue.getLine())
						? RefactoringHelper.getMethodSignatureAsString(method)
						: null;
				oldMethodName = method.getNameAsString();
				// If it is
				if (globalMethodSignature != null) {
					// Set method and class signatures of the method
					methodToRefactor = method;
					foundMethod = true;

					// Add class to refactoring
					refactoring.addClass(currentClass.resolve().getQualifiedName());

					// Get all super classes
					List<ResolvedReferenceType> ancestors = null;
					try {
						ancestors = currentClass.resolve().getAllAncestors();
					} catch (UnsolvedSymbolException u) {
						ancestors = RefactoringHelper.getAllAncestors(currentClass.resolve());
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

		// If new name equals old
		if (oldMethodName != null && oldMethodName.equals(issue.getRefactorString())) {
			throw new BotRefactoringException("New method name must differ from the current one!");
		}

		// Add all Subclasses and their Superclasses to AST-Tree
		while (true) {
			int before = refactoring.getClasses().size();
			refactoring = RefactoringHelper.addSubClasses(refactoring, issue.getAllJavaFiles());
			int after = refactoring.getClasses().size();
			// Break if all classes found
			if (before == after) {
				break;
			}
		}

		// Find all Methods and Method-Calls for renaming
		refactoring = RefactoringHelper.findAndAddMethods(refactoring, issue.getAllJavaFiles(), localMethodSignature);
		refactoring = RefactoringHelper.findAndAddMethodCalls(refactoring, issue.getAllJavaFiles());

		// Get local method signature after rename
		String postRefactoringSignature = getPostRefactoringSignature(refactoring, issue.getRefactorString());

		// Check Overriden Methods
		RefactoringHelper.checkForDuplicatedMethodSignatures(refactoring, postRefactoringSignature);

		// Rename method declarations and their calls
		renameFindings(refactoring, issue.getRefactorString());

		return "Renamed method '" + oldMethodName + "' to '" + issue.getRefactorString() + "'."
				+ refactoring.getWarning();
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
	private void renameFindings(ParserRefactoring refactoring, String newName) throws FileNotFoundException {

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
	 * This method gets the Method signature that our methods will have without the
	 * parameter.
	 * 
	 * @param refactoring
	 * @param paramName
	 * @return signature
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private String getPostRefactoringSignature(ParserRefactoring refactoring, String methodName)
			throws FileNotFoundException, BotRefactoringException {
		for (String javaFile : refactoring.getJavaFiles()) {
			// Create compilation unit
			FileInputStream methodPath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(methodPath));

			// Get all Methods and MethodCalls of File
			List<MethodDeclaration> fileMethods = compilationUnit.findAll(MethodDeclaration.class);

			// Rename one method and return method signature
			for (MethodDeclaration fileMethod : fileMethods) {
				if (refactoring.getMethods().contains(fileMethod)) {
					performRenameMethod(fileMethod, methodName);
					return RefactoringHelper.getMethodSignatureAsString(fileMethod);
				}
			}

		}
		throw new BotRefactoringException("Error reading methods that need their parameter removed!");
	}

	/**
	 * This method returns the global signature of a method as a string.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @return
	 * @throws BotRefactoringException
	 */
	private String getFullMethodSignature(MethodDeclaration methodDeclaration, Integer position)
			throws BotRefactoringException {
		// If method is at the refactored position
		if (methodDeclaration.getName().getBegin().isPresent()) {
			if (position == methodDeclaration.getName().getBegin().get().line) {
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
