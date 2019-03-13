package smells.methodtoolong;

import java.util.Scanner;

/**
 * Too long methods are more difficult to understand and might have too many
 * responsibilities. They should therefore be refactored into smaller methods.
 */
public class RefactorLongExample {

    public static void main(String[] args) {
        new RefactorLongExample().runProgram();
    }

    /**
     * Depending on the threshold value, this method can be too long with more than
     * 30 lines of code. Comments describing the functionality of multiple lines of
     * code are sometimes a good indication that this part can be extracted.
     */
    public void runProgram() {
        print("hello");
        print("hello again");
        for (int i = 0; i<10; i++) {

            // this should be extracted
            // only this
            if (i%2 == 0) {
                print(i);
            }

        }
        print("bye");
        print("bye again");
    }

    private void println(Object object) {
        System.out.println(object); // NOSONAR
    }

}