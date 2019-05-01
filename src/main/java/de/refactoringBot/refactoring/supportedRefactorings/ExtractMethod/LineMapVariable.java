package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.sun.source.tree.LineMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LineMapVariable {
    public Map<LocalVariable, Set<Long>> in = new HashMap<>();
    Map<LocalVariable, Set<Long>> out = new HashMap<>();
}
