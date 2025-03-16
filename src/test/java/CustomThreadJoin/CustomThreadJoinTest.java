package CustomThreadJoin;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class CustomThreadJoinTest {

    @Test
    public void testThreadCompletionBeforeJoin() throws InterruptedException {
        Runnable runnable = () -> {
            System.out.println("Runnable will sleep now for 10 Seconds");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Runnable Finished its task");
        };
        CustomThread thread = new CustomThread(runnable);
        thread.start();
        System.out.println("Waiting for thread to finish");
        thread.customJoin();// Custom join implementation
        System.out.println("Thread finished");
        assertFalse(thread.isRunning());// "Thread should have completed execution before proceeding.");
    }

    public static void main(String[] args) throws InterruptedException {
        test1();
        test2();
    }
    public static void test2() throws InterruptedException {
        Runnable runnable = () -> {
            System.out.println("Runnable will sleep now for 10 Seconds");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Runnable Finished its task");
        };
        CustomThread thread = new CustomThread(runnable);
        thread.start();

        Thread waitingThread1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() +" Waiting for thread to finish");
            thread.customJoin();
            System.out.println(Thread.currentThread().getName() +" Finished its task");
        });

        Thread waitingThread2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() +" Waiting for thread to finish");
            thread.customJoin();
            System.out.println(Thread.currentThread().getName() +" Finished its task");
        });

        waitingThread1.start();
        waitingThread2.start();

        waitingThread1.join();
        waitingThread2.join();

    }
    public static void test1() throws InterruptedException {
        Runnable runnable = () -> {
            System.out.println("Runnable will sleep now for 10 Seconds");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Runnable Finished its task");
        };
        CustomThread thread = new CustomThread(runnable);
        thread.start();
        System.out.println("Waiting for thread to finish");
        thread.customJoin();// Custom join implementation
        System.out.println("Thread finished");
    }
}
