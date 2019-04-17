package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.tomcat.jni.Local;

public class RefactorCandidate {
    List<StatementGraphNode> statements = new ArrayList<>();
    Long startLine;
    Long endLine;
    double score = 0L;
    int nestingDepth = 0;
    Set<LocalVariable> inVariables = new HashSet<>();
    Set<LocalVariable> outVariables = new HashSet<>();

    public boolean containsLine(Long lineNumber) {
        return startLine <= lineNumber && endLine >= lineNumber;
    }
    public boolean containsLine(int lineNumber) {
        return startLine <= lineNumber && endLine >= lineNumber;
    }
    public boolean inVariablesContain(LocalVariable outVar) {
        for (LocalVariable inVar : this.inVariables) {
            if (inVar.type.equals(outVar.type) && inVar.name.equals(outVar.name)) {
                return true;
            }
        }
        return false;
    }
}
