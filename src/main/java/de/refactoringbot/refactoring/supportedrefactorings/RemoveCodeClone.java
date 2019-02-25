package de.refactoringbot.refactoring.supportedrefactorings;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.refactoring.RefactoringImpl;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This refactoring class is used for removing code clones inside a java
 * project.
 *
 * @author Dennis Maseluk
 */
@Component
public class RemoveCodeClone extends ModifierVisitor<Void> implements RefactoringImpl {

	ArrayList<Node> cloneNodes = new ArrayList<Node>();

	String extractedMethodName = "extractedMethod";

	/**
	 * This method performs the refactoring and returns a commit message.
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
		path = gitConfig.getRepoFolder() + "/" + path;

		// Read file
		FileInputStream in = new FileInputStream(path);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

		// Search for clones
		ArrayList<Integer> searchResult = this.searchChildNodes(compilationUnit.getChildNodes().get(2).getChildNodes());

		// Remove clone nodes
		this.removeCodeClones(compilationUnit, searchResult);

		// Create extracted method
		this.createExtractedMethod(compilationUnit, path, searchResult);

		// Return commit message
		return "Removed code clones";
	}

	/*
	 * Search the code for clones without the information where they start.
	 */
	private ArrayList<Integer> searchChildNodes(List<Node> childNodes) {
		int range1 = 0;
		int range2 = 0;
		int cloneBegin1 = -1;
		int cloneBegin2 = -1;

		for (int u = 0; u < childNodes.size(); u++) {
			Node childNode1 = childNodes.get(u);

			for (int f = u; f < childNodes.size(); f++) {
				Node childNode2 = childNodes.get(f);

				// Skip same node
				if (childNode1.equals(childNode2)) {
					continue;
				}

				// Method declarations
				for (int j = 0; j < childNode1.getChildNodes().size(); j++) {
					Node child1 = childNode1.getChildNodes().get(j);

					if (child1.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {

						for (int i = j; i < childNode2.getChildNodes().size(); i++) {
							Node child2 = childNode2.getChildNodes().get(i);

							if (child2.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {

								// Lines of method
								for (Node c1 : child1.getChildNodes()) {
									for (Node c2 : child2.getChildNodes()) {

										// If lines are identical
										if (c1.getChildNodes().equals(c2.getChildNodes())) {
											if (cloneBegin2 == -1) {
												// Save begin of the clones
												cloneBegin1 = c1.getBegin().get().line;
												cloneBegin2 = c2.getBegin().get().line;
											}

											// Add comment line to range TODO erweitern auf mehr als eine Zeile
											if (c1.hasComment()
													&& c1.getBegin().get().line == cloneBegin1 + range1 + 1) {
												range1++;
											}
											if (c2.hasComment()
													&& c2.getBegin().get().line == cloneBegin2 + range2 + 1) {
												range2++;
											}
											
											if (c1.getBegin().get().line == cloneBegin1 + range1
													&& c2.getBegin().get().line == cloneBegin2 + range2) {
												// Keep clone nodes to write them into the new extracted method
												cloneNodes.add(c1);
												range1++;
												range2++;
											}
											break;
										}
									}
								}

							}
						}
					}
				}
			}
		}
		System.out.println("Clone Begin: " + cloneBegin1);
		System.out.println("Clone Begin2: " + cloneBegin2);
		System.out.println("Range1: " + range1);
		System.out.println("Range2: " + range2);
		ArrayList<Integer> searchResult = new ArrayList<Integer>();
		searchResult.add(cloneBegin1);
		searchResult.add(cloneBegin2);
		searchResult.add(range1);
		searchResult.add(range2);
		return searchResult;
	}

	/*
	 * Searches after the code clone nodes and removes the nodes. It adds also a
	 * method call for the extracted method at the first code clone line of the
	 * methods.
	 */
	private void removeCodeClones(CompilationUnit compilationUnit, ArrayList<Integer> searchResult) {

		ArrayList<Node> nodesToDelete1 = new ArrayList<Node>();
		ArrayList<Node> nodesToDelete2 = new ArrayList<Node>();

		for (Node node : compilationUnit.getChildNodes().get(2).getChildNodes()) {
			for (Node node2 : node.getChildNodes()) {
				if (node2.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {

					for (Node node3 : node2.getChildNodes()) {
						// searchResult[0] => cloneBeginLine1
						// searchResult[1] => cloneBeginLine2
						// searchResult[2] => range of Clone
						// Code Clone 1 nodes
						if (node3.getBegin().get().line >= searchResult.get(0)
								&& node3.getBegin().get().line < searchResult.get(0) + searchResult.get(2)) {
							nodesToDelete1.add(node3);
						}
						// Code Clone 2 nodes
						if (node3.getBegin().get().line >= searchResult.get(1)
								&& node3.getBegin().get().line < searchResult.get(1) + searchResult.get(3)) {
							nodesToDelete2.add(node3);
						}
					}

				}
			}

		}

		removeNodesAndAddMethodCall(compilationUnit, nodesToDelete1);
		removeNodesAndAddMethodCall(compilationUnit, nodesToDelete2);

	}

	/*
	 * Removes nodes and adds the method call for the extracted method.
	 */
	private void removeNodesAndAddMethodCall(CompilationUnit cu, ArrayList<Node> nodesToDelete) {
		boolean methodCallAdded = false;
		for (Node node : nodesToDelete) {
			if (!methodCallAdded) {
				// Add method call of extracted method
				cu.accept(new AddMethodCallVisitor(), node);
				methodCallAdded = true;
			} else {
				// visit and delete Clone nodes
				cu.accept(new RemoveNodeVisitor(), node);
			}
		}
	}

	/*
	 * Creates extracted method and writes the new file with the refactored code.
	 */
	private void createExtractedMethod(CompilationUnit compilationUnit, String path, ArrayList<Integer> searchResult)
			throws FileNotFoundException, IOException {
		// Go through all the types in the file
		NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
		for (TypeDeclaration<?> type : types) {

			// Add extracted method
			MethodDeclaration extractedMethod = type.addMethod(extractedMethodName, Modifier.Keyword.PRIVATE);
			BlockStmt block = new BlockStmt();
			for (Node n : cloneNodes) {
				// Add statements to the extracted method
				block.addStatement(n.toString());
			}
			extractedMethod.setBody(block);

		}
		System.out.println("CompilationUnit: ");
		System.out.println(compilationUnit.toString());

		// Write into file
		PrintWriter out = new PrintWriter(path);
		out.println(compilationUnit);
		out.close();
	}

	// ------------------------------------------------------------------------------------------

	/**
	 * Visitor implementation for removing nodes.
	 */
	class RemoveNodeVisitor extends ModifierVisitor<Node> {
		@Override
		public Visitable visit(ExpressionStmt n, Node args) {
			if (n == args) {
				return null;
			}
			return super.visit(n, args);
		}
	}

	/**
	 * Visitor implementation for adding method call of the extracted method.
	 */
	class AddMethodCallVisitor extends ModifierVisitor<Node> {
		@Override
		public Visitable visit(ExpressionStmt n, Node args) {
			if (n == args) {
				return new ExpressionStmt(new MethodCallExpr(new ThisExpr(), extractedMethodName));
			}
			return super.visit(n, args);
		}
	}

}