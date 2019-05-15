package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.ast.body.MethodDeclaration;

public class CodeCloneInfo {
    MethodDeclaration method1;
    MethodDeclaration method2;
    long clone1BeginLine;
    long clone2BeginLine;
    long clone1Range;
    long clone2Range;

    public CodeCloneInfo() {
    }

    public CodeCloneInfo(long clone1BeginLine, long clone2BeginLine, long clone1Range, long clone2Range) {
        this.clone1BeginLine = clone1BeginLine;
        this.clone2BeginLine = clone2BeginLine;
        this.clone1Range = clone1Range;
        this.clone2Range = clone2Range;
    }

    public CodeCloneInfo(MethodDeclaration method1, MethodDeclaration method2, long clone1BeginLine, long clone2BeginLine, long clone1Range, long clone2Range) {
        this.method1 = method1;
        this.method2 = method2;
        this.clone1BeginLine = clone1BeginLine;
        this.clone2BeginLine = clone2BeginLine;
        this.clone1Range = clone1Range;
        this.clone2Range = clone2Range;
    }
}
