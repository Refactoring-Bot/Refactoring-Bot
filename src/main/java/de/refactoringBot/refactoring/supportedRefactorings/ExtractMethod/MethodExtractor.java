package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import com.github.javaparser.ast.visitor.VoidVisitor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import javassist.expr.MethodCall;
import org.apache.tomcat.jni.Local;

public class MethodExtractor extends VoidVisitorAdapter<Void> {
    private final CompilationUnit compilationUnit;
    private final RefactorCandidate candidate;
    private final String fileName;

    public MethodExtractor(RefactorCandidate candidate, String fileName) throws FileNotFoundException {
        // Read file
        FileInputStream in = new FileInputStream(fileName);
        this.compilationUnit = JavaParser.parse(in);
        this.candidate = candidate;
        this.fileName = fileName;
    }

    public String apply() throws FileNotFoundException {
        String originalMethodName = "";
        for (TypeDeclaration type : compilationUnit.getTypes()) {
            Optional<Position> beginPosition = type.getBegin();
            Optional<Position> endPosition = type.getEnd();
            if (beginPosition.isPresent() && endPosition.isPresent()) {
                int startLine = beginPosition.get().line;
                int endLine = endPosition.get().line;
                if (startLine <= candidate.startLine && endLine >= candidate.endLine) {
                    // create new method
                    EnumSet<Modifier> modifiers = EnumSet.of(Modifier.PRIVATE);
                    Type methodType = new VoidType();
                    if (this.candidate.outVariables.size() > 0) {
                        methodType = new ClassOrInterfaceType(this.candidate.outVariables.iterator().next().type);
                    }
                    MethodDeclaration extractedMethod = new MethodDeclaration(modifiers, methodType, "extractedMethod");
                    for (LocalVariable inVar : this.candidate.inVariables) {
                        extractedMethod.addParameter(inVar.type, inVar.name);
                    }
                    type.addMember(extractedMethod);

                    // call new method in original method
                    Expression methodCall;
                    MethodCallExpr call = new MethodCallExpr("extractedMethod");
                    for (LocalVariable var : this.candidate.inVariables) {
                        call.addArgument(var.name);
                    }
                    if (this.candidate.outVariables.size() > 0) {
                        LocalVariable outVar = this.candidate.outVariables.iterator().next();
                        Expression target = new NameExpr(outVar.name);
                        if (!this.candidate.inVariablesContain(outVar)) {
                            target =  new VariableDeclarationExpr(new ClassOrInterfaceType(outVar.type), outVar.name);
                        }
                        AssignExpr assign = new AssignExpr(target, call, AssignExpr.Operator.ASSIGN);
                        methodCall = assign;
                    } else {
                        methodCall = call;
                    }

                    // remove code from original method and add candidate code to new method
                    MethodVisitor methodVisitor = new MethodVisitor(this.candidate, methodCall);
                    List<Statement> nodes = compilationUnit.accept(methodVisitor, null);
                    originalMethodName = methodVisitor.methodName;
                    BlockStmt block = new BlockStmt();
                    extractedMethod.setBody(block);
                    for (Statement node : nodes) {
                        node.remove();
                        block.addStatement(node);
                    }
                    // add return statement if necessary
                    if (this.candidate.outVariables.size() > 0) {
                        LocalVariable outVar = this.candidate.outVariables.iterator().next();
                        ReturnStmt returnStmt = new ReturnStmt(new NameExpr(outVar.name));
                        block.addStatement(returnStmt);
                    }

                    // Save changes to file
                    PrintWriter out = new PrintWriter(this.fileName);
                    String refactordedCode = compilationUnit.toString();
                    out.println(refactordedCode);
                    out.close();
                }
            }
        }
        return originalMethodName;
    }



    private static class MethodVisitor extends GenericVisitorAdapter<List<Statement>, Void> {
        private RefactorCandidate candidate;
        private Expression methodCall;

        public String methodName = "";

        private enum StatementResult {
            NONE, SUBSTATEMENT, STATEMENT
        }

        public MethodVisitor(RefactorCandidate candidate, Expression methodCall) {
            this.candidate = candidate;
            this.methodCall = methodCall;
        }

