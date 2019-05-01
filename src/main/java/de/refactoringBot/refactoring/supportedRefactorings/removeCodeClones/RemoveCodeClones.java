package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.RefactoringImpl;
import de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.*;
import de.refactoringBot.refactoring.supportedRefactorings.shared.PrepareCode;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

/**
 * This refactoring class is used for removing code clones inside a java
 * project.
 *
 * @author Dennis Maseluk
 */
@Component
public class RemoveCodeClones extends ModifierVisitor<Void> implements RefactoringImpl {

    ArrayList<Node> cloneNodes = new ArrayList<Node>();
    String tempFolderName = "/tempFolderForPreparedCode";

    String extractedMethodName = "extractedMethod";
    public Set<LocalVariable> extractedMethodOutVariables = new HashSet<>();

    private CFGContainer cfgContainer;
    private LineMap lineMap;
    // constants
    private final int minLineLength = 5;

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
        // Get filepath of original file
        String path = issue.getFilePath();
        path = gitConfig.getRepoFolder() + "/" + path;

        // Get file name of file that will be refactored
        String[] splitFilePath = issue.getFilePath().split("\\\\");
        String fileName = splitFilePath[splitFilePath.length - 1];

        // Create temp folder for prepared code file
        String pathForPreparedCode = gitConfig.getRepoFolder() + tempFolderName;
        new File(pathForPreparedCode).mkdirs();

        // Read file
        FileInputStream in = new FileInputStream(path);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

