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
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import de.refactoringbot.model.exceptions.BotRefactoringException;

/**
 * Utility methods for use in performing refactorings
 */
public class RefactoringHelper {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringHelper.class);

	private RefactoringHelper() {
	}

	/**
	 * @param classOrInterface
	 * @param localMethodSignature
	 * @return true if local method signature is present in given class or
	 *         interface, false otherwise
	 */
	public static boolean isLocalMethodSignatureInClassOrInterface(ClassOrInterfaceDeclaration classOrInterface,
			String localMethodSignature) {
		List<MethodDeclaration> fileMethods = classOrInterface.findAll(MethodDeclaration.class);

		for (MethodDeclaration fileMethod : fileMethods) {
			if (getLocalMethodSignatureAsString(fileMethod).equals(localMethodSignature)) {
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
			ancestors = RefactoringHelper.getAllResolvableAncestors(targetClass.resolve());
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
	 * Get all direct and indirect ancestors of a given class if possible (if
	 * ancestor is not a external dependency for example).
	 * 
	 * This is a modified (fallback) implementation of
	 * ResolvedReferenceTypeDeclaration.getAllAncestors() that we use in case, for
	 * example, that external classes are extended (which would throw an
	 * UnresolvedSymbolException).
	 * 
	 * @param resolvedClass
	 * @return ancestors
	 */
	private static List<ResolvedReferenceType> getAllResolvableAncestors(
			ResolvedReferenceTypeDeclaration resolvedClass) {
		List<ResolvedReferenceType> ancestors = new ArrayList<>();

		if (!(Object.class.getCanonicalName().equals(resolvedClass.getQualifiedName()))) {
			// Get all direct ancestors that can be resolved
			for (ResolvedReferenceType ancestor : resolvedClass.getAncestors(true)) {
				ancestors.add(ancestor);
				// Get indirect ancestors recursively
				for (ResolvedReferenceType inheritedAncestor : getAllResolvableAncestors(
						ancestor.getTypeDeclaration())) {
					if (!ancestors.contains(inheritedAncestor)) {
						ancestors.add(inheritedAncestor);
					}
				}
			}
		}

		return ancestors;
	}
}
