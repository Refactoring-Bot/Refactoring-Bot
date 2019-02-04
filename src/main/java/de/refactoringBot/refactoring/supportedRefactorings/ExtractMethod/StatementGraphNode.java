package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.dataflow.cfg.block.*;

public class StatementGraphNode {
    public enum StatementGraphNodeType {
        REGULARNODE, IFNODE, ELSENODE, TRYNODE, CATCHNODE, FINALLYNODE
    }

    public String code;
    public Long linenumber;
    public List<Block> cfgBlocks = new ArrayList<>();
    public List<StatementGraphNode> children = new ArrayList<>();
    public StatementGraphNodeType type = StatementGraphNodeType.REGULARNODE;
    public boolean isExitNode = false;
    public boolean isNestingNode = false;

    public StatementGraphNode clone() {
        StatementGraphNode clone = new StatementGraphNode();
        clone.isNestingNode = this.isNestingNode;
        clone.isExitNode = this.isExitNode;
        clone.linenumber = this.linenumber;
        clone.code = this.code;
        clone.cfgBlocks = new ArrayList<>(this.cfgBlocks);
        clone.type = this.type;
        clone.children = new ArrayList<>();
        for (StatementGraphNode node : this.children) {
            clone.children.add(node.clone());
        }
        return clone;
    }
}
