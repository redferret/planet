
package planet.util;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Modified thread that can be continuous or be signaled to run. Each
 * thread has a delay time on each update.
 * @author Richard DeSilvey
 */
public abstract class MThread extends Thread {

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
    private boolean isFinished;
    
    
    private boolean continuous;
    private AtomicInteger timeLapse;
    
    public MThread(int delay, String name, boolean continuous){
        miliSeconds = delay;
        this.continuous = continuous;
        running = false;
        isFinished = false;
        timeLapse = new AtomicInteger(0);
        setName(name);
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
        interrupt();
    }
    
    public void kill(){
        isFinished = true;
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
        while(!isFinished){
            try {
                if (running) {
                    long start = System.currentTimeMillis();
                    update();
                    timeLapse.getAndSet((int) (System.currentTimeMillis() - start));
                }
                
                sleep(miliSeconds);
                if (!running || !continuous){
                    sleep(60000);
                }
            } catch (InterruptedException interruptedException) {
            }
        }
    }
    
}