        @Override
        public List<Statement> visit(MethodDeclaration n, Void arg) {
            Optional<Position> beginPosition = n.getBody().get().getBegin();
            Optional<Position> endPosition = n.getBody().get().getEnd();
            List<Statement> removedNodes = new ArrayList<>();
            if (beginPosition.isPresent() && endPosition.isPresent()) {
                int startLine = beginPosition.get().line;
                int endLine = endPosition.get().line;
                if (startLine <= candidate.startLine && endLine >= candidate.endLine) {
                    this.methodName = n.getDeclarationAsString(false, false, false);
                    removedNodes = this.extractStatements(n.getBody().get(), 0, this.methodCall);
                    return removedNodes;

                    //removedNodes = this.analyseStatements(n.getBody().get(), removedNodes);
                }
            }
            return null;
        }

        private List<Statement> extractStatements(BlockStmt parentStatement, int currentDepth, Expression methodCall) {
            List<Statement> removedNodes = new ArrayList<>();
            Integer index = null;
            for (Statement statement : parentStatement.getStatements()) {
                if (this.candidateContainsStatement(statement)) {
                    if (candidate.nestingDepth == currentDepth) {
                        if (index == null) {
                            index = parentStatement.getStatements().indexOf(statement);
                        }
                        removedNodes.add(statement);
                    } else {
                        BlockStmt newParentStatement = null;
                        if (statement.isForStmt()) {
                            newParentStatement = statement.asForStmt().getBody().asBlockStmt();
                        } else if (statement.isForEachStmt()) {
                            newParentStatement = statement.asForEachStmt().getBody().asBlockStmt();
                        } else if (statement.isForeachStmt()) {
                            newParentStatement = statement.asForeachStmt().getBody().asBlockStmt();
                        } else if (statement.isIfStmt()) {
                            System.out.println(statement);
                            newParentStatement = statement.asIfStmt().getThenStmt().asBlockStmt();
                            boolean inThenBranch = false;
                            for (Statement thenStatement: newParentStatement.getStatements()) {
                                if (this.candidateContainsStatement(thenStatement)) {
                                    inThenBranch = true;
                                }
                            }
                            if (!inThenBranch) {
                                newParentStatement = statement.asIfStmt().getElseStmt().get().asBlockStmt();
                            }
                        }
                        return this.extractStatements(newParentStatement, ++currentDepth, methodCall);
                    }
                }
            }
            for (Statement statement : removedNodes) {
                statement.remove();
            }
            parentStatement.addStatement(index, methodCall);
            return removedNodes;
        }
/*
        private List<Statement> analyseStatements(Node parentNode, List<Statement> removedNodes) {
            for (Node node : parentNode.getChildNodes()) {
                Statement statement = (Statement) node;
                switch (this.candidateContainsStatement(statement)) {
                    case NONE:
                        break;
                    case STATEMENT:
                        removedNodes.add(statement);
                        break;
                    case SUBSTATEMENT:
                        removedNodes = this.analyseStatements(statement, removedNodes);
                        break;
                }
            }
            int index = parentNode.getChildNodes().indexOf(removedNodes.get(0));
            //int index = n.getBody().get().getStatements().indexOf(removedNodes.get(0));
            ((Statement) parentNode).asBlockStmt().addStatement()
            n.getBody().get().addStatement(index, this.methodCall);
            return removedNodes;
        }*/

        private boolean candidateContainsStatement(Statement statement) {
            Optional<Position> beginPosition = statement.getBegin();
            Optional<Position> endPosition = statement.getEnd();
            if (beginPosition.isPresent() && endPosition.isPresent()) {
                Long startLine = new Long(beginPosition.get().line);
                Long endLine = new Long(endPosition.get().line);
                if (rangeContainsLineNumber(startLine, endLine, candidate.startLine) ||
                    rangeContainsLineNumber(startLine, endLine, candidate.endLine) ||
                    rangeContainsLineNumber(candidate.startLine, candidate.endLine, startLine) ||
                    rangeContainsLineNumber(candidate.startLine, candidate.endLine, endLine)) {
                    return true;
                }
            }
            return false;
        }

        private boolean rangeContainsLineNumber(Long startLine, Long endLine, Long lineNumber) {
            return startLine <= lineNumber && endLine >= lineNumber;
        }
    }
}
