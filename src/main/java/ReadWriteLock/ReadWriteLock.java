package ReadWriteLock;

import java.util.concurrent.atomic.AtomicInteger;

public class ReadWriteLock {
    private AtomicInteger contentUpdateCounter = new AtomicInteger(0);
    private int readers;
    private boolean isWriting = false;
    public synchronized void startReading() throws InterruptedException {
        while(isWriting){
            System.out.println("Reader "+ Thread.currentThread().getName()+" waiting for writing to stop");
            wait();
        }
        readers++;
    }

    public synchronized void endReading() throws InterruptedException {
        readers--;
        if(readers==0){
            System.out.println("Readers has finished reading");
            notifyAll();
        }
    }
    public synchronized void startWriting() throws InterruptedException {
        while(readers>0 || isWriting){
            System.out.println("Writer "+ Thread.currentThread().getName()+" waiting for readers to stop");
            wait();
        }
        isWriting = true;
        contentUpdateCounter.incrementAndGet();
    }

    public synchronized void endWriting() throws InterruptedException {
        isWriting = false;
        System.out.println("Writer "+ Thread.currentThread().getName()+" releasing writing lock");
        notifyAll();
    }
    public synchronized int getCount(){
        return contentUpdateCounter.get();
    }
}
