package concurrencyPractice;


public class ThreadClassExample {

    public static void main(String[] args) {
        System.out.println("First running from Thread: "+Thread.currentThread().getName());
        ThreadClass tc = new ThreadClass();
        tc.start();

        System.out.println("Finished running Thread: "+Thread.currentThread().getName());

    }
}

class ThreadClass extends Thread{
    @Override
    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Running Thread: "+ Thread.currentThread().getName());
    }
}
