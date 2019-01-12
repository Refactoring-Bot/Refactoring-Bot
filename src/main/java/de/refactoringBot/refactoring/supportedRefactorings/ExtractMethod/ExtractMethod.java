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
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.constantpropagation.Constant;
import org.checkerframework.dataflow.constantpropagation.ConstantPropagationStore;
import org.checkerframework.dataflow.constantpropagation.ConstantPropagationTransfer;
import org.springframework.stereotype.Component;

import javax.naming.ldap.Control;
import javax.swing.plaf.nimbus.State;
import javax.tools.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * This refactoring class is used for renaming methods inside a java project.
 *
 * @author Stefan Basaric
 */
@Component
public class ExtractMethod implements RefactoringImpl {
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

	public String refactorMethod(String sourcePath, Integer lineNumber) {
		// parse Java
		ParseResult parseResult = this.parseJava(sourcePath);

		for (CompilationUnitTree compilationUnitTree : parseResult.parseResult) {
			compilationUnitTree.accept(new MethodRefactor(compilationUnitTree, parseResult.sourcePositions, lineNumber), null);
		}

		/*
		// Save changes to file
		PrintWriter out = new PrintWriter(gitConfig.getRepoFolder() + "/" + path);
		out.println(compilationUnit.toString());
		out.close();
		*/

		return "extracted method";
	}

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

	private static class MethodRefactor extends TreeScanner<Void, Void> {
		private final CompilationUnitTree compilationUnitTree;
		private final int lineNumber;
		private final SourcePositions sourcePositions;
		private final LineMap lineMap;
		private ClassTree classTree;

		private MethodRefactor(CompilationUnitTree compilationUnitTree, SourcePositions sourcePositions, int lineNumber) {
			this.compilationUnitTree = compilationUnitTree;
			this.lineMap = compilationUnitTree.getLineMap();
			this.lineNumber = lineNumber;
			this.sourcePositions = sourcePositions;
		}

		@Override
		public Void visitMethod(MethodTree node, Void aVoid) {
			long startPosition = sourcePositions.getStartPosition(compilationUnitTree, node);
			long startLine = lineMap.getLineNumber(startPosition);
			long endPosition = sourcePositions.getEndPosition(compilationUnitTree, node);
			long endLine = lineMap.getLineNumber(endPosition);

			if (startLine <= this.lineNumber && endLine >= this.lineNumber) {
				ControlFlowGraph cfg = CFGBuilder.build(this.compilationUnitTree, node, this.classTree, DummyTypeProcessor.processingEnv);


				Map<Long, List<Long>> blockMap = this.getBlockToLineMapping(cfg, lineMap);

				StatementGraphNode graph = this.createStatementGraph(cfg, blockMap);

				System.out.println(graph);

				/* DEBUG
				ConstantPropagationTransfer transfer = new ConstantPropagationTransfer();
				Analysis<Constant, ConstantPropagationStore, ConstantPropagationTransfer> analysis = new Analysis<>(transfer, DummyTypeProcessor.processingEnv);
				analysis.performAnalysis(cfg);
				DOTCFGVisualizer<Constant, ConstantPropagationStore, ConstantPropagationTransfer> visualizer = new DOTCFGVisualizer<>();
				Map<String, Object> args = new HashMap<>();
				args.put("outdir", "/Users/johanneshubert/Documents/projects/refactoring-bot/test");
				args.put("checkerName", "");
				visualizer.init(args);
				Map<String, Object> graph = visualizer.visualize(cfg, cfg.getEntryBlock(), analysis);
				System.out.println(graph);
				System.out.println("done");
				 /* DEBUGEND */
			}

			return super.visitMethod(node, aVoid);
		}

		@Override
		public Void visitClass(ClassTree node, Void aVoid) {
			this.classTree = node;
			return super.visitClass(node, aVoid);
		}

		private StatementGraphNode createStatementGraph(ControlFlowGraph cfg, Map<Long, List<Long>> blockMapping) {
			List<Block> orderedBlocks = cfg.getDepthFirstOrderedBlocks();
			StatementGraphNode methodHead = new StatementGraphNode();
			long lastID = orderedBlocks.get(orderedBlocks.size() - 1).getId();
			return createStatementGraphNodeRecursive(orderedBlocks, 1, lastID, methodHead, blockMapping);
		}

