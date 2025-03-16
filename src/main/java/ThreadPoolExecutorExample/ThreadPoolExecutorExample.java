package ThreadPoolExecutorExample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPoolExecutorExample implements ExecutorInterface {
    private final int maximumPoolSize;
    private final BlockingQueue<Runnable> workQueue;
    private final List<WorkerThread> workerThreads;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private ReentrantLock lock = new ReentrantLock();
    private AtomicInteger executeCount = new AtomicInteger(0);

    public ThreadPoolExecutorExample(int corePoolSize, int maximumPoolSize, int queueCapacity) {
        this.maximumPoolSize = maximumPoolSize;
        workQueue = new LinkedBlockingQueue<>(queueCapacity);
        workerThreads = new CopyOnWriteArrayList<>();
        for(int i = 0; i < corePoolSize; i++) {
            startNewWorker();
        }

    }

    @Override
    public void execute(Runnable runnable) {
        try{
            lock.lock();
            System.out.println("Lock acquired by thread : "+ Thread.currentThread().getName());
            if(isShutdown.get()) {
                System.out.println("Executor is shutting down");
                return;
            }
            if(workQueue.remainingCapacity()==0){
                throw new RejectQueueException("Queue is Full");
            }else{
                System.out.println("Executing " + runnable + " with count " + executeCount.incrementAndGet());
                System.out.println("Adding task to queue");
                workQueue.add(runnable);
            }

            boolean idleWorkerFound = false;
            System.out.println("Checking for idle threads for runnable " +runnable + " with count " + executeCount.get());
            for(WorkerThread workerThread : workerThreads) {
                if(workerThread.isIdle()) {
                    idleWorkerFound = true;
                    System.out.println("Worker "+workerThread.getWorkerName()+" is idle");
                    break;
                }
            }

            // check if the idle workers has picked up tasks or not. If not, create new workers
            if( (!workQueue.isEmpty() || !idleWorkerFound) && workerThreads.size() < maximumPoolSize) {
                System.out.println("Still tasks in queue or No idle threads found for runnable " +runnable + " with count " + executeCount.get());
                startNewWorker();
            }else if(workerThreads.size()>=maximumPoolSize){
                System.out.println("Reached maximum pool size, cannot create new worker." +
                        " Wait for other worker to finish");
            }



        }finally {
            lock.unlock();
        }
    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
        for(WorkerThread workerThread : workerThreads) {
            workerThread.stopWorker();
        }
        System.out.println("Executor is shutting down");

    }

    @Override
    public void shutdownNow() {
        System.out.println("Executor forcefully shutting down all active worker threads");
        isShutdown.set(true);
        for(WorkerThread workerThread : workerThreads){
            if(workerThread.isRunning()){
                try {
                    System.out.println("Worker Thread"+ workerThread.getWorkerName()+" is still running");
                    workerThread.stopWorker();
                    workerThread.interrupt();
                }catch (Exception e){
                    System.out.println("Worker Thread"+ workerThread.getWorkerName()+" stop failed");
                }
            }
        }
        workQueue.clear();
    }

    @Override
    public int getActiveCount() {
        return (int)workerThreads.stream().filter(Thread::isAlive).count();
    }

    @Override
    public int getQueueLength() {
        return workQueue.size();
    }
    private void startNewWorker() {
        System.out.println("Starting new worker thread");
        WorkerThread workerThread = new WorkerThread(workQueue);
        workerThreads.add(workerThread);
        workerThread.start();
    }
}
