package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ExtractMethodTest {
    String simpleExamplePath;
    String ifElseExamplePath;
    String switchExamplePath;

    @Before
    public void setUp() throws Exception {
        // refactor simple example
        this.simpleExamplePath = this.copyFileToTemp("RefactorSimpleExample.java");

        // refactor if else example
        this.ifElseExamplePath = this.copyFileToTemp("RefactorIfElseExample.java");

        // refactor switch example
        this.switchExamplePath = this.copyFileToTemp("RefactorSwitchExample.java");
    }


    @Test
    public void refactorMethodSimple() {
        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.simpleExamplePath, 4);
        Assert.assertEquals(commitMessage, "extracted method");
    }

    @Test
    public void refactorMethodIfElse() {
        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.ifElseExamplePath, 4);
        Assert.assertEquals(commitMessage, "extracted method");
    }

    @Test
    public void refactorMethodSwitch() {
        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.switchExamplePath, 4);
        Assert.assertEquals(commitMessage, "extracted method");
    }

    @After
    public void tearDown() {
        this.deleteFile(simpleExamplePath);
        this.deleteFile(ifElseExamplePath);
        this.deleteFile(switchExamplePath);
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