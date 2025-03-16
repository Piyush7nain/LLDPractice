package PrintOddEven;

public class PrintOddEvenTest {
    public static void main(String[] args) {
        OddEvenPrinter printer = new OddEvenPrinter(10);

        Thread t1 = new Thread(() -> {
            try {
                printer.printOdd();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Odd-Thread");
        Thread t2 = new Thread(() -> {
            try {
                printer.printEven();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Even-Thread");

        t1.start();
        t2.start();
    }

}
