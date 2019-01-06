package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.RefactoringImpl;
import org.checkerframework.dataflow.cfg.CFGBuilder;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.springframework.stereotype.Component;
import org.checkerframework.dataflow.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.*;
import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This refactoring class is used for renaming methods inside a java project.
 *
 * @author Stefan Basaric
 */
@Component
public class ExtractMethod extends ModifierVisitor<Void> implements RefactoringImpl {

	Integer line = 0;
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
		this.line = issue.getLine();

		// parse Java
		ParseResult parseResult = this.parseJava(gitConfig.getRepoFolder() + "/" + path);

		for (CompilationUnitTree compilationUnitTree : parseResult.parseResult) {
			compilationUnitTree.accept(new MethodRefactor(compilationUnitTree, parseResult.sourcePositions, issue.getLine()), null);
		}
		/*
		// Read file
		FileInputStream in = new FileInputStream(gitConfig.getRepoFolder() + "/" + path);
		CompilationUnit compilationUnit = JavaParser.parse(in);

		// Visit place in the code that needs refactoring
		visit(compilationUnit, null);

		// Save changes to file
		PrintWriter out = new PrintWriter(gitConfig.getRepoFolder() + "/" + path);
		out.println(compilationUnit.toString());
		out.close();
		*/
		return "extracted method";
	}

	public Node visit(MethodDeclaration md, Void arg) {
		Optional<Range> range = md.getRange();
		if (!range.isPresent()) {
			return md;
		}
		if (!(range.get().begin.line <= this.line && range.get().end.line >= this.line)) {
			return md;
		}
		System.out.println(md.toString());
		/*
		// find refactoring Candidates
		CompilationController controller = CompilationController
		CompilationUnitTree tree = CompilationUnitTree
		ControlFlowGraph cfg = CFGBuilder.build();

		this.findCandidates(md);*/

		return md;
	}

	private void findCandidates(MethodDeclaration md) {

	}

	private ParseResult parseJava(String sourcePath) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
		Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(sourcePath);
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, null, null, fileObjects);

		JavacTask javacTask = (JavacTask) task;
		SourcePositions sourcePositions = Trees.instance(javacTask).getSourcePositions();
		Iterable<? extends CompilationUnitTree> parseResult = null;
		try {
			parseResult = javacTask.parse();
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
		private final Integer lineNumber;
		private final SourcePositions sourcePositions;
		private final LineMap lineMap;
		private ClassTree classTree;

		private MethodRefactor(CompilationUnitTree compilationUnitTree, SourcePositions sourcePositions, Integer lineNumber) {
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
				System.out.println("found the method");
				Context context = new Context();
				ProcessingEnvironment env = JavacProcessingEnvironment.instance(context);
				ControlFlowGraph cfg = CFGBuilder.build(this.compilationUnitTree, node, this.classTree, env);
				System.out.println(cfg);
			}

			return super.visitMethod(node, aVoid);
		}

		@Override
		public Void visitClass(ClassTree node, Void aVoid) {
			this.classTree = node;
			return super.visitClass(node, aVoid);
		}
	}
}

