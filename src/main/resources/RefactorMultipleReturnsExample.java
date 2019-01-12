package refactor;

public class RefactorMultipleReturnsExample {
    public int simpleMethod() {
        int a = 0;
        int b = 1;
        int c = 2;

        if ( c < 2) {
            return b;
        }

        a += 2;
        return a;
    }
}
