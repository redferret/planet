
package engine.util.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Modified Runnable class.
 * @author Richard DeSilvey
 */
public abstract class TaskRunner implements Runnable {

    /**
     * The delay speed in milliseconds.
     */
    private int miliSeconds;
    
    /**
     * Flag used to determine if the thread is running or not.
     */
    protected boolean running;
    
    /**
     * Determines if this thread is finished running
     */
    private boolean executing;
    
    
    private boolean continuous;
    private AtomicInteger timeLapse;
    private CyclicBarrier waiter;
    
    public TaskRunner(int delay, boolean continuous){
        miliSeconds = delay;
        this.continuous = continuous;
        running = false;
        executing = true;
        timeLapse = new AtomicInteger(0);
        waiter = new CyclicBarrier(2);
    }
    
    public int timeLapse(){
        return timeLapse.get();
    }
    
    public void setDelay(int delay){
        miliSeconds = delay;
    }
    
    public void pause(){
        running = false;
    }
    public void play(){
        running = true;
        waiter.reset();
    }
    
    public void kill(){
        executing = false;
        waiter.reset();
    }
    
    public boolean paused(){
        return !running;
    }
    
    public abstract void update();
    
    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }
    
    @Override
    public void run() {
        while(executing){
            try {
                if (running) {
                    long start = System.currentTimeMillis();
                    update();
                    timeLapse.getAndSet((int) (System.currentTimeMillis() - start));
                }
                
                Thread.sleep(miliSeconds);
                
                if (!running || !continuous){
                    waiter.await();
                }
            } catch (InterruptedException | BrokenBarrierException e) {
            } 
        }
    }
    
}
