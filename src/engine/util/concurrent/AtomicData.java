package engine.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Places a lock on the data stored in this object. If other threads attempt to
 * get the data the thread will "be disabled for thread scheduling purposes and
 * lies dormant until the lock has been acquired".
 *
 * @author Richard DeSilvey
 * @param <CellType> The data type
 */
public class AtomicData<CellType> {

    private CellType data;
    private Thread currentOwner;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock cellLock = readWriteLock.writeLock();

    public AtomicData(CellType data) {
        this.data = data;
        currentOwner = null;
    }

    /**
     * Threads needing to access this cell data will wait until the lock is
     * free following the fairness policy for concurrency.
     *
     * @return The data stored in this lock, if this thread is interrupted while
     * waiting on the lock this method will return null.
     * @throws RuntimeException if starvation happens on the calling thread.
     */
    public CellType waitForData() throws RuntimeException {
        try {
            boolean acquired = cellLock.tryLock(3000, TimeUnit.MILLISECONDS);
            if (!acquired){
                String threadName = Thread.currentThread().getName();
                Logger.getLogger("Starvation").log(Level.SEVERE, "Starvation"
                        + " occured on thread {0} for resource {1}", 
                        new Object[]{threadName, data.toString()});
                throw new RuntimeException("Starvation on thread " + threadName
                + " for resource " + data);
            }
        } catch (InterruptedException interruptedException) {
            Logger.getLogger("Thread Interruption").log(Level.SEVERE, "Thread "
                    + "{0} was interrupted while waiting on resource", 
                    Thread.currentThread().getName());
            return null;
        }
        currentOwner = Thread.currentThread();
        return data;
    }
    
    /**
     * If the cell has already been locked the thread calling this method
     * will not wait, instead if the data has already been locked by another
     * thread this method will return null.
     * @return The data or null if locked.
     */
    public CellType getData(){
        return (cellLock.tryLock()? data : null);
    }

    /**
     * Sets the data and unlocks it if the calling thread is the current owner
     * of this resource, otherwise nothing happens.
     * @param data The new updated version of the data.
     */
    public void unlock(CellType data){
        if (Thread.currentThread() == currentOwner){
            this.data = data;
            unlock();
        }
    }
    
    /**
     * Unlocks the data for the next thread, only the thread holding the 
     * resource can unlock it.
     */
    public void unlock() {
        cellLock.unlock();
    }

}
