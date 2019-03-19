package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import static de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.StatementGraphNode.StatementGraphNodeType.CATCHNODE;
import static de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.StatementGraphNode.StatementGraphNodeType.ELSENODE;
import static de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.StatementGraphNode.StatementGraphNodeType.FINALLYNODE;
import static de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.StatementGraphNode.StatementGraphNodeType.IFNODE;
import static de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.StatementGraphNode.StatementGraphNodeType.TRYNODE;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.utils.SourceZip;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javassist.compiler.ast.MethodDecl;
import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.transform.Source;
import org.checkerframework.com.github.javaparser.ast.CompilationUnit;
import org.checkerframework.dataflow.cfg.CFGBuilder;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.ConditionalBlock;
import org.checkerframework.dataflow.cfg.block.ExceptionBlock;
import org.checkerframework.dataflow.cfg.block.RegularBlock;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.CaseNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.MarkerNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.VariableDeclarationNode;
import sun.jvm.hotspot.debugger.cdbg.Sym;

public class ExtractMethodUtil {
    // helper var for line to block map generation
    private static Integer currentTryIndex = 0;

    public static Symbol.ClassSymbol symbol = null;
    public static Symbol.VarSymbol varSymbol = null;
    public static Type type = null;

    // MARK: begin java parser
    public static ParseResult parseJava(String sourcePath) {
        JavaCompiler compiler =  JavacTool.create(); //ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(sourcePath);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, null, null, fileObjects);

        JavacTask javacTask = (JavacTask) task;
        javacTask.setProcessors(Arrays.asList(new DummyTypeProcessor()));
        SourcePositions sourcePositions = Trees.instance(javacTask).getSourcePositions();
        Iterable<? extends CompilationUnitTree> parseResult = null;
        try {
            parseResult = javacTask.parse();
            javacTask.analyze();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        return new ParseResult(sourcePositions, parseResult);
    }

    public static class ParseResult {
        public final SourcePositions sourcePositions;
        public final Iterable<? extends CompilationUnitTree> parseResult;

        private ParseResult(SourcePositions sourcePositions, Iterable<? extends CompilationUnitTree> parseResult) {
            this.sourcePositions = sourcePositions;
            this.parseResult = parseResult;
        }
    }
    // MARK: end java parser

    // MARK: begin line to block mapping

    public static Map<Long, LineMapBlock> getLineToBlockMapping(ControlFlowGraph cfg, LineMap lineMap) {
        Map<Long, LineMapBlock> lineMapping = new HashMap<>();
        Long currentLineNumber = 0L;
        for (Block block: ExtractMethodUtil.cleanUpOrderedBlocks(cfg.getDepthFirstOrderedBlocks())) {
            switch (block.getType()) {
                case SPECIAL_BLOCK:
                    break;
                case REGULAR_BLOCK:
                    RegularBlock regularBlock = (RegularBlock) block;
                    for (Node node : regularBlock.getContents()) {
                        currentLineNumber = ExtractMethodUtil.addLineNumber(lineMap, lineMapping, currentLineNumber, block, node);
                    }
                    break;
                case EXCEPTION_BLOCK:
                    ExceptionBlock exceptionBlock = (ExceptionBlock) block;
                    Node node = exceptionBlock.getNode();
                    currentLineNumber = ExtractMethodUtil.addLineNumber(lineMap, lineMapping, currentLineNumber, block, node);
                    break;
                case CONDITIONAL_BLOCK:
                    lineMapping.computeIfAbsent(currentLineNumber, k -> new LineMapBlock());
                    lineMapping.get(currentLineNumber).blocks.add(block.getId());
                    break;
            }
        }
        return lineMapping;
    }

    public static void getDummyClass() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = classLoader.getResource("DummyClass.java").getPath();
        ParseResult result = ExtractMethodUtil.parseJava(path);
        CompilationUnitTree tree = result.parseResult.iterator().next();
        Tree type = tree.getTypeDecls().get(0);
        Field f = null; //NoSuchFieldException
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) type;
        JCTree.JCVariableDecl var = (JCTree.JCVariableDecl) classDecl.defs.get(1);
        try {
            f = type.getClass().getDeclaredField("sym");
            f.setAccessible(true);
            ExtractMethodUtil.symbol = (Symbol.ClassSymbol) f.get(type);
            f = var.getClass().getDeclaredField("sym");
            f.setAccessible(true);
            ExtractMethodUtil.varSymbol = (Symbol.VarSymbol) f.get(var);
            ExtractMethodUtil.type = new Type.JCNoType();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Long addLineNumber(LineMap lineMap, Map<Long, LineMapBlock> lineMapping, Long currentLineNumber, Block block, Node node) {
        Long lineNumber = null;
        // handle try catch nodes
        if (node.getClass().equals(MarkerNode.class) && (node.getTree().getClass().equals(JCTree.JCTry.class))) {
            MarkerNode markerNode = (MarkerNode) node;
            String message = markerNode.getMessage();
            JCTree.JCTry tree = (JCTree.JCTry) markerNode.getTree();
            List<JCTree.JCCatch> catchBlocks = tree.catchers;
            if (message.startsWith("start of try statement")) {
                lineNumber = lineMap.getLineNumber(tree.pos);
            } else if (message.startsWith("start of try block")) {
                JCTree.JCBlock bodyBlock = tree.body;
                lineNumber = lineMap.getLineNumber(bodyBlock.pos);
            } else if (message.startsWith("end of try block")) {
                JCTree.JCBlock bodyBlock = tree.body;
                lineNumber = lineMap.getLineNumber(bodyBlock.endpos);
                ExtractMethodUtil.currentTryIndex = 0;
            } else if (message.startsWith("start of catch block")) {
                JCTree.JCBlock catchBlock = tree.catchers.get(ExtractMethodUtil.currentTryIndex).body;
                lineNumber = lineMap.getLineNumber(catchBlock.pos);
            } else if (message.startsWith("end of catch block")) {
                JCTree.JCBlock catchBlock = tree.catchers.get(ExtractMethodUtil.currentTryIndex).body;
                lineNumber = lineMap.getLineNumber(catchBlock.endpos);
                ExtractMethodUtil.currentTryIndex++;
            } else if (message.startsWith("start of finally block")) {
                JCTree.JCBlock finalBLock = tree.finalizer;
                lineNumber = lineMap.getLineNumber(finalBLock.pos);
            } else if (message.startsWith("end of finally block")) {
                JCTree.JCBlock finalBLock = tree.finalizer;
                lineNumber = lineMap.getLineNumber(finalBLock.endpos);
            }
        } else {
            lineNumber = ExtractMethodUtil.getLineNumber(lineMap, node);
        }
        if (lineNumber != null) {
            currentLineNumber = lineNumber;
        }
        lineMapping.computeIfAbsent(currentLineNumber, k -> new LineMapBlock());
        lineMapping.get(currentLineNumber).blocks.add(block.getId());
        return currentLineNumber;
    }

    private static Long getLineNumber(LineMap lineMap, Node node) {
        if (node.getInSource() && node.getTree() != null) {
            int pos = ((JCTree) node.getTree()).pos;
            if (pos != 0) {
                return lineMap.getLineNumber(pos);
            }
        }
        return null;
    }
    private static Map<Long, SortedSet<Long>> reverseLineToBlockMapping(Map<Long, LineMapBlock> lineMapping) {
        Map<Long, SortedSet<Long>> blockMapping = new HashMap<>();
        for (Map.Entry<Long, LineMapBlock> blocks : lineMapping.entrySet()) {
            for (Long block : blocks.getValue().blocks) {
                blockMapping.computeIfAbsent(block, k -> new TreeSet<>());
                blockMapping.get(block).add(blocks.getKey());
            }
        }
        return blockMapping;
    }

    public static Map<Long, SortedSet<Long>> getBlockToLineMapping(ControlFlowGraph cfg, LineMap lineMap) {
        return ExtractMethodUtil.reverseLineToBlockMapping(ExtractMethodUtil.getLineToBlockMapping(cfg, lineMap));
    }
    // MARK: end line to block mapping


    // MARK: begin create statement graph
    private static Map<Long, Block> mapBlocksToID(List<Block> orderedBlocks) {
        Map<Long, Block> map = new HashMap<>();
        for (Block block : orderedBlocks) {
            map.put(block.getId(), block);
        }
        return map;
    }

    private static StatementGraphNode addNextBlock(StatementGraphNode lastNode, StatementGraphNode parentNode, Block block, Map<Long, SortedSet<Long>> blockMapping, Block successor) {
        boolean exitNode = (successor == null) || (successor.getType().equals(Block.BlockType.SPECIAL_BLOCK) && ((SpecialBlock) successor).getSpecialType().equals(SpecialBlock.SpecialBlockType.EXIT));
        Set<Long> lineNumbers = blockMapping.get(block.getId());
        for (Long lineNumber : lineNumbers) {
            if (lastNode != null && lineNumber.equals(lastNode.linenumber)) {
                lastNode.cfgBlocks.add(block);
            } else {
                StatementGraphNode node = new StatementGraphNode();
                node.linenumber = lineNumber;
                node.cfgBlocks.add(block);
                node.type = StatementGraphNode.StatementGraphNodeType.REGULARNODE;
                parentNode.children.add(node);
                lastNode = node;
            }
        }
        lastNode.isExitNode = exitNode;
        return lastNode;
    }

    public static StatementGraphNode createStatementGraph(ControlFlowGraph cfg, Map<Long, LineMapBlock> lineMapping) {
        List<Block> orderedBlocks = ExtractMethodUtil.cleanUpOrderedBlocks(cfg.getDepthFirstOrderedBlocks());
        StatementGraphNode methodHead = new StatementGraphNode();

        long lastID = orderedBlocks.get(orderedBlocks.size() - 1).getId();
        Map<Long, SortedSet<Long>> blockMapping = ExtractMethodUtil.reverseLineToBlockMapping(lineMapping);
        return createStatementGraphNodeRecursive(orderedBlocks, 1, lastID, methodHead, blockMapping, lineMapping);
    }

    private static List<Block> cleanUpOrderedBlocks(List<Block> orderedBlocks) {
        List<Block> cleanedUp = new ArrayList<>();
        for (int i = 0; i < orderedBlocks.size(); i++) {
            Block block = orderedBlocks.get(i);
            int index = orderedBlocks.lastIndexOf(block);
            if (index == i) {
                cleanedUp.add(block);
            } else {
                System.out.println("penis");
            }
        }
        return cleanedUp;
    }

    private static StatementGraphNode createStatementGraphNodeRecursive(List<Block> orderedBlocks, int index, long exitID, StatementGraphNode parentNode, Map<Long, SortedSet<Long>> blockMapping, Map<Long, LineMapBlock> lineMapping) {
        StatementGraphNode lastNode = parentNode;
        Map<Long, Block> orderedBlocksMap = ExtractMethodUtil.mapBlocksToID(orderedBlocks);
        while (orderedBlocks.get(index).getId() != exitID) {
            Block nextBlock = orderedBlocks.get(index);
            System.out.println("adding block with index " + index + " id: " + nextBlock.getId());
            switch (nextBlock.getType()) {
                case SPECIAL_BLOCK:
                    break;
                case CONDITIONAL_BLOCK:
                    ConditionalBlock conditionalBlock = (ConditionalBlock) nextBlock;
                    // find the block which is the first successor of both paths
                    Set<Long> visitedBlocks = new HashSet<>();
                    Long realSuccessor = ExtractMethodUtil.findSuccessor(conditionalBlock, orderedBlocksMap, visitedBlocks);
                    // check which successor is the next in the ordered blocks
                    long nextID = orderedBlocks.get(index + 1).getId();
                    long newExitID = (conditionalBlock.getThenSuccessor().getId() == nextID) ? conditionalBlock.getElseSuccessor().getId() : nextID;
                    lastNode.type = StatementGraphNode.StatementGraphNodeType.IFNODE;
                    lastNode.isNestingNode = true;
                    ExtractMethodUtil.createStatementGraphNodeRecursive(orderedBlocks, ++index, newExitID, lastNode, blockMapping, lineMapping);
                    index = orderedBlocks.indexOf(orderedBlocksMap.get(newExitID));
                    // travel else successor
                    if (realSuccessor != null && newExitID != realSuccessor) {
                        StatementGraphNode elseNode = new StatementGraphNode();
                        elseNode.code = lastNode.code;
                        elseNode.linenumber = lastNode.linenumber;
                        elseNode.cfgBlocks = lastNode.cfgBlocks;
                        elseNode.type = StatementGraphNode.StatementGraphNodeType.ELSENODE;
                        elseNode.isNestingNode = true;
                        parentNode.children.add(ExtractMethodUtil.createStatementGraphNodeRecursive(orderedBlocks, index, realSuccessor, elseNode, blockMapping, lineMapping));
                        index = orderedBlocks.indexOf(orderedBlocksMap.get(realSuccessor));
                    }
                    // lower index by one because we increment in the end of the loop and for conditional blocks this is already the next index
                    index--;
                    break;
                case REGULAR_BLOCK:
                    RegularBlock regularBlock = (RegularBlock) nextBlock;
                    lastNode = ExtractMethodUtil.addNextBlock(lastNode, parentNode, nextBlock, blockMapping, regularBlock.getRegularSuccessor());
                    break;
                case EXCEPTION_BLOCK:
                    ExceptionBlock exceptionBlock = (ExceptionBlock) nextBlock;
                    lastNode = ExtractMethodUtil.addNextBlock(lastNode, parentNode, nextBlock, blockMapping, exceptionBlock.getSuccessor());
                    break;
            }

            if (++index >= orderedBlocks.size()) {
                break;
            }
        }
        return parentNode;
    }

    private static Long findSuccessor(ConditionalBlock block, Map<Long, Block> orderedBlocksMap, Set<Long> visitedBlocks) {
        // find all ordered successors of thenBlock
        Set<Long> visitedThenBlocks = visitedBlocks;
        visitedThenBlocks.add(block.getId());
        List<Long> thenSuccessors = ExtractMethodUtil.findSuccessors(block.getThenSuccessor(), orderedBlocksMap, visitedThenBlocks);
        // find all ordered successors of ifBlock
        Set<Long> visitedElseBlocks = visitedBlocks;
        visitedElseBlocks.add(block.getId());
        List<Long> elseSuccessors = ExtractMethodUtil.findSuccessors(block.getElseSuccessor(), orderedBlocksMap, visitedElseBlocks);
        // compare all successors
        Set<Long> thenSuccessorsSet = new HashSet<>(thenSuccessors);
        for (Long id : elseSuccessors) {
            if (thenSuccessorsSet.contains(id)) {
                return id;
            }
        }
        return null;
    }

    private static List<Long> findSuccessors(Block block, Map<Long, Block> orderedBlocksMap, Set<Long> visitedBlocks) {
        Block nextBlock = block;
        List<Long> successors = new ArrayList<>();
        while (nextBlock != null && nextBlock.getType() != Block.BlockType.SPECIAL_BLOCK && !visitedBlocks.contains(nextBlock.getId())) {
            successors.add(nextBlock.getId());
            visitedBlocks.add(nextBlock.getId());
            switch (nextBlock.getType()) {
                case SPECIAL_BLOCK:
                    // should never happen
                    break;
                case EXCEPTION_BLOCK:
                    nextBlock = ((ExceptionBlock) nextBlock).getSuccessor();
                    break;
                case REGULAR_BLOCK:
                    nextBlock = ((RegularBlock) nextBlock).getRegularSuccessor();
                    break;
                case CONDITIONAL_BLOCK:
                    ConditionalBlock conditionalBlock = (ConditionalBlock) nextBlock;
                    Long nextID = ExtractMethodUtil.findSuccessor(conditionalBlock, orderedBlocksMap, visitedBlocks);
                    if (nextID != null) {
                        nextBlock = orderedBlocksMap.get(nextID);
                    } else {
                        nextBlock = null;
                    }
            }
        }
        return successors;
    }
    // MARK: end create statement graph

    // MARK: begin analyse try catch
    private static StatementGraphNode findParentNode(StatementGraphNode graph, StatementGraphNode node) {
        for (StatementGraphNode childNode : graph.children) {
            if (childNode.equals(node)) {
                return graph;
            }
            StatementGraphNode childSearch = ExtractMethodUtil.findParentNode(childNode, node);
            if (childSearch != null) { return childSearch; }
        }
        return null;
    }

    private static StatementGraphNode findNodeForLine(StatementGraphNode graph, Long lineNumber) {
        for (StatementGraphNode node : graph.children) {
            if (node.linenumber.equals(lineNumber)) {
                return node;
            }
            StatementGraphNode childSearch = ExtractMethodUtil.findNodeForLine(node, lineNumber);
            if (childSearch != null) { return childSearch; }
        }
        return null;
    }

    public static void analyseTryCatch(ControlFlowGraph cfg, StatementGraphNode graph, LineMap lineMap) {
        List<Block> orderedBlocks = ExtractMethodUtil.cleanUpOrderedBlocks(cfg.getDepthFirstOrderedBlocks());
        for (Block block : orderedBlocks) {
            switch (block.getType()) {
                case REGULAR_BLOCK:
                    RegularBlock regularBlock = (RegularBlock) block;
                    for (Node node : regularBlock.getContents()) {
                        ExtractMethodUtil.analyseTryCatchNode(node, graph, lineMap);
                    }
                    break;
                case EXCEPTION_BLOCK:
                    ExceptionBlock exceptionBlock = (ExceptionBlock) block;
                    ExtractMethodUtil.analyseTryCatchNode(exceptionBlock.getNode(), graph, lineMap);
                    break;
                default:
                    break;
            }
        }
    }

    private static void analyseTryCatchNode(Node node, StatementGraphNode graph, LineMap lineMap) {
        if (node.getClass().equals(MarkerNode.class)) {
            MarkerNode markerNode = (MarkerNode) node;
            String message = markerNode.getMessage();
            if (message.startsWith("start of try statement")) {
                JCTree.JCTry tree = (JCTree.JCTry) markerNode.getTree();
                JCTree.JCBlock bodyBlock = tree.body;
                List<JCTree.JCCatch> catchBlocks = tree.catchers;
                JCTree.JCBlock finalBLock = tree.finalizer;
                LineRange tryRange = new LineRange(lineMap.getLineNumber(bodyBlock.pos), lineMap.getLineNumber(bodyBlock.endpos));
                List<LineRange> catchRanges = new ArrayList<>();
                for (JCTree.JCCatch catchBlock : catchBlocks) {
                    JCTree.JCBlock block = catchBlock.body;
                    LineRange catchRange = new LineRange(lineMap.getLineNumber(block.pos), lineMap.getLineNumber(block.endpos));
                    catchRanges.add(catchRange);
                }
                LineRange finalRange = new LineRange(lineMap.getLineNumber(finalBLock.pos), lineMap.getLineNumber(finalBLock.endpos));
                // alter statement graph wit new try catch structure
                StatementGraphNode tryStartNode = ExtractMethodUtil.findNodeForLine(graph, tryRange.from);
                tryStartNode.type = TRYNODE;
                tryStartNode.isNestingNode = true;
                List<StatementGraphNode> catchStartNodes = new ArrayList<>();
                for (LineRange catchRange : catchRanges) {
                    StatementGraphNode catchStartNode = ExtractMethodUtil.findNodeForLine(graph, catchRange.from);
                    catchStartNode.type = CATCHNODE;
                    catchStartNode.isNestingNode = true;
                    catchStartNodes.add(catchStartNode);
                }
                Long endLineNumber = (finalBLock != null) ? finalRange.to : catchRanges.get(catchRanges.size() - 1).to;
                StatementGraphNode finallyStartNode = ExtractMethodUtil.findNodeForLine(graph, finalRange.from);
                finallyStartNode.type = FINALLYNODE;
                finallyStartNode.isNestingNode = true;

                for (Long lineNumber = tryRange.from; lineNumber <= endLineNumber; lineNumber++) {
                    if (lineNumber > tryRange.from && lineNumber < catchRanges.get(0).from) {
                        StatementGraphNode inTryNode = ExtractMethodUtil.findNodeForLine(graph, lineNumber);
                        if (inTryNode != null){
                            ExtractMethodUtil.findParentNode(graph, inTryNode).children.remove(inTryNode);
                            tryStartNode.children.add(inTryNode);
                        }
                    }
                    for (int catchIndex = 0; catchIndex < catchRanges.size(); catchIndex++) {
                        Long endLine = (catchRanges.size() - 1 == catchIndex) ? finalRange.from : catchRanges.get(catchIndex + 1).from;
                        if (lineNumber > catchRanges.get(catchIndex).from && lineNumber < endLine) {
                            StatementGraphNode inCatchNode = ExtractMethodUtil.findNodeForLine(graph, lineNumber);
                            if (inCatchNode != null) {
                                ExtractMethodUtil.findParentNode(graph, inCatchNode).children.remove(inCatchNode);
                                catchStartNodes.get(catchIndex).children.add(inCatchNode);
                            }
                        }
                    }
                    if (lineNumber > finalRange.from && lineNumber <= finalRange.to) {
                        StatementGraphNode inFinalNode = ExtractMethodUtil.findNodeForLine(graph, lineNumber);
                        if (inFinalNode != null) {
                            ExtractMethodUtil.findParentNode(graph, inFinalNode).children.remove(inFinalNode);
                            finallyStartNode.children.add(inFinalNode);
                        }
                    }
                }
            }
        }
    }
    // MARK: end analyse try catch

    // MARK: begin analyse data flow
    public static Set<LocalVariable> findLocalVariables(ControlFlowGraph cfg) {
        Set<LocalVariable> localVariables = new HashSet<>();
        List<Block> blocks = ExtractMethodUtil.cleanUpOrderedBlocks(cfg.getDepthFirstOrderedBlocks());
        for (Block block : blocks) {
            switch (block.getType()) {
                case EXCEPTION_BLOCK: {
                    Node node = ((ExceptionBlock) block).getNode();
                    LocalVariable variable = ExtractMethodUtil.getLocalVariables(node);
                    if (variable != null) localVariables.add(variable);
                    break;
                }
                case REGULAR_BLOCK: {
                    for (Node node : ((RegularBlock) block).getContents()) {
                        LocalVariable variable = ExtractMethodUtil.getLocalVariables(node);
                        if (variable != null) localVariables.add(variable);
                    }
                    break;
                }
                case SPECIAL_BLOCK:
                    break;
                case CONDITIONAL_BLOCK:
                    break;
            }
        }
        return localVariables;
    }

    private static LocalVariable getLocalVariables(Node node) {
        if (node.getClass().equals(VariableDeclarationNode.class)) {
            String name = ((VariableDeclarationNode)node).getName();
            String type = ((VariableDeclarationNode)node).getType().toString();
            return new LocalVariable(name, type);
        }
        return null;
    }

    public static Map<Long, LineMapVariable> analyseLocalDataFlow(ControlFlowGraph cfg, Set<LocalVariable> localVariables, LineMap lineMap) {
        Map<Long, LineMapVariable> map = new HashMap<>();
        SpecialBlock entryBlock = cfg.getEntryBlock();
        for (LocalVariable variable : localVariables) {
            ExtractMethodUtil.mapVariable(map, entryBlock, variable, null, lineMap, new HashSet<>());
        }
        return map;
    }

    private static void mapVariable(Map<Long, LineMapVariable> variableMap, Block block, LocalVariable variable, Long lastLine, LineMap lineMap, Set<Long> visitedBlocks) {
        if (block == null || visitedBlocks.contains(block.getId())) {
            return;
        }
        visitedBlocks.add(block.getId());
        switch (block.getType()) {
            case EXCEPTION_BLOCK:
                ExceptionBlock exceptionBlock = (ExceptionBlock) block;
                lastLine = ExtractMethodUtil.addNodeToMap(variableMap, exceptionBlock.getNode(), variable, lastLine, lastLine, lineMap);
                ExtractMethodUtil.mapVariable(variableMap, exceptionBlock.getSuccessor(), variable, lastLine, lineMap, visitedBlocks);
                break;
            case CONDITIONAL_BLOCK:
                ConditionalBlock conditionalBlock = (ConditionalBlock) block;
                ExtractMethodUtil.mapVariable(variableMap, conditionalBlock.getThenSuccessor(), variable, lastLine, lineMap, visitedBlocks);
                ExtractMethodUtil.mapVariable(variableMap, conditionalBlock.getElseSuccessor(), variable, lastLine, lineMap, visitedBlocks);
                break;
            case SPECIAL_BLOCK:
                SpecialBlock specialBlock = (SpecialBlock) block;
                if (specialBlock.getSpecialType() == SpecialBlock.SpecialBlockType.ENTRY) {
                    ExtractMethodUtil.mapVariable(variableMap, specialBlock.getSuccessor(), variable, lastLine, lineMap, visitedBlocks);
                }
                break;
            case REGULAR_BLOCK:
                RegularBlock regularBlock = (RegularBlock) block;
                Long newLine = lastLine;
                for (Node node : regularBlock.getContents()) {
                    Long copyLine = newLine;
                    newLine = ExtractMethodUtil.addNodeToMap(variableMap, node, variable, newLine, lastLine, lineMap);
                    lastLine = copyLine;
                }
                ExtractMethodUtil.mapVariable(variableMap, regularBlock.getRegularSuccessor(), variable, newLine, lineMap, visitedBlocks);
                break;
        }
    }

    private static Long addNodeToMap(Map<Long, LineMapVariable> variableMap, Node node, LocalVariable variable, Long lastLine, Long previousLine, LineMap lineMap) {
        Long newLine = lastLine;
        if (ExtractMethodUtil.nodeContainsVariable(node, variable)) {
            Long lineNumber = ExtractMethodUtil.getLineNumber(lineMap, node);
            if (ExtractMethodUtil.nodeIsAssignmentNode(node)) {
                newLine = lineNumber;
            } else {
                Long in = (lastLine != null && lineNumber != null && lineNumber > lastLine) ? lastLine : previousLine;
                // add in
                variableMap.computeIfAbsent(lineNumber, k -> new LineMapVariable());
                variableMap.get(lineNumber).in.computeIfAbsent(variable, k -> new HashSet<>());
                variableMap.get(lineNumber).in.get(variable).add(in);
                // add out
                variableMap.computeIfAbsent(lastLine, k -> new LineMapVariable());
                variableMap.get(in).out.computeIfAbsent(variable, k -> new HashSet<>());
                variableMap.get(in).out.get(variable).add(lineNumber);
                System.out.println(variableMap);
            }
        }
        return newLine;
    }

    private static boolean nodeIsAssignmentNode(Node node) {
        return node.getClass().equals(AssignmentNode.class);
    }

    private static boolean nodeContainsVariable(Node node, LocalVariable variable) {
        if (node.getClass().equals(LocalVariableNode.class)) {
            LocalVariableNode varNode = (LocalVariableNode) node;
            return varNode.getName().equals(variable.name);
        }
        if (node.getClass().equals(VariableDeclarationNode.class)) {
            VariableDeclarationNode decNode = (VariableDeclarationNode) node;
            return decNode.getName().equals(variable.name);
        }
        if (node.getClass().equals(AssignmentNode.class)) {
            AssignmentNode assNode = (AssignmentNode) node;
            return ExtractMethodUtil.nodeContainsVariable(assNode.getTarget(), variable);
        }
        return false;
    }
    // MARK: end analyse data flow

    // MARK: begin candidate generation
    public static List<RefactorCandidate> findCandidates(StatementGraphNode graph, Map<Long, LineMapVariable> variableMap, Map<Long, Long> breakContinueMap, List<Long> allLines, List<Long> commentLines, List<Long> emptyLines, LineMap lineMap, int minLineLength) {
        List<RefactorCandidate> candidates = new ArrayList<>();
        for (int outerIndex = 0; outerIndex < graph.children.size(); outerIndex++) {
            for (int innerIndex = graph.children.size() - 1; innerIndex >= outerIndex; innerIndex--) {

                RefactorCandidate potentialCandidate = new RefactorCandidate();
                potentialCandidate.startLine = graph.children.get(outerIndex).linenumber;
                Long lastLine = ExtractMethodUtil.getLastLine(graph.children.get(innerIndex));
                if (innerIndex < graph.children.size() - 1) {
                    potentialCandidate.endLine = ExtractMethodUtil.getRealLastLine(allLines, lastLine, commentLines, emptyLines);
                } else {
                    potentialCandidate.endLine = lastLine;
                }
                potentialCandidate.statements.addAll(ExtractMethodUtil.getStatements(graph, outerIndex, innerIndex));

                if (ExtractMethodUtil.isLongEnough(potentialCandidate, minLineLength) &&
                        ExtractMethodUtil.isValid(potentialCandidate, graph) &&
                        ExtractMethodUtil.isExtractable(potentialCandidate, variableMap, breakContinueMap, lineMap)) {
                    candidates.add(potentialCandidate);
                }
            }
            candidates.addAll(ExtractMethodUtil.findCandidates(graph.children.get(outerIndex), variableMap, breakContinueMap, allLines, commentLines, emptyLines, lineMap, minLineLength));
        }
        return candidates;
    }

    private static List<StatementGraphNode> getStatements(StatementGraphNode graph, int startIndex, int endIndex) {
        List<StatementGraphNode> statements = new ArrayList<>();
        for (int index = startIndex; index <= endIndex; index++) {
            statements.add(graph.children.get(index));
        }
        return statements;
    }

    private static Long getLastLine(StatementGraphNode node) {
        if (node.children.size() == 0) {
            return node.linenumber;
        } else {
            return ExtractMethodUtil.getLastLine(node.children.get(node.children.size() - 1));
        }
    }

    private static Long getRealLastLine(List<Long> allLines, Long lastLine, List<Long> commentLines, List<Long> emptyLines) {
        ArrayList<Long> allLinesClone = (ArrayList) ((ArrayList) allLines).clone();
        allLinesClone.removeIf(line -> line <= lastLine);
        if (!allLinesClone.isEmpty()) {
            Collections.sort(allLines);
            Long nextLine = allLinesClone.get(0);
            while (--nextLine > lastLine) {
                if (!(commentLines.contains(nextLine) || emptyLines.contains(nextLine))) {
                    return nextLine;
                }
            }
        }
        return lastLine;
    }


    // checks if the candidate contains only complete if/else and try/catch/finally statements
    private static boolean isValid(RefactorCandidate candidate, StatementGraphNode parentNode) {
        // check beginning of candidate
        StatementGraphNode firstNode = candidate.statements.get(0);
        StatementGraphNode.StatementGraphNodeType firstType = firstNode.type;

        if (firstType.equals(ELSENODE) ||
                firstType.equals(CATCHNODE) ||
                firstType.equals(FINALLYNODE)) {
            return false;
        }

        // check end of candidate
        StatementGraphNode lastNode = candidate.statements.get(candidate.statements.size() - 1);
        StatementGraphNode.StatementGraphNodeType lastType = lastNode.type;
        int parentIndex = parentNode.children.indexOf(lastNode);

        if (lastType.equals(IFNODE)|| lastType.equals(ELSENODE)) {
            if (parentIndex < parentNode.children.size() - 1 &&
                    parentNode.children.get(parentIndex + 1).type.equals(ELSENODE) ) {
                return false;
            }
        } else if (lastType.equals(TRYNODE) || lastType.equals(CATCHNODE)) {
            if (parentIndex < parentNode.children.size() - 1 &&
                    (parentNode.children.get(parentIndex + 1).type.equals(CATCHNODE) ||
                            parentNode.children.get(parentIndex + 1).type.equals(FINALLYNODE))) {
                return false;
            }
        }

        return true;
    }

    // checks if the candidate is long enough
    private static boolean isLongEnough(RefactorCandidate candidate, int minLineLength) {
        return (candidate.endLine - candidate.startLine) >= (minLineLength - 1);
    }

    // checks if the candidate has only one output parameter and continue, break or return are handled correct
    private static boolean isExtractable(RefactorCandidate candidate, Map<Long, LineMapVariable> variableMap, Map<Long, Long> breakContinueMap, LineMap lineMap) {
        // check output parameters
        Set<LocalVariable> outVariables = new HashSet<>();
        for (Long lineNumber = candidate.startLine; lineNumber <= candidate.endLine; lineNumber++) {
            if (variableMap.get(lineNumber) != null) {
                for (Map.Entry<LocalVariable, Set<Long>> variable : variableMap.get(lineNumber).out.entrySet()) {
                    for (Long outNumber : variable.getValue()) {
                        if (outNumber == null) {
                            outNumber = 0L;
                        }
                        if (outNumber > candidate.endLine) {
                            outVariables.add(variable.getKey());
                        }
                    }
                }
            }
        }
        candidate.outVariables.addAll(outVariables);
        if (outVariables.size() > 1) {
            return false;
        }
        // check return
        if (!ExtractMethodUtil.isExtractableReturnCheck(candidate)) { return false; }
        // check continue and break
        if (!ExtractMethodUtil.isExtractableContinueBreakCheck(candidate, breakContinueMap)) { return false; }
        // check switch case
        if (!ExtractMethodUtil.isExtractableSwitchCaseCheck(candidate, lineMap)) { return false; }

        return true;
    }

    private static boolean isExtractableSwitchCaseCheck(RefactorCandidate candidate, LineMap lineMap) {
        Set<Block> allBlocks = ExtractMethodUtil.getAllBlocks(candidate.statements);
        List<Node> allContent = ExtractMethodUtil.getAllContent(allBlocks);
        for (Node node : allContent) {
            if (node.getClass().equals(CaseNode.class)) {
                CaseNode caseNode = (CaseNode) node;
                if (!allBlocks.contains(caseNode.getSwitchOperand().getBlock())) {
                    return false;
                }
            }
            if (node.getClass().equals(MarkerNode.class)) {
                if (((MarkerNode) node).getMessage().contains("switch")) {
                    JCTree.JCSwitch switchTree = (JCTree.JCSwitch) node.getTree();
                    for (JCTree.JCCase caseNode : switchTree.cases) {
                        if (!candidate.containsLine(lineMap.getLineNumber(caseNode.pos))) {
                            return false;
                        }

                    }
                }
            }
        }
        return true;
    }

    private static boolean isExtractableContinueBreakCheck(RefactorCandidate candidate, Map<Long, Long> breakContinueMap) {
        if (breakContinueMap == null) {
            return true;
        }
        for (Map.Entry<Long, Long> breakContinueLine : breakContinueMap.entrySet()) {
            if (candidate.containsLine(breakContinueLine.getKey()) && !candidate.containsLine(breakContinueLine.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isExtractableReturnCheck(RefactorCandidate candidate) {
        Set<Block> blocks = ExtractMethodUtil.getAllBlocks(candidate.statements);
        Set<Long> ids = new HashSet<>();
        for (Block block : blocks) {
            ids.add(block.getId());
        }
        int regularSuccessor = 0;
        int exitSuccessor = 0;
        for (Block block : blocks) {
            List<Block> successors = new ArrayList<>();
            switch (block.getType()) {
                case CONDITIONAL_BLOCK:
                    ConditionalBlock condBlock = (ConditionalBlock) block;
                    successors.add(condBlock.getElseSuccessor());
                    successors.add(condBlock.getThenSuccessor());
                    break;
                case EXCEPTION_BLOCK:
                    ExceptionBlock exceptionBlock = (ExceptionBlock) block;
                    successors.add(exceptionBlock.getSuccessor());
                    break;
                case REGULAR_BLOCK:
                    RegularBlock regularBlock = (RegularBlock) block;
                    successors.add(regularBlock);
                    break;
                case SPECIAL_BLOCK:
                    break;
            }
            for (Block successor : successors) {
                if (successor == null || successor.getType().equals(Block.BlockType.SPECIAL_BLOCK)) {
                    exitSuccessor++;
                } else {
                    if (!ids.contains(successor.getId())) {
                        regularSuccessor++;
                    }
                }
            }
        }
        return !(regularSuccessor > 0 && exitSuccessor > 0);
    }
    private static Set<Block> getAllBlocks(List<StatementGraphNode> statements) {
        Set<Block> blocks = new HashSet<>();
        for (StatementGraphNode statement: statements) {
            blocks.addAll(statement.cfgBlocks);
            if (statement.children.size() > 0) {
                blocks.addAll(ExtractMethodUtil.getAllBlocks(statement.children));
            }
        }
        return blocks;
    }
    private static List<Node> getAllContent(Set<Block> blocks) {
        List<Node> allContent = new ArrayList<>();
        for (Block block : blocks) {
            switch (block.getType()) {
                case REGULAR_BLOCK:
                    RegularBlock regularBlock = (RegularBlock) block;
                    allContent.addAll(regularBlock.getContents());
                    break;
                case EXCEPTION_BLOCK:
                    ExceptionBlock exceptionBlock = (ExceptionBlock) block;
                    allContent.add(exceptionBlock.getNode());
                    break;
                default:
                    break;
            }
        }
        return allContent;
    }
    // MARK: end candidate generation

    public static List<Long> findEmptyLines(String sourcePath) throws IOException {
        List<Long> emptyLines = new ArrayList<>();
        Scanner scanner = new Scanner(new File(sourcePath));
        Long index = 0L;
        while(scanner.hasNextLine()) {
            index++;
            if (scanner.nextLine().replaceAll("\\s","").isEmpty()) {
                emptyLines.add(index);
            }
        }
        scanner.close();
        return emptyLines;
    }

    public static List<Long> findCommentLine(String sourcePath) throws IOException {
        List<Long> commentLines = new ArrayList<>();
        Scanner scanner = new Scanner(new File(sourcePath));
        Long index = 0L;
        boolean inCommentBlock = false;
        while(scanner.hasNextLine()) {
            index++;
            String line = scanner.nextLine().trim();
            if (inCommentBlock) {
                if (line.endsWith("*/")) {
                    commentLines.add(index);
                    inCommentBlock = false;
                } else if (line.contains("*/")) {
                    inCommentBlock = false;
                } else {
                    commentLines.add(index);
                }
            } else if (line.startsWith("//")) {
                commentLines.add(index);
            } else if (line.startsWith("/*")) {
                if (line.endsWith("*/")) {
                    commentLines.add(index);
                } else if (!line.contains("*/")) {
                    commentLines.add(index);
                    inCommentBlock = true;
                }
            }
        }
        scanner.close();
        return commentLines;
    }

    public static class BreakContinueVisitor extends TreeScanner<Map<Long, Long>, Void> {
        private final LineMap lineMap;

        public BreakContinueVisitor(LineMap lineMap) {
            this.lineMap = lineMap;
        }
        @Override
        public Map<Long, Long> visitBreak(BreakTree node, Void aVoid) {
            Map<Long, Long> breakMap = new HashMap<>();
            JCTree.JCBreak breakTree_ = (JCTree.JCBreak) node;
            Long breakLine = this.lineMap.getLineNumber(breakTree_.pos);
            Long outerLine = this.lineMap.getLineNumber(breakTree_.target.pos);
            breakMap.put(breakLine, outerLine);
            return breakMap;
        }

        @Override
        public Map<Long, Long> visitContinue(ContinueTree node, Void aVoid) {
            Map<Long, Long> continueMap = new HashMap<>();
            JCTree.JCContinue continueTree_ = (JCTree.JCContinue) node;
            Long breakLine = this.lineMap.getLineNumber(continueTree_.pos);
            if (continueTree_.target == null) {
                return null;
            }
            Long outerLine = this.lineMap.getLineNumber(continueTree_.target.pos);
            continueMap.put(breakLine, outerLine);
            return continueMap;
        }

        @Override
        public Map<Long, Long> reduce(Map<Long, Long> r1, Map<Long, Long> r2) {
            if (r1 == null) { return r2; }
            else if (r2 == null) { return r1; }

            r1.putAll(r2);
            return r1;
        }
    }


    public static class ClassVisitor extends TreeScanner<ClassTree, Void> {
        @Override
        public ClassTree visitClass(ClassTree node, Void aVoid) {
            return node;
        }
    }

    /*
    public static CFGContainer generateControlFlowGraph(CompilationUnitTree compilationUnitTree, SourcePositions sourcePositions, Long lineNumber) {
        List<ControlFlowGraph> graphs = new ArrayList();
        for (Tree _classTree : compilationUnitTree.getTypeDecls()) {
            ClassTree classTree = (ClassTree) _classTree;
            for (Tree _methodTree: classTree.getMembers()) {
                if (_methodTree.getClass().equals(MethodDecl.class)) {
                    MethodTree methodTree = (MethodTree) _methodTree;
                    ControlFlowGraph graph = CFGBuilder.build(compilationUnitTree, methodTree, classTree, DummyTypeProcessor.processingEnv);
                    graphs.add(graph);
                }
            }
        }
        return new CFGContainer(graphs.get(0), 0,0 );
    }*/

    public static class ControlFlowGraphGenerator extends TreeScanner<CFGContainer, Void> {
        private final CompilationUnitTree compilationUnitTree;
        private final Long lineNumber;
        private final SourcePositions sourcePositions;
        private final ClassTree classTree;

        public ControlFlowGraphGenerator(CompilationUnitTree compilationUnitTree, SourcePositions sourcePositions, Long lineNumber, ClassTree classTree) {
            this.compilationUnitTree = compilationUnitTree;
            this.lineNumber = lineNumber;
            this.sourcePositions = sourcePositions;
            this.classTree = classTree;
        }

        @Override
        public CFGContainer visitMethod(MethodTree node, Void aVoid) {
            LineMap lineMap = this.compilationUnitTree.getLineMap();
            long startPosition = sourcePositions.getStartPosition(compilationUnitTree, node);
            long startLine = lineMap.getLineNumber(startPosition);
            long endPosition = sourcePositions.getEndPosition(compilationUnitTree, node);
            long endLine = lineMap.getLineNumber(endPosition);

            if (startLine <= this.lineNumber && endLine >= this.lineNumber) {
                // generate cfg
                return new CFGContainer(CFGBuilder.build(this.compilationUnitTree, node, this.classTree, DummyTypeProcessor.processingEnv), startLine, endLine);
            } else {
                return null;
            }
        }

        @Override
        public CFGContainer reduce(CFGContainer r1, CFGContainer r2) {
            if (r1 != null) {
                return r1;
            }
            return r2;
        }
    }
}
