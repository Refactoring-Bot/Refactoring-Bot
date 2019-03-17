package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.util.Name;
import java.lang.reflect.Field;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementVisitor;
import org.checkerframework.dataflow.cfg.CFGBuilder;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.javacutil.AnnotationProvider;
import org.checkerframework.javacutil.BasicAnnotationProvider;
import org.checkerframework.javacutil.trees.TreeBuilder;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import javax.lang.model.element.TypeElement;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.ImplicitThisLiteralNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.VariableDeclarationNode;
import org.checkerframework.javacutil.TreeUtils;
import sun.jvm.hotspot.memory.Generation;
import sun.tools.java.ClassType;
import sun.tools.java.Type;

public class CustomCFGBuilder extends CFGBuilder {

    public static ControlFlowGraph build(CompilationUnitTree root, UnderlyingAST underlyingAST, boolean assumeAssertionsEnabled, boolean assumeAssertionsDisabled, ProcessingEnvironment env) {
        TreeBuilder builder = new TreeBuilder(env);
        AnnotationProvider annotationProvider = new BasicAnnotationProvider();
        CFGBuilder.PhaseOneResult phase1result = (new CustomCFGBuilder.CustomCFGTranslationPhaseOne(builder, annotationProvider, assumeAssertionsEnabled, assumeAssertionsDisabled, env)).process(root, underlyingAST);
        ControlFlowGraph phase2result = CFGBuilder.CFGTranslationPhaseTwo.process(phase1result);
        ControlFlowGraph phase3result = CFGBuilder.CFGTranslationPhaseThree.process(phase2result);
        return phase3result;
    }

    public static ControlFlowGraph build(TreePath bodyPath, UnderlyingAST underlyingAST, boolean assumeAssertionsEnabled, boolean assumeAssertionsDisabled, ProcessingEnvironment env) {
        TreeBuilder builder = new TreeBuilder(env);
        AnnotationProvider annotationProvider = new BasicAnnotationProvider();
        CFGBuilder.PhaseOneResult phase1result = (new CustomCFGBuilder.CustomCFGTranslationPhaseOne(builder, annotationProvider, assumeAssertionsEnabled, assumeAssertionsDisabled, env)).process(bodyPath, underlyingAST);
        ControlFlowGraph phase2result = CFGBuilder.CFGTranslationPhaseTwo.process(phase1result);
        ControlFlowGraph phase3result = CFGBuilder.CFGTranslationPhaseThree.process(phase2result);
        return phase3result;
    }

    public static ControlFlowGraph build(CompilationUnitTree root, UnderlyingAST underlyingAST, ProcessingEnvironment env) {
        return build(root, underlyingAST, false, false, env);
    }

    public static ControlFlowGraph build(CompilationUnitTree root, MethodTree tree, ClassTree classTree, ProcessingEnvironment env) {
        UnderlyingAST underlyingAST = new UnderlyingAST.CFGMethod(tree, classTree);
        return build((CompilationUnitTree)root, underlyingAST, false, false, env);
    }

    public static class CustomCFGTranslationPhaseOne extends CFGTranslationPhaseOne {
        public CustomCFGTranslationPhaseOne(TreeBuilder treeBuilder, AnnotationProvider annotationProvider, boolean assumeAssertionsEnabled, boolean assumeAssertionsDisabled, ProcessingEnvironment env) {
            super(treeBuilder, annotationProvider, assumeAssertionsEnabled, assumeAssertionsDisabled, env);
        }

        @Override
        public Node visitVariable(VariableTree tree, Void p) {

            // see JLS 14.4

            boolean isField =
                    getCurrentPath().getParentPath() != null
                            && getCurrentPath().getParentPath().getLeaf().getKind() == Kind.CLASS;
            Node node = null;

            ClassTree enclosingClass = TreeUtils.enclosingClass(getCurrentPath());
            TypeElement classElem = TreeUtils.elementFromDeclaration(enclosingClass);
            if (classElem == null) {
                classElem = ExtractMethodUtil.symbol;
            }
            Node receiver = new ImplicitThisLiteralNode(classElem.asType());

            if (isField) {
                ExpressionTree initializer = tree.getInitializer();
                assert initializer != null;
                node =
                        translateAssignment(
                                tree,
                                new FieldAccessNode(
                                        tree, TreeUtils.elementFromDeclaration(tree), receiver),
                                initializer);
            } else {
                // local variable definition
                if (TreeUtils.typeOf(tree) == null) {
                    try {
                        Field f = null;
                        f = tree.getClass().getDeclaredField("sym");
                        f.setAccessible(true);
                        f.set(tree, ExtractMethodUtil.varSymbol);

                        /*
                        f = tree.getClass().getDeclaredField("type");
                        f.setAccessible(true);
                        f.set(tree, ExtractMethodUtil.type);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                VariableDeclarationNode decl = new VariableDeclarationNode(tree);
                extendWithNode(decl);

                // initializer

                ExpressionTree initializer = tree.getInitializer();
                if (initializer != null) {
                    node =
                            translateAssignment(
                                    tree, new LocalVariableNode(tree, receiver), initializer);
                }
            }

            return node;
        }
    }
}
