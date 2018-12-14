package de.refactoringBot.refactoring.supportedRefactorings;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * This class is used for executing the removal of commented out code
 *
 * @author Justin Kissling
 */
@Component
public class RemoveCommentedOutCode extends VoidVisitorAdapter<Object> {

    Integer line;

    @Autowired
    BotConfiguration botConfig;

    /**
     * This method performs the refactoring and returns a commit message.
     *
     * @param issue
     * @param gitConfig
     * @return commitMessage
     * @throws FileNotFoundException
     */
    public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws FileNotFoundException, IOException {
        // Prepare data
        String path
                = gitConfig.getRepoFolder() + "/" + issue.getFilePath();
        line = issue.getLine();

        // Check and see if the commented out code is old enough to be removed. Newer code may still be in use
        int minAgeInDays = 7;

        LocalDate localDate = LocalDate.now();
        LocalDate issueDate = LocalDate.parse(issue.getCreationDate().substring(0, 10));

        if (Period.between(issueDate, localDate).getDays() > minAgeInDays) {
            System.out.println("Issue is old enough.");
        } else {
            System.out.println("Issue is not old enough.");
        }

        // Read file
        FileInputStream in = new FileInputStream(path);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

        // TODO: Is this list sorted?
        List<Comment> comments = compilationUnit.getAllContainedComments();

        // Start and end line of the comment(s) that we want to remove
        int start = line;
        int end = line;
        
        // Remembering the current line to find a block of comments
        int currentLine = line;
        
        for (Comment comment : comments) {
            if ((currentLine >= comment.getBegin().get().line) && (currentLine <= comment.getEnd().get().line)) {
                if (comment.isLineComment()) {
                    // Trying to find more line comments below since Sonarqube only reports the first
                    end = comment.getBegin().get().line;
                    currentLine++;
                } else {
                    // The comment is a multi-line comment, so we remove the entire thing right away
                    start = comment.getBegin().get().line;
                    end = comment.getEnd().get().line;
                    break;
                }
            }
        }

        removeLinesFromFile(start, end, path);
        
        // Return commit message
        return "Removed commented out code at line " + line;
    }
   
    private void removeLinesFromFile(int start, int end, String path) throws FileNotFoundException, IOException {
        File inputFile = new File(path);
        File tempFile = new File(inputFile.getParent() + File.separator + "temp.java");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String currentLine;
        int lineNumber = 0;

        while ((currentLine = reader.readLine()) != null) {
            lineNumber++;

            if ((lineNumber >= start) && (lineNumber <= end)) {
                continue;
            }
            writer.write(currentLine + System.getProperty("line.separator"));
        }

        writer.close();
        reader.close();
        inputFile.delete();
        tempFile.renameTo(inputFile);

    }

}
