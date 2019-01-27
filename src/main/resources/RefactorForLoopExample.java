package refactor;

public class RefactorForLoopExample {
    public void simpleMethod() {
        int a = 0;
        int[] b = {2 , 3, 4, 6};

        for (int c : b) {
            a += c;
            System.out.println(a);
            if (a == 10) {
                break;
            }
        }

        System.out.println(b);
    }
}
