package refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EnumSet;

import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;

public class ReorderModifier extends ModifierVisitor<Void> implements Refactoring{
	
	@Override
	public Node visit(FieldDeclaration declarator, Void args) {
		EnumSet<Modifier> modifiers = declarator.getModifiers();
		declarator.setModifiers(modifiers);
		return declarator;

	}
	
	public void reorderModifier(JSONObject issue, String projectPath) throws FileNotFoundException {
		String project = issue.getString("project");
		String component = issue.getString("component");
		String path = component.substring(project.length() + 1, component.length());
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
		return  "Reorder modifier";
	}
}
