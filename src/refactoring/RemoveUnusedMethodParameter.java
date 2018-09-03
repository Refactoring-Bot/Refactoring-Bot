package refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class RemoveUnusedMethodParameter extends VoidVisitorAdapter implements Refactoring{
	int line;
	String name;

	@Override
	public void visit(MethodDeclaration declaration,Object arg) {
		if(line == declaration.getName().getBegin().get().line) {
			NodeList<Parameter> parameters = declaration.getParameters();
			parameters.remove(declaration.getParameterByName(name).get());
			declaration.setParameters(parameters);		
		}

		
	}
	public void removeUnusedMethodParameter(JSONObject issue, String projectPath) throws FileNotFoundException {
		String project = issue.getString("project");
		String component = issue.getString("component");
		String path = component.substring(project.length() + 1, component.length());
		String message = issue.getString("message");
		name = StringUtils.substringBetween(message, "\"", "\"");
		line = issue.getInt("line");
		FileInputStream in = new FileInputStream(projectPath + path);
		CompilationUnit compilationUnit = JavaParser.parse(in);
		System.out.println(compilationUnit.toString());
		this.visit(compilationUnit, null);
		System.out.println(compilationUnit.toString());
		
		/**
		 * Actually apply changes to the File 
		 */
		
		 PrintWriter out = new PrintWriter(projectPath + path);
		 out.println(compilationUnit.toString());
		 out.close();
		 
	}
	@Override
	public String getCommitMessage() {

		return  "Remove unused method parameter";
	}
}
