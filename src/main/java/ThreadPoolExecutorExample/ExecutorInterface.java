package ThreadPoolExecutorExample;

public interface ExecutorInterface  {
    void execute(Runnable runnable);
    void shutdown();
    void shutdownNow();
    int getActiveCount();
    int getQueueLength();
}
