package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.ast.body.MethodDeclaration;

public class PreparedCodeCloneInfo {
    MethodDeclaration methodDeclaration;
    long startLine;
    long endLine;

    public PreparedCodeCloneInfo() {

    }

    public PreparedCodeCloneInfo(MethodDeclaration methodDeclaration, long startLine, long endLine) {
        this.methodDeclaration = methodDeclaration;
        this.startLine = startLine;
        this.endLine = endLine;
    }
}
