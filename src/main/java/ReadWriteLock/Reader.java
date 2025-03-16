package ReadWriteLock;

import java.util.Random;

public class Reader implements Runnable {
    private ReadWriteLock lock;
    private String name;
    private StringBuilder sharedContent;

    public Reader(ReadWriteLock lock, String name, StringBuilder sharedContent) {
        this.lock = lock;
        this.name = name;
        this.sharedContent = sharedContent;

    }
    @Override
    public void run() {
        try {
            int wait = new Random().nextInt(2000);
            System.out.println("Reader: "+ name+" Waiting for " + wait + " seconds");
            Thread.sleep(wait);
            lock.startReading();
            System.out.println(Thread.currentThread().getName() + " is READING "+ sharedContent.toString());
            Thread.sleep(5000); // Simulate reading time
            System.out.println(Thread.currentThread().getName() + " finished reading.");
            lock.endReading();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
