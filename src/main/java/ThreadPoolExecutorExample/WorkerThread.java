package ThreadPoolExecutorExample;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerThread extends Thread{
    private static final AtomicInteger workerNumber = new AtomicInteger(0);
    private final String workerName;
    private final BlockingQueue<Runnable> workQueue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean idle = new AtomicBoolean(true);
    public WorkerThread(BlockingQueue<Runnable> workQueue) {
        this.workerName = "Worker-" + workerNumber.incrementAndGet();
        this.setName(workerName);
        this.workQueue = workQueue;
    }
    @Override
    public void run() {
        System.out.println("WorkerThread " + workerName + " started");
        while (running.get() || !workQueue.isEmpty()) {
            try{
//                Runnable runnable = workQueue.poll(1, TimeUnit.SECONDS);
                System.out.println("WorkerThread " + workerName + " working queue is "+ Arrays.toString(workQueue.toArray()));
                Runnable runnable = workQueue.take();
                idle.set(false);
                System.out.println("WorkerThread " + workerName + " is running "+ runnable);
                runnable.run();
                idle.set(true);
                System.out.println("WorkerThread " + workerName + " finished " + runnable);
            } catch (Exception e){
                System.out.println("Runnable failed on Worker : "+ workerName +", Moving on to next task");
            }
        }
    }

    public void stopWorker() {
        try{
            System.out.println("Stopping thread : "+ this.workerName);
            running.set(false);
        }catch (Exception e){
            System.out.println("Thread Stopped");
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public String getWorkerName() {
        return workerName;
    }
    public boolean isIdle() {
        return idle.get();
    }
}
