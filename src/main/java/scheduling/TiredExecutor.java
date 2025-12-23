package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
//import java.util.ArrayList; # I got angry because of the yellow comments :(
//import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads];
        for(int i = 0 ; i < numThreads ; i++) {
            double f = Math.random()+0.5;
            TiredThread worker = new TiredThread(i,f);
            workers[i] = worker;
            idleMinHeap.add(worker);
            worker.start();
        }
    }

    public void submit(Runnable task) {
        try{
            TiredThread tired = idleMinHeap.take();
            inFlight.incrementAndGet();
            tired.newTask(() -> {
                try{
                    task.run();
                }
                finally{
                    inFlight.decrementAndGet();
                    idleMinHeap.add(tired);
                    synchronized(this) { this.notifyAll(); }
                }
            });
        }
        catch(Exception e){
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable runnable : tasks) {
            submit(runnable);
        }
        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    this.wait(); 
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        for (TiredThread tiredThread : workers) {
            tiredThread.shutdown();
        }
        for (TiredThread worker : workers) {
            worker.join(); 
        }
    }

    public synchronized String getWorkerReport() {
        StringBuilder sb = new StringBuilder();
        for (TiredThread worker : workers) {
            sb.append(String.format("Worker %d: Fatigue=%.2f, TimeUsed=%d ns, TimeIdle=%d ns\n",
                    worker.getWorkerId(), 
                    worker.getFatigue(), 
                    worker.getTimeUsed(), 
                    worker.getTimeIdle()));
        }
        sb.append(getFairnessReport());
        return sb.toString();
    }

    public String getFairnessReport() {
        if (workers.length == 0) return "";

        double totalFatigue = 0;
        for (TiredThread worker : workers) {
            totalFatigue += worker.getFatigue();
        }
        double averageFatigue = totalFatigue / workers.length;

        double sum = 0;
        for (TiredThread worker : workers) {
            double deviation = worker.getFatigue() - averageFatigue;
            sum += deviation * deviation;
        }

        return String.format("Average Fatigue: %.2f, Fairness Score (Lower is better): %.4f\n", averageFatigue, sum);
    }
}
