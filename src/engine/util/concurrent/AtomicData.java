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
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
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
            String threadName = Thread.currentThread().getName();
            String exMsg = "Starvation on thread " + threadName
                + " for resource " + data;
            
            boolean acquired = cellLock.tryLock(10000, TimeUnit.MILLISECONDS);
            
            if (!acquired && currentOwner != null){
                String ownerName = currentOwner.getName();
                Logger.getLogger("Starvation").log(Level.SEVERE, "Starvation"
                        + " occurred on thread {0} for resource {1},\n current"
                        + " owner of resource is {2}", 
                        new Object[]{threadName, data.toString(), ownerName});
                
                throw new RuntimeException(exMsg);
            }else if (acquired){
                currentOwner = Thread.currentThread();
                return data;
            }
        } catch (InterruptedException interruptedException) {
            Logger.getLogger("Thread Interruption").log(Level.SEVERE, "Thread "
                    + "{0} was interrupted while waiting on resource", 
                    Thread.currentThread().getName());
        }
        return null;
    }
    
    /**
     * If the cell has already been locked the thread calling this method
     * will not wait, instead if the data has already been locked by another
     * thread this method will return null.
     * @return The data or null if locked.
     */
    public CellType getData(){
        
        boolean acquired = cellLock.tryLock();
        
        if (acquired){
            currentOwner = Thread.currentThread();
            return data;
        }else{
            return null;
        }
        
    }

    /**
     * Sets the data and unlocks it if the calling thread is the current owner
     * of this resource, otherwise nothing happens.
     * @param data The new updated version of the data.
     */
    public void unlock(CellType data){
        if (Thread.currentThread().equals(currentOwner)){
            this.data = data;
            currentOwner = null;
            cellLock.unlock();
        }
    }
    
}
