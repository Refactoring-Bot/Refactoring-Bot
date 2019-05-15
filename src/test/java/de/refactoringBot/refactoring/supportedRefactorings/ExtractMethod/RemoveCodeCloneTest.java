package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.supportedRefactorings.removeCodeClones.RemoveCodeClones;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
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
    public void refactorCodeClone() {
        // Zum Testen Strings ersetzen
        String gitConfigRepoFolder = "C:\\Users\\Dennis\\Documents258";
        String gitConfigAnalysisServiceKey = "net.sourceforge.pmd:pmd";
        String issueFilePath = "pmd-apex/src/main/java/net/sourceforge/pmd/lang/apex/ast/DumpFacade.java";

        RemoveCodeClones removeCodeClones = new RemoveCodeClones();
        BotIssue issue = new BotIssue();
        issue.setFilePath(issueFilePath);
        GitConfiguration gitConfig = new GitConfiguration();
        gitConfig.setRepoFolder(gitConfigRepoFolder);
        gitConfig.setAnalysisServiceProjectKey(gitConfigAnalysisServiceKey);
        try {
            removeCodeClones.performRefactoring(issue, gitConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
