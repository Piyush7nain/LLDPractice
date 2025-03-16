package CustomThreadJoin;

import java.util.concurrent.atomic.AtomicBoolean;

public class CustomThread extends Thread {
    private final Runnable runnable;
    private final AtomicBoolean running = new AtomicBoolean(false);
    public CustomThread(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public synchronized void run ()  {
        while(running.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }
        running.compareAndSet(false, true);
        runnable.run();
        running.compareAndSet(true, false);
        notifyAll();
    }

    public synchronized void customStop() {
        running.compareAndSet(true, false);
    }

    public boolean isRunning() {
        return running.get();
    }
    public synchronized void customJoin(){
        while(running.get()) {
            try {
                System.out.println("Thread " + Thread.currentThread().getId() + " join called");
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
