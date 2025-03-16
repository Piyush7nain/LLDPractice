package ProducerConsumer;

public class Consumer implements Runnable {
    private BoundedBuffer<Integer> buffer;
    private final int id;
    private final int itemCount;
    public Consumer(BoundedBuffer<Integer> buffer, int id, int itemCount) {
        this.buffer = buffer;
        this.id = id;
        this.itemCount = itemCount;
    }

    @Override
    public void run() {
        for (int i = 0; i < itemCount; i++) {
            try {
                int value = buffer.consume();
                System.out.println("Consumer "+id+" Consumed " + value);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
