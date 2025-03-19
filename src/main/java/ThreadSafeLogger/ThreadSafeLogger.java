package ThreadSafeLogger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeLogger {
    private final Deque<Log> logs = new LinkedList<>();
    private final ReentrantLock writelock = new ReentrantLock();
    private final Condition writing = writelock.newCondition();
    private final String filePath;
    private final AtomicInteger writerCount = new AtomicInteger(0);
    public ThreadSafeLogger(String filePath) {
        this.filePath = filePath;
    }
    public void log(String message, LogLevel level, String threadName) {
        writelock.lock();
        try{
            writerCount.incrementAndGet();
            Log log = new Log(message, LocalTime.now().toString(), level, threadName);
            logs.addLast(log);
            writerCount.decrementAndGet();
        } finally {
            if(writerCount.get()==0){
                writing.signal();
            }
            writelock.unlock();
        }

    }
    public void flush(FlushType flushType) {
        writelock.lock();
        while( writerCount.get()>0 ) {
            System.out.println(Thread.currentThread().getName()+" waiting for writing to complete");
            try {
                writing.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try{
            if(flushType.equals(FlushType.FILE)){
                writeLogsToFile();
            }else if( flushType.equals(FlushType.PRINT)){
                writeLogsToConsole();
            }
            try {
                System.out.println(Thread.currentThread().getName()+" flush sleeping");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }finally {
            writelock.unlock();
        }
    }

    private void writeLogsToFile() {
        try (FileWriter writer = new FileWriter(filePath, true)) { // Append mode
            while(!logs.isEmpty()) {
                writer.write(logs.pop().toString() + System.lineSeparator()); // Remove and write
            }
        } catch (IOException e) {
            System.err.println("Error writing logs to file: " + e.getMessage());
        }
    }
    private void writeLogsToConsole() {
        while(!logs.isEmpty()) {
            System.out.println(logs.pop().toString() + System.lineSeparator());
        }
    }
    private int getLogLine(){
        return logs.size()+1;
    }
}

enum FlushType {
    PRINT, FILE
}