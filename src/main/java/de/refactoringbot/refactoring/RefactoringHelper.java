package de.refactoringbot.refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
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
 * Utility methods for use in performing refactorings
 */
public class RefactoringHelper {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringHelper.class);

	private RefactoringHelper() {
	}

	/**
	 * @param classOrInterface
	 * @param methodSignature
	 * @return true if local method signature is present in given class or
	 *         interface, false otherwise
	 */
	public static boolean isLocalMethodSignaturePresentInClassOrInterface(ClassOrInterfaceDeclaration classOrInterface,
			String methodSignature) {
		List<MethodDeclaration> fileMethods = classOrInterface.findAll(MethodDeclaration.class);

		for (MethodDeclaration fileMethod : fileMethods) {
			if (getLocalMethodSignatureAsString(fileMethod).equals(methodSignature)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param methodDeclaration
	 * @return the local signature of a method as a string
	 */
	public static String getLocalMethodSignatureAsString(MethodDeclaration methodDeclaration) {
		return methodDeclaration.getSignature().asString();
	}

	/**
	 * @param methodDeclaration
	 * @return qualified method signature of the given method declaration
	 * @throws BotRefactoringException
	 */
	public static String getQualifiedMethodSignatureAsString(MethodDeclaration methodDeclaration)
			throws BotRefactoringException {
		try {
			ResolvedMethodDeclaration resolvedMethod = methodDeclaration.resolve();
			return resolvedMethod.getQualifiedSignature();
		} catch (Exception e) {
			throw new BotRefactoringException("Method '" + methodDeclaration.getSignature().asString()
					+ "' can't be resolved. It might have parameters from external projects/libraries or method might be"
					+ " inside a class that extends a generic class! Error: " + e);
		}
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
	 * @param methodDeclaration
	 * @param lineNumber
	 * @return true if given method starts at given line, false otherwise
	 */
	public static boolean isMethodDeclarationAtLine(MethodDeclaration methodDeclaration, Integer lineNumber) {
		Optional<Position> beginPositionOfName = methodDeclaration.getName().getBegin();
		return (beginPositionOfName.isPresent() && lineNumber == beginPositionOfName.get().line);
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
			if (isFieldDeclarationAtLine(field, lineNumber)) {
				result = field;
			}
		}
		return result;
	}

	/**
	 * @param fieldDeclaration
	 * @param lineNumber
	 * @return true if given field starts at given line, false otherwise
	 */
	public static boolean isFieldDeclarationAtLine(FieldDeclaration fieldDeclaration, Integer lineNumber) {
		Optional<Position> beginPositionOfField = fieldDeclaration.getBegin();
		return (beginPositionOfField.isPresent() && beginPositionOfField.get().line == lineNumber);
	}

	/**
	 * @param methodDeclaration
	 * @return parent node of the given method declaration as
	 *         ClassOrInterfaceDeclaration
	 * @throws IllegalStateException
	 *             if no parent node is present
	 */
	public static ClassOrInterfaceDeclaration getMethodParentNodeAsClassOrInterface(
			MethodDeclaration methodDeclaration) {
		Optional<Node> parentNode = methodDeclaration.getParentNode();
		if (parentNode.isPresent()) {
			return ((ClassOrInterfaceDeclaration) parentNode.get());
		}
		throw new IllegalStateException("MethodDeclaration expected to have a parent node.");
	}

	/**
	 * @param filePath
	 * @return all <code>ClassOrInterfaceDeclaration</code> in the given file
	 * @throws FileNotFoundException
	 */
	public static List<ClassOrInterfaceDeclaration> getAllClassesAndInterfacesFromFile(String filePath)
			throws FileNotFoundException {
		FileInputStream is = new FileInputStream(filePath);
		CompilationUnit cu = LexicalPreservingPrinter.setup(JavaParser.parse(is));
		return cu.findAll(ClassOrInterfaceDeclaration.class);
	}

	/**
	 * @param allJavaFiles
	 * @param targetClass
	 * @return list of qualified class or interface names which are reachable via
	 *         the inheritance hierarchy of the given class (ancestors, descendants,
	 *         siblings, ...)
	 * @throws BotRefactoringException
	 * @throws FileNotFoundException
	 */
	public static Set<String> findQualifiedNamesOfRelatedClassesAndInterfaces(List<String> allJavaFiles,
			ClassOrInterfaceDeclaration targetClass) throws BotRefactoringException, FileNotFoundException {
		Set<ResolvedReferenceTypeDeclaration> ancestorsOfTargetClass = findAllAncestors(targetClass);
		return findQualifiedNamesOfRelatedClassesAndInterfaces(targetClass, ancestorsOfTargetClass, allJavaFiles);
	}

	/**
	 * @param targetClass
	 * @return list of resolved classes and interfaces which are ancestors of the
	 *         given classes (not including java.lang.Object or external
	 *         dependencies)
	 * @throws BotRefactoringException
	 */
	private static Set<ResolvedReferenceTypeDeclaration> findAllAncestors(ClassOrInterfaceDeclaration targetClass)
			throws BotRefactoringException {
		List<ResolvedReferenceType> ancestors = new ArrayList<>();
		Set<ResolvedReferenceTypeDeclaration> result = new HashSet<>();

		try {
			ancestors = targetClass.resolve().getAllAncestors();
		} catch (UnsolvedSymbolException u) {
			ancestors = RefactoringHelper.getAllAncestorsAcceptIncompleteList(targetClass.resolve());
			logger.warn("Refactored classes might extend/implement classes or interfaces from external dependency! "
					+ "Please validate the correctness of the refactoring.");
			// TODO propagate warning
		} catch (InvalidPathException i) {
			throw new BotRefactoringException("Javaparser could not parse file: " + i.getMessage());
		} catch (Exception e) {
			throw new BotRefactoringException("Error while resolving superclasses occured!");
		}

		for (ResolvedReferenceType ancestor : ancestors) {
			if (!ancestor.getQualifiedName().equals("java.lang.Object")) {
				result.add(ancestor.getTypeDeclaration());
			}
		}

		return result;
	}

	/**
	 * @param targetClass
	 * @param ancestorsOfTargetClass
	 * @param allJavaFiles
	 * @return list of qualified class or interface names which are reachable via
	 *         the inheritance hierarchy of the given classes (ancestors,
	 *         descendants, siblings, ...)
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	private static Set<String> findQualifiedNamesOfRelatedClassesAndInterfaces(ClassOrInterfaceDeclaration targetClass,
			Set<ResolvedReferenceTypeDeclaration> ancestorsOfTargetClass, List<String> allJavaFiles)
			throws FileNotFoundException, BotRefactoringException {
		Set<ResolvedReferenceTypeDeclaration> relatedClassesAndInterfaces = new HashSet<>();
		relatedClassesAndInterfaces.add(targetClass.resolve());
		relatedClassesAndInterfaces.addAll(ancestorsOfTargetClass);

		for (String file : allJavaFiles) {
			List<ClassOrInterfaceDeclaration> classesOrInterfaces = RefactoringHelper
					.getAllClassesAndInterfacesFromFile(file);

			for (ClassOrInterfaceDeclaration classOrInterface : classesOrInterfaces) {
				if (relatedClassesAndInterfaces.contains(classOrInterface.resolve())) {
					continue;
				}
				Set<ResolvedReferenceTypeDeclaration> ancestorsOfCurrentClassOrInterface = findAllAncestors(
						classOrInterface);
				if (!Collections.disjoint(relatedClassesAndInterfaces, ancestorsOfCurrentClassOrInterface)) {
					// descendant found
					relatedClassesAndInterfaces.add(classOrInterface.resolve());
				}
			}
		}

		Set<String> result = new HashSet<>();
		for (ResolvedReferenceTypeDeclaration declaration : relatedClassesAndInterfaces) {
			result.add(declaration.getQualifiedName());
		}

		return result;
	}

	/**
	 * Get all direct and indirect ancestors of a given class if possible (if
	 * ancestor is not a external dependency for example).
	 * 
	 * This is a modified (fallback) implementation of
	 * ResolvedReferenceTypeDeclaration.getAllAncestors() that we use in case, for
	 * example, that external classes are extended (which would throw an
	 * UnresolvedSymbolException).
	 * 
	 * @param currentClass
	 * @return ancestors
	 */
	public static List<ResolvedReferenceType> getAllAncestorsAcceptIncompleteList(
			ResolvedReferenceTypeDeclaration currentClass) {
		// TODO make private as soon as RenameMethod.java has been refactored
		List<ResolvedReferenceType> ancestors = new ArrayList<>();

		if (!(Object.class.getCanonicalName().equals(currentClass.getQualifiedName()))) {
			// Get all direct ancestors that can be resolved
			for (ResolvedReferenceType ancestor : currentClass.getAncestors(true)) {
				ancestors.add(ancestor);
				// Get indirect ancestors recursively
				for (ResolvedReferenceType inheritedAncestor : getAllAncestorsAcceptIncompleteList(
						ancestor.getTypeDeclaration())) {
					if (!ancestors.contains(inheritedAncestor)) {
						ancestors.add(inheritedAncestor);
					}
				}
			}
		}

		return ancestors;
	}

	// ##############################################
	// Deprecated
	// ##############################################

	/**
	 * This method scans all Java files for methods that match the passed method
	 * signature and adds them to the given refactoring
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	@Deprecated
	public static ParserRefactoring findAndAddMethods(ParserRefactoring refactoring, List<String> allJavaFiles,
			String methodSignature) throws FileNotFoundException, BotRefactoringException {
		// Iterate all Java-Files
		for (String javaFile : allJavaFiles) {
			// parse a file
			FileInputStream filepath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(filepath));

			// Get all Classes
			List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
			// Iterate all Classes
			for (ClassOrInterfaceDeclaration currentClass : classes) {
				boolean isSubOrSuperClass = refactoring.getClasses()
						.contains(currentClass.resolve().getQualifiedName());
				if (isSubOrSuperClass) {
					List<MethodDeclaration> methods = currentClass.getMethods();
					for (MethodDeclaration method : methods) {
						if (method.getSignature().asString().equals(methodSignature)) {
							refactoring.addMethod(method);
							refactoring.addMethodSignature(getQualifiedMethodSignatureAsString(method));
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
	@Deprecated
	public static ParserRefactoring findAndAddMethodCalls(ParserRefactoring refactoring, List<String> allJavaFiles)
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
	 * This method scans all Java files for classes that could be subclasses in the
	 * AST and adds them to the ParserRefactoring
	 * 
	 * @param refactoring
	 * @param allJavaFiles
	 * @return ParserRefactoring
	 * @throws FileNotFoundException
	 * @throws BotRefactoringException
	 */
	@Deprecated
	public static ParserRefactoring addSubClasses(ParserRefactoring refactoring, List<String> allJavaFiles)
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
					ancestors = getAllAncestorsAcceptIncompleteList(currentClass.resolve());
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

	@Deprecated
	public static boolean isMethodSignaturePresentInFile(String filePath, String methodSignature)
			throws FileNotFoundException {
		FileInputStream is = new FileInputStream(filePath);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(is));
		List<MethodDeclaration> fileMethods = compilationUnit.findAll(MethodDeclaration.class);

		for (MethodDeclaration fileMethod : fileMethods) {
			if (getLocalMethodSignatureAsString(fileMethod).equals(methodSignature)) {
				return true;
			}
		}

		return false;
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
	@Deprecated
	public static void checkForDuplicatedMethodSignatures(List<String> javaFiles, String methodSignature)
			throws BotRefactoringException, FileNotFoundException {

		// Iterate all Javafiles
		for (String javaFile : javaFiles) {
			// Create compilation unit
			FileInputStream methodPath = new FileInputStream(javaFile);
			CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(methodPath));

			// Get all Methods and MethodCalls of File
			List<MethodDeclaration> fileMethods = compilationUnit.findAll(MethodDeclaration.class);

			// Check if class contains a method equal to the given method signature
			for (MethodDeclaration fileMethod : fileMethods) {
				if (getLocalMethodSignatureAsString(fileMethod).equals(methodSignature)) {
					throw new BotRefactoringException(
							"Removal of parameter would result in method signature already present in file '" + javaFile
									+ "'.");
				}
			}
		}
	}
}
