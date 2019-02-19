package de.refactoringbot.refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
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

	private static final Logger logger = LoggerFactory.getLogger(RefactoringPicker.class);

	private RefactoringHelper() {
	}

	/**
	 * This method scans all Java files for methods that match the passed method
	 * signature and adds them to the given refactoring
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ParserRefactoring findAndAddMethods(ParserRefactoring refactoring, List<String> allJavaFiles,
			String methodSignature) throws FileNotFoundException {

		// Iterate all Java-Files
		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(filepath));

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
	 * This method scans all Java files for method calls that match the method
	 * signatures in the ParserRefactoring and adds them as method calls to the
	 * ParserRefactoring
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ParserRefactoring findAndAddMethodCalls(ParserRefactoring refactoring, List<String> allJavaFiles)
			throws FileNotFoundException {

		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(filepath));

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
	 * This method scans all Java files for classes that could be subclasses in the
	 * AST and adds them to the ParserRefactoring
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return ParserRefactoring
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	public static ParserRefactoring addSubClasses(ParserRefactoring refactoring, List<String> allJavaFiles)
			throws FileNotFoundException {

		// Search all Java-Files
		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(filepath));

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
	 * This method checks all given Java files for methods that equal the given
	 * method signature. This is relevant, for example, to check whether a method
	 * overwrites a method of a superclass after refactoring.
	 * 
	 * @param javaFiles
	 * @param methodSignature
	 * @throws BotRefactoringException
	 *             if there is a duplicate
	 * @throws FileNotFoundException
	 */
	public static void checkForDuplicatedMethodSignatures(List<String> javaFiles, String methodSignature)
			throws BotRefactoringException, FileNotFoundException {

		// Iterate all Javafiles
		for (String javaFile : javaFiles) {
			// Create compilation unit
			FileInputStream methodPath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(methodPath));

			// Get all Methods and MethodCalls of File
			List<MethodDeclaration> fileMethods = compilationUnit.findAll(MethodDeclaration.class);

			// Check if class contains a method equal to the given method signature
			for (MethodDeclaration fileMethod : fileMethods) {
				if (getMethodSignatureAsString(fileMethod).equals(methodSignature)) {
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
	 * @return
	 */
	public static String getFullMethodSignature(MethodDeclaration methodDeclaration) {
		ResolvedMethodDeclaration resolvedMethod = methodDeclaration.resolve();
		return resolvedMethod.getQualifiedSignature();
	}

	/**
	 * @param methodDeclaration
	 * @param position
	 * @return true if given method starts at given position, false otherwise
	 */
	public static boolean isMethodDeclarationAtLine(MethodDeclaration methodDeclaration, Integer position) {
		if (methodDeclaration.getName().getBegin().isPresent()) {
			if (position == methodDeclaration.getName().getBegin().get().line) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param methodDeclaration
	 * @return the local signature of a method as a string
	 */
	public static String getMethodSignatureAsString(MethodDeclaration methodDeclaration) {
		return methodDeclaration.getSignature().asString();
	}

	/**
	 * This method gets all direct and indirect Ancestors of a given class if
	 * possible. (If ancestor is not a external dependency for example)
	 * 
	 * @param currentClass
	 * @return ancestors
	 */
	public static List<ResolvedReferenceType> getAllAncestors(ResolvedReferenceTypeDeclaration currentClass) {
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
	 * 
	 * @param lineNumber
	 * @param cu
	 * @return MethodDeclaration or null if none found
	 */
	public static MethodDeclaration getMethodByLineNumberOfMethodName(int lineNumber, CompilationUnit cu) {
		MethodDeclaration result = null;
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods) {
			if (isMethodDeclarationAtLine(method, lineNumber)) {
				result = method;
			}
		}
		return result;
	}

	/**
	 * Finds a field in a compilation unit that starts at the specified line number
	 * 
	 * @param lineNumber
	 * @param cu
	 * @return FieldDeclaration or null if none found
	 */
	public static FieldDeclaration getFieldDeclarationByLineNumber(int lineNumber, CompilationUnit cu) {
		FieldDeclaration result = null;
		List<FieldDeclaration> fields = cu.findAll(FieldDeclaration.class);
		for (FieldDeclaration field : fields) {
			if (field.getBegin().isPresent()) {
				if (field.getBegin().get().line == lineNumber) {
					result = field;
				}
			}
		}
		return result;
	}

}
