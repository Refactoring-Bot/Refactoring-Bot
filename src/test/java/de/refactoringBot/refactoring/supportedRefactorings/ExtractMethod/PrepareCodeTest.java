package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;
import de.refactoringBot.refactoring.supportedRefactorings.shared.PrepareCode;
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

/**
 * Prints the transformed code of the file in the setup
 *
 * If you want to replace PrepareCodeExample, dont forget to change class name in the file to PrepareCodeExample
 */
public class PrepareCodeTest {
    String codeCloneExamplePath;

    @Before
    public void setUp() throws Exception {
        this.codeCloneExamplePath = this.copyFileToTemp("PrepareCodeExample.java");
    }

    @Test
    public void refactorCodeClone() throws FileNotFoundException {
        // Read file
        FileInputStream in = new FileInputStream(this.codeCloneExamplePath);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

        PrepareCode removeCodeClones = new PrepareCode();
        removeCodeClones.prepareCode(compilationUnit);

        Assert.assertEquals("test", "test");
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