        // Search for clones in the original file
        CodeCloneInfo cloneInfo = new CodeCloneInfo();
        for (Node childNode : compilationUnit.getChildNodes()) {
            if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                cloneInfo = this.searchChildNodes(childNode.getChildNodes(), true);
            }
        }

        // If no clones were found
        if (cloneInfo == null) {
            return "No code clones found";
        }

        // Write new file with prepared code of the compilationUnit
        PrepareCode prepareCode = new PrepareCode();
        String fileContent = prepareCode.prepareCode(compilationUnit).toString();
        FileWriter fileWriter = new FileWriter(pathForPreparedCode + "/" + fileName);
        fileWriter.write(fileContent);
        fileWriter.close();

        // Get new compilationUnit of the new prepare code file
        FileInputStream in2 = new FileInputStream(pathForPreparedCode + "/" + fileName );
        CompilationUnit preparedCompilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in2));

        // Search the code clone in the prepared code file
        CodeCloneInfo cloneInfoOfPreparedCode = new CodeCloneInfo();
        for (Node childNode : preparedCompilationUnit.getChildNodes()) {
            if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                cloneInfoOfPreparedCode = this.searchChildNodes(childNode.getChildNodes(), false);
            }
        }

        // Get best candidate to extract code in the new prepared code file
        // for code clone 1
        RefactorCandidate bestCandidate1 = this.getCandidates(pathForPreparedCode + "/" + fileName,
                cloneInfoOfPreparedCode.clone1BeginLine, cloneInfoOfPreparedCode.clone1BeginLine + cloneInfoOfPreparedCode.clone1Range);
        // for code clone 2
        RefactorCandidate bestCandidate2 = this.getCandidates(pathForPreparedCode + "/" + fileName,
                cloneInfoOfPreparedCode.clone2BeginLine, cloneInfoOfPreparedCode.clone2BeginLine + cloneInfoOfPreparedCode.clone2Range);
        this.extractedMethodOutVariables = bestCandidate1.outVariables;

        // Calculate correct lines
        cloneInfo.clone1BeginLine = bestCandidate1.startLine + (cloneInfo.clone1BeginLine - cloneInfoOfPreparedCode.clone1BeginLine);
        cloneInfo.clone1Range = bestCandidate1.endLine - bestCandidate1.startLine;
        cloneInfo.clone2BeginLine = bestCandidate2.startLine + (cloneInfo.clone2BeginLine - cloneInfoOfPreparedCode.clone2BeginLine);
        cloneInfo.clone2Range = bestCandidate2.endLine - bestCandidate2.startLine;

        // Delete tempFolder and prepared code file
        try {
            Files.delete(Paths.get(pathForPreparedCode + "/" + fileName));
            Files.delete(Paths.get(pathForPreparedCode));
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", path);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", path);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }

        // Get original compUnit
        FileInputStream in3 = new FileInputStream(path);
        CompilationUnit originalCompUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in3));

        // Remove clone nodes
        this.removeCodeClones(originalCompUnit, cloneInfo);

        // Create extracted method
        this.createExtractedMethod(originalCompUnit, path);

        // Return commit message
        return "Removed code clones";
    }

    private RefactorCandidate getCandidates(String path, Long cloneBeginLine, Long cloneEndLine) {
        // parse Java
        ExtractMethodUtil.ParseResult parseResult = ExtractMethodUtil.parseJava(path);

        for (CompilationUnitTree compilationUnitTree : parseResult.parseResult) {
            // get classTree
            ClassTree classTree = compilationUnitTree.accept(new ExtractMethodUtil.ClassVisitor(), null);
            // get cfg
            this.cfgContainer = compilationUnitTree.accept(new ExtractMethodUtil.ControlFlowGraphGenerator(compilationUnitTree, parseResult.sourcePositions, cloneBeginLine, classTree), null);
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
                    emptyLines = ExtractMethodUtil.findEmptyLines(path);
                    commentLines = ExtractMethodUtil.findCommentLine(path);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // find candidates
                Map<Long, Long> breakContinueMap = compilationUnitTree.accept(new ExtractMethodUtil.BreakContinueVisitor(this.lineMap), null);
                List<RefactorCandidate> candidates = ExtractMethodUtil.findCandidates(graph, variableMap, breakContinueMap, allLines, commentLines, emptyLines, this.lineMap, this.minLineLength);

                // get best candidate
                RemoveCodeClonesCandidateSelector selector = new RemoveCodeClonesCandidateSelector(graph, candidates, variableMap, commentLines, emptyLines,
                        this.cfgContainer.startLine, this.cfgContainer.endLine, cloneBeginLine, cloneEndLine);
                RefactorCandidate bestCandidate = selector.selectBestCandidate();

                return bestCandidate;
            }
        }
        return null; // TODO null Rückgabe abfangen
    }

    /*
     * Search the code for clones without the information where they start.
     */
    private CodeCloneInfo searchChildNodes(List<Node> childNodes, boolean keepCloneNodes) {

        // Get all methods
        ArrayList<MethodDeclaration> methods = new ArrayList<>();
        for (Node node : childNodes) {
            if (node.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                MethodDeclaration method = (MethodDeclaration) node;
                methods.add(method);
            }
        }

        // Search all methods for clones
        CodeCloneInfo cloneInfo = new CodeCloneInfo();
        boolean foundClone = false;
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                cloneInfo = this.compareMethods(methods.get(i), methods.get(j), keepCloneNodes);
                if (cloneInfo != null) {
                    foundClone = true;
                    break;
                }
            }
            if (foundClone) {
                break;
            }
        }
        return cloneInfo;
    }

    private CodeCloneInfo compareMethods(MethodDeclaration method1, MethodDeclaration method2, boolean keepCloneNodes) {
        CodeCloneInfo cloneInfo = new CodeCloneInfo(-1, -1, 0, 0);

        int method1LineCount = 0;
        if (method1.getBegin().isPresent() && method1.getEnd().isPresent()) {
            method1LineCount = method1.getEnd().get().line - method1.getBegin().get().line;
        }
        int method2LineCount = 0;
        if (method2.getBegin().isPresent() && method2.getEnd().isPresent()) {
            method2LineCount = method2.getEnd().get().line - method2.getBegin().get().line;
        }

        // Lines of method
        for (Node c1 : method1.getBody().get().getStatements()) {
            for (Node c2 : method2.getBody().get().getStatements()) {

                // If there is a return statement, dont add it to the clones
                if (c1.getMetaModel() == JavaParserMetaModel.returnStmtMetaModel || c2.getMetaModel() == JavaParserMetaModel.returnStmtMetaModel) {
                    break;
                }

                // If lines are identical
                if (c1.getChildNodes().equals(c2.getChildNodes())) {
                    if (cloneInfo.clone2BeginLine == -1) {
                        // Save begin of the clones
                        cloneInfo.clone1BeginLine = c1.getBegin().get().line;
                        cloneInfo.clone2BeginLine = c2.getBegin().get().line;
                    }

                    // Add comment lines to range
                    if (c1.hasComment()) {
                        for (int commentLineCount = 1; commentLineCount < method1LineCount; commentLineCount++) {
                            if (c1.getBegin().get().line == cloneInfo.clone1BeginLine + cloneInfo.clone1Range + commentLineCount) {
                                cloneInfo.clone1Range = cloneInfo.clone1Range + commentLineCount;
                                break;
                            }
                        }
                    }
                    if (c2.hasComment()) {
                        for (int commentLineCount = 1; commentLineCount < method1LineCount; commentLineCount++) {
                            if (c2.getBegin().get().line == cloneInfo.clone2BeginLine + cloneInfo.clone2Range + commentLineCount) {
                                cloneInfo.clone2Range = cloneInfo.clone2Range + commentLineCount;
                            }
                        }
                    }

                    if (c1.getBegin().get().line == cloneInfo.clone1BeginLine + cloneInfo.clone1Range
                            && c2.getBegin().get().line == cloneInfo.clone2BeginLine + cloneInfo.clone2Range) {
                        // Keep clone nodes to write them into the new extracted method
                        if (keepCloneNodes) {
                            cloneNodes.add(c1);
                        }
                        cloneInfo.clone1Range++;
                        cloneInfo.clone2Range++;
                    }
                    break;
                }
            }
        }

        cloneInfo.clone1Range--;
        cloneInfo.clone2Range--;

        // Consider only clones that have more than 10 lines
        if (cloneInfo.clone1Range < 10 || cloneInfo.clone2Range < 10) {
            // Remove all cloneNodes if there are not meet the line requirement
            if (keepCloneNodes) {
                cloneNodes.clear();
            }
            return null;
        } else {
            return cloneInfo;
        }
    }

    /*
     * Searches after the code clone nodes and removes the nodes. It adds also a
     * method call for the extracted method at the first code clone line of the
     * methods.
     */
    private void removeCodeClones(CompilationUnit compilationUnit, CodeCloneInfo cloneInfo) {

        ArrayList<Node> nodesToDelete1 = new ArrayList<Node>();
        ArrayList<Node> nodesToDelete2 = new ArrayList<Node>();

        for (Node node : compilationUnit.getChildNodes()) {
            if (node.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node childNode : node.getChildNodes()) {
                    if (childNode.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                        for (Node node2 : childNode.getChildNodes()) {
                            if (node2.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {

                                for (Node node3 : node2.getChildNodes()) {
                                    // Code Clone 1 nodes
                                    if (node3.getBegin().get().line >= cloneInfo.clone1BeginLine
                                            && node3.getBegin().get().line <= cloneInfo.clone1BeginLine + cloneInfo.clone1Range) {
                                        nodesToDelete1.add(node3);
                                    }
                                    // Code Clone 2 nodes
                                    if (node3.getBegin().get().line >= cloneInfo.clone2BeginLine
                                            && node3.getBegin().get().line <= cloneInfo.clone2BeginLine + cloneInfo.clone2Range) {
                                        nodesToDelete2.add(node3);
                                    }
                                }

                            }
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
    private void createExtractedMethod(CompilationUnit compilationUnit, String path)
            throws FileNotFoundException, IOException {
        // Go through all the types in the file
        NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
        for (TypeDeclaration<?> type : types) {

            // Add extracted method
            MethodDeclaration extractedMethod = type.addMethod(extractedMethodName, Modifier.PRIVATE);

            // Add return type
            LocalVariable var = extractedMethodOutVariables.iterator().next();
            if (extractedMethodOutVariables.size() > 0) {
                extractedMethod.setType(var.type);
            }
            BlockStmt block = new BlockStmt();
            for (Node n : cloneNodes) {
                // Add statements to the extracted method
                block.addStatement(n.toString());
            }
            // Add return statement if needed
            if (extractedMethodOutVariables.size() > 0) {
                block.addStatement(new ReturnStmt(var.name));
            }
            extractedMethod.setBody(block);

        }
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
                // If the extracted method has a return value
                if (extractedMethodOutVariables.size() > 0) {
                    LocalVariable var = extractedMethodOutVariables.iterator().next();
                    return new ExpressionStmt(new AssignExpr(new NameExpr(var.type + " " + var.name), new MethodCallExpr(new ThisExpr(), extractedMethodName), AssignExpr.Operator.ASSIGN));
                } else {
                    return new ExpressionStmt(new MethodCallExpr(new ThisExpr(), extractedMethodName));
                }
            }
            return super.visit(n, args);
        }
    }

}
