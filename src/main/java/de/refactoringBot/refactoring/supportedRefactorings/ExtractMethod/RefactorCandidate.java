package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RefactorCandidate {
    List<StatementGraphNode> statements = new ArrayList<>();
    Long startLine;
    Long endLine;
    double score = 0L;
    Set<String> inVariables = new HashSet<>();
    Set<String> outVariables = new HashSet<>();

    public boolean containsLine(Long lineNumber) {
        return startLine <= lineNumber && endLine >= lineNumber;
    }
}
