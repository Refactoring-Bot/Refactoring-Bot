package de.unistuttgart.iste.refactoringbot.refactorings;

import de.unistuttgart.iste.refactoringbot.Refactoring;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

/**
 * 
 * @author Timo Pfaff
 *
 *         this class is used to execute the remove unused method parameter
 *         refactoring.
 *
 */
public class RemoveUnusedMethodParameter extends VoidVisitorAdapter implements Refactoring {
	int line;
	String parameterName;
	String methodName;

	@Override
	public void visit(MethodDeclaration declaration, Object arg) {
		if (line == declaration.getName().getBegin().get().line) {
			methodName = declaration.getNameAsString();
			NodeList<Parameter> parameters = declaration.getParameters();
			parameters.remove(declaration.getParameterByName(parameterName).get());
			declaration.setParameters(parameters);
		}

	}

	public void removeUnusedMethodParameter(JSONObject issue, String projectPath) throws FileNotFoundException {
		String project = issue.getString("project");
		String component = issue.getString("component");
		String path = component.substring(project.length() + 1, component.length());
		String message = issue.getString("message");
		parameterName = StringUtils.substringBetween(message, "\"", "\"");
		line = issue.getInt("line");
		FileInputStream in = new FileInputStream(projectPath + path);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));
		System.out.println(LexicalPreservingPrinter.print(compilationUnit));
		this.visit(compilationUnit, null);
		System.out.println(LexicalPreservingPrinter.print(compilationUnit));

		/**
		 * Actually apply changes to the File
		 */
		 PrintWriter out = new PrintWriter(projectPath + path);
		 out.println(LexicalPreservingPrinter.print(compilationUnit));
		 out.close();

	}

	@Override
	public String getCommitMessage() {

		return "Remove unused method parameter " + parameterName + " from method " + methodName;
	}

	@Override
	public void performRefactoring(JSONObject issue, String projectPath) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
	}
}
