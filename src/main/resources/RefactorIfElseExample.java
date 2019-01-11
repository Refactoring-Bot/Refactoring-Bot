package refactor;

public class RefactorIfElseExample {
    public void simpleMethod() {
        int a = 0;
        int b = 1;

        if (a == b) {
            b = 2;
        } else {
            a = 2;
        }

        System.out.println(b);
    }
}
