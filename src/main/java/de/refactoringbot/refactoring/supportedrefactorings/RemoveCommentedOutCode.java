package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.FileNotFoundException;

import org.springframework.stereotype.Component;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;


import de.refactoringbot.configuration.BotConfiguration;

import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.refactoring.RefactoringImpl;
import de.refactoringbot.model.botissue.BotIssue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used for executing the removal of commented out code
 *
 * @author Justin Kissling
 */

@Component
public class RemoveCommentedOutCode implements RefactoringImpl {

    Integer line;
    BotConfiguration botConfig;
    HashMap<Integer,Comment> commentsWithLine;

    /**
     * This method performs the refactoring and returns a commit message.
     *
     * @param issue
     * @param gitConfig
     * @return commitMessage
     * @throws FileNotFoundException
     */
    @Override
    public String performRefactoring(BotIssue issue, GitConfiguration gitConfig)
            throws FileNotFoundException, IOException {
        
        // Prepare data
        String path = gitConfig.getRepoFolder() + "/" + issue.getFilePath();
        
        line = issue.getLine();
        
        // Read file
        FileInputStream in = new FileInputStream(path);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(in));

        List<Comment> comments = compilationUnit.getAllContainedComments();

        for (Comment comment : comments) {      
            if ((line >= comment.getBegin().get().line) && (line <= comment.getEnd().get().line)) {
                if (comment.isLineComment()) {
                    // If it's the SonarQube line, we remove it without checking, otherwise check for commented out code
                    if (line.equals(issue.getLine()) || isCommentedOutCode(comment.getContent())){
                        comment.remove();
                        // Increase the line variable to find more commented out code lines below
                        line++;
                    } else {
                        break;
                    }
                } else {
                    // The comment is a multi-line comment, so we remove the entire thing right away
                    comment.remove();
                    break;
                }
            }
        }
        
        // Printing the output file with JavaParser
        PrintWriter out = new PrintWriter(path);
        out.println(LexicalPreservingPrinter.print(compilationUnit));
        out.close();

        // Return commit message
        return "Removed commented out code at line " + line;
    }

    /**
     * Since sonarqube only provides one line per comment block, this method is
     * used to determine if other comments also contain code
     *
     * @param line The content of the comment
     * @return Whether or not the comment contains code
     */
    private boolean isCommentedOutCode(String content) {

        // Method call
        if (content.matches("[a-zA-Z]+\\.[a-zA-Z] +\\(.*\\)")) {
            return true;
            // if or while statement
        } else if (content.matches("(if\\s*\\(.*)| (while\\s*\\(.*)")) {
            return true;
            // empty lines or lines ending with a semicolon
        } else if ((content.trim().endsWith(";")) || content.trim().equals("")) {
            return true;
            // Single brackets (from methods or if statements)
        } else if ((content.trim().equals("{")) || content.trim().equals("}")) {
            return true;
        }

        return false;
    }

}