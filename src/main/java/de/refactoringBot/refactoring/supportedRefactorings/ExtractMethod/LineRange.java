package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

public class LineRange {
    Long from = 0L;
    Long to = 0L;

    public LineRange(Long from, Long to) {
        this.from = from;
        this.to = to;
    }

    public boolean isInRange(Long line) {
        return line >= this.from && line <= this.to;
    }
}
