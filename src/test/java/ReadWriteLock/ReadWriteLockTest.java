package ReadWriteLock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadWriteLockTest {

    public static void main(String[] args) {
        ReadWriteLock lock = new ReadWriteLock();
        StringBuilder sb = new StringBuilder();
        Thread r1 = new Thread(new Reader(lock, "Reader-1", sb), "Reader-1");
        Thread r2 = new Thread(new Reader(lock, "Reader-2", sb), "Reader-2");
        Thread w1 = new Thread(new Writer(lock, "Writer-1",sb), "Writer-1");
        Thread r3 = new Thread(new Reader(lock, "Reader-3", sb), "Reader-3");
        Thread w2 = new Thread(new Writer(lock, "Writer-2", sb), "Writer-2");
        w1.start();
        r1.start();
        r2.start();

        r3.start();
        w2.start();
    }
}
