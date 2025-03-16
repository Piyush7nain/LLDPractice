package PrintOddEven;

public class OddEvenPrinter {
    private final int max;
    private final Object lock;
    private int count =0;
    public OddEvenPrinter(int max) {
        this.max = max;
        this.lock = new Object();
    }

    public void printEven() throws InterruptedException {
        synchronized (lock){
            while (count < max){
                if (count % 2 != 0){
                    System.out.println(Thread.currentThread().getName() +" waiting on number "+ count);
                    lock.wait();
                }
                System.out.println(Thread.currentThread().getName() +" printing even number "+ count);
                Thread.sleep(5000);
                count++;
                lock.notify();
            }
        }
    }

    public void printOdd() throws InterruptedException {
        synchronized (lock){
            while (count < max){
                if (count % 2 == 0){
                    System.out.println(Thread.currentThread().getName() +" waiting on number "+ count);
                    lock.wait();
                }
                System.out.println(Thread.currentThread().getName() +" printing odd number "+ count);
                Thread.sleep(5000);
                count++;
                lock.notify();
            }
        }
    }
}
