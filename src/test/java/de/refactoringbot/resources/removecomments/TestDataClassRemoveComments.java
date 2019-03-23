package de.refactoringbot.resources.removecomments;

public class TestDataClassRemoveComments {

    public int add(int a, int b) {
        // int d = a + b;
        // int e = a + b;
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
    
    // Normal comment here
    // Sonarcloud line here;
    // Another normal comment

}