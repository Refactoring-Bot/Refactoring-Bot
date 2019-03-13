package de.refactoringBot.refactoring.supportedRefactorings.ExtractMethod;

import java.io.FileNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MethodExtractorTest {

    @Test
    public void refactorMethodSimple() throws FileNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = classLoader.getResource("RefactorInnerExample.java").getPath();
        RefactorCandidate candidate = new RefactorCandidate();
        candidate.startLine = 25L;
        candidate.endLine = 29L;
        MethodExtractor extractor = new MethodExtractor(candidate, path);
        extractor.apply();
    }

}