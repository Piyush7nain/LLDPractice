package TaskScheduler;

import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TaskSchedulor {

    private final PriorityQueue<ScheduledTask> tasks;
    private final ExecutorService executor;
    private volatile boolean shutdown;
    private volatile boolean running;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition queueNotEmpty = lock.newCondition();
    private Thread schedulerThread;
    public TaskSchedulor(int poolsize) {
        tasks = new PriorityQueue<>();
        if(poolsize < 1) {
            throw new IllegalArgumentException("poolsize must be greater than 0");
        }
        this.executor = Executors.newFixedThreadPool(poolsize);
        this.shutdown = false;
        this.running = true;
        runTasks();
    }

    public ScheduledTask scheduleOnce(Runnable task, long delay, TimeUnit unit) {
        if(delay < 0) {
            throw new IllegalArgumentException("delay must be greater than 0");
        }
        long executionTime = System.currentTimeMillis() + unit.toMillis(delay);
        ScheduledTask scheduledTask = new ScheduledTask(task, executionTime);
        addTasks(scheduledTask);
        return scheduledTask;
    }
    public ScheduledTask scheduleAtFixedRate(Runnable runnable, long delay, TimeUnit unit, long period ){
        if(delay < 0) {
            throw new IllegalArgumentException("delay must be greater than 0");
        }
        long executionTime = System.currentTimeMillis() + unit.toMillis(delay);
        ScheduledTask scheduledTask = new ScheduledTask(runnable, executionTime, period, true);
        addTasks(scheduledTask);
        return scheduledTask;
    }
    public ScheduledTask scheduleAtFixedDelay(Runnable runnable, long delay, TimeUnit unit, long period) {
        if(delay < 0) {
            throw new IllegalArgumentException("delay must be greater than 0");
        }
        long executionTime = System.currentTimeMillis() + unit.toMillis(delay);
        ScheduledTask scheduledTask = new ScheduledTask(runnable, executionTime, period, false);
        addTasks(scheduledTask);
        return scheduledTask;
    }

    private void runTasks() {
        schedulerThread = new Thread(() ->{
            while(running) {
                ScheduledTask scheduledTask = null;
                long delay = 0;
                lock.lock();
                try{
                // check if queue has tasks
                if(running && tasks.isEmpty()) {
                    try {
                        queueNotEmpty.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(!running){
                    break;
                }

                scheduledTask = tasks.peek();
                long currentTime = System.currentTimeMillis();
                assert scheduledTask != null;
                if(currentTime < scheduledTask.getExecutionTime() ){
                    if(!scheduledTask.isCancelled()) {
                        tasks.poll();
                        executor.submit(scheduledTask.getTask());
                        if (scheduledTask.getPeriod() > 0 && !scheduledTask.isCancelled()) {
                            scheduledTask.setExecutionTime(System.currentTimeMillis() + scheduledTask.getPeriod());
                            addTasks(scheduledTask);
                        } else {
                            System.out.println("Task was cancelled, not rescheduling");
                        }
                    }else{
                        continue;
                    }
                }else{
                    delay = scheduledTask.getExecutionTime() - currentTime;
                }
                }finally {
                    lock.unlock();
                }

                if(delay > 0) {
                    lock.lock();
                    try{
                        queueNotEmpty.awaitNanos(TimeUnit.MILLISECONDS.toNanos(delay));
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                        System.out.println("Interrupted while waiting for delay to complete");
                        running = false;
                    }finally {
                        lock.unlock();
                    }
                }
            }
            System.out.println("Scheduler Stopped");

        });

        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }

    private void addTasks(ScheduledTask scheduledTask) {
        lock.lock();
        try {
            tasks.offer(scheduledTask);
            queueNotEmpty.signal();
        }finally {
            lock.unlock();
        }
    }
    public void shutdown() {
        shutdown = true;
        running = false;
        lock.lock();
        try{
            queueNotEmpty.signalAll();
        }finally {
            lock.unlock();
        }

        try{
            if(schedulerThread != null) {
                schedulerThread.join(5000);
                if(schedulerThread.isAlive()) {
                    schedulerThread.interrupt();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();
        try{
            if(executor.awaitTermination(30000, TimeUnit.MILLISECONDS)){
                executor.shutdownNow();
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownNow() {
        shutdown = true;
        running = false;
        lock.lock();
        try {
            queueNotEmpty.signalAll();
        }finally {
            lock.unlock();
        }
        if(schedulerThread != null) {
            schedulerThread.interrupt();
        }
        executor.shutdownNow();
        System.out.println("Task Scheduler Stopped");
    }
}
