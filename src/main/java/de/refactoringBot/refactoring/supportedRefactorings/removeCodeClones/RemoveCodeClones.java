package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import de.refactoringBot.api.sonarQube.SonarQubeDataGrabber;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.sonarQube.Block;
import de.refactoringBot.model.sonarQube.Blocks;
import de.refactoringBot.model.sonarQube.Duplicates;
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
    ArrayList<Node> cloneNodeLiterals = new ArrayList<>();
    String tempFolderName = "/tempFolderForPreparedCode";

    CloneInfo cloneInfo = new CloneInfo();
    String extractedMethodName = "extractedMethod";
    public Set<LocalVariable> extractedMethodOutVariables = new HashSet<>();
    ArrayList<LocalVariable> params = new ArrayList<>();
    ArrayList<LocalVariable> literalParams = new ArrayList<>();

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

        // Get clone information from SonarQube
        Duplicates duplicates = new Duplicates();
        SonarQubeDataGrabber grabber = new SonarQubeDataGrabber();
        try {
            duplicates = grabber.getDuplicatesData(gitConfig.getAnalysisServiceProjectKey(), issue.getFilePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (checkIfDuplicatesRemovable(gitConfig.getRepoFolder(), duplicates)) {
            return "Could not refactor!";
        }
        analyseDuplicates(gitConfig.getRepoFolder(), duplicates);
        checkLiterals();

        // Get file name of file that will be refactored
        String[] splitFilePath = issue.getFilePath().split("\\\\");
        String fileName = splitFilePath[splitFilePath.length - 1];
        this.cloneInfo.mainExtractFile = fileName.replace(".java", "");

        // Create temp folder for prepared code file
        String pathForPreparedCode = gitConfig.getRepoFolder() + tempFolderName;
        new File(pathForPreparedCode).mkdirs();

        // Read file
        FileInputStream in = new FileInputStream(path);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));
        in.close();
        getParametersAndPackageName(compilationUnit);

        // Write new file with prepared code of the compilationUnit
        PrepareCode prepareCode = new PrepareCode();
        String fileContent = prepareCode.prepareCode(compilationUnit).toString();
        FileWriter fileWriter = new FileWriter(pathForPreparedCode + "/" + fileName);
        fileWriter.write(fileContent);
        fileWriter.close();

        // Get new compilationUnit of the new prepare code file
        FileInputStream in2 = new FileInputStream(pathForPreparedCode + "/" + fileName );
        CompilationUnit preparedCompilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in2));
        in2.close();

        // Get the difference of the lines between the original file and the prepared file
        PreparedCodeCloneInfo prepCloneInfo = getPreparedCodeCloneLines(preparedCompilationUnit);
        RefactorCandidate bestCandidate1 = this.getCandidates(pathForPreparedCode + "/" + fileName,
                prepCloneInfo.startLine, prepCloneInfo.endLine);

        // Get output variables from best candidate
        this.extractedMethodOutVariables = bestCandidate1.outVariables;

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

        // Find parameters for extracted method
        params = this.findParamsForExtractedMethod(prepCloneInfo);

        // Remove clone nodes
        this.extractCloneNodes(prepCloneInfo, bestCandidate1);
        this.extractLiteralParameters();
        this.removeCodeClones();

        // Create extracted method
        this.createExtractedMethod(path);

        // Return commit message
        return "Removed code clones";
    }

    /**
     * Get parameters of the constructor of the main file and the package main
     *
     * @param compUnit
     */
    private void getParametersAndPackageName(CompilationUnit compUnit) {
        this.cloneInfo.mainExtractPackage = compUnit.getPackageDeclaration().get().getName().toString();
        for (Node node : compUnit.getChildNodes()) {
            if (node.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node node1 : node.getChildNodes()) {
                    if (node1.getMetaModel() == JavaParserMetaModel.constructorDeclarationMetaModel) {
                        ConstructorDeclaration constr = (ConstructorDeclaration) node1;
                        for (Parameter parameter : constr.getParameters()) {
                            this.cloneInfo.constructorParams.add(parameter.getName().toString());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     *  Calculate the line difference
     */
    private PreparedCodeCloneInfo getPreparedCodeCloneLines(CompilationUnit preparedCompUnit) {
        MethodDeclaration preparedMethodWithClone = null;
        for (TypeDeclaration typeDec : preparedCompUnit.getTypes()) {
            List<BodyDeclaration> members = typeDec.getMembers();
            if (members != null) {
                for (BodyDeclaration member : members) {
                    if (member.isMethodDeclaration()) {
                        MethodDeclaration methodDeclaration = (MethodDeclaration) member;
                        MethodDeclaration originalMethodDecl = this.cloneInfo.methodDeclarationOfFirstFile;
                        if (methodDeclaration.getName().toString().equals(originalMethodDecl.getName().toString()) &&
                                methodDeclaration.getType().toString().equals(originalMethodDecl.getType().toString()) &&
                                methodDeclaration.getParameters().size() == originalMethodDecl.getParameters().size()) {
                            boolean paramsIdentical = true;
                            for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
                                if (!methodDeclaration.getParameters().get(i).toString().equals(originalMethodDecl.getParameters().get(i).toString())) {
                                    paramsIdentical = false;
                                }
                            }
                            if (paramsIdentical) {
                                preparedMethodWithClone = methodDeclaration;
                                break;
                            }
                        }

                    }
                }
            }
        }


        PreparedCodeCloneInfo prepCloneInfo = new PreparedCodeCloneInfo();
        prepCloneInfo.methodDeclaration = preparedMethodWithClone;
        NodeList<Statement> prepStatementList = preparedMethodWithClone.getBody().get().getStatements();
        NodeList<Statement> origStatementList = this.cloneInfo.methodDeclarationOfFirstFile.getBody().get().getStatements();
        boolean foundStartLine = false;
        for (int i = 0; i < prepStatementList.size(); i++) {
            if (!foundStartLine && origStatementList.get(i).getBegin().get().line >= this.cloneInfo.cloneBlocks.getBlocks().get(0).getFrom()) {
                if (compareStatements(origStatementList.get(i), prepStatementList.get(i))) {
                    prepCloneInfo.startLine = prepStatementList.get(i).getBegin().get().line;
                    foundStartLine = true;
                }
            } else if (origStatementList.get(i).getBegin().get().line <= this.cloneInfo.cloneBlocks.getBlocks().get(0).getFrom() + this.cloneInfo.cloneBlocks.getBlocks().get(0).getSize()) {
                if (compareStatements(origStatementList.get(i), prepStatementList.get(i))) {
                    prepCloneInfo.endLine = prepStatementList.get(i).getEnd().get().line;
                }
            }
        }
        return prepCloneInfo;
    }

    /**
     * Return true if statements are identical
     */
    private boolean compareStatements(Statement stmt1, Statement stmt2) {
        return stmt1.toString().equals(stmt2.toString());
    }

    /**
     * Sort out all literals that are not different
     */
    private void checkLiterals() {
        ArrayList<Integer> indexesToRemove = new ArrayList<>();
        for (int i = 0; i < this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().size(); i++) {
            LiteralInfo lastLiteral = this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().get(i);
            boolean allEqual = true;
            for (Block block : this.cloneInfo.cloneBlocks.getBlocks()) {
                if (!lastLiteral.value.equals(block.getLiterals().get(i).value)) {
                    allEqual = false;
                    break;
                }
            }
            if (allEqual) {
                indexesToRemove.add(i);
            }
        }

        // Remove all equal literals
        Collections.reverse(indexesToRemove);
        for (int literalIndex : indexesToRemove) {
            for (Block block : this.cloneInfo.cloneBlocks.getBlocks()) {
                block.getLiterals().remove(literalIndex);
            }
        }
    }

    /**
     * Correct the duplicate lines and find all literals
     */
    private void analyseDuplicates(String repoFolderPath, Duplicates duplicates) throws IOException {
        for (Block block : this.cloneInfo.cloneBlocks.getBlocks()) {
            // Set file path of block
            block.setFilePath(repoFolderPath + "/" + duplicates.getDuplicateFiles()
                    .getFileInfo(block.getRef()).getFileName());

            // Read file
            FileInputStream in = new FileInputStream(block.getFilePath());
            CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));
            in.close();
            Block tempBlock = getMethodDeclarationOfDuplicate(compilationUnit, block);
            block.setMethodStartLine(tempBlock.getMethodStartLine());
            block.setLiterals(tempBlock.getLiterals());
        }
    }

    /**
     * Get method start line and all literals of a clone block
     *
     * @param compUnit
     * @param block
     * @return
     */
    private Block getMethodDeclarationOfDuplicate(CompilationUnit compUnit, Block block) {
        for (Node childNode : compUnit.getChildNodes()) {
            if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                // Get method declaration with clone
                for (Node node : childNode.getChildNodes()) {
                    if (node.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                        MethodDeclaration method = (MethodDeclaration) node;
                        if (method.getBegin().get().line <= block.getFrom() && method.getEnd().get().line >= block.getFrom() + block.getSize()) {
                            // Set method begin line
                            block.setMethodStartLine(method.getBegin().get().line);
                            // Get all literals
                            block.setLiterals(RemoveCodeClonesUtil.getAllLiterals(method.getBody().get(), block.getFrom(), block.getFrom() + block.getSize()));
                            return block;
                        }
                    }
                }
            }
        }
        return block;
    }

    /**
     * Check if duplicates are removable
     *
     * @param repoFilePath
     * @param duplicates
     * @return
     * @throws IOException
     */
    private boolean checkIfDuplicatesRemovable(String repoFilePath, Duplicates duplicates) throws IOException {
        boolean foundRemovableDuplicates = false;
        boolean inFile1 = true;

        // Are all clone blocks in one file?
        Blocks firstBlocks = duplicates.getDuplications().get(0);
        for (Block block : firstBlocks.getBlocks()) {
            if (block.getRef() != 1) {
                inFile1 = false;
            }
        }

        // Get main file
        String filePath = duplicates.getDuplicateFiles().getFileInfo(duplicates.getDuplications().get(0).getBlocks().get(0).getRef()).getFileName();

        // Read file
        FileInputStream in = new FileInputStream(repoFilePath + "/" + filePath);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));
        in.close();

        ArrayList<MethodDeclaration> methods = new ArrayList<>();
        for (Node childNode : compilationUnit.getChildNodes()) {
            if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {

                // Get all methods
                for (Node node : childNode.getChildNodes()) {
                    if (node.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                        MethodDeclaration method = (MethodDeclaration) node;
                        if (method.getEnd().get().line - method.getBegin().get().line > 7) {
                            methods.add(method);
                        }
                    }
                }
            }
        }

        // Get only the first clone group and the method declaration of the first file (--> extracted method will be placed in file 1)
        for (MethodDeclaration method : methods) {
            if (method.getBegin().get().line <= duplicates.getDuplications().get(0).getBlocks().get(0).getFrom() &&
            method.getEnd().get().line >= duplicates.getDuplications().get(0).getBlocks().get(0).getFrom() + duplicates.getDuplications().get(0).getBlocks().get(0).getSize()) {
                this.cloneInfo.cloneBlocks = firstBlocks;
                this.cloneInfo.allInFile1 = inFile1;
                this.cloneInfo.methodDeclarationOfFirstFile = method;
                return true;
            }
        }

        return foundRemovableDuplicates;
    }

    /**
     * Analyze a clone to get params for extracted method
     *
     * @param cloneInfo
     * @return
     */
    private ArrayList<LocalVariable> findParamsForExtractedMethod(PreparedCodeCloneInfo cloneInfo) {
        ArrayList<LocalVariable> params = new ArrayList<>();
        ArrayList<Node> test = searchLine(cloneInfo.methodDeclaration, cloneInfo.startLine);

        // Search method parameters
        for (Parameter param : cloneInfo.methodDeclaration.getParameters()) {
            if (searchVar(cloneInfo.methodDeclaration, param.getName(), cloneInfo.startLine, cloneInfo.endLine).size() > 0) {
                params.add(new LocalVariable(param.getName().toString(), param.getType().toString()));
            }
        }

        // Search local variables
        for (Node varDecl : test) {
            VariableDeclarator var = (VariableDeclarator) varDecl;
            if (searchVar(cloneInfo.methodDeclaration, var.getName(), cloneInfo.startLine, cloneInfo.endLine).size() > 0) {
                params.add(new LocalVariable(var.getName().toString(), var.getType().toString()));
            }
        }
        return params;
    }

    /**
     * Get all nodes until a certain line
     *
     * @param node
     * @param line
     * @return
     */
    private ArrayList<Node> searchLine(Node node, long line) {
        ArrayList<Node> nodeList = new ArrayList<>();
        for (Node childNode : node.getChildNodes()) {
            if (childNode.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel && childNode.getBegin().get().line < line) {
                nodeList.add(childNode);
            }
            nodeList.addAll(searchLine(childNode, line));
        }
        return nodeList;
    }

    /**
     * Get all variables in between some lines
     *
     * @param node
     * @param varName
     * @param startLine
     * @param endLine
     * @return
     */
    private ArrayList<Node> searchVar(Node node, SimpleName varName, long startLine, long endLine) {
        ArrayList<Node> nodeList = new ArrayList<>();
        for (Node childNode : node.getChildNodes()) {
            if (childNode.getMetaModel() == JavaParserMetaModel.simpleNameMetaModel && ((SimpleName) childNode).getIdentifier().equals(varName.getIdentifier())
                    && childNode.getBegin().get().line >= startLine && childNode.getBegin().get().line <= endLine) {
                nodeList.add(childNode);
            }
            nodeList.addAll(searchVar(childNode, varName, startLine, endLine));
        }
        return nodeList;
    }

    /**
     * Get candidates that are possible to extract
     *
     * @param path
     * @param cloneBeginLine
     * @param cloneEndLine
     * @return
     */
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
        return null;
    }

    /**
     * Search the code for clones without the information where they start.
     *
     * @param childNodes
     * @param keepCloneNodes
     * @return
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
                        // Save method declaration
                        cloneInfo.method1 = method1;
                        cloneInfo.method2 = method2;
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

    /**
     * Remove code clones
     *
     * @throws IOException
     */
    private void removeCodeClones() throws IOException {
        HashMap<Integer, ArrayList<Block>> blockChunks = new HashMap();

        // Cluster all blocks which are in the same file to refactor them at the same time
        for (Block block : cloneInfo.cloneBlocks.getBlocks()) {
            if (!blockChunks.containsKey(block.getRef())) {
                ArrayList<Block> temp = new ArrayList<>();
                temp.add(block);
                blockChunks.put(block.getRef(), temp);
            } else {
                ArrayList<Block> temp = blockChunks.get(block.getRef());
                temp.add(block);
                blockChunks.replace(block.getRef(), temp);
            }
        }

        // Remove block clusters at the same time
        for (ArrayList<Block> blocks : blockChunks.values()) {
            FileInputStream in = new FileInputStream(blocks.get(0).getFilePath());
            CompilationUnit compUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));
            in.close();

            // Get class name
            String[] splitFilePath = blocks.get(0).getFilePath().split("/");
            String fileName = splitFilePath[splitFilePath.length - 1];

            removeNodesAndAddMethodCall(compUnit, blocks, fileName.replace(".java", ""));
        }
    }

    /**
     * Get all clone nodes that are save to extract from the best candidate
     *
     * @param prepCloneInfo
     * @param candidate
     */
    private void extractCloneNodes(PreparedCodeCloneInfo prepCloneInfo, RefactorCandidate candidate) {
        for (Node node : prepCloneInfo.methodDeclaration.getBody().get().getChildNodes()) {
            if (node.getBegin().get().line >= candidate.startLine && node.getBegin().get().line <= candidate.endLine) {
                this.cloneNodes.add(node);
            }
        }
    }

    /**
     * Sort out all literals that are not in the clone
     */
    private void extractLiteralParameters() {
        ArrayList<Integer> indexesToRemove = new ArrayList<>();
        for (int i = 0; i < this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().size(); i++) {
            boolean foundLiteral = false;
            for (Node cloneNode : this.cloneNodes) {
                if (cloneNode.getBegin().get().line == this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().get(i).position.line) {
                    literalParams.add(new LocalVariable(this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().get(i).value,
                            this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().get(i).type));
                    foundLiteral = true;
                    break;
                }
            }

            // Remove all literals that are not in the clone
            if (!foundLiteral) {
                indexesToRemove.add(i);
            }
        }

        // Remove all equal literals
        Collections.reverse(indexesToRemove);
        for (int literalIndex : indexesToRemove) {
            for (Block block : this.cloneInfo.cloneBlocks.getBlocks()) {
                block.getLiterals().remove(literalIndex);
            }
        }

        // Get literal clone nodes for replacing later
        for (Node cloneNode : this.cloneNodes) {
            ArrayList<Node> allLiteralNodes = RemoveCodeClonesUtil.getAllLiteralNodes(cloneNode);
            for (Node literalNode : allLiteralNodes) {
                for (LiteralInfo literalInfo : this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals()) {
                    if (literalNode.getBegin().get().line == literalInfo.position.line && literalNode.toString().equals(literalInfo.value)) {
                        cloneNodeLiterals.add(literalNode);
                        break;
                    }
                }
            }
        }

    }

    /**
     * Removes nodes and adds method call to the extracted method
     *
     * @param cu
     * @param blocks
     * @param className
     * @throws IOException
     */
    private void removeNodesAndAddMethodCall(CompilationUnit cu, ArrayList<Block> blocks, String className) throws IOException {
        ArrayList<Node> nodesToDelete = new ArrayList<>();
        ArrayList<Integer> indexesForMethodCall = new ArrayList<>();

        for (Block block : blocks) {
            indexesForMethodCall.add(nodesToDelete.size());
            MethodDeclaration foundMethod = new MethodDeclaration();
            for (Node childNode : cu.getChildNodes()) {
                if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {

                    // Get method with clone
                    for (Node node : childNode.getChildNodes()) {
                        if (node.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                            MethodDeclaration method = (MethodDeclaration) node;
                            if (method.getBegin().get().line == block.getMethodStartLine()) {
                                foundMethod = method;
                                break;
                            }
                        }
                    }
                }
            }

            // Get nodes to delete
            for (Node cloneNode : this.cloneNodes) {
                Node temp = cloneNode.clone();
                String tempString = temp.toString();
                for (Comment comment : temp.getAllContainedComments()) {
                    tempString = tempString.replace(comment.getContent(), "");
                }
                tempString = tempString.replace(" ", "");
                tempString = tempString.replaceAll("\\r\\n|\\r|\\n", " ");
                for (Node node : foundMethod.getBody().get().getChildNodes()) {
                    Node temp2 = node.clone();
                    String temp2String = temp2.toString();
                    for (Comment comment : temp2.getAllContainedComments()) {
                        temp2String = temp2String.replace(comment.getContent(), "");
                    }
                    temp2String = temp2String.replace(" ", "");
                    temp2String = temp2String.replaceAll("\\r\\n|\\r|\\n", " ");
                    // if equal
                    if (tempString.equals(temp2String)) {
                        nodesToDelete.add(node);
                        break;
                    // if they have different literals
                    } else {
                        for (int i = 0; i < this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().size(); i++) {
                            if (cloneNode.getBegin().get().line == this.cloneInfo.cloneBlocks.getBlocks().get(0).getLiterals().get(i).position.line) {
                                if (node.getBegin().get().line == block.getLiterals().get(i).position.line) {
                                    nodesToDelete.add(node);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        int blockIndex = 0;
        for (int i = 0; i < nodesToDelete.size(); i++) {
            if (indexesForMethodCall.contains(i)) {
                // Add method call of extracted method
                changeLiteralParameter(blockIndex);
                AddMethodCallVisitor visitor = new AddMethodCallVisitor();
                // If the extracted method and methodcall are in the same file
                if (className.equals(this.cloneInfo.mainExtractFile)) {
                    visitor.className = null;
                } else {
                    visitor.className = this.cloneInfo.mainExtractFile;
                    visitor.constructorParams = this.cloneInfo.constructorParams;
                    // Add import statement
                    cu.addImport(this.cloneInfo.mainExtractPackage + "." + this.cloneInfo.mainExtractFile);
                }
                cu.accept(visitor, nodesToDelete.get(i));
                blockIndex++;
            } else {
                // visit and delete Clone nodes
                if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.expressionStmtMetaModel) {
                    cu.accept(new RemoveNodeVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.forStmtMetaModel) {
                    cu.accept(new RemoveForStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.ifStmtMetaModel) {
                    cu.accept(new RemoveIfStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.switchStmtMetaModel) {
                    cu.accept(new RemoveSwitchStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.continueStmtMetaModel) {
                    cu.accept(new RemoveContinueStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.breakStmtMetaModel) {
                    cu.accept(new RemoveBreakStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.tryStmtMetaModel) {
                    cu.accept(new RemoveTryStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.throwStmtMetaModel) {
                    cu.accept(new RemoveThrowStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.returnStmtMetaModel) {
                    cu.accept(new RemoveReturnStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.whileStmtMetaModel) {
                    cu.accept(new RemoveWhileStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.switchEntryStmtMetaModel) {
                    cu.accept(new RemoveSwitchEntryStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.doStmtMetaModel) {
                    cu.accept(new RemoveDoStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.forEachStmtMetaModel) {
                    cu.accept(new RemoveForEachStmtVisitor(), nodesToDelete.get(i));
                } else if (nodesToDelete.get(i).getMetaModel() == JavaParserMetaModel.synchronizedStmtMetaModel) {
                    cu.accept(new RemoveSynchronizeStmtVisitor(), nodesToDelete.get(i));
                }
            }
        }

        // Write into file
        PrintWriter out = new PrintWriter(blocks.get(0).getFilePath());
        out.println(cu);
        out.close();
    }

    /**
     * Replace literals with variables
     *
     * @param blockIndex
     */
    private void changeLiteralParameter(int blockIndex) {
        ArrayList<LocalVariable> newLiterals = new ArrayList<>();
        for (LiteralInfo literalInfo : this.cloneInfo.cloneBlocks.getBlocks().get(blockIndex).getLiterals()) {
            newLiterals.add(new LocalVariable(literalInfo.value, literalInfo.type));
        }
        literalParams = newLiterals;
    }

    /**
     * Create extracted method in the main file
     *
     * @param path
     * @throws IOException
     */
    private void createExtractedMethod(String path)
            throws IOException {

        FileInputStream in = new FileInputStream(path);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));
        in.close();

        // Go through all the types in the file
        NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
        for (TypeDeclaration<?> type : types) {

            // Add extracted method
            MethodDeclaration extractedMethod = type.addMethod(extractedMethodName, Modifier.PUBLIC);

            // Add parameters to method
            for (LocalVariable param : params) {
                extractedMethod.addParameter(param.type, param.name);
            }
            for (int i = 0; i < literalParams.size(); i++) {
                extractedMethod.addParameter(literalParams.get(i).type, "literalParam" + i);
            }

            // Replace all different literals in the clone nodes
            for (int i = 0; i < this.cloneNodeLiterals.size(); i++) {
                Node cloneNodeLiteral = this.cloneNodeLiterals.get(i);
                for (Node cloneNode : this.cloneNodes) {
                    if (cloneNode.getBegin().get().line == cloneNodeLiteral.getBegin().get().line) {
                        if (cloneNodeLiteral.getMetaModel() == JavaParserMetaModel.integerLiteralExprMetaModel) {
                            ChangeIntLiteralVisitor visitor = new ChangeIntLiteralVisitor();
                            visitor.varName = "literalParam" + i;
                            cloneNode.accept(visitor, cloneNodeLiteral);
                            break;
                        } else if (cloneNodeLiteral.getMetaModel() == JavaParserMetaModel.stringLiteralExprMetaModel) {
                            ChangeStringLiteralVisitor visitor = new ChangeStringLiteralVisitor();
                            visitor.varName = "literalParam" + i;
                            cloneNode.accept(visitor, cloneNodeLiteral);
                            break;
                        } else if (cloneNodeLiteral.getMetaModel() == JavaParserMetaModel.longLiteralExprMetaModel) {
                            ChangeLongLiteralVisitor visitor = new ChangeLongLiteralVisitor();
                            visitor.varName = "literalParam" + i;
                            cloneNode.accept(visitor, cloneNodeLiteral);
                            break;
                        } else if (cloneNodeLiteral.getMetaModel() == JavaParserMetaModel.doubleLiteralExprMetaModel) {
                            ChangeDoubleLiteralVisitor visitor = new ChangeDoubleLiteralVisitor();
                            visitor.varName = "literalParam" + i;
                            cloneNode.accept(visitor, cloneNodeLiteral);
                            break;
                        } else if (cloneNodeLiteral.getMetaModel() == JavaParserMetaModel.charLiteralExprMetaModel) {
                            ChangeCharLiteralVisitor visitor = new ChangeCharLiteralVisitor();
                            visitor.varName = "literalParam" + i;
                            cloneNode.accept(visitor, cloneNodeLiteral);
                            break;
                        }
                    }
                }
            }

            // Add return type
            LocalVariable var = new LocalVariable("test", "test");
            if (extractedMethodOutVariables.size() > 0) {
                var = extractedMethodOutVariables.iterator().next();
                // if java.lang.Boolean
                String varType = var.type.replace("java.lang.", "");
                extractedMethod.setType(varType);
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
        // out.println(LexicalPreservingPrinter.print(compilationUnit));
        out.println(compilationUnit);
        out.close();
    }


    // -------------------------------------- Visitor classes ----------------------------------------------------

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

    class RemoveForStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(ForStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveIfStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(IfStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveSwitchStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(SwitchStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveContinueStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(ContinueStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveBreakStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(BreakStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveTryStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(TryStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveThrowStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(ThrowStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveReturnStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(ReturnStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveWhileStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(WhileStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveSwitchEntryStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(SwitchEntryStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveDoStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(DoStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveForEachStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(ForEachStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class RemoveSynchronizeStmtVisitor extends ModifierVisitor<Node> {
        @Override
        public Visitable visit(SynchronizedStmt n, Node args) {
            if (n == args) {
                return null;
            }
            return super.visit(n, args);
        }
    }

    class ChangeIntLiteralVisitor extends ModifierVisitor<Node> {
        String varName;
        @Override
        public Visitable visit(IntegerLiteralExpr n, Node args) {
            if (n == args) {
                return new NameExpr(varName);
            }
            return super.visit(n, args);
        }
    }

    class ChangeStringLiteralVisitor extends ModifierVisitor<Node> {
        String varName;
        @Override
        public Visitable visit(StringLiteralExpr n, Node args) {
            if (n == args) {
                return new NameExpr(varName);
            }
            return super.visit(n, args);
        }
    }

    class ChangeLongLiteralVisitor extends ModifierVisitor<Node> {
        String varName;
        @Override
        public Visitable visit(LongLiteralExpr n, Node args) {
            if (n == args) {
                return new NameExpr(varName);
            }
            return super.visit(n, args);
        }
    }

    class ChangeDoubleLiteralVisitor extends ModifierVisitor<Node> {
        String varName;
        @Override
        public Visitable visit(DoubleLiteralExpr n, Node args) {
            if (n == args) {
                return new NameExpr(varName);
            }
            return super.visit(n, args);
        }
    }

    class ChangeCharLiteralVisitor extends ModifierVisitor<Node> {
        String varName;
        @Override
        public Visitable visit(CharLiteralExpr n, Node args) {
            if (n == args) {
                return new NameExpr(varName);
            }
            return super.visit(n, args);
        }
    }

    /**
     * Visitor implementation for adding method call of the extracted method.
     */
    class AddMethodCallVisitor extends ModifierVisitor<Node> {
        String className;
        ArrayList<String> constructorParams;
        @Override
        public Visitable visit(ExpressionStmt n, Node args) {
            if (n == args) {
                NodeList<Expression> paramsForExtractedMethod = new NodeList<>();
                for (LocalVariable param : params) {
                    paramsForExtractedMethod.add(new NameExpr(param.name));
                }
                for (LocalVariable param : literalParams) {
                    paramsForExtractedMethod.add(new NameExpr(param.name));
                }

                // If the extracted method has a return value
                if (className == null) {
                    if (extractedMethodOutVariables.size() > 0) {
                        LocalVariable var = extractedMethodOutVariables.iterator().next();
                        // if java.lang.Boolean
                        String varType = var.type.replace("java.lang.", "");
                        return new ExpressionStmt(new AssignExpr(new NameExpr(varType + " " + var.name), new MethodCallExpr(new ThisExpr(), extractedMethodName, paramsForExtractedMethod), AssignExpr.Operator.ASSIGN));
                    } else {
                        return new ExpressionStmt(new MethodCallExpr(new ThisExpr(), extractedMethodName, paramsForExtractedMethod));
                    }
                } else {
                    String classNameExpr = "new " + className + "(";
                    for (int i = 0; i < constructorParams.size(); i++) {
                        if (constructorParams.get(i).equals("int")) {
                            classNameExpr = classNameExpr + "1";
                        } else if (constructorParams.get(i).equals("String")) {
                            classNameExpr = classNameExpr + "test";
                        } else if (constructorParams.get(i).equals("boolean")) {
                            classNameExpr = classNameExpr + "true";
                        } else {
                            classNameExpr = classNameExpr + "null";
                        }
                        classNameExpr = classNameExpr + constructorParams.get(i);
                        if (i != constructorParams.size() - 1) {
                            classNameExpr = classNameExpr + ",";
                        }
                    }
                    classNameExpr = classNameExpr + ")";
                    if (extractedMethodOutVariables.size() > 0) {
                        LocalVariable var = extractedMethodOutVariables.iterator().next();
                        // if java.lang.Boolean
                        String varType = var.type.replace("java.lang.", "");
                        return new ExpressionStmt(new AssignExpr(new NameExpr(varType + " " + var.name), new MethodCallExpr(new NameExpr(classNameExpr), extractedMethodName, paramsForExtractedMethod), AssignExpr.Operator.ASSIGN));
                    } else {
                        return new ExpressionStmt(new MethodCallExpr(new NameExpr(classNameExpr), extractedMethodName, paramsForExtractedMethod));
                    }
                }

            }
            return super.visit(n, args);
        }
    }

}
