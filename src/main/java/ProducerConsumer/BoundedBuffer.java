package ProducerConsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BoundedBuffer<T> {
    private final BlockingQueue<T> buffer;
    private final int capacity;
    public BoundedBuffer(int capacity) {
        buffer = new LinkedBlockingQueue<>();
        this.capacity = capacity;
    }

    public synchronized void produce(T t) throws InterruptedException {
       while(buffer.size()==capacity){
           System.out.println(Thread.currentThread().getName()+" :: Bugger is full, waiting for capacity");
           wait();
       }
       buffer.put(t);
       notifyAll();
    }

    public synchronized T consume() throws InterruptedException {
        while(buffer.isEmpty()){
            System.out.println(Thread.currentThread().getName()+" :: Buffer is empty. Waiting for capacity");
            wait();
        }
        T t = buffer.take();
        notifyAll();
        return t;
    }

    public synchronized int size(){
        return buffer.size();
    }
}
