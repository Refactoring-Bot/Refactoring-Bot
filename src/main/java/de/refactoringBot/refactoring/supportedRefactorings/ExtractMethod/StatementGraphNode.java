package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.dataflow.cfg.block.*;

public class StatementGraphNode {
    public enum TryCatchMarker {
        STATEMENTTRY, STARTTRY, ENDTRY, STARTCATCH, ENDCATCH, NONE
    }
    public enum StatementGraphNodeType {
        REGULARNODE, IFNODE, ELSENODE, EXITNODE, TRYNODE, CATCHNODE
    }

    public String code;
    public Long linenumber;
    public List<Block> cfgBlocks = new ArrayList<>();
    public List<StatementGraphNode> children = new ArrayList<>();
    public StatementGraphNodeType type = StatementGraphNodeType.REGULARNODE;
    public boolean isExitNode = false;
}
