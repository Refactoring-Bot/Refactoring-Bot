package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.ArrayList;
import java.util.List;

public class LineMapBlock {
    List<Long> blocks = new ArrayList<>();
    StatementGraphNode.TryCatchMarker tryCatchMarker = StatementGraphNode.TryCatchMarker.NONE;
}
