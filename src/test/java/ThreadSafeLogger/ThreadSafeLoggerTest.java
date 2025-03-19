package ThreadSafeLogger;

public class ThreadSafeLoggerTest {

    public static void main(String[] args) throws InterruptedException {
        ThreadSafeLogger logger = new ThreadSafeLogger("src/test/java/ThreadSafeLogger/log.txt");
        Thread t1 = new Thread(()->{
              for (int i = 0; i < 1000; i++) {
                  logger.log("Writer-1 writing log with count "+i, LogLevel.INFO, Thread.currentThread().getName());
                  try {
                      Thread.sleep(500);
                  } catch (InterruptedException e) {
                      throw new RuntimeException(e);
                  }
              }
        }, "Write-1");
        Thread t2 = new Thread(()->{
            for (int i = 0; i < 1000; i++) {
                logger.log("Writer-2 writing log with count "+i, LogLevel.INFO, Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Write-2");
        Thread t3 = new Thread(()->{
            for (int i = 0; i < 1000; i++) {
                logger.log("Writer-3 writing log with count "+i, LogLevel.INFO, Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Write-3");
        Thread t4 = new Thread(()->{
            for (int i = 0; i < 1000; i++) {
                logger.log("Writer-4 writing log with count "+i, LogLevel.INFO, Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Write-4");

        Thread t5 = new Thread(()->{
            for (int i = 0; i < 3; i++) {
                System.out.println("Flushing logs to file");
                logger.flush(FlushType.FILE);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Flusher-1");


        t1.start();
        t2.start();
        t3.start();
        t4.start();

        t5.start();
    }
}
