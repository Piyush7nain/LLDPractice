package ThreadSafeLogger;

public class Log {
    private final String logLine;
    private final String time;
    private final LogLevel level;
    private final String threadName;
    public Log(String logLine, String time, LogLevel level, String threadName) {
        this.logLine = logLine;
        this.time = time;
        this.level = level;
        this.threadName = threadName;
    }

    public void print(){
        System.out.println(threadName + "-" + level + "-" + time + "-" + logLine);
    }
}

enum LogLevel {
    INFO, WARN, ERROR
}
