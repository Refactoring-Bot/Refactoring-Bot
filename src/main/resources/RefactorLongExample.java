package smells.methodtoolong;

import java.util.Scanner;

/**
 * Too long methods are more difficult to understand and might have too many
 * responsibilities. They should therefore be refactored into smaller methods.
 */
public class RefactorLongExample {

    public static void main(String[] args) {
        new MethodTooLong().runProgram();
    }

    /**
     * Depending on the threshold value, this method can be too long with more than
     * 30 lines of code. Comments describing the functionality of multiple lines of
     * code are sometimes a good indication that this part can be extracted.
     */
    public void runProgram() {
        println("The calculator is ready to calculate!");

        // get numbers
        Scanner inp = new Scanner(System.in);
        int num1;
        int num2;
        println("Enter first number:");
        num1 = inp.nextInt();
        println("Enter second number:");
        num2 = inp.nextInt();

        // get operation
        println("Enter your selection: 1 for addition, 2 for subtraction, 3 for multiplication and 4 for division:");
        int choose;
        choose = inp.nextInt();

        // calculate and print result
        switch (choose) {
            case 1:
                println("The result of the addition is:");
                println(num1 + num2);
                break;
            case 2:
                println("The result of the subtraction is:");
                println(num1 - num2);
                break;
            case 3:
                println("The result of the multiplication is:");
                println(num1 * num2);
                break;
            case 4:
                println("The result of the division is:");
                println(num1 / num2);
                break;
            default:
                println("Illegal Operation");
        }

        inp.close();
        println("Thank you for using this awesome calculator.");
    }

    private void println(Object object) {
        System.out.println(object); // NOSONAR
    }

}