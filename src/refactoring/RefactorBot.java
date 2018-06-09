package refactoring;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.metamodel.VariableDeclaratorMetaModel;

public class RefactorBot {

	public static void refactor() throws FileNotFoundException {

		//CompilationUnit compilationUnit = JavaParser.parse(in);

		//FileInputStream in = new FileInputStream("c://Users/Timo/eclipse-workspace/Test/src/main/java/test/MainTest.java");
		//CompilationUnit compilationUnit = JavaParser.parse(in);
		//compilationUnit.accept(new MyVisitor(), null);
		
		//CompilationUnit compilationUnit = JavaParser.parse(in);
		//<ClassOrInterfaceDeclaration> classA = compilationUnit.getClassByName("A");
		//compilationUnit.accept(new MethodVisitor(), null);
		
		//System.out.println(compilationUnit);
		//Optional<ClassOrInterfaceDeclaration> classA = compilationUnit.getClassByName("Test");
		
		/*
		compilationUnit.findAll(Method.class).stream()
        .filter(c -> !c.isInterface()
                && c.isAbstract()
                && !c.getNameAsString().startsWith("Abstract"))
        .forEach(c -> {
            String oldName = c.getNameAsString();
            String newName = "Abstract" + oldName;
            System.out.println("Renaming class " + oldName + " into " + newName);
            c.setName(newName);
        });
        */
	}
	
	   private static class MethodVisitor extends VoidVisitorAdapter<Void> {
	        @Override
	        public void visit(MethodDeclaration n, Void arg) {
	            /* here you can access the attributes of the method.
	             this method will be called for all methods in this 
	             CompilationUnit, including inner class methods */
	            System.out.println(n.getName());
	            super.visit(n, arg);
	        }
	    }
	   

	  private static class MyVisitor extends ModifierVisitor<Void> {
		    @Override
		    public Node visit(VariableDeclarator declarator, Void args) {
		        if (declarator.getNameAsString().equals("notUsed")
		                // the initializer is optional, first check if there is one
		                && declarator.getInitializer().isPresent()) {
		            Expression expression = declarator.getInitializer().get();
		            // We're looking for a literal integer.
		            if (expression instanceof StringLiteralExpr) {
		                    return null;
		                
		            }
		        }
		        return declarator;
		    }
		}

	public static void main(String[] args) throws IOException {
		
		String path = "";
		int line;
		HttpGet httpGet =  new HttpGet("http://localhost:9000/api/issues/search?resolved=false&format=json");
		try(CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(httpGet);){
			System.out.println(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			//System.out.println(EntityUtils.toString(entity));
			JSONObject obj = new JSONObject(json);
			System.out.println(obj);
			JSONArray arr = obj.getJSONArray("issues");
			
			for(int i = 0; i< arr.length(); i++) {
				String rule = arr.getJSONObject(i).getString("rule");
				if(rule.equals("squid:S1068")) {
					String component = arr.getJSONObject(i).getString("component");
					path  = component.substring(10, component.length());
					line = arr.getJSONObject(i).getInt("line");
				}
			}
		FileInputStream in = new FileInputStream("c://Users/Timo/eclipse-workspace/Test/" + path);
		CompilationUnit compilationUnit = JavaParser.parse(in);
		compilationUnit.accept(new MyVisitor(), null);
		

		compilationUnit.findAll(VariableDeclarator.class).stream()
        .filter(f -> f.getNameAsString().equals("notUsed"))
        .forEach(f -> {
        });
	System.out.println(compilationUnit);
	/*PrintWriter out =  new PrintWriter("c://Users/Timo/eclipse-workspace/Test/" + path);
	out.println(compilationUnit.toString());
	out.close();
	*/
		/*
		BodyDeclaration<?> declaration = compilationUnit.getType(0).getMembers().get(0);
		FieldDeclaration variable = declaration.asFieldDeclaration();
		VariableDeclarator test = variable.getVariable(0);
		System.out.println(test.getName());
		Expression exp = test.getInitializer().get();
		if(exp instanceof StringLiteralExpr) {
			System.out.println(exp);
		}
		if(test.getNameAsString().equals("notUsed")) {
		
		}
		
		*/			
		
		
		//System.out.println(compilationUnit);		
	}

}
}

