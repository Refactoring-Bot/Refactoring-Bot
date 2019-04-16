package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.RefactoringImpl;

/**
 * Immediately returns an expression if the return statement directly follows an
 * assignment to a local variable. Throws an exception if the variable is used
 * more than once.<br>
 * 
 * Example:<br>
 * {@code int a = 5 * b;}<br>
 * {@code return a;} <br>
 * becomes <br>
 * {@code return 5 * b;}
 */
public class ImmediatelyReturnExpression implements RefactoringImpl {

	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws Exception {
		String issueFilePath = gitConfig.getRepoFolder() + File.separator + issue.getFilePath();
		int lineWithStmtToBeReturned = issue.getLine();

		FileInputStream in = new FileInputStream(issueFilePath);
		CompilationUnit cu = LexicalPreservingPrinter.setup(StaticJavaParser.parse(in));

		MethodDeclaration targetMethod = RefactoringHelper
				.getMethodDeclarationByLineNumberInMethod(lineWithStmtToBeReturned, cu);
		if (targetMethod == null) {
			throw new BotRefactoringException(
					"Could not find a method that contains the code position to be improved!");
		}

		VariableDeclarator variableDeclarator = findVariableDeclarator(lineWithStmtToBeReturned, cu);		
		Optional<Expression> initializer = variableDeclarator.getInitializer(); // expression to be returned later
		if (!initializer.isPresent()) {
			throw new BotRefactoringException("Could not find an initializer of the variable declarator.");
		}

		String variableName = variableDeclarator.getNameAsString();
		List<ReturnStmt> returnStmts = findCorrespondingReturnStmts(variableName, targetMethod);
		if (returnStmts.isEmpty()) {
			throw new BotRefactoringException(
					"Could not find the corresponding return statement for variable " + variableName + ".");
		}
		if (returnStmts.size() > 1) {
			throw new BotRefactoringException("Variable is used in more than one return statement.");
		}

		returnStmts.get(0).setExpression(StaticJavaParser.parseExpression(initializer.get().toString()));

		ExpressionStmt expression = RefactoringHelper.getExpressionStmtByLineNumber(lineWithStmtToBeReturned, cu);
		expression.remove();

		PrintWriter out = new PrintWriter(issueFilePath);
		out.println(LexicalPreservingPrinter.print(cu));
		out.close();

		return "Immediately return the expression assigned to variable '" + variableName + "'.";
	}

	protected VariableDeclarator findVariableDeclarator(int lineWithStmtToBeReturned, CompilationUnit cu) {
		VariableDeclarationExpr variableDeclarationExpr = RefactoringHelper
				.getVariableDeclarationExprByLineNumber(lineWithStmtToBeReturned, cu);
		if (variableDeclarationExpr.getVariables().size() > 1) {
			throw new UnsupportedOperationException(
					"Refactoring not yet supported for multiple variables declared at the same line.");
		}

		return variableDeclarationExpr.getVariable(0);
	}

	private List<ReturnStmt> findCorrespondingReturnStmts(String variableName, MethodDeclaration method) {
		List<ReturnStmt> result = new LinkedList<>();
		for (ReturnStmt returnStmt : method.findAll(ReturnStmt.class)) {
			Optional<Expression> returnExpr = returnStmt.getExpression();

			if (returnExpr.isPresent() && returnExpr.get().toString().equals(variableName)) {
				result.add(returnStmt);
			}
		}

		return result;
	}

}
