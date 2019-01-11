package de.refactoringbot.refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.javaparser.ParserRefactoring;

/**
 * This class contains many methods that can be used by multiple
 * Refactoring-Classes.
 * 
 * @author Stefan Basaric
 *
 */
public class RefactoringHelper {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(RefactoringPicker.class);

	/**
	 * This method scanns all java files for method calls that need to be renamed.
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return
	 * @throws FileNotFoundException
	 */
	public ParserRefactoring findMethods(ParserRefactoring refactoring, List<String> allJavaFiles,
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
	public ParserRefactoring findMethodCalls(ParserRefactoring refactoring, List<String> allJavaFiles)
			throws FileNotFoundException {

		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

			List<MethodCallExpr> methodCalls = compilationUnit.findAll(MethodCallExpr.class);

			for (MethodCallExpr methodCall : methodCalls) {

				ResolvedMethodDeclaration calledMethod = null;
				try {
					calledMethod = methodCall.resolve();
				} catch (Exception e) {
					logger.debug(e.getMessage());
					continue;
				}

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
	 * This method scanns all java files for classes that could be subclasses in our
	 * AST-Tree.
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return refactoring
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	public ParserRefactoring addSubClasses(ParserRefactoring refactoring, List<String> allJavaFiles)
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
				List<String> classAncestors = new ArrayList<>();
				boolean isSubClass = false;
				boolean hasExternalDep = false;

				// Get all Super-Classes
				List<ResolvedReferenceType> ancestors = null;
				try {
					ancestors = currentClass.resolve().getAllAncestors();
				} catch (InvalidPathException | UnsolvedSymbolException i) {
					ancestors = getAllAncestors(currentClass.resolve());
					hasExternalDep = true;
				} catch (Exception e) {
					logger.debug(e.getMessage());
					continue;
				}

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
					// Add warning
					if (hasExternalDep) {
						refactoring.setWarning(
								" Refactored classes might extend/implement external project! Check if overriden method was NOT renamed!");
					}
				}
			}
		}

		return refactoring;
	}

	/**
	 * This method checks all Java-Classes that will be refactored if they have a
	 * method with the same signature as our 'to be refactored' method after the
	 * refactoring.
	 * 
	 * @param refactoring
	 * @param postRefactoringSignature
	 * @throws BotRefactoringException
	 * @throws FileNotFoundException 
	 */
	public void checkForDuplicatedMethodSignatures(ParserRefactoring refactoring, String postRefactoringSignature)
			throws BotRefactoringException, FileNotFoundException {

		// Iterate all Javafiles
		for (String javaFile : refactoring.getJavaFiles()) {
			// Create compilation unit
			FileInputStream methodPath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(methodPath));

			// Get all Methods and MethodCalls of File
			List<MethodDeclaration> fileMethods = compilationUnit.findAll(MethodDeclaration.class);

			// Check if class has an overriden method without the "to be removed" parameter
			for (MethodDeclaration fileMethod : fileMethods) {
				if (getMethodDeclarationAsString(fileMethod).equals(postRefactoringSignature)) {
					throw new BotRefactoringException(
							"File '" + javaFile + "' has a method with the same signature as our refactored method!");
				}
			}
		}
	}

	/**
	 * This method returns the global signature of a method as a string.
	 * 
	 * @param methodDeclaration
	 * @param position
	 * @return
	 */
	public String getFullMethodSignature(MethodDeclaration methodDeclaration) {
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
	public String getMethodDeclarationAsString(MethodDeclaration methodDeclaration, Integer position) {
		// If method is at the refactored position
		if (methodDeclaration.getName().getBegin().isPresent()) {
			if (position == methodDeclaration.getName().getBegin().get().line) {
				return methodDeclaration.getSignature().asString();
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
	public String getMethodDeclarationAsString(MethodDeclaration methodDeclaration) {
		// If method is at the refactored position
		return methodDeclaration.getSignature().asString();
	}

	/**
	 * This method gets all direct and indirect Ancestors of a given class if
	 * possible. (If ancestor is not a external dependency for example)
	 * 
	 * @param currentClass
	 * @return ancestors
	 */
	public List<ResolvedReferenceType> getAllAncestors(ResolvedReferenceTypeDeclaration currentClass) {
		// Init ancestor list
		List<ResolvedReferenceType> ancestors = new ArrayList<>();

		// Check class
		if (!(Object.class.getCanonicalName().equals(currentClass.getQualifiedName()))) {
			// Get all direct ancestors that can be resolved
			for (ResolvedReferenceType ancestor : currentClass.getAncestors(true)) {
				// Add them to list
				ancestors.add(ancestor);
				// Get indirect ancestors recursively
				for (ResolvedReferenceType inheritedAncestor : getAllAncestors(ancestor.getTypeDeclaration())) {
					if (!ancestors.contains(inheritedAncestor)) {
						ancestors.add(inheritedAncestor);
					}
				}
			}
		}

		return ancestors;
	}
	
	/**
	 * Finds a method in a compilation unit that starts at the specified line number
	 * @param lineNumber
	 * @param cu
	 * @return MethodDeclaration or null if none found
	 */
	public static MethodDeclaration getMethodByLineNumberOfMethodName(int lineNumber, CompilationUnit cu) {
		MethodDeclaration result = null;
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods) {
			if (method.getName().getBegin().get().line == lineNumber) {
				result = method;
			}
		}
		return result;
	}
	

	/**
	 * Finds a method in a compilation unit with a specific name
	 * @param methodName
	 * @param cu
	 * @return MethodDeclaration or null if none found
	 */
	public static MethodDeclaration getMethodByName(String methodName, CompilationUnit cu) {
		MethodDeclaration result = null;
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods) {
			if (method.getNameAsString().equals(methodName)) {
				result = method;
			}
		}
		return result;
	}

}
