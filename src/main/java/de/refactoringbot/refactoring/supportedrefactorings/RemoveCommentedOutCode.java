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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.Period;
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
        // Check and see if the commented out code is old enough to be removed. Newer
        // code may still be in use
        // TODO: Don't perform a refactoring on newer code
        int minAgeInDays = 7;

        LocalDate localDate = LocalDate.now();
        LocalDate issueDate = LocalDate.parse(issue.getCreationDate().substring(0, 10));

        if (Period.between(issueDate, localDate).getDays() > minAgeInDays) {
            System.out.println("Issue is old enough (" + Period.between(issueDate, localDate).getDays() + " days)");
        } else {
            System.out.println("Issue is not old enough (" + Period.between(issueDate, localDate).getDays() + " days)");
        }

        // Read file
        FileInputStream in = new FileInputStream(path);
        CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(in));

        List<Comment> comments = compilationUnit.getAllContainedComments();

        // Start and end line of the comment(s) that we want to remove
        int start = line;
        int end = line;

        boolean isLineComments = true;

        for (Comment comment : comments) {      
            if ((line >= comment.getBegin().get().line) && (line <= comment.getEnd().get().line)) {
                if (comment.isLineComment()) {
                    commentsWithLine = getCommentsWithLine(comments);
                    
                    int currentLine = line;
                    
                    while (isCommentedOutCode(currentLine + 1)) {
                        currentLine++;
                    }
                    
                    end = currentLine;
                    
                    currentLine = line;
                    
                    while (isCommentedOutCode(currentLine - 1)) {
                        currentLine--;
                    }
                    
                    start = currentLine;
                    
                    break;

                } else {
                    // The comment is a multi-line comment, so we remove the entire thing right away
                    comment.remove();
                    isLineComments = false;
                    start = comment.getBegin().get().line;
                    end = comment.getEnd().get().line;
                    break;
                }
            }
        }

        // If it's a block or Javadoc comment, we can use JavaParser to print the output file
        // For line comments, JavaParser can't properly remove them, so the method below is used
        
        if (!isLineComments) {
            PrintWriter out = new PrintWriter(path);
            out.println(LexicalPreservingPrinter.print(compilationUnit));
            out.close();
        } else {
            removeLinesFromFile(start, end, path, isLineComments);            
        }

        // Return commit message
        return "Removed commented out code at line " + line;
    }

    /**
     * This method is used to print the output file when line comments were
     * removed, since Javaparser can't handle this case correctly
     *
     * @param start The starting line of the comment block to remove
     * @param end The end line of the comment block
     * @param path The path of the .java file to edit
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void removeLinesFromFile(int start, int end, String path, boolean isLineComments)
            throws FileNotFoundException, IOException {

        File inputFile = new File(path);
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {

            String currentLine;
            // Default: UNIX style line endings, use \r\n for Windows
            System.setProperty("line.separator", "\n");

            int lineNumber = 0;

            while ((currentLine = reader.readLine()) != null) {

                lineNumber++;

                if ((lineNumber >= start) && (lineNumber <= end)) {
                    // If the line also contains regular code before the comment, preserve it
                    if ((!currentLine.trim().startsWith("//")) && isLineComments) {
                        sb.append(currentLine.substring(0, currentLine.indexOf("//")))
                                .append(System.getProperty("line.separator"));
                    }
                    continue;
                }
                if (lineNumber != 1) {
                    sb.append(System.getProperty("line.separator"));
                }

                sb.append(currentLine);

            }
        }

        PrintWriter out = new PrintWriter(path);
        out.println(sb.toString());
        out.close();

    }

    /**
     * Since sonarqube only provides one line per comment block, this method is
     * used to determine if other comments also contain code
     *
     * @param line The content of the comment
     * @return Whether or not the comment contains code
     */
    private boolean isCommentedOutCode(int line) {
        String content;
        
        // If the line doesn't have a comment at all, we return false
        if (commentsWithLine.containsKey(line)) {
            content = commentsWithLine.get(line).getContent();
        } else {
            return false;
        }

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
    
    /**
     * To check for additional line comments above and below the line reported
     * by SonarQube, this method collects all comments along with the code line
     * @param comments The list of comments to be analyzed
     * @return A Hashmap with comments and their associated code line
     */
    
    private HashMap<Integer,Comment> getCommentsWithLine (List<Comment> comments) {
        HashMap<Integer,Comment> collectCommentsWithLine = new HashMap<>();
        
        for (Comment comment: comments) {
            collectCommentsWithLine.put(comment.getBegin().get().line, comment);
        }
        
        return collectCommentsWithLine;
    }
     
}