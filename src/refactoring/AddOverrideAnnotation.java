package refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.json.JSONObject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * 
 * @author Timo Pfaff
 * 
 * Class for adding the Override Annotation 
 *
 */
public class AddOverrideAnnotation extends VoidVisitorAdapter{
	int line;
	
	@Override
	public void visit(MethodDeclaration declaration,Object arg) {

		if(line == declaration.getName().getBegin().get().line) {
			declaration.addMarkerAnnotation("Override");
		}

		
	}
	
	public void addOverrideAnnotation(JSONObject issue, String projectPath) throws FileNotFoundException {
		String project = issue.getString("project");
		String component = issue.getString("component");
		String path = component.substring(project.length() + 1, component.length());
		line = issue.getInt("line");
		FileInputStream in = new FileInputStream(projectPath + path);
		CompilationUnit compilationUnit = JavaParser.parse(in);
		System.out.println(compilationUnit.toString());
		this.visit(compilationUnit, null);
		System.out.println(compilationUnit.toString());
		
		/**
		 * Actually apply changes to the File 
		 */
		/*
		 PrintWriter out = new PrintWriter(projectPath + path);
		 out.println(compilationUnit.toString());
		 out.close();
		 */
	}
}
