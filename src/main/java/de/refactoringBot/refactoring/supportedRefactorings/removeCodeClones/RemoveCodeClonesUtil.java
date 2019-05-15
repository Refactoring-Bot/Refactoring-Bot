package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.ast.Node;
import com.github.javaparser.metamodel.JavaParserMetaModel;

import java.util.ArrayList;

public class RemoveCodeClonesUtil {

    // Search for all literals in the clone lines
    public static ArrayList<LiteralInfo> getAllLiterals(Node statement, long cloneStartLine, long cloneEndLine) {
        ArrayList<LiteralInfo> literalList = new ArrayList<>();
        if (statement.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {
            for(Node blockStatement : statement.getChildNodes())
                literalList.addAll(getAllLiterals(blockStatement, cloneStartLine, cloneEndLine));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.booleanLiteralExprMetaModel &&
                statement.getBegin().get().line >= cloneStartLine && statement.getBegin().get().line <= cloneEndLine) {
            literalList.add(new LiteralInfo("boolean", statement.toString(), statement.getBegin().get()));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.integerLiteralExprMetaModel &&
                statement.getBegin().get().line >= cloneStartLine && statement.getBegin().get().line <= cloneEndLine) {
            literalList.add(new LiteralInfo("int", statement.toString(), statement.getBegin().get()));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.charLiteralExprMetaModel &&
                statement.getBegin().get().line >= cloneStartLine && statement.getBegin().get().line <= cloneEndLine) {
            literalList.add(new LiteralInfo("char", statement.toString(), statement.getBegin().get()));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.doubleLiteralExprMetaModel &&
                statement.getBegin().get().line >= cloneStartLine && statement.getBegin().get().line <= cloneEndLine) {
            literalList.add(new LiteralInfo("double", statement.toString(), statement.getBegin().get()));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.longLiteralExprMetaModel &&
                statement.getBegin().get().line >= cloneStartLine && statement.getBegin().get().line <= cloneEndLine) {
            literalList.add(new LiteralInfo("long", statement.toString(), statement.getBegin().get()));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.stringLiteralExprMetaModel &&
                statement.getBegin().get().line >= cloneStartLine && statement.getBegin().get().line <= cloneEndLine) {
            literalList.add(new LiteralInfo("String", statement.toString(), statement.getBegin().get()));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
            for (Node varDelNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(varDelNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.objectCreationExprMetaModel) {
            for (Node objectExprNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(objectExprNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.ifStmtMetaModel) {
            for (Node ifNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(ifNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.forStmtMetaModel) {
            for (Node forNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(forNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.unaryExprMetaModel) {
            for (Node unaryNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(unaryNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.enclosedExprMetaModel) {
            for (Node enclosedNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(enclosedNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.binaryExprMetaModel) {
            for (Node binaryNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(binaryNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.methodCallExprMetaModel) {
            for (Node methodCallNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(methodCallNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.assignExprMetaModel) {
            for (Node assignExprNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(assignExprNode, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.fieldAccessExprMetaModel) {
            for (Node fieldAccessExpr : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(fieldAccessExpr, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.forEachStmtMetaModel) {
            for (Node forEachNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(forEachNode, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.whileStmtMetaModel) {
            for (Node whileNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(whileNode, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.doStmtMetaModel) {
            for (Node doNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(doNode, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.expressionStmtMetaModel) {
            for (Node exprNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(exprNode, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.switchStmtMetaModel) {
            for (Node switchNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(switchNode, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.switchEntryStmtMetaModel) {
            for (Node switchEntryNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(switchEntryNode, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.assertStmtMetaModel) {
            for (Node assertNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(assertNode, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.labeledStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(node, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.returnStmtMetaModel) {
            for (Node returnNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(returnNode, cloneStartLine, cloneEndLine));
            }
        }
//        else if(statement instanceof SynchronizedStatement) {
//            SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
//            //handling of SynchronizedStatement
//            statementList.addAll(getStatements(synchronizedStatement.getBody()));
//        }
        else if(statement.getMetaModel() == JavaParserMetaModel.throwStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(node, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.tryStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(node, cloneStartLine, cloneEndLine));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(node, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.variableDeclarationExprMetaModel) {
            for (Node varDeclNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(varDeclNode, cloneStartLine, cloneEndLine));
            }
        }
//        else if(statement instanceof ConstructorInvocation) {
//            ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
//            statementList.add(constructorInvocation);
//        }
//        else if(statement instanceof SuperConstructorInvocation) {
//            SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
//            statementList.add(superConstructorInvocation);
//        }
        else if(statement.getMetaModel() == JavaParserMetaModel.breakStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(node, cloneStartLine, cloneEndLine));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.continueStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiterals(node, cloneStartLine, cloneEndLine));
            }
        }
        return literalList;
    }

    public static ArrayList<Node> getAllLiteralNodes(Node statement) {
        ArrayList<Node> literalList = new ArrayList<>();
        if (statement.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {
            for(Node blockStatement : statement.getChildNodes())
                literalList.addAll(getAllLiteralNodes(blockStatement));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.booleanLiteralExprMetaModel) {
            literalList.add(statement);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.integerLiteralExprMetaModel) {
            literalList.add(statement);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.charLiteralExprMetaModel) {
            literalList.add(statement);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.doubleLiteralExprMetaModel) {
            literalList.add(statement);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.longLiteralExprMetaModel) {
            literalList.add(statement);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.stringLiteralExprMetaModel) {
            literalList.add(statement);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
            for (Node varDelNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(varDelNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.objectCreationExprMetaModel) {
            for (Node objectExprNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(objectExprNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.ifStmtMetaModel) {
            for (Node ifNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(ifNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.forStmtMetaModel) {
            for (Node forNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(forNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.unaryExprMetaModel) {
            for (Node unaryNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(unaryNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.enclosedExprMetaModel) {
            for (Node enclosedNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(enclosedNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.binaryExprMetaModel) {
            for (Node binaryNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(binaryNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.methodCallExprMetaModel) {
            for (Node methodCallNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(methodCallNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.assignExprMetaModel) {
            for (Node assignExprNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(assignExprNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.fieldAccessExprMetaModel) {
            for (Node fieldAccessExpr : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(fieldAccessExpr));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.forEachStmtMetaModel) {
            for (Node forEachNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(forEachNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.whileStmtMetaModel) {
            for (Node whileNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(whileNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.doStmtMetaModel) {
            for (Node doNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(doNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.expressionStmtMetaModel) {
            for (Node exprNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(exprNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.switchStmtMetaModel) {
            for (Node switchNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(switchNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.switchEntryStmtMetaModel) {
            for (Node switchEntryNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(switchEntryNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.assertStmtMetaModel) {
            for (Node assertNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(assertNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.labeledStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(node));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.returnStmtMetaModel) {
            for (Node returnNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(returnNode));
            }
        }
//        else if(statement instanceof SynchronizedStatement) {
//            SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
//            //handling of SynchronizedStatement
//            statementList.addAll(getStatements(synchronizedStatement.getBody()));
//        }
        else if(statement.getMetaModel() == JavaParserMetaModel.throwStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(node));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.tryStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(node));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(node));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.variableDeclarationExprMetaModel) {
            for (Node varDeclNode : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(varDeclNode));
            }
        }
//        else if(statement instanceof ConstructorInvocation) {
//            ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
//            statementList.add(constructorInvocation);
//        }
//        else if(statement instanceof SuperConstructorInvocation) {
//            SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
//            statementList.add(superConstructorInvocation);
//        }
        else if(statement.getMetaModel() == JavaParserMetaModel.breakStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(node));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.continueStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                literalList.addAll(getAllLiteralNodes(node));
            }
        }
        return literalList;
    }
}
