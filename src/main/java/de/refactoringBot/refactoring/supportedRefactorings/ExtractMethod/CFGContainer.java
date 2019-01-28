package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import org.checkerframework.dataflow.cfg.ControlFlowGraph;

public class CFGContainer {
    public final ControlFlowGraph cfg;
    public final long startLine;
    public final long endLine;

    public CFGContainer(ControlFlowGraph cfg, long startLine, long endLine) {
        this.cfg = cfg;
        this.startLine = startLine;
        this.endLine = endLine;
    }
}
