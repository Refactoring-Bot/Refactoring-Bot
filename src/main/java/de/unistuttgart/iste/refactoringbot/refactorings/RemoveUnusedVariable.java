package main.java.de.unistuttgart.iste.refactoringbot.refactorings;

import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import main.java.de.unistuttgart.iste.refactoringbot.Refactoring;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author Timo Pfaff
 * 
 *         this class is used to execute the removing unused variables
 *         refactoring.
 * 
 *         it does not use LexicalPreservingPrinter yet, because it throws an
 *         execption when the type of the variable, that should be removed is
 *         not a primitive type. there is already an open issue for that on the
 *         JavaParser GitHub page:
 *         https://github.com/javaparser/javaparser/issues/1667 When this issue
 *         gets fixed the LexicalPreservingPrinter should be used to get rid of
 *         the formatting problem.
 *
 */
public class RemoveUnusedVariable extends ModifierVisitor<Void> implements Refactoring {

	private String variableName;

	@Override
	public Node visit(VariableDeclarator declarator, Void args) {
		if (declarator.getNameAsString().equals(variableName)) {
			return null;
		}
		return declarator;

	}

	public void removeUnusedVariable(JSONObject issue, String projectPath) throws FileNotFoundException {
		String project = issue.getString("project");
		String component = issue.getString("component");
		String path = component.substring(project.length() + 1, component.length());
		String message = issue.getString("message");
		String name = StringUtils.substringBetween(message, "\"", "\"");
		FileInputStream in = new FileInputStream(projectPath + path);
		CompilationUnit compilationUnit = JavaParser.parse(in);
		System.out.println(compilationUnit.toString());
		this.setVariableName(name);
		this.visit(compilationUnit, null);
		System.out.println(compilationUnit.toString());

		/**
		 * Actually apply changes to the File
		 */
		PrintWriter out = new PrintWriter(projectPath + path);
		out.println(compilationUnit.toString());
		out.close();

	}

	private void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public String getCommitMessage() {
		return "Remove unused variable " + variableName;
	}

}
