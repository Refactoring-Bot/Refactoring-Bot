package org.checkerframework.dataflow.cfg;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod.ExtractMethodUtil;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

public class DefaultVariable implements VariableElement {

    @Override
    public Object getConstantValue() {
        return null;
    }

    @Override
    public TypeMirror asType() {
        return Type.noType;
        //return new JCPrimitivType(TypeTag.INT, ExtractMethodUtil.symbol);
    }

    @Override
    public ElementKind getKind() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return null;
    }

    @Override
    public Name getSimpleName() {
        return null;
    }

    @Override
    public Element getEnclosingElement() {
        return null;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        return null;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return null;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return null;
    }
}
