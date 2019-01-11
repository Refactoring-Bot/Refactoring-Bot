package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ExtractMethodTest {
    String simpleExamplePath;

    @Before
    public void setUp() throws Exception {
        String fileName = "RefactorSimpleExample.java";
        Path tempDir = Files.createTempDirectory("ExtractMethodTest");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Path filePath = Paths.get(tempDir.toString(), fileName);
        File file = new File(classLoader.getResource(fileName).getFile());
        this.simpleExamplePath = Files.copy(file.toPath(), (new File(filePath.toString())).toPath(), StandardCopyOption.REPLACE_EXISTING).toAbsolutePath().toString();
    }


    @Test
    public void refactorMethod() {
        ExtractMethod extractMethod = new ExtractMethod();
        extractMethod.refactorMethod(this.simpleExamplePath, 4);
    }

    @After
    public void tearDown() throws Exception {
        (new File(this.simpleExamplePath)).delete();
    }
}