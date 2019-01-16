package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.sun.source.tree.LineMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LineMapVariable {
    Map<String, Set<Long>> in = new HashMap<>();
    Map<String, Set<Long>> out = new HashMap<>();
}
