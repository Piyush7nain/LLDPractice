package ThreadSafeLogger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeLogger {
    private List<Log> logs = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private String filePath;
    public ThreadSafeLogger(List<Log> logs) {}
}
