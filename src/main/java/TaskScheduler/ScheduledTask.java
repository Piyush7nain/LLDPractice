package TaskScheduler;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ScheduledTask implements Comparable<ScheduledTask> {
    private static AtomicLong sequence = new AtomicLong(0);
    @Getter private final Runnable task;
    @Getter@Setter
    private boolean cancelled;
    @Getter@Setter
    private long executionTime;
    private final long sequenceNumber;
    @Getter
    private final long period;
    @Getter
    private final boolean fixedRate;
    public ScheduledTask(Runnable task, long executionTime, long period, boolean fixedRate) {
        this.task = task;
        this.period = period;
        this.fixedRate = fixedRate;
        this.cancelled = false;
        this.executionTime = executionTime;
        this.sequenceNumber = sequence.getAndIncrement();
    }
    public ScheduledTask(Runnable task, long delay, long period) {
        this(task, System.currentTimeMillis() + delay, period, false);
    }
    public ScheduledTask(Runnable task) {
        this(task, System.currentTimeMillis(), 0);
    }
    public ScheduledTask(Runnable task, long executionTime) {
        this(task, executionTime, 0);
    }
    @Override
    public int compareTo(ScheduledTask o) {
        if (Long.compare(executionTime, o.executionTime) == 0){
            return Long.compare(this.sequenceNumber, o.sequenceNumber);
        }else{
            return Long.compare(this.executionTime, o.executionTime);
        }
    }

}
