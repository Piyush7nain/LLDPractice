package ProducerConsumer;

public class Producer implements Runnable {
    private BoundedBuffer<Integer> buffer;
    private final int id;
    private final int itemCount;
    public Producer(BoundedBuffer<Integer> buffer, int id, int itemCount) {
        this.buffer = buffer;
        this.id = id;
        this.itemCount = itemCount;
    }
    @Override
    public void run() {
        for (int i = 0; i < itemCount; i++) {
            try {
                buffer.produce(i);
                System.out.println("Producer " + id + " produced " + i);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
