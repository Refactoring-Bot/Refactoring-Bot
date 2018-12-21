package de.refactoringBot.model.javaparser;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.body.MethodDeclaration;

public class ParserRefactoring {

	private CompilationUnit unit;
	private List<MethodCallExpr> methodCalls;
	private MethodDeclaration method;
	private String javaFile;

	public CompilationUnit getUnit() {
		return unit;
	}

	public void setUnit(CompilationUnit unit) {
		this.unit = unit;
	}

	public List<MethodCallExpr> getMethodCall() {
		return methodCalls;
	}

	public void setMethodCall(List<MethodCallExpr> methodCalls) {
		this.methodCalls = methodCalls;
	}

	public void addMethodCall(MethodCallExpr methodCall) {
		this.methodCalls.add(methodCall);
	}

	public MethodDeclaration getMethod() {
		return method;
	}

	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}

	public String getJavaFile() {
		return javaFile;
	}

	public void setJavaFile(String javaFile) {
		this.javaFile = javaFile;
	}

}
