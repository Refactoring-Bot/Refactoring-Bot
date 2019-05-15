package de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones;

import com.github.javaparser.ast.body.MethodDeclaration;
import de.refactoringBot.model.sonarQube.Blocks;

import java.util.ArrayList;
import java.util.List;

public class CloneInfo {
    Blocks cloneBlocks;
    MethodDeclaration methodDeclarationOfFirstFile;
    String mainExtractFile;
    String mainExtractPackage;
    ArrayList<String> constructorParams;
    boolean allInFile1;

    public CloneInfo() {
        this.constructorParams = new ArrayList();
    }

}
