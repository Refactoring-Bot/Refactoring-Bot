package refactor;

public class RefactorSwitchExample {
    public void simpleMethod() {
        int a = 0;
        int b = 1;

        switch (a) {
            case 0:
                b = a;
                break;
            case 1:
                b = 69;
                break;
            case 2:
                b = 42;
                break;
            default:
                b = 1000;
                break;
        }

        System.out.println(b);
    }
}
