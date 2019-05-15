package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.Position;

public class LiteralInfo {
    String type;
    String value;
    Position position;

    public LiteralInfo(String type, String value, Position position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }
}
