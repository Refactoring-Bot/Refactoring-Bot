package de.refactoringBot.model.javaparser;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class ParserRefactoringNew {

	private List<String> classes = new ArrayList<>();
	private List<String> javaFiles = new ArrayList<>();
	private List<MethodDeclaration> methods = new ArrayList<>();
	private List<MethodCallExpr> methodCalls = new ArrayList<>();
	private List<String> methodSignatures = new ArrayList<>();
	
	public List<String> getClasses() {
		return classes;
	}
	
	public void setClasses(List<String> classes) {
		this.classes = classes;
	}
	
	public void addClass(String newClass) {
		this.classes.add(newClass);
	}
	
	public List<String> getJavaFiles() {
		return javaFiles;
	}
	
	public void setJavaFiles(List<String> javaFiles) {
		this.javaFiles = javaFiles;
	}
	
	public void addJavaFile(String javaFile) {
		this.javaFiles.add(javaFile);
	}
	
	public List<MethodDeclaration> getMethods() {
		return methods;
	}
	
	public void setMethods(List<MethodDeclaration> methods) {
		this.methods = methods;
	}
	
	public void addMethod(MethodDeclaration method) {
		this.methods.add(method);
	}
	
	public List<MethodCallExpr> getMethodCalls() {
		return methodCalls;
	}
	
	public void setMethodCalls(List<MethodCallExpr> methodCalls) {
		this.methodCalls = methodCalls;
	}
	
	public void addMethodCall(MethodCallExpr methodCall) {
		this.methodCalls.add(methodCall);
	}

	public List<String> getMethodSignatures() {
		return methodSignatures;
	}

	public void setMethodSignatures(List<String> methodSignatures) {
		this.methodSignatures = methodSignatures;
	}
	
	public void addMethodSignature(String methodSignature) {
		this.methodSignatures.add(methodSignature);
	}
	
}
