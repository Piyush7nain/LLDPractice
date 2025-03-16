package ReadWriteLock;

import java.util.Random;

public class Writer implements Runnable {
    private final ReadWriteLock lock;
    private String name;
    private StringBuilder sharedContent;

    public Writer(ReadWriteLock lock, String name, StringBuilder sharedContent) {
        this.lock = lock;
        this.name = name;
        this.sharedContent = sharedContent;
    }

    @Override
    public void run() {
        try {
            int wait = new Random().nextInt(2000);
            System.out.println("Writer: "+ name+" Waiting for " + wait + " seconds");
            Thread.sleep(wait);
            lock.startWriting();
            sharedContent.append(lock.getCount());
            System.out.println("Writer " + name+ " is writing content "+ sharedContent.toString() );
            Thread.sleep(1000); // Simulate writing time
            System.out.println(Thread.currentThread().getName() + " finished writing.");
            lock.endWriting();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
