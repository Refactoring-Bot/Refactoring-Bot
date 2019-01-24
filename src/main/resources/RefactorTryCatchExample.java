package refactor;

public class RefactorTryCatchExample {
    public void simpleMethod() {
        int a = 0;
        int[] b = {2 , 3, 4, 6};

        try {
            System.out.println(b);
        } catch (exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("finally");
        }

        System.out.println(b);
    }
}
