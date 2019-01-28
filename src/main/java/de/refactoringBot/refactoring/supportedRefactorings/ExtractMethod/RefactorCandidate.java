package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.ArrayList;
import java.util.List;

public class RefactorCandidate {
    List<StatementGraphNode> statements = new ArrayList<>();
    Long startLine;
    Long endLine;
    Long score = 0L;

    public boolean containsLine(Long lineNumber) {
        return startLine <= lineNumber && endLine >= lineNumber;
    }
}