		private StatementGraphNode createStatementGraphNodeRecursive(List<Block> orderedBlocks, int index, long exitID, StatementGraphNode parentNode, Map<Long, List<Long>> blockMapping) {
			StatementGraphNode lastNode = null;
			Map<Long, Block> orderedBlocksMap = this.mapBlocksToID(orderedBlocks);
			while (orderedBlocks.get(index).getId() != exitID) {
				Block nextBlock = orderedBlocks.get(index);
				switch (nextBlock.getType()) {
					case SPECIAL_BLOCK:
						break;
					case CONDITIONAL_BLOCK:
						ConditionalBlock conditionalBlock = (ConditionalBlock) nextBlock;
						// find the block which is the first successor of both paths
						Long realSuccessor = this.findSuccessor(conditionalBlock, orderedBlocksMap);
						// check which successor is the next in the ordered blocks
						long nextID = orderedBlocks.get(index + 1).getId();
						long newExitID = (conditionalBlock.getThenSuccessor().getId() == nextID) ? conditionalBlock.getElseSuccessor().getId() : nextID;
						lastNode.type = StatementGraphNode.StatementGraphNodeType.IFNODE;
						this.createStatementGraphNodeRecursive(orderedBlocks, ++index, newExitID, lastNode, blockMapping);
						index = orderedBlocks.indexOf(orderedBlocksMap.get(newExitID));
						// travel else successor
						if (realSuccessor != null && newExitID != realSuccessor) {
							StatementGraphNode elseNode = new StatementGraphNode();
							elseNode.code = lastNode.code;
							elseNode.linenumber = lastNode.linenumber;
							elseNode.cfgBlocks = lastNode.cfgBlocks;
							elseNode.type = StatementGraphNode.StatementGraphNodeType.ELSENODE;
							parentNode.children.add(this.createStatementGraphNodeRecursive(orderedBlocks, index, realSuccessor, elseNode, blockMapping));
							index = orderedBlocks.indexOf(orderedBlocksMap.get(realSuccessor));
						}
						// lower index buy one because we increment in the end of the loop
						index--;
						break;
					default:
						lastNode = this.addNextBlock(lastNode, parentNode, nextBlock, blockMapping);
						break;
				}

				if (++index >= orderedBlocks.size()) {
					break;
				}
			}
			return parentNode;
		}

		private Long findSuccessor(ConditionalBlock block, Map<Long, Block> orderedBlocksMap) {
			Set<Long> visitedBlocks = new HashSet<>();
			visitedBlocks.add(block.getId());
			// find all ordered successors of thenBlock
			List<Long> thenSuccessors = this.findSuccessors(block.getThenSuccessor(), orderedBlocksMap, visitedBlocks);
			// find all ordered successors of ifBlock
			List<Long> elseSuccessors = this.findSuccessors(block.getElseSuccessor(), orderedBlocksMap, visitedBlocks);
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
						Long nextID = this.findSuccessor(conditionalBlock, orderedBlocksMap);
						if (nextID != null) {
							nextBlock = orderedBlocksMap.get(nextID);
						} else {
							nextBlock = null;
						}
				}
			}
			return successors;
		}

		private Map<Long, Block> mapBlocksToID(List<Block> orderedBlocks) {
			Map<Long, Block> map = new HashMap<>();
			for (Block block : orderedBlocks) {
				map.put(block.getId(), block);
			}
			return map;
		}

		private StatementGraphNode addNextBlock(StatementGraphNode lastNode, StatementGraphNode parentNode, Block block, Map<Long, List<Long>> blockMapping) {
			List<Long> lineNumbers = blockMapping.get(block.getId());
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
			return lastNode;
		}

		private Map<Long, List<Long>> getLineToBlockMapping(ControlFlowGraph cfg, LineMap lineMap) {
			Map<Long, List<Long>> lineMapping = new HashMap<>();
			Long currentLineNumber = 0L;
			for (Block block: cfg.getDepthFirstOrderedBlocks()) {
				switch (block.getType()) {
					case SPECIAL_BLOCK:
						break;
					case REGULAR_BLOCK:
						RegularBlock regularBlock = (RegularBlock) block;
						for (Node node : regularBlock.getContents()) {
							currentLineNumber = addLineNumber(lineMap, lineMapping, currentLineNumber, block, node);
						}
						break;
					case EXCEPTION_BLOCK:
						ExceptionBlock exceptionBlock = (ExceptionBlock) block;
						Node node = exceptionBlock.getNode();
						currentLineNumber = addLineNumber(lineMap, lineMapping, currentLineNumber, block, node);
						break;
					case CONDITIONAL_BLOCK:
						lineMapping.computeIfAbsent(currentLineNumber, k -> new ArrayList<>());
						lineMapping.get(currentLineNumber).add(block.getId());
						break;
				}
			}
			return lineMapping;
		}

		private Long addLineNumber(LineMap lineMap, Map<Long, List<Long>> lineMapping, Long currentLineNumber, Block block, Node node) {
			if (node.getInSource() && node.getTree() != null) {
				int pos = ((JCTree) node.getTree()).pos;
				if (pos != 0) {
					currentLineNumber = lineMap.getLineNumber(pos);
				}
			}
			lineMapping.computeIfAbsent(currentLineNumber, k -> new ArrayList<>());
			lineMapping.get(currentLineNumber).add(block.getId());
			return currentLineNumber;
		}
		private Map<Long, List<Long>> reverseLineToBlockMapping(Map<Long, List<Long>> lineMapping) {
			Map<Long, List<Long>> blockMapping = new HashMap<>();
			for (Map.Entry<Long, List<Long>> blocks : lineMapping.entrySet()) {
				for (Long block : blocks.getValue()) {
					blockMapping.computeIfAbsent(block, k -> new ArrayList<>());
					blockMapping.get(block).add(blocks.getKey());
				}
			}
			// remove duplicate lines for each block
			for (Map.Entry<Long, List<Long>> blocks : blockMapping.entrySet()) {
				Set<Long> set = new HashSet<>(blocks.getValue());
				blocks.getValue().clear();
				blocks.getValue().addAll(set);
			}
			return blockMapping;
		}
		private Map<Long, List<Long>> getBlockToLineMapping(ControlFlowGraph cfg, LineMap lineMap) {
			return this.reverseLineToBlockMapping(this.getLineToBlockMapping(cfg, lineMap));
		}
	}
}

