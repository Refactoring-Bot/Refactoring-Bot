package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.ast.Node;

import java.util.ArrayList;

public class CloneBlock {
    long startLine;
    int range;
    long methodStartLine;
    String filePath;
    ArrayList<Node> literalNodes;
}
