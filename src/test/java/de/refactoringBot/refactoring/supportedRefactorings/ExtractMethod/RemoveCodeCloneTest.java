package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import de.refactoringBot.refactoring.supportedRefactorings.prepareCodeForCF.PrepareCode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RemoveCodeCloneTest {
    String codeCloneExamplePath;

    @Before
    public void setUp() throws Exception {
        this.codeCloneExamplePath = this.copyFileToTemp("RefactorCodeCloneExample.java");
    }

    @Test
    public void refactorCodeClone() throws FileNotFoundException {
        // Read file
        FileInputStream in = new FileInputStream(this.codeCloneExamplePath);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

        PrepareCode removeCodeClones = new PrepareCode();
        removeCodeClones.checkPrecondition8(compilationUnit);

        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.codeCloneExamplePath, 88);
        Assert.assertEquals(commitMessage, "extracted method"); // TODO
    }

    @After
    public void tearDown() {
        this.deleteFile(this.codeCloneExamplePath);
    }

    private String copyFileToTemp(String fileName) throws Exception {

        Path tempDir = Files.createTempDirectory("ExtractMethodTest");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Path filePath = Paths.get(tempDir.toString(), fileName);
        File file = new File(classLoader.getResource(fileName).getFile());
        return Files.copy(file.toPath(), (new File(filePath.toString())).toPath(), StandardCopyOption.REPLACE_EXISTING).toAbsolutePath().toString();
    }

    private void deleteFile(String path) {
        (new File(path)).delete();
    }
}
