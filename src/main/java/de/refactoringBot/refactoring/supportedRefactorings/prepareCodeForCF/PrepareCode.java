package de.refactoringBot.refactoring.supportedRefactorings.prepareCodeForCF;

import java.util.*;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.metamodel.JavaParserMetaModel;

public class PrepareCode {

    private ArrayList<PrepareCodeInnerClass> innerClasses = new ArrayList<>();

    public void checkPrecondition8(CompilationUnit compUnit) {
        // Remove imports
        ArrayList<Node> importNodesToDelete = new ArrayList<>(compUnit.getImports());
        for (Node nodeToDelete : importNodesToDelete) {
            compUnit.accept(new RemoveImportNodeVisitor(), nodeToDelete);
        }
        compUnit.addImport("java.util.*");

        // Remove parent class
        ArrayList<Node> parentNodesToDelete = new ArrayList<>();
        for (Node classNode : compUnit.getChildNodes()) {
            if (classNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node childNode : classNode.getChildNodes()) {
                    if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                        parentNodesToDelete.add(childNode);
                    }
                }
            }
        }
        for (Node nodeToDelete : parentNodesToDelete) {
            compUnit.accept(new RemoveNodeVisitor(), nodeToDelete);
        }

        //
        for (Node classNode : compUnit.getChildNodes()) {
            // Get class nodes
            if (classNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node methodNode : classNode.getChildNodes()) {
                    // Get method nodes
                    if (methodNode.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                        for (Node statementNode : methodNode.getChildNodes()) {

                            // 1. Get parameter of method
                            if (statementNode.getMetaModel() == JavaParserMetaModel.parameterMetaModel) {
                                for (Node someOtherNode : statementNode.getChildNodes()) {
                                    if (someOtherNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                                        addInnerClassToCollection(getClassOrInterfaceName(someOtherNode));
                                    }
                                }
                            }

                            // 2. get statements of methods
                            if (statementNode.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {
                                getStatements(statementNode);
                            }
                        }
                    }
                }
            }
        }



        System.out.println("innerClassNames-------------------------------_____----");
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            compUnit = addInnerClass(compUnit, innerClass);
        }
        System.out.println(compUnit);
    }

    private ArrayList<String> getStatements(Node statement) {
        System.out.println("________" + statement.getMetaModel());
        ArrayList<String> statementList = new ArrayList<>(); // TODO evtl. entfernen
        if(statement.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {
            for(Node blockStatement : statement.getChildNodes())
                statementList.addAll(getStatements(blockStatement));
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.ifStmtMetaModel) {
            for (Node ifNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(ifNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.forStmtMetaModel) {
            for (Node forNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(forNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.binaryExprMetaModel) {
            for (Node binaryNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(binaryNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.methodCallExprMetaModel) {
            for (Node methodCallNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(methodCallNode));
            }
            // Add methods to the inner class collection
            addInnerClassMethodCallToCollection(statement);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.assignExprMetaModel) {
            for (Node assignExprNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(assignExprNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.fieldAccessExprMetaModel) {
            for (Node fieldAccessExpr : statement.getChildNodes()) {
                statementList.addAll(getStatements(fieldAccessExpr));
            }
            addInnerClassVarToCollection(statement);
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.forEachStmtMetaModel) {
            for (Node forEachNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(forEachNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.whileStmtMetaModel) {
            for (Node whileNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(whileNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.doStmtMetaModel) {
            for (Node doNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(doNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.expressionStmtMetaModel) {
            for (Node exprNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(exprNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.switchStmtMetaModel) {
            for (Node switchNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(switchNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.switchEntryStmtMetaModel) {
            for (Node switchEntryNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(switchEntryNode));
            }
        }
//        else if(statement instanceof AssertStatement) {
//            AssertStatement assertStatement = (AssertStatement)statement;
//            statementList.add(assertStatement);
//        }
//        else if(statement instanceof LabeledStatement) {
//            LabeledStatement labeledStatement = (LabeledStatement)statement;
//            //handling of LabeledStatement
//            statementList.addAll(getStatements(labeledStatement.getBody()));
//        }
        else if(statement.getMetaModel() == JavaParserMetaModel.returnStmtMetaModel) {
            for (Node returnNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(returnNode));
            }
        }
//        else if(statement instanceof SynchronizedStatement) {
//            SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
//            //handling of SynchronizedStatement
//            statementList.addAll(getStatements(synchronizedStatement.getBody()));
//        }
//        else if(statement instanceof ThrowStatement) {
//            ThrowStatement throwStatement = (ThrowStatement)statement;
//            statementList.add(throwStatement);
//        }
//        else if(statement instanceof TryStatement) {
//            TryStatement tryStatement = (TryStatement)statement;
//            statementList.addAll(getStatements(tryStatement.getBody()));
//			/*List<CatchClause> catchClauses = tryStatement.catchClauses();
//			for(CatchClause catchClause : catchClauses) {
//				statementList.addAll(getStatements(catchClause.getBody()));
//			}
//			Block finallyBlock = tryStatement.getFinally();
//			if(finallyBlock != null)
//				statementList.addAll(getStatements(finallyBlock));*/
//        }
        else if(statement.getMetaModel() == JavaParserMetaModel.variableDeclarationExprMetaModel) {
            for (Node varDeclNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(varDeclNode));
            }
            //statementList.addAll(getClassNamesFromVariableDeclr(statement)); TODO evtl. löschen

            // Add inner classes to the inner class collection
            ArrayList<String> classNames = getClassNamesFromVariableDeclr(statement);
            for (String className : classNames) {
                addInnerClassToCollection(className);
            }
            // Add the decl variables to the inner class collection
            addInnerClassDeclVarToCollection(statement);
            // Add methods to the inner class collection
            addInnerClassMethodsToCollection(statement);
        }
//        else if(statement instanceof ConstructorInvocation) {
//            ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
//            statementList.add(constructorInvocation);
//        }
//        else if(statement instanceof SuperConstructorInvocation) {
//            SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
//            statementList.add(superConstructorInvocation);
//        }
//        else if(statement instanceof BreakStatement) {
//            BreakStatement breakStatement = (BreakStatement)statement;
//            statementList.add(breakStatement);
//        }
//        else if(statement instanceof ContinueStatement) {
//            ContinueStatement continueStatement = (ContinueStatement)statement;
//            statementList.add(continueStatement);
//        }
        return statementList;
    }

    // Second step: Add inner class
    private CompilationUnit addInnerClass(CompilationUnit compUnit, PrepareCodeInnerClass innerClass) {
        // Füge eine temporäre Methode hinzu um sie gleich danach durch eine inner class zu ersetzen
        NodeList<TypeDeclaration<?>> types = compUnit.getTypes();
        for (TypeDeclaration<?> type : types) {
            // Add extracted method
            type.addMethod(innerClass.className, Modifier.PRIVATE);
        }

        // Suche die Node mit der letzten Methode, die gerade hinzugefügt wurde
        Node lastMethodNode = null;
        for (Node classNode : compUnit.getChildNodes()) {
            // Get class nodes
            if (classNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node methodNode : classNode.getChildNodes()) {
                    // Get method nodes
                    if (methodNode.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                        lastMethodNode = methodNode;
                    }
                }

            }
        }

        // Füll die Methode mit Infos
        MethodDeclaration methodInfo = (MethodDeclaration) lastMethodNode;
        BlockStmt block = new BlockStmt();
        // Füll die Methode mit Infos
        for (PrepareCodeICVariable variable : innerClass.variables) {
            VariableDeclarationExpr b = new VariableDeclarationExpr(new TypeParameter("String"), "XXX_" + variable.varName);
            block.addStatement(b);
        }
        for (PrepareCodeICMethod method : innerClass.methods) {
            VariableDeclarationExpr a = new VariableDeclarationExpr(new TypeParameter("String"), method.methodName + "_" + method.returnValue + "_" + method.params.size());
            block.addStatement(a);
        }
        methodInfo.setBody(block);

        // Ersetze die Methode durch inner class
        compUnit.accept(new ReplaceMethodWithInnerClass(), lastMethodNode);
        return compUnit;
    }

    private ArrayList<String> getClassNamesFromVariableDeclr(Node varDeclrNode) {
        ArrayList<String> classNames = new ArrayList<>();
        for (Node nodePart : varDeclrNode.getChildNodes()) {
            for (Node nodePart2 : nodePart.getChildNodes()) {
                if (nodePart2.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                    classNames.add(getClassOrInterfaceName(nodePart2));
                }
            }
        }
        return classNames;
    }

    private void addInnerClassVarToCollection(Node fieldAccessNode) {
        FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) fieldAccessNode;
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            if (innerClass.varNames.contains(fieldAccessExpr.getScope().toString())) {
                innerClass.variables.add(new PrepareCodeICVariable(fieldAccessExpr.getName().toString()));
            }
        }
    }

    private void addInnerClassToCollection(String newClassName) {
        boolean found = false;
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            if (innerClass.className.equals(newClassName)) {
                found = true;
            }
        }

        if (!found) {
            innerClasses.add(new PrepareCodeInnerClass(newClassName));
        }
    }

    // Add declared variables to the collection
    private void addInnerClassDeclVarToCollection(Node declVarNode) {
        boolean found = false;
        String foundClassName = "";
        for (Node declVarPart : declVarNode.getChildNodes()) {
            for (Node declVarPart2 : declVarPart.getChildNodes()) {

                // If the class is in the innerClassCollection than add the declVar to the collection
                if (declVarPart2.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                    for (PrepareCodeInnerClass innerClass : innerClasses) {
                        if (innerClass.className.equals(declVarPart2.toString())) {
                            found = true;
                            foundClassName = declVarPart2.toString();
                            break;
                        }
                    }
                }
                if (declVarPart2.getMetaModel() == JavaParserMetaModel.simpleNameMetaModel && found) {
                    for (PrepareCodeInnerClass innerClass : innerClasses) { // TODO refactoren (for Schleife kann man entfernen)
                        if (innerClass.className.equals(foundClassName)) {
                            innerClass.varNames.add(declVarPart2.toString());
                            break;
                        }
                    }
                    break;
                }
            }
            if (found) {
                break;
            }
        }
    }

    // For methods in in method calls
    private void addInnerClassMethodCallToCollection(Node possibleMethodCallNode) {
        MethodCallExpr methodCallExpr = (MethodCallExpr) possibleMethodCallNode;
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            if (methodCallExpr.getScope().isPresent() && innerClass.varNames.contains(methodCallExpr.getScope().get().toString())) {
                // TODO falls noch nicht hinzugefügt/ Doppelte Methoden abfangen
                innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(), "void", methodCallExpr.getArguments()));
            }
        }
    }

    // For methods in variable declaration statements
    private void addInnerClassMethodsToCollection(Node possibleMethodCallNode) {
        for (Node node : possibleMethodCallNode.getChildNodes()) {
            if (node.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
                VariableDeclarator variableDeclarator = (VariableDeclarator) node;
                for (Node node2 : node.getChildNodes()) {
                    if (node2.getMetaModel() == JavaParserMetaModel.methodCallExprMetaModel) {
                        MethodCallExpr methodCallExpr = (MethodCallExpr) node2;
                        for (PrepareCodeInnerClass innerClass : innerClasses) {
                            if (methodCallExpr.getScope().isPresent() && innerClass.varNames.contains(methodCallExpr.getScope().get().toString())) {
                                // TODO falls noch nicht hinzugefügt/ Doppelte Methoden abfangen
                                innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(), variableDeclarator.getType().toString(), methodCallExpr.getArguments()));
                            }
                        }

                    }
                }
            }
        }
    }

    // Es kann sein das man ein Array oder Set oder so bekommt, dass muss getrennt werden
    private String getClassOrInterfaceName(Node node) {
        for (Node tempNode : node.getChildNodes()) {
            if (tempNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                return tempNode.toString();
            }
        }
        return node.toString();
    }
}

class RemoveImportNodeVisitor extends ModifierVisitor<Node> {
    @Override
    public Node visit(ImportDeclaration n, Node args) {
        if (n == args) {
            return null;
        }
        return super.visit(n, args);
    }
}

/**
 * Visitor implementation for removing nodes.
 */
class RemoveNodeVisitor extends ModifierVisitor<Node> {
    @Override
    public Visitable visit(ClassOrInterfaceType n, Node args) {
        if (n == args) {
            return null;
        }
        return super.visit(n, args);
    }
}

/**
 * Visitor implementation for adding method call of the extracted method.
 */
class ReplaceMethodWithInnerClass extends ModifierVisitor<Node> {
    @Override
    public Visitable visit(MethodDeclaration n, Node args) {
        if (n == args) {
            ArrayList<String[]> allMethods = new ArrayList<>();
            String className = n.getName().toString();
            EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.add(Modifier.PUBLIC);
            ClassOrInterfaceDeclaration newInnerClass = new ClassOrInterfaceDeclaration(modifiers, false, className);
            for (Statement statement : n.getBody().get().getStatements()) {
                String[] methodInfoString = statement.toString().replace(";", "").split(" ");

                if (methodInfoString[1].startsWith("XXX")) {
                    String[] splitMethodInfos = methodInfoString[1].split("_"); // TODO besseren Split Character wählen
                    //VariableDeclarator var = new VariableDeclarator(new TypeParameter("Object"), splitMethodInfos[1]);
                    newInnerClass.addField(new TypeParameter("Object"), splitMethodInfos[1]);
                } else {
                    String[] splitMethodInfos = methodInfoString[1].split("_"); // TODO besseren Split Character wählen
                    allMethods.add(splitMethodInfos);
                }
            }

            for (String[] methodInfo : allMethods) {
                //newInnerClass.addMethod(methodInfo[0], Modifier.PUBLIC);
                NodeList<Parameter> params = new NodeList<>();
                for (int i = 0; i < Integer.valueOf(methodInfo[2]); i++) {
                    params.add(new Parameter(new TypeParameter("Object"), "test" + i));
                }
                MethodDeclaration method = new MethodDeclaration(modifiers, methodInfo[0], new TypeParameter(methodInfo[1]), params);

                // Add return statement
                if (!methodInfo[1].equals("void")) {
                    BlockStmt blockStmt = new BlockStmt();
                    blockStmt.addStatement(new ReturnStmt("null"));
                    method.setBody(blockStmt);
                }

                newInnerClass.addMember(method);
            }
            // They all need a method or an error will be thrown TODO kann man löschen
            //newInnerClass.addMethod("test", Modifier.PUBLIC);
            return newInnerClass;
        }
        return super.visit(n, args);
    }
}
