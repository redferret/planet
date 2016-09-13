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
     * @return
     */
    public CellType waitForData() {
        try {
            boolean acquired = cellLock.tryLock(1500, TimeUnit.MILLISECONDS);
            if (!acquired){
                Logger.getLogger("Starvation").log(Level.SEVERE, "Starvation"
                        + " occured on thread {0}", Thread.currentThread().getName());
                throw new RuntimeException("Starvation occured on thread");
            }
        } catch (InterruptedException interruptedException) {
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
