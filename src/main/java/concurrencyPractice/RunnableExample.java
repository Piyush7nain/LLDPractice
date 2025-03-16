package concurrencyPractice;

public class RunnableExample {
    public static void main(String[] args) {

        MyRunnable myRunnable = new MyRunnable();
        System.out.println("Running thread: " + Thread.currentThread().getName());
        // This should not be called, as this directly does not create a new thread. It runs sequentially
//        myRunnable.run();

        // Always call like this, as this creates new thread and run async
        Thread thread = new Thread(myRunnable);
        thread.start();
        System.out.println("Completed thread: " + Thread.currentThread().getName());
    }
}

class MyRunnable implements Runnable {
    public void run() {
        try {
            Thread.sleep(2000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Running runnable class: " +Thread.currentThread().getName());
    }
}
