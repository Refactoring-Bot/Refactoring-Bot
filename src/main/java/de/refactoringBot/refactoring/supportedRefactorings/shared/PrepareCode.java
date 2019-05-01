package de.refactoringBot.refactoring.supportedRefactorings.shared;

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
import sun.reflect.annotation.AnnotationType;

public class PrepareCode {

    private ArrayList<PrepareCodeInnerClass> innerClasses = new ArrayList<>();

    public CompilationUnit prepareCode(CompilationUnit compUnit) {
        // Remove imports
        ArrayList<Node> importNodesToDelete = new ArrayList<>(compUnit.getImports());
        for (Node nodeToDelete : importNodesToDelete) {
            compUnit.accept(new RemoveImportNodeVisitor(), nodeToDelete);
        }
        compUnit.addImport("java.util.*");

        // Remove parent class and annotations
        ArrayList<Node> parentNodesToDelete = new ArrayList<>();
        ArrayList<Node> annotationNodesToDelete = new ArrayList<>();
        for (Node classNode : compUnit.getChildNodes()) {
            if (classNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node childNode : classNode.getChildNodes()) {
                    if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                        // parent class node
                        parentNodesToDelete.add(childNode);
                    } else if (childNode.getMetaModel() == JavaParserMetaModel.normalAnnotationExprMetaModel) {
                        // annotation node
                        annotationNodesToDelete.add(childNode);
                    }
                }
            }
        }

        // Go through the class methods and variables to find all dependencies
        for (Node classNode : compUnit.getChildNodes()) {
            // Get class nodes
            if (classNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node methodOrVariableNode : classNode.getChildNodes()) {

                    // Get field variables nodes
                    if (methodOrVariableNode.getMetaModel() == JavaParserMetaModel.fieldDeclarationMetaModel) {
                        for (Node fieldVar : methodOrVariableNode.getChildNodes()) {
                            if (fieldVar.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
                                for (Node classOrInterfaceNode : fieldVar.getChildNodes()) {
                                    if (classOrInterfaceNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                                        addInnerClassToCollection(getClassOrInterfaceName(classOrInterfaceNode));
                                    }
                                }
                            }
                        }
                    }

                    // Get method nodes
                    if (methodOrVariableNode.getMetaModel() == JavaParserMetaModel.methodDeclarationMetaModel) {
                        for (Node statementNode : methodOrVariableNode.getChildNodes()) {
                            // Remove annotation node
                            if (statementNode.getMetaModel() == JavaParserMetaModel.normalAnnotationExprMetaModel) {
                                annotationNodesToDelete.add(statementNode);
                            }

                            // Check the parameters of the method for inner classes
                            if (statementNode.getMetaModel() == JavaParserMetaModel.parameterMetaModel) {
                                for (Node someOtherNode : statementNode.getChildNodes()) {
                                    if (someOtherNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                                        addInnerClassToCollection(getClassOrInterfaceName(someOtherNode));
                                    }
                                }
                            }

                            // Check the statements of methods for inner classes
                            if (statementNode.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {
                                getStatements(statementNode);
                            }
                        }
                    }
                }
            }
        }

        // Remove parent node of the class
        for (Node nodeToDelete : parentNodesToDelete) {
            compUnit.accept(new RemoveParentNodeVisitor(), nodeToDelete);
        }
        // Remove annotations
        for (Node nodeToDelete : annotationNodesToDelete) {
            compUnit.accept(new RemoveAnnotationNodeVisitor(), nodeToDelete);
        }

        // Remove inner classes that primitive data types, like int, string, long, etc.
        removePrimitiveDataTypes();

        // Add inner classes
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            compUnit = addInnerClass(compUnit, innerClass);
        }
        System.out.println(compUnit);
        return compUnit;
    }

    // Removes primitive types from the inner class collection
    private void removePrimitiveDataTypes() {
        ArrayList<PrepareCodeInnerClass> innerClassesToRemove = new ArrayList<>();
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            switch (innerClass.className) {
                case "String":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "int":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "byte":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "short":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "long":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "float":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "double":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "char":
                    innerClassesToRemove.add(innerClass);
                    break;
                case "boolean":
                    innerClassesToRemove.add(innerClass);
                    break;
            }
        }
        innerClasses.removeAll(innerClassesToRemove);
    }

    // Goes recursive through the statements and finds the inner classes which needds to be added to the compUnit
    private ArrayList<String> getStatements(Node statement) {
        ArrayList<String> statementList = new ArrayList<>(); // TODO evtl. entfernen
        if (statement.getMetaModel() == JavaParserMetaModel.blockStmtMetaModel) {
            for(Node blockStatement : statement.getChildNodes())
                statementList.addAll(getStatements(blockStatement));
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.objectCreationExprMetaModel) {
            for (Node objectExprNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(objectExprNode));
                if (objectExprNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                    ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) objectExprNode;
                    addInnerClassToCollection(classOrInterfaceType.getName().toString());
                }
            }
            ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) statement;
            addObjectCreationToInnerClassCollection(objectCreationExpr);
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.ifStmtMetaModel) {
            for (Node ifNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(ifNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.forStmtMetaModel) {
            for (Node forNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(forNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.unaryExprMetaModel) {
            for (Node unaryNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(unaryNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.enclosedExprMetaModel) {
            for (Node enclosedNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(enclosedNode));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.binaryExprMetaModel) {
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

    // Adds inner class
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
            VariableDeclarationExpr b = new VariableDeclarationExpr(new TypeParameter("String"), "XXX" + "_" + variable.varName);
            block.addStatement(b);
        }
        for (int paramNum : innerClass.constructorParamNumber) {
            VariableDeclarationExpr c = new VariableDeclarationExpr(new TypeParameter("String"), "YYY" + "_" + paramNum);
            block.addStatement(c);
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

    private void addObjectCreationToInnerClassCollection(ObjectCreationExpr objectCreation) {
        addInnerClassToCollection(objectCreation.getType().getName().toString());

        for (PrepareCodeInnerClass innerClass : innerClasses) {
            if (innerClass.className.equals(objectCreation.getType().getName().toString())
                    && !innerClass.constructorParamNumber.contains(objectCreation.getArguments().size())) {
                innerClass.constructorParamNumber.add(objectCreation.getArguments().size());
            }
        }
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
                    for (PrepareCodeInnerClass innerClass : innerClasses) { // TODO refactoren (for Schleife kann man ersetzen)
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

    // For methods in method calls
    private void addInnerClassMethodCallToCollection(Node possibleMethodCallNode) {
        MethodCallExpr methodCallExpr = (MethodCallExpr) possibleMethodCallNode;
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            if (methodCallExpr.getScope().isPresent() && innerClass.varNames.contains(methodCallExpr.getScope().get().toString())) {
                // Falls die Methode noch nicht hinzugefügt wurde, füge sie hinzu
                if (!isMethodAlreadyAdded(innerClass, methodCallExpr.getName().toString(), methodCallExpr.getArguments(), "void")) {
                    innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(), "void", methodCallExpr.getArguments()));
                }
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
                                // Falls die Methode noch nicht hinzugefügt wurde, füge sie hinzu
                                if (!isMethodAlreadyAdded(innerClass, methodCallExpr.getName().toString(), methodCallExpr.getArguments(), variableDeclarator.getType().toString())) {
                                    innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(), variableDeclarator.getType().toString(), methodCallExpr.getArguments()));
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private boolean isMethodAlreadyAdded(PrepareCodeInnerClass innerClass, String newMethodName, NodeList<Expression> params, String returnType) {
        boolean found = false;
        for (PrepareCodeICMethod method : innerClass.methods) {
            if (method.methodName.equals(newMethodName) && method.params.size() == params.size() && method.returnValue.equals(returnType)) {
//                boolean isIdentical = true;
//                for (int i = 0; i < method.params.size(); i++) {
//                    if (method.params.get(i).getMetaModel() != params.get(i).getMetaModel()) {
//                        isIdentical = false;
//                    }
//                }
                found = true;
            }
        }
        return found;
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
class RemoveParentNodeVisitor extends ModifierVisitor<Node> {
    @Override
    public Visitable visit(ClassOrInterfaceType n, Node args) {
        if (n == args) {
            return null;
        }
        return super.visit(n, args);
    }
}

/**
 * Visitor implementation for removing annotation nodes.
 */
class RemoveAnnotationNodeVisitor extends ModifierVisitor<Node> {
    @Override
    public Visitable visit(NormalAnnotationExpr n, Node args) {
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
            ArrayList<Integer> allConstructors = new ArrayList<>();
            String className = n.getName().toString();
            EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.add(Modifier.PUBLIC);
            ClassOrInterfaceDeclaration newInnerClass = new ClassOrInterfaceDeclaration(modifiers, false, className);
            for (Statement statement : n.getBody().get().getStatements()) {
                String[] methodInfoString = statement.toString().replace(";", "").split(" ");

                if (methodInfoString[1].startsWith("XXX")) {
                    // Add variable
                    String[] splitMethodInfos = methodInfoString[1].split("_"); // TODO besseren Split Character wählen
                    newInnerClass.addField(new TypeParameter("Object"), splitMethodInfos[1]);
                } else if (methodInfoString[1].startsWith("YYY")) {
                    // Add constructor
                    String[] splitMethodInfos = methodInfoString[1].split("_"); // TODO besseren Split Character wählen
                    allConstructors.add(Integer.valueOf(splitMethodInfos[1]));
                } else {
                    String[] splitMethodInfos = methodInfoString[1].split("_"); // TODO besseren Split Character wählen
                    allMethods.add(splitMethodInfos);
                }
            }

            // Add constructors
            for (int paramNum : allConstructors) {
                NodeList<Parameter> params = new NodeList<>();
                for (int i = 0; i < paramNum; i++) {
                    params.add(new Parameter(new TypeParameter("Object"), "test" + i));
                }
                ConstructorDeclaration constructor = new ConstructorDeclaration(className);
                for (Parameter param : params) {
                    constructor.addParameter(param);
                }
                newInnerClass.addMember(constructor);
            }

            // Add methods
            for (String[] methodInfo : allMethods) {
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
            return newInnerClass;
        }
        return super.visit(n, args);
    }
}
