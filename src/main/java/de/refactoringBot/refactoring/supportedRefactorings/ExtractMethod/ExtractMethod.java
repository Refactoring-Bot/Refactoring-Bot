package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.RefactoringImpl;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.cfg.CFGBuilder;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.DOTCFGVisualizer;
import org.checkerframework.dataflow.cfg.block.*;
import org.checkerframework.dataflow.cfg.node.*;
import org.checkerframework.dataflow.constantpropagation.Constant;
import org.checkerframework.dataflow.constantpropagation.ConstantPropagationStore;
import org.checkerframework.dataflow.constantpropagation.ConstantPropagationTransfer;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.StatementGraphNode.StatementGraphNodeType.*;

/**
 * This refactoring class is used for renaming methods inside a java project.
 *
 * @author Stefan Basaric
 */
@Component
public class ExtractMethod implements RefactoringImpl {

	private CFGContainer cfgContainer;
	private LineMap lineMap;

	// helper var for line to block map generation
	private Integer currentTryIndex = 0;

	// constants
	private final int minLineLength = 3;

	private final double lengthScoreWeight = 0.1;
	private final double maxLineLengthScore = 30;
	private final double nestingScoreWeight = 1;
	private final double paramScoreWeight = 1;
	private final double maxParameterScore = 4;
	private final double semanticsScoreWeight = 1;
	private final double semanticsBeginningWeight = 2;

	private final String debugDir = "/Users/johanneshubert/Documents/projects/refactoring-bot/test";

	/**
	 * This method performs the refactoring and returns the a commit message.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws IOException
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws IOException {
		// Get filepath
		String path = issue.getFilePath();
		String sourcePath = gitConfig.getRepoFolder() + "/" + path;

		return this.refactorMethod(sourcePath, issue.getLine());
	}
	/*
		TODO:
			- handle special switch case
			- handle function parameter in data flow
			- PARTIALLY DONE - handle empty line / unrecognized lines with closing brackets in candidates
			- handle data flow for assignment nodes
			- WONT DO - goto
	 */

