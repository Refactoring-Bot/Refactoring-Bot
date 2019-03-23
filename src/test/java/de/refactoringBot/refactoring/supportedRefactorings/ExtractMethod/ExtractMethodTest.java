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
    String forLoopExamplePath;
    String tryCatchExamplePath;
    String multipleReturnsExamplePath;
    String longExamplePath;

    @Before
    public void setUp() throws Exception {
        // refactor simple example
        this.simpleExamplePath = this.copyFileToTemp("RefactorSimpleExample.java");

        // refactor if else example
        this.ifElseExamplePath = this.copyFileToTemp("RefactorIfElseExample.java");

        // refactor switch example
        this.switchExamplePath = this.copyFileToTemp("RefactorSwitchExample.java");

        // refactor for loop example
        this.forLoopExamplePath = this.copyFileToTemp("RefactorForLoopExample.java");

        // refactor try catch example
        this.tryCatchExamplePath = this.copyFileToTemp("RefactorTryCatchExample.java");

        // refactor multiple returns example
        this.multipleReturnsExamplePath = this.copyFileToTemp( "RefactorMultipleReturnsExample.java");

        // refactor long example
        this.longExamplePath = this.copyFileToTemp("KafkaAdminClient.java");
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

    @Test
    public void refactorMethodForLoop() {
        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.forLoopExamplePath, 4);
        Assert.assertEquals(commitMessage, "extracted method");
    }

    @Test
    public void refactorMethodTryCatch() {
        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.tryCatchExamplePath, 4);
        Assert.assertEquals(commitMessage, "extracted method");
    }
    @Test
    public void refactorMethodMultipleReturns() {
        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.multipleReturnsExamplePath, 4);
        Assert.assertEquals(commitMessage, "extracted method");
    }

    @Test
    public void refactorLong() {
        ExtractMethod extractMethod = new ExtractMethod();
        String commitMessage = extractMethod.refactorMethod(this.longExamplePath, 949);
        Assert.assertEquals(commitMessage, "extracted method");
    }

    @After
    public void tearDown() {
        this.deleteFile(this.simpleExamplePath);
        this.deleteFile(this.ifElseExamplePath);
        this.deleteFile(this.switchExamplePath);
        this.deleteFile(this.forLoopExamplePath);
        this.deleteFile(this.tryCatchExamplePath);
        this.deleteFile(this.multipleReturnsExamplePath);
        this.deleteFile(this.longExamplePath);
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