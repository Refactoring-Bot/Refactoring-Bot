package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import org.checkerframework.javacutil.AbstractTypeProcessor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import com.sun.source.util.TreePath;

// hack to macke processing environment for cfg creation work see https://github.com/typetools/checker-framework/issues/2046

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DummyTypeProcessor extends AbstractTypeProcessor {
    public static ProcessingEnvironment processingEnv;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.processingEnv = env;
    }

    @Override
    public void typeProcess(TypeElement element, TreePath tree) {}
}
