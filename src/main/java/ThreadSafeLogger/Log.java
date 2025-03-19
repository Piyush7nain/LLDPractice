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
        print();
    }

    public void print(){
        System.out.println(threadName + " -- " + level + " -- " + time + " -- " + logLine);
    }

    public String getLogLine() {
        return threadName + " -- " + level + " -- " + time + " -- " + logLine;
    }

    @Override
    public String toString() {
        return getLogLine();
    }
}

enum LogLevel {
    INFO, WARN, ERROR
}
