package de.refactoringBot.refactoring.supportedRefactorings.shared;

import java.lang.reflect.Method;
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

/**
 * Class for dependencies in a compilationUnit
 */
public class PrepareCode {

    private ArrayList<PrepareCodeInnerClass> innerClasses = new ArrayList<>();
    private ArrayList<String> staticVariablesFromImports = new ArrayList<>();
    private ArrayList<String> existingStaticVariablesList = new ArrayList<>();
    private String classNameOfFile = "";

    public CompilationUnit prepareCode(CompilationUnit compUnit) {
        // Remove imports
        ArrayList<Node> importNodesToDelete = new ArrayList<>(compUnit.getImports());
        for (Node nodeToDelete : importNodesToDelete) {
            ImportDeclaration test = (ImportDeclaration) nodeToDelete;
            if (!test.getName().toString().startsWith("java")) {
                compUnit.accept(new RemoveImportNodeVisitor(), nodeToDelete);
            }
        }

        // Remove parent class and annotations
        ArrayList<Node> parentNodesToDelete = new ArrayList<>();
        ArrayList<Node> annotationNodesToDelete = new ArrayList<>();
        for (Node classNode : compUnit.getChildNodes()) {
            if (classNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceDeclarationMetaModel) {
                for (Node childNode : classNode.getChildNodes()) {

                    // Get name of class
                    if (childNode.getMetaModel() == JavaParserMetaModel.simpleNameMetaModel) {
                        SimpleName className = (SimpleName) childNode;
                        this.classNameOfFile = className.getIdentifier();
                    }

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
                                for (Node childNode : fieldVar.getChildNodes()) {
                                    if (childNode.getMetaModel() == JavaParserMetaModel.classOrInterfaceTypeMetaModel) {
                                        addInnerClassToCollection(getClassOrInterfaceName(childNode));
                                    } else if (childNode.getMetaModel() == JavaParserMetaModel.methodCallExprMetaModel) {
                                        checkIfMethodCallIsStatic(fieldVar, childNode);
                                    } else if (childNode.getMetaModel() == JavaParserMetaModel.simpleNameMetaModel) {
                                        SimpleName name = (SimpleName) childNode;
                                        if (name.toString().equals(name.toString().toUpperCase())) {
                                            existingStaticVariablesList.add(name.toString());
                                        }
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

        // Remove unnecessary inner classes
        removeUnnecessaryInnerClasses();
        removeUnnecessaryMethods();

        // Add static variables
        for (String name : staticVariablesFromImports) {
            compUnit = addStaticVariable(compUnit, name);
        }

        // Add inner classes
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            compUnit = addInnerClass(compUnit, innerClass);
        }

        // Print transformed compUnit
        System.out.println(compUnit);
        return compUnit;
    }

    // Remove unnecessary methods
    private void removeUnnecessaryMethods() {
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            ArrayList<Integer> indexesToRemove = new ArrayList<>();
            for (int i = 0; i < innerClass.methods.size(); i++) {
                for (int j = i + 1; j < innerClass.methods.size(); j++) {
                    if (innerClass.methods.get(i).methodName.equals(innerClass.methods.get(j).methodName) &&
                    innerClass.methods.get(i).params.size() == innerClass.methods.get(j).params.size()) {
                        if (innerClass.methods.get(i).returnValue.equals("void")) {
                            if (!indexesToRemove.contains(i)) {
                                indexesToRemove.add(i);
                            }
                        } else if (innerClass.methods.get(j).returnValue.equals("void")) {
                            if (!indexesToRemove.contains(j)) {
                                indexesToRemove.add(j);
                            }
                        }
                    }
                }
            }
            Collections.sort(indexesToRemove);
            Collections.reverse(indexesToRemove);
            for (int index : indexesToRemove) {
                innerClass.methods.remove(index);
            }
        }
    }

    // Removes primitive types, specific classes and class name of file from the inner class collection
    private void removeUnnecessaryInnerClasses() {
        ArrayList<PrepareCodeInnerClass> innerClassesToRemove = new ArrayList<>();
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            if (this.classNameOfFile.equals(innerClass.className)) {
                innerClassesToRemove.add(innerClass);
                continue;
            } else if (innerClass.className.contains("?")) {
                innerClassesToRemove.add(innerClass);
                continue;
            }
            switch (innerClass.className) {
                case "System":
                case "System.out":
                case "Map":
                case "Set":
                case "List":
                case "ArrayList":
                case "Array":
                case "Object":
                case "Throwable":
                case "String":
                case "int":
                case "Integer":
                case "byte":
                case "Byte":
                case "short":
                case "Short":
                case "long":
                case "Long":
                case "float":
                case "Float":
                case "double":
                case "Double":
                case "char":
                case "boolean":
                case "Boolean":
                    innerClassesToRemove.add(innerClass);
                    break;
            }
        }
        innerClasses.removeAll(innerClassesToRemove);
    }

    // Goes recursive through the statements and finds the inner classes which needs to be added to the compUnit
    private ArrayList<String> getStatements(Node statement) {
        ArrayList<String> statementList = new ArrayList<>();
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
        else if (statement.getMetaModel() == JavaParserMetaModel.nameExprMetaModel) {
            for (Node nameNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(nameNode));
            }
            addStaticClassVariablesFromImports(statement);
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
            checkIfMethodCallIsStatic(statement.getParentNode().get(), statement);
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
        else if(statement.getMetaModel() == JavaParserMetaModel.assertStmtMetaModel) {
            for (Node assertNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(assertNode));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.labeledStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                statementList.addAll(getStatements(node));
            }
        }
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
        else if(statement.getMetaModel() == JavaParserMetaModel.throwStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                statementList.addAll(getStatements(node));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.tryStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                statementList.addAll(getStatements(node));
            }
        }
        else if (statement.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
            for (Node node : statement.getChildNodes()) {
                statementList.addAll(getStatements(node));
            }
            for (Node classOrInterfaceNode : statement.getChildNodes()) {
                if (classOrInterfaceNode.getMetaModel() == JavaParserMetaModel.methodCallExprMetaModel) {
                    checkIfMethodCallIsStatic(statement, classOrInterfaceNode);
                }
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.variableDeclarationExprMetaModel) {
            for (Node varDeclNode : statement.getChildNodes()) {
                statementList.addAll(getStatements(varDeclNode));
            }
            //statementList.addAll(getClassNamesFromVariableDeclr(statement));

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
        else if(statement.getMetaModel() == JavaParserMetaModel.breakStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                statementList.addAll(getStatements(node));
            }
        }
        else if(statement.getMetaModel() == JavaParserMetaModel.continueStmtMetaModel) {
            for (Node node : statement.getChildNodes()) {
                statementList.addAll(getStatements(node));
            }
        }
        return statementList;
    }

    private CompilationUnit addStaticVariable(CompilationUnit compUnit, String name) {
        NodeList<TypeDeclaration<?>> types = compUnit.getTypes();
        for (TypeDeclaration<?> type : types) {
            // Add extracted method
            type.addField(new TypeParameter("Object"), name, Modifier.STATIC, Modifier.PUBLIC);
        }
        return compUnit;
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

        // Ersetze die Methode durch inner class
        ReplaceMethodWithInnerClass visitor = new ReplaceMethodWithInnerClass();
        //visitor.isStatic = innerClass.isStatic;
        visitor.innerClass = innerClass;
        compUnit.accept(visitor, lastMethodNode);
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

    private void addStaticClassVariablesFromImports(Node nameNode) {
        NameExpr nameExpr = (NameExpr) nameNode;
        boolean isStatic = nameExpr.getName().toString().equals(nameExpr.getName().toString().toUpperCase());
        if (isStatic && !staticVariablesFromImports.contains(nameExpr.getName().toString()) && !existingStaticVariablesList.contains(nameExpr.getName().toString())) {
            staticVariablesFromImports.add(nameExpr.getName().toString());
        }
    }

    private void addInnerClassVarToCollection(Node fieldAccessNode) {
        FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) fieldAccessNode;
        String firstChar = fieldAccessExpr.getScope().toString().substring(0,1);
        boolean hasUppercase = !firstChar.equals(firstChar.toLowerCase());
        // If scope is static
        if (hasUppercase) {
            addInnerClassToCollection(fieldAccessExpr.getScope().toString());
        }

        for (PrepareCodeInnerClass innerClass : innerClasses) {
            if (innerClass.varNames.contains(fieldAccessExpr.getScope().toString()) && !isVarAlreadyAdded(fieldAccessExpr.getName().toString())) {
                innerClass.variables.add(new PrepareCodeICVariable(fieldAccessExpr.getName().toString()));
            }
            if (hasUppercase && innerClass.className.equals(fieldAccessExpr.getScope().toString()) && !isVarAlreadyAdded(fieldAccessExpr.getName().toString())) {
                innerClass.isStatic = true;
                innerClass.variables.add(new PrepareCodeICVariable(fieldAccessExpr.getName().toString()));
            }
        }
    }

    private boolean isVarAlreadyAdded(String varName) {
        for (PrepareCodeInnerClass innerClass : innerClasses) {
            for (PrepareCodeICVariable var : innerClass.variables) {
                if (var.varName.equals(varName)) {
                    return true;
                }
            }
        }
        return false;
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

    // Adds static modifier to inner class
    private void checkIfMethodCallIsStatic(Node parentNode, Node node) {
        MethodCallExpr methodCallExpr = (MethodCallExpr) node;

        String varDelrType = "void";
        if (parentNode.getMetaModel() == JavaParserMetaModel.variableDeclaratorMetaModel) {
            VariableDeclarator variableDeclarator = (VariableDeclarator) parentNode;
            varDelrType = variableDeclarator.getType().toString();
        }

        // Check if the first Character is upper case, then the class is static
        if (methodCallExpr.getScope().isPresent()) {
            String scopeFirstChar = methodCallExpr.getScope().get().toString().substring(0, 1);
            boolean hasUppercase = !scopeFirstChar.equals(scopeFirstChar.toLowerCase());
            //boolean hasLowercase = !scopeFirstChar.equals(scopeFirstChar.toUpperCase());
            if (hasUppercase) {
                addInnerClassToCollection(methodCallExpr.getScope().get().toString());
                for (PrepareCodeInnerClass innerClass : innerClasses) {
                    if (innerClass.className.equals(methodCallExpr.getScope().get().toString())) {
                        innerClass.isStatic = true;
                        if (!isMethodAlreadyAdded(innerClass, methodCallExpr.getName().toString(), methodCallExpr.getArguments(), varDelrType)) {
                            innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(),
                                    varDelrType,
                                    methodCallExpr.getArguments(),
                                    innerClass.isStatic));
                        }
                    }
                }
            } else {
                for (PrepareCodeInnerClass innerClass : innerClasses) {
                    innerClass.varNames.add(methodCallExpr.getScope().get().toString());
                    for (String varName : innerClass.varNames) {
                        if (varName.equals(methodCallExpr.getScope().get().toString())) {
                            if (!isMethodAlreadyAdded(innerClass, methodCallExpr.getName().toString(), methodCallExpr.getArguments(), varDelrType)) {
                                innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(),
                                        varDelrType,
                                        methodCallExpr.getArguments(),
                                        false));
                            }
                        }
                    }
                }
            }
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
                    for (PrepareCodeInnerClass innerClass : innerClasses) {
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
                    innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(),
                            "void",
                            methodCallExpr.getArguments(),
                            false));
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
                                    innerClass.methods.add(new PrepareCodeICMethod(methodCallExpr.getName().toString(),
                                            variableDeclarator.getType().toString(),
                                            methodCallExpr.getArguments(),
                                            false));
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
                break;
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
    PrepareCodeInnerClass innerClass;
    @Override
    public Visitable visit(MethodDeclaration n, Node args) {
        if (n == args) {
            String className = n.getName().toString();
            EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.add(Modifier.PUBLIC);
            if (innerClass.isStatic) {
                modifiers.add(Modifier.STATIC);
            }
            ClassOrInterfaceDeclaration newInnerClass = new ClassOrInterfaceDeclaration(modifiers, false, className);
            for (PrepareCodeICVariable var : innerClass.variables) {
                if (innerClass.isStatic) {
                    newInnerClass.addField(new TypeParameter("Object"), var.varName, Modifier.PUBLIC, Modifier.STATIC);
                } else {
                    newInnerClass.addField(new TypeParameter("Object"), var.varName, Modifier.PUBLIC);
                }

            }

            // Add constructors
            for (int paramNum : innerClass.constructorParamNumber) {
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
            for (PrepareCodeICMethod methodInfo : innerClass.methods) {
                NodeList<Parameter> params = new NodeList<>();
                for (int i = 0; i < methodInfo.params.size(); i++) {
                    params.add(new Parameter(new TypeParameter("Object"), "test" + i));
                }
                if (methodInfo.returnValue.equals("void")) {
                    methodInfo.returnValue = "Object";
                }
                MethodDeclaration method = new MethodDeclaration(modifiers, methodInfo.methodName, new TypeParameter(methodInfo.returnValue), params);

                // Add return statement
                if (!methodInfo.returnValue.equals("void")) {
                    BlockStmt blockStmt = new BlockStmt();
                    if (methodInfo.returnValue.equals("int")) {
                        blockStmt.addStatement(new ReturnStmt("1"));
                    } else if (methodInfo.returnValue.equals("long")) {
                        blockStmt.addStatement(new ReturnStmt("1l"));
                    } else if (methodInfo.returnValue.equals("boolean")) {
                        blockStmt.addStatement(new ReturnStmt("true"));
                    } else {
                        blockStmt.addStatement(new ReturnStmt("null"));
                    }
                    method.setBody(blockStmt);
                }

                newInnerClass.addMember(method);
            }
            return newInnerClass;
        }
        return super.visit(n, args);
    }
}

