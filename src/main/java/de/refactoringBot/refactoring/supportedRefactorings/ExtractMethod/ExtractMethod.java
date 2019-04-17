package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.sun.source.tree.*;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.RefactoringImpl;
import javax.lang.model.element.TypeElement;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.DOTCFGVisualizer;
import org.checkerframework.dataflow.constantpropagation.Constant;
import org.checkerframework.dataflow.constantpropagation.ConstantPropagationStore;
import org.checkerframework.dataflow.constantpropagation.ConstantPropagationTransfer;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * This refactoring class is used for renaming methods inside a java project.
 *
 * @author Stefan Basaric
 */
@Component
public class ExtractMethod implements RefactoringImpl {

	private CFGContainer cfgContainer;
	private LineMap lineMap;

	// constants
	private final int minLineLength = 3;

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
			- handle function parameter in data flow
			- PARTIALLY DONE - handle empty line / unrecognized lines with closing brackets in candidates
			- handle data flow for assignment nodes
			- return extractions
			- extraction of child statements
			- nesting area scoring?
			- method naming
	 */

	public String refactorMethod(String sourcePath, Integer lineNumber) {
		// parse Java
		ExtractMethodUtil.ParseResult parseResult = ExtractMethodUtil.parseJava(sourcePath);
		ExtractMethodUtil.getDummyClass();

		for (CompilationUnitTree compilationUnitTree : parseResult.parseResult) {
			// get classTree
			ClassTree classTree = compilationUnitTree.accept(new ExtractMethodUtil.ClassVisitor(), null);
			// get cfg
			this.cfgContainer = compilationUnitTree.accept(new ExtractMethodUtil.ControlFlowGraphGenerator(compilationUnitTree, parseResult.sourcePositions, Long.valueOf(lineNumber), classTree), null);
			//this.cfgContainer = ExtractMethodUtil.generateControlFlowGraph(compilationUnitTree, parseResult.sourcePositions, Long.valueOf(lineNumber));
			if (this.cfgContainer.cfg != null) {
				this.lineMap = compilationUnitTree.getLineMap();

				Map<Long, LineMapBlock> lineMapping = ExtractMethodUtil.getLineToBlockMapping(this.cfgContainer.cfg, this.lineMap);
				List<Long> allLines = new ArrayList<>(lineMapping.keySet());

				// generate statement graph
				StatementGraphNode graph = ExtractMethodUtil.createStatementGraph(this.cfgContainer.cfg, lineMapping);

				// add try catch structure to statement graph
				ExtractMethodUtil.analyseTryCatch(this.cfgContainer.cfg, graph, this.lineMap);

				// add data flow to statement graph
				Set<LocalVariable> localVariables = ExtractMethodUtil.findLocalVariables(this.cfgContainer.cfg);
				Map<Long, LineMapVariable> variableMap = ExtractMethodUtil.analyseLocalDataFlow(this.cfgContainer.cfg, localVariables, this.lineMap);

				// find empty and comment lines
				List<Long> emptyLines = new ArrayList<>();
				List<Long> commentLines = new ArrayList<>();
				try {
					emptyLines = ExtractMethodUtil.findEmptyLines(sourcePath);
					commentLines = ExtractMethodUtil.findCommentLine(sourcePath);
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				// find candidates
				Map<Long, Long> breakContinueMap = compilationUnitTree.accept(new ExtractMethodUtil.BreakContinueVisitor(this.lineMap), null);
				List<RefactorCandidate> candidates = ExtractMethodUtil.findCandidates(graph, variableMap, breakContinueMap, allLines, commentLines, emptyLines, this.lineMap, this.minLineLength);

				// get best candidate
				ExtractMethodCandidateSelector selector = new ExtractMethodCandidateSelector(graph, candidates, variableMap, commentLines, emptyLines, this.cfgContainer.startLine, this.cfgContainer.endLine);
				RefactorCandidate bestCandidate = selector.selectBestCandidate();

				/*
				try {
					MethodExtractor methodExtractor = new MethodExtractor(bestCandidate, sourcePath);
					methodExtractor.apply();
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				}*/

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
}