	public String refactorMethod(String sourcePath, Integer lineNumber) {
		// parse Java
		ParseResult parseResult = this.parseJava(sourcePath);

		for (CompilationUnitTree compilationUnitTree : parseResult.parseResult) {
			// get classTree
			ClassTree classTree = compilationUnitTree.accept(new ClassVisitor(), null);
			// get cfg
			this.cfgContainer = compilationUnitTree.accept(new ControlFlowGraphGenerator(compilationUnitTree, parseResult.sourcePositions, Long.valueOf(lineNumber), classTree), null);
			if (this.cfgContainer.cfg != null) {
				this.lineMap = compilationUnitTree.getLineMap();

				Map<Long, LineMapBlock> lineMapping = this.getLineToBlockMapping(this.cfgContainer.cfg, this.lineMap);
				List<Long> allLines = new ArrayList<>(lineMapping.keySet());

				// generate statement graph
				StatementGraphNode graph = this.createStatementGraph(this.cfgContainer.cfg, lineMapping);

				// add try catch structure to statement graph
				this.analyseTryCatch(this.cfgContainer.cfg, graph, this.lineMap);

				// add data flow to statement graph
				Set<String> localVariables = this.findLocalVariables(this.cfgContainer.cfg);
				Map<Long, LineMapVariable> variableMap = this.analyseLocalDataFlow(this.cfgContainer.cfg, localVariables, this.lineMap);

				// find empty and comment lines
				List<Long> emptyLines = new ArrayList<>();
				List<Long> commentLines = new ArrayList<>();
				try {
					emptyLines = this.findEmptyLines(sourcePath);
					commentLines = this.findCommentLine(sourcePath);
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				// find candidates
				Map<Long, Long> breakContinueMap = compilationUnitTree.accept(new BreakContinueVisitor(this.lineMap), null);
				List<RefactorCandidate> candidates = this.findCandidates(graph, variableMap, breakContinueMap, allLines, commentLines, emptyLines);

				// score each candidate
				this.scoreCandidates(graph, candidates, variableMap, commentLines, emptyLines);

				candidates.sort(new Comparator<RefactorCandidate>() {
                    @Override
                    public int compare(RefactorCandidate o1, RefactorCandidate o2) {
                        return -Double.compare(o1.score, o2.score);
                    }
                });

				RefactorCandidate bestCandidate = candidates.get(0);
				System.out.println(bestCandidate);

				// this.printGraphToFile(this.debugDir, this.cfgContainer.cfg);

				return "extracted method";
			}
		}

		/*
		// Save changes to file
		PrintWriter out = new PrintWriter(gitConfig.getRepoFolder() + "/" + path);
		out.println(compilationUnit.toString());
		out.close();
		*/

		return null;
	}

	private void printGraphToFile(String path, ControlFlowGraph cfg) {
		ConstantPropagationTransfer transfer = new ConstantPropagationTransfer();
		Analysis<Constant, ConstantPropagationStore, ConstantPropagationTransfer> analysis = new Analysis<>(transfer, DummyTypeProcessor.processingEnv);
		analysis.performAnalysis(cfg);
		DOTCFGVisualizer<Constant, ConstantPropagationStore, ConstantPropagationTransfer> visualizer = new DOTCFGVisualizer<>();
		Map<String, Object> args = new HashMap<>();
		args.put("outdir", path);
		args.put("checkerName", "");
		visualizer.init(args);
		Map<String, Object> graph = visualizer.visualize(cfg, cfg.getEntryBlock(), analysis);
		System.out.println(graph);
		System.out.println("done");
	}

	private List<Long> findEmptyLines(String sourcePath) throws IOException {
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

	private List<Long> findCommentLine(String sourcePath) throws IOException {
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

	// MARK: begin candidate scoring
	private void scoreCandidates(StatementGraphNode fullGraph, List<RefactorCandidate> candidates, Map<Long, LineMapVariable> variableMap, List<Long> commentLines, List<Long> emptyLines) {
		for (RefactorCandidate candidate : candidates) {
			double lengthScore = this.scoreLength(candidate, this.cfgContainer.startLine, this.cfgContainer.endLine);
			double nestingScore = this.scoreNesting(fullGraph, candidate);
			double parameterScore = this.scoreParameters(candidate, variableMap);
			double semanticScore = this.scoreSemantics(candidate, commentLines, emptyLines);
			candidate.score = lengthScore + nestingScore + parameterScore + semanticScore;
		}
	}

	private double scoreLength(RefactorCandidate candidate, long methodStartLine, long methodEndLine) {
		long lengthCandidate = candidate.endLine - candidate.startLine;
		long lengthRemainder = (methodEndLine - methodStartLine) - lengthCandidate;
		return this.lengthScoreWeight * (Math.min(Math.min(lengthCandidate, lengthRemainder), this.maxLineLengthScore));
	}

	private double scoreNesting(StatementGraphNode fullGraph, RefactorCandidate candidate) {
		long complexityMethod = this.calculateCognitiveComplexity(fullGraph.children, 1);
		long complexityCandidate = this.calculateCognitiveComplexity(candidate.statements, 1);
		StatementGraphNode remainderGraph = fullGraph;
		for (StatementGraphNode node : candidate.statements) {
			remainderGraph = this.removeNodeFromGraph(remainderGraph, node);
		}
		long complexityRemainder = this.calculateCognitiveComplexity(remainderGraph.children, 1);
		return this.nestingScoreWeight * (complexityMethod - Math.max(complexityCandidate, complexityRemainder));
	}

	private StatementGraphNode removeNodeFromGraph(StatementGraphNode originalGraph, StatementGraphNode node) {
		StatementGraphNode clonedGraph = originalGraph.clone();
		for (int i = 0; i < clonedGraph.children.size(); i++) {
			if (clonedGraph.children.get(i).linenumber.equals(node.linenumber)) {
				clonedGraph.children.remove(i);
				return clonedGraph;
			}
		}
		for (int i = 0; i < clonedGraph.children.size(); i++) {
			StatementGraphNode newNode = this.removeNodeFromGraph(clonedGraph.children.get(i), node);
			originalGraph.children.remove(i);
			originalGraph.children.add(i, newNode);
		}
		return clonedGraph;
	}

	private long calculateCognitiveComplexity(List<StatementGraphNode> graph, int depth) {
		long score = 0;
		for (StatementGraphNode node: graph) {
			if (node.isNestingNode) {
				score += depth;
				if (node.children.size() > 0) {
					score += this.calculateCognitiveComplexity(node.children, ++depth);
				}
			}
		}
		return score;
	}

	private double scoreParameters(RefactorCandidate candidate, Map<Long, LineMapVariable> variableMap) {
		// check input parameters
		Set<String> inVariables = new HashSet<>();
		for (Long lineNumber = candidate.startLine; lineNumber <= candidate.endLine; lineNumber++) {
			if (variableMap.get(lineNumber) != null) {
				for (Map.Entry<String, Set<Long>> variable : variableMap.get(lineNumber).in.entrySet()) {
					for (Long inNumber : variable.getValue()) {
						if (inNumber != null && inNumber < candidate.startLine) {
							inVariables.add(variable.getKey());
						}
					}
				}
			}
		}
		candidate.inVariables.addAll(inVariables);
		int numberOfInVars = inVariables.size();
		int numberOfOutVars = candidate.outVariables.size();
		return this.paramScoreWeight * (maxParameterScore - numberOfInVars - numberOfOutVars);
	}

	private double scoreSemantics(RefactorCandidate candidate, List<Long> commentLines, List<Long> emptyLines) {
        // lines at beginning
        int numberAtBeginning = 0;
        Long firstLine = candidate.startLine - 1;
        while (commentLines.contains(firstLine) || emptyLines.contains(firstLine)) {
            firstLine--;
            numberAtBeginning++;
        }
        // lines at end
        int numberAtEnd = 0;
        Long lastLine = candidate.endLine + 1;
        while (commentLines.contains(lastLine) || emptyLines.contains(lastLine)) {
            lastLine++;
            numberAtEnd++;
        }

        int linesAtBeginning = (numberAtBeginning > 0) ? 1 : 0;
        int linesAtEnd = (numberAtEnd > 0) ? 1 : 0;
		return this.semanticsScoreWeight * (this.semanticsBeginningWeight * (linesAtBeginning + numberAtBeginning) + linesAtEnd + numberAtEnd);
	}
	// MARK: end candidate scoring

	// MARK: begin candidate generation
	private List<RefactorCandidate> findCandidates(StatementGraphNode graph, Map<Long, LineMapVariable> variableMap, Map<Long, Long> breakContinueMap, List<Long> allLines, List<Long> commentLines, List<Long> emptyLines) {
		List<RefactorCandidate> candidates = new ArrayList<>();
		for (int outerIndex = 0; outerIndex < graph.children.size(); outerIndex++) {
			for (int innerIndex = graph.children.size() - 1; innerIndex >= outerIndex; innerIndex--) {

				RefactorCandidate potentialCandidate = new RefactorCandidate();
				potentialCandidate.startLine = graph.children.get(outerIndex).linenumber;
				Long lastLine = this.getLastLine(graph.children.get(innerIndex));
				if (innerIndex < graph.children.size() - 1) {
                    potentialCandidate.endLine = this.getRealLastLine(allLines, lastLine, commentLines, emptyLines);
                } else {
				    potentialCandidate.endLine = lastLine;
                }
				potentialCandidate.statements.addAll(this.getStatements(graph, outerIndex, innerIndex));

				if (this.isLongEnough(potentialCandidate) &&
						this.isValid(potentialCandidate, graph) &&
						this.isExtractable(potentialCandidate, variableMap, breakContinueMap)) {
					candidates.add(potentialCandidate);
				}
			}
			candidates.addAll(this.findCandidates(graph.children.get(outerIndex), variableMap, breakContinueMap, allLines, commentLines, emptyLines));
		}
		return candidates;
	}

	private List<StatementGraphNode> getStatements(StatementGraphNode graph, int startIndex, int endIndex) {
		List<StatementGraphNode> statements = new ArrayList<>();
		for (int index = startIndex; index <= endIndex; index++) {
			statements.add(graph.children.get(index));
		}
		return statements;
	}

	private Long getLastLine(StatementGraphNode node) {
		if (node.children.size() == 0) {
			return node.linenumber;
		} else {
			return this.getLastLine(node.children.get(node.children.size() - 1));
		}
	}

	private Long getRealLastLine(List<Long> allLines, Long lastLine, List<Long> commentLines, List<Long> emptyLines) {
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
	private boolean isValid(RefactorCandidate candidate, StatementGraphNode parentNode) {
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
	private boolean isLongEnough(RefactorCandidate candidate) {
		return (candidate.endLine - candidate.startLine) >= (this.minLineLength - 1);
	}

	// checks if the candidate has only one output parameter and continue, break or return are handled correct
	private boolean isExtractable(RefactorCandidate candidate, Map<Long, LineMapVariable> variableMap, Map<Long, Long> breakContinueMap) {
		// check output parameters
		Set<String> outVariables = new HashSet<>();
		for (Long lineNumber = candidate.startLine; lineNumber <= candidate.endLine; lineNumber++) {
			if (variableMap.get(lineNumber) != null) {
				for (Map.Entry<String, Set<Long>> variable : variableMap.get(lineNumber).out.entrySet()) {
					for (Long outNumber : variable.getValue()) {
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
		if (!this.isExtractableReturnCheck(candidate)) { return false; }
		// check continue and break
		if (!this.isExtractableContinueBreakCheck(candidate, breakContinueMap)) { return false; }
        // check switch case
        if (!this.isExtractableSwitchCaseCheck(candidate)) { return false; }

		return true;
	}

	private boolean isExtractableSwitchCaseCheck(RefactorCandidate candidate) {

	    return true;
    }

	private boolean isExtractableContinueBreakCheck(RefactorCandidate candidate, Map<Long, Long> breakContinueMap) {
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

	private boolean isExtractableReturnCheck(RefactorCandidate candidate) {
		Set<Block> blocks = this.getAllBlocks(candidate.statements);
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
				if (successor.getType().equals(Block.BlockType.SPECIAL_BLOCK)) {
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
	private Set<Block> getAllBlocks(List<StatementGraphNode> statements) {
		Set<Block> blocks = new HashSet<>();
		for (StatementGraphNode statement: statements) {
			blocks.addAll(statement.cfgBlocks);
			if (statement.children.size() > 0) {
				blocks.addAll(this.getAllBlocks(statement.children));
			}
		}
		return blocks;
	}
	// MARK: end candidate generation

	// MARK: begin analyse data flow
	private Set<String> findLocalVariables(ControlFlowGraph cfg) {
		Set<String> localVariables = new HashSet<>();
		List<Block> blocks = cfg.getDepthFirstOrderedBlocks();
		for (Block block : blocks) {
			switch (block.getType()) {
				case EXCEPTION_BLOCK: {
					Node node = ((ExceptionBlock) block).getNode();
					String variable = this.getLocalVariables(node);
					if (variable != null) localVariables.add(variable);
					break;
				}
				case REGULAR_BLOCK: {
					for (Node node : ((RegularBlock) block).getContents()) {
						String variable = this.getLocalVariables(node);
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

	private String getLocalVariables(Node node) {
		if (node.getClass().equals(VariableDeclarationNode.class)) {
			return ((VariableDeclarationNode)node).getName();
		}
		return null;
	}

	private Map<Long, LineMapVariable> analyseLocalDataFlow(ControlFlowGraph cfg, Set<String> localVariables, LineMap lineMap) {
		Map<Long, LineMapVariable> map = new HashMap<>();
		SpecialBlock entryBlock = cfg.getEntryBlock();
		for (String variable : localVariables) {
			this.mapVariable(map, entryBlock, variable, null, lineMap, new HashSet<>());
		}
		return map;
	}

	private void mapVariable(Map<Long, LineMapVariable> variableMap, Block block, String variable, Long lastLine, LineMap lineMap, Set<Long> visitedBlocks) {
		if (visitedBlocks.contains(block.getId())) {
			return;
		}
		visitedBlocks.add(block.getId());
		switch (block.getType()) {
			case EXCEPTION_BLOCK:
				ExceptionBlock exceptionBlock = (ExceptionBlock) block;
				lastLine = this.addNodeToMap(variableMap, exceptionBlock.getNode(), variable, lastLine, lastLine, lineMap);
				this.mapVariable(variableMap, exceptionBlock.getSuccessor(), variable, lastLine, lineMap, visitedBlocks);
				break;
			case CONDITIONAL_BLOCK:
				ConditionalBlock conditionalBlock = (ConditionalBlock) block;
				this.mapVariable(variableMap, conditionalBlock.getThenSuccessor(), variable, lastLine, lineMap, visitedBlocks);
				this.mapVariable(variableMap, conditionalBlock.getElseSuccessor(), variable, lastLine, lineMap, visitedBlocks);
				break;
			case SPECIAL_BLOCK:
				SpecialBlock specialBlock = (SpecialBlock) block;
				if (specialBlock.getSpecialType() == SpecialBlock.SpecialBlockType.ENTRY) {
					this.mapVariable(variableMap, specialBlock.getSuccessor(), variable, lastLine, lineMap, visitedBlocks);
				}
				break;
			case REGULAR_BLOCK:
				RegularBlock regularBlock = (RegularBlock) block;
				Long newLine = lastLine;
				for (Node node : regularBlock.getContents()) {
					Long copyLine = newLine;
					newLine = this.addNodeToMap(variableMap, node, variable, newLine, lastLine, lineMap);
					lastLine = copyLine;
				}
				this.mapVariable(variableMap, regularBlock.getRegularSuccessor(), variable, lastLine, lineMap, visitedBlocks);
				break;
		}
	}

	private Long addNodeToMap(Map<Long, LineMapVariable> variableMap, Node node, String variable, Long lastLine, Long previousLine, LineMap lineMap) {
		Long newLine = lastLine;
		if (this.nodeContainsVariable(node, variable)) {
			Long lineNumber = this.getLineNumber(lineMap, node);
			if (this.nodeIsAssignmentNode(node)) {
				newLine = lineNumber;
			} else {
				Long in = (lastLine != null && lineNumber > lastLine) ? lastLine : previousLine;
				// add in
				variableMap.computeIfAbsent(lineNumber, k -> new LineMapVariable());
				variableMap.get(lineNumber).in.computeIfAbsent(variable, k -> new HashSet<>());
				variableMap.get(lineNumber).in.get(variable).add(in);
				// add out
				variableMap.computeIfAbsent(lastLine, k -> new LineMapVariable());
				variableMap.get(in).out.computeIfAbsent(variable, k -> new HashSet<>());
				variableMap.get(in).out.get(variable).add(lineNumber);
			}
		}
		return newLine;
	}

	private boolean nodeIsAssignmentNode(Node node) {
		return node.getClass().equals(AssignmentNode.class);
	}

	private boolean nodeContainsVariable(Node node, String variable) {
		if (node.getClass().equals(LocalVariableNode.class)) {
			LocalVariableNode varNode = (LocalVariableNode) node;
			return varNode.getName().equals(variable);
		}
		if (node.getClass().equals(VariableDeclarationNode.class)) {
			VariableDeclarationNode decNode = (VariableDeclarationNode) node;
			return decNode.getName().equals(variable);
		}
		if (node.getClass().equals(AssignmentNode.class)) {
			AssignmentNode assNode = (AssignmentNode) node;
			return this.nodeContainsVariable(assNode.getTarget(), variable);
		}
		return false;
	}
	// MARK: end analyse data flow

	// MARK: begin analyse try catch
	private StatementGraphNode findParentNode(StatementGraphNode graph, StatementGraphNode node) {
		for (StatementGraphNode childNode : graph.children) {
			if (childNode.equals(node)) {
				return graph;
			}
			StatementGraphNode childSearch = this.findParentNode(childNode, node);
			if (childSearch != null) { return childSearch; }
		}
		return null;
	}

	private StatementGraphNode findNodeForLine(StatementGraphNode graph, Long lineNumber) {
		for (StatementGraphNode node : graph.children) {
			if (node.linenumber.equals(lineNumber)) {
				return node;
			}
			StatementGraphNode childSearch = this.findNodeForLine(node, lineNumber);
			if (childSearch != null) { return childSearch; }
		}
		return null;
	}

	private void analyseTryCatch(ControlFlowGraph cfg, StatementGraphNode graph, LineMap lineMap) {
		List<Block> orderedBlocks = cfg.getDepthFirstOrderedBlocks();
		for (Block block : orderedBlocks) {
			switch (block.getType()) {
				case REGULAR_BLOCK:
					RegularBlock regularBlock = (RegularBlock) block;
					for (Node node : regularBlock.getContents()) {
						this.analyseTryCatchNode(node, graph, lineMap);
					}
					break;
				case EXCEPTION_BLOCK:
					ExceptionBlock exceptionBlock = (ExceptionBlock) block;
					this.analyseTryCatchNode(exceptionBlock.getNode(), graph, lineMap);
					break;
				default:
					break;
			}
		}
	}

	private void analyseTryCatchNode(Node node, StatementGraphNode graph, LineMap lineMap) {
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
				StatementGraphNode tryStartNode = this.findNodeForLine(graph, tryRange.from);
				tryStartNode.type = TRYNODE;
				tryStartNode.isNestingNode = true;
				List<StatementGraphNode> catchStartNodes = new ArrayList<>();
				for (LineRange catchRange : catchRanges) {
					StatementGraphNode catchStartNode = this.findNodeForLine(graph, catchRange.from);
					catchStartNode.type = CATCHNODE;
					catchStartNode.isNestingNode = true;
					catchStartNodes.add(catchStartNode);
				}
				Long endLineNumber = (finalBLock != null) ? finalRange.to : catchRanges.get(catchRanges.size() - 1).to;
				StatementGraphNode finallyStartNode = this.findNodeForLine(graph, finalRange.from);
				finallyStartNode.type = FINALLYNODE;
				finallyStartNode.isNestingNode = true;

				for (Long lineNumber = tryRange.from; lineNumber <= endLineNumber; lineNumber++) {
					if (lineNumber > tryRange.from && lineNumber < catchRanges.get(0).from) {
						StatementGraphNode inTryNode = this.findNodeForLine(graph, lineNumber);
						if (inTryNode != null){
							this.findParentNode(graph, inTryNode).children.remove(inTryNode);
							tryStartNode.children.add(inTryNode);
						}
					}
					for (int catchIndex = 0; catchIndex < catchRanges.size(); catchIndex++) {
						Long endLine = (catchRanges.size() - 1 == catchIndex) ? finalRange.from : catchRanges.get(catchIndex + 1).from;
						if (lineNumber > catchRanges.get(catchIndex).from && lineNumber < endLine) {
							StatementGraphNode inCatchNode = this.findNodeForLine(graph, lineNumber);
							if (inCatchNode != null) {
								this.findParentNode(graph, inCatchNode).children.remove(inCatchNode);
								catchStartNodes.get(catchIndex).children.add(inCatchNode);
							}
						}
					}
					if (lineNumber > finalRange.from && lineNumber <= finalRange.to) {
						StatementGraphNode inFinalNode = this.findNodeForLine(graph, lineNumber);
						if (inFinalNode != null) {
							this.findParentNode(graph, inFinalNode).children.remove(inFinalNode);
							finallyStartNode.children.add(inFinalNode);
						}
					}
				}
			}
		}
	}
	// MARK: end analyse try catch

	// MARK: begin create statement graph
	private Map<Long, Block> mapBlocksToID(List<Block> orderedBlocks) {
		Map<Long, Block> map = new HashMap<>();
		for (Block block : orderedBlocks) {
			map.put(block.getId(), block);
		}
		return map;
	}

	private StatementGraphNode addNextBlock(StatementGraphNode lastNode, StatementGraphNode parentNode, Block block, Map<Long, SortedSet<Long>> blockMapping, Block successor) {
		boolean exitNode = (successor.getType().equals(Block.BlockType.SPECIAL_BLOCK) && ((SpecialBlock) successor).getSpecialType().equals(SpecialBlock.SpecialBlockType.EXIT));
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

	private StatementGraphNode createStatementGraph(ControlFlowGraph cfg, Map<Long, LineMapBlock> lineMapping) {
		List<Block> orderedBlocks = cfg.getDepthFirstOrderedBlocks();
		StatementGraphNode methodHead = new StatementGraphNode();

		long lastID = orderedBlocks.get(orderedBlocks.size() - 1).getId();
		Map<Long, SortedSet<Long>> blockMapping = this.reverseLineToBlockMapping(lineMapping);
		return createStatementGraphNodeRecursive(orderedBlocks, 1, lastID, methodHead, blockMapping, lineMapping);
	}

	private StatementGraphNode createStatementGraphNodeRecursive(List<Block> orderedBlocks, int index, long exitID, StatementGraphNode parentNode, Map<Long, SortedSet<Long>> blockMapping, Map<Long, LineMapBlock> lineMapping) {
		StatementGraphNode lastNode = parentNode;
		Map<Long, Block> orderedBlocksMap = this.mapBlocksToID(orderedBlocks);
		while (orderedBlocks.get(index).getId() != exitID) {
			Block nextBlock = orderedBlocks.get(index);
			switch (nextBlock.getType()) {
				case SPECIAL_BLOCK:
					break;
				case CONDITIONAL_BLOCK:
					ConditionalBlock conditionalBlock = (ConditionalBlock) nextBlock;
					// find the block which is the first successor of both paths
					Set<Long> visitedBlocks = new HashSet<>();
					Long realSuccessor = this.findSuccessor(conditionalBlock, orderedBlocksMap, visitedBlocks);
					// check which successor is the next in the ordered blocks
					long nextID = orderedBlocks.get(index + 1).getId();
					long newExitID = (conditionalBlock.getThenSuccessor().getId() == nextID) ? conditionalBlock.getElseSuccessor().getId() : nextID;
					lastNode.type = StatementGraphNode.StatementGraphNodeType.IFNODE;
					lastNode.isNestingNode = true;
					this.createStatementGraphNodeRecursive(orderedBlocks, ++index, newExitID, lastNode, blockMapping, lineMapping);
					index = orderedBlocks.indexOf(orderedBlocksMap.get(newExitID));
					// travel else successor
					if (realSuccessor != null && newExitID != realSuccessor) {
						StatementGraphNode elseNode = new StatementGraphNode();
						elseNode.code = lastNode.code;
						elseNode.linenumber = lastNode.linenumber;
						elseNode.cfgBlocks = lastNode.cfgBlocks;
						elseNode.type = StatementGraphNode.StatementGraphNodeType.ELSENODE;
						elseNode.isNestingNode = true;
						parentNode.children.add(this.createStatementGraphNodeRecursive(orderedBlocks, index, realSuccessor, elseNode, blockMapping, lineMapping));
						index = orderedBlocks.indexOf(orderedBlocksMap.get(realSuccessor));
					}
					// lower index by one because we increment in the end of the loop and for conditional blocks this is already the next index
					index--;
					break;
				case REGULAR_BLOCK:
					RegularBlock regularBlock = (RegularBlock) nextBlock;
					lastNode = this.addNextBlock(lastNode, parentNode, nextBlock, blockMapping, regularBlock.getRegularSuccessor());
					break;
				case EXCEPTION_BLOCK:
					ExceptionBlock exceptionBlock = (ExceptionBlock) nextBlock;
					lastNode = this.addNextBlock(lastNode, parentNode, nextBlock, blockMapping, exceptionBlock.getSuccessor());
					break;
			}

			if (++index >= orderedBlocks.size()) {
				break;
			}
		}
		return parentNode;
	}

	private Long findSuccessor(ConditionalBlock block, Map<Long, Block> orderedBlocksMap, Set<Long> visitedBlocks) {
		// find all ordered successors of thenBlock
		Set<Long> visitedThenBlocks = visitedBlocks;
		visitedThenBlocks.add(block.getId());
		List<Long> thenSuccessors = this.findSuccessors(block.getThenSuccessor(), orderedBlocksMap, visitedThenBlocks);
		// find all ordered successors of ifBlock
		Set<Long> visitedElseBlocks = visitedBlocks;
		visitedElseBlocks.add(block.getId());
		List<Long> elseSuccessors = this.findSuccessors(block.getElseSuccessor(), orderedBlocksMap, visitedElseBlocks);
		// compare all successors
		Set<Long> thenSuccessorsSet = new HashSet<>(thenSuccessors);
		for (Long id : elseSuccessors) {
			if (thenSuccessorsSet.contains(id)) {
				return id;
			}
		}
		return null;
	}

	private List<Long> findSuccessors(Block block, Map<Long, Block> orderedBlocksMap, Set<Long> visitedBlocks) {
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
					Long nextID = this.findSuccessor(conditionalBlock, orderedBlocksMap, visitedBlocks);
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

	// MARK: begin line to block mapping

	private Map<Long, LineMapBlock> getLineToBlockMapping(ControlFlowGraph cfg, LineMap lineMap) {
		Map<Long, LineMapBlock> lineMapping = new HashMap<>();
		Long currentLineNumber = 0L;
		for (Block block: cfg.getDepthFirstOrderedBlocks()) {
			switch (block.getType()) {
				case SPECIAL_BLOCK:
					break;
				case REGULAR_BLOCK:
					RegularBlock regularBlock = (RegularBlock) block;
					for (Node node : regularBlock.getContents()) {
						currentLineNumber = this.addLineNumber(lineMap, lineMapping, currentLineNumber, block, node);
					}
					break;
				case EXCEPTION_BLOCK:
					ExceptionBlock exceptionBlock = (ExceptionBlock) block;
					Node node = exceptionBlock.getNode();
					currentLineNumber = this.addLineNumber(lineMap, lineMapping, currentLineNumber, block, node);
					break;
				case CONDITIONAL_BLOCK:
					lineMapping.computeIfAbsent(currentLineNumber, k -> new LineMapBlock());
					lineMapping.get(currentLineNumber).blocks.add(block.getId());
					break;
			}
		}
		return lineMapping;
	}

	private Long addLineNumber(LineMap lineMap, Map<Long, LineMapBlock> lineMapping, Long currentLineNumber, Block block, Node node) {
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
				this.currentTryIndex = 0;
			} else if (message.startsWith("start of catch block")) {
				JCTree.JCBlock catchBlock = tree.catchers.get(this.currentTryIndex).body;
				lineNumber = lineMap.getLineNumber(catchBlock.pos);
			} else if (message.startsWith("end of catch block")) {
				JCTree.JCBlock catchBlock = tree.catchers.get(this.currentTryIndex).body;
				lineNumber = lineMap.getLineNumber(catchBlock.endpos);
				this.currentTryIndex++;
			} else if (message.startsWith("start of finally block")) {
				JCTree.JCBlock finalBLock = tree.finalizer;
				lineNumber = lineMap.getLineNumber(finalBLock.pos);
			} else if (message.startsWith("end of finally block")) {
				JCTree.JCBlock finalBLock = tree.finalizer;
				lineNumber = lineMap.getLineNumber(finalBLock.endpos);
			}
		} else {
			lineNumber = this.getLineNumber(lineMap, node);
		}
		if (lineNumber != null) {
			currentLineNumber = lineNumber;
		}
		lineMapping.computeIfAbsent(currentLineNumber, k -> new LineMapBlock());
		lineMapping.get(currentLineNumber).blocks.add(block.getId());
		return currentLineNumber;
	}

	private Long getLineNumber(LineMap lineMap, Node node) {
		if (node.getInSource() && node.getTree() != null) {
			int pos = ((JCTree) node.getTree()).pos;
			if (pos != 0) {
				return lineMap.getLineNumber(pos);
			}
		}
		return null;
	}
	private Map<Long, SortedSet<Long>> reverseLineToBlockMapping(Map<Long, LineMapBlock> lineMapping) {
		Map<Long, SortedSet<Long>> blockMapping = new HashMap<>();
		for (Map.Entry<Long, LineMapBlock> blocks : lineMapping.entrySet()) {
			for (Long block : blocks.getValue().blocks) {
				blockMapping.computeIfAbsent(block, k -> new TreeSet<>());
				blockMapping.get(block).add(blocks.getKey());
			}
		}
		return blockMapping;
	}

	private Map<Long, SortedSet<Long>> getBlockToLineMapping(ControlFlowGraph cfg, LineMap lineMap) {
		return this.reverseLineToBlockMapping(this.getLineToBlockMapping(cfg, lineMap));
	}
	// MARK: end line to block mapping

	// MARK: begin java parser
	private ParseResult parseJava(String sourcePath) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
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

	private class ParseResult {
		private final SourcePositions sourcePositions;
		private final Iterable<? extends CompilationUnitTree> parseResult;

		private ParseResult(SourcePositions sourcePositions, Iterable<? extends CompilationUnitTree> parseResult) {
			this.sourcePositions = sourcePositions;
			this.parseResult = parseResult;
		}
	}
	// MARK: end java parser

	private static class ClassVisitor extends TreeScanner<ClassTree, Void> {
		@Override
		public ClassTree visitClass(ClassTree node, Void aVoid) {
			return node;
		}
	}
	private static class BreakContinueVisitor extends TreeScanner<Map<Long, Long>, Void> {
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

	private static class ControlFlowGraphGenerator extends TreeScanner<CFGContainer, Void> {
		private final CompilationUnitTree compilationUnitTree;
		private final Long lineNumber;
		private final SourcePositions sourcePositions;
		private final ClassTree classTree;

		private ControlFlowGraphGenerator(CompilationUnitTree compilationUnitTree, SourcePositions sourcePositions, Long lineNumber, ClassTree classTree) {
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
				return super.visitMethod(node, aVoid);
			}
		}
	}
}

