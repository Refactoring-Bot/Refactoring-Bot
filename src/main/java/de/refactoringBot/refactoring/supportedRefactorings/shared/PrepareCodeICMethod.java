package de.refactoringBot.refactoring.supportedRefactorings.shared;


import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;

public class PrepareCodeICMethod {
    String methodName;
    String returnValue;
    boolean isStatic;
    NodeList<Expression> params;

    public PrepareCodeICMethod(String methodName, String returnValue, NodeList<Expression> params, boolean isStatic) {
        this.methodName = methodName;
        this.returnValue = returnValue;
        this.params = params;
        this.isStatic = isStatic;
    }
}
