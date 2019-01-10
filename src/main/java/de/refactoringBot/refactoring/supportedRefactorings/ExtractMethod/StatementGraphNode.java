package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.ArrayList;
import java.util.List;
import org.checkerframework.dataflow.cfg.block.*;

public class StatementGraphNode {
    public String code;
    public Long linenumber;
    public List<Block> cfgBlocks = new ArrayList<>();
    public List<StatementGraphNode> children = new ArrayList<>();
}
