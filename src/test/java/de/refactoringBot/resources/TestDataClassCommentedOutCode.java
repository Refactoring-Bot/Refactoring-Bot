package de.refactoringBot.resources;

public class TestDataClassCommentedOutCode {
    
    public int add (int a, int b) {
        // Here's a line comment
        int c = a + b;
        /*
        And here's a block comment!
        */
        c = b - a;
        /**
         * Javadoc comment!
         */
        return a + b;
    }
    
}
