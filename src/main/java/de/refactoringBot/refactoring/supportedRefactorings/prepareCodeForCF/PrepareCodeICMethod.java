package de.refactoringBot.refactoring.supportedRefactorings.prepareCodeForCF;


import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;

import java.util.ArrayList;

public class PrepareCodeICMethod {
    String methodName;
    String returnValue;
    NodeList<Expression> params;

    public PrepareCodeICMethod(String methodName, String returnValue, NodeList<Expression> params) {
        this.methodName = methodName;
        this.returnValue = returnValue;
        this.params = params;
    }
}
