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
    private boolean isAtomic;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock cellLock = readWriteLock.writeLock();

    public AtomicData(CellType data) {
        this.data = data;
        currentOwner = null;
        isAtomic = true;
    }

    /**
     * In some cases it isn't completely necessary to make all the data in a map
     * atomic to multiple threads. This will increase the speed of the
     * simulation. Setting whether the data is atomic or not will cause the lock
     * to be used. Race conditions or corruption of data may occur if multiple
     * threads access data this isn't atomic.
     *
     * @param atomic Whether this data will be atomic or not.
     */
    public void setAsAtomic(boolean atomic) {
        isAtomic = atomic;
    }

    public boolean isAtomic() {
        return isAtomic;
    }

    /**
     * Threads needing to access this cell data will wait until the lock is free
     * following the fairness policy for concurrency.
     *
     * @return The data stored in this lock, if this thread is interrupted while
     * waiting on the lock this method will return null.
     * @throws RuntimeException if starvation happens on the calling thread.
     */
    public CellType waitForData() throws RuntimeException {
        return isAtomic ? waitOnData() : getData();
    }

    /**
     * Performs thread synchronization on the data stored in this guard.
     *
     * @return The data that is now locked to the current thread.
     * @throws RuntimeException If starvation happens on the calling thread.
     */
    private CellType waitOnData() throws RuntimeException {
        try {
            String threadName = Thread.currentThread().getName();
            String exMsg = "Starvation on thread " + threadName
                    + " for resource " + data;

            boolean acquired = cellLock.tryLock(10000, TimeUnit.MILLISECONDS);

            if (!acquired && currentOwner != null) {
                String ownerName = currentOwner.getName();
                Logger.getLogger("Starvation").log(Level.SEVERE, "Starvation"
                        + " occurred on thread {0} for resource {1},\n current"
                        + " owner of resource is {2}",
                        new Object[]{threadName, data.toString(), ownerName});

                throw new RuntimeException(exMsg);
            } else if (acquired) {
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
     * If the cell has already been locked then the thread calling this method
     * will not wait, instead if the data has already been locked this method
     * will return null. If the data is not atomic then this method will always
     * return the data.
     *
     * @return The data or null if locked.
     */
    public CellType getData() {
        if (isAtomic) {
            boolean acquired = cellLock.tryLock();

            if (acquired) {
                currentOwner = Thread.currentThread();
                return data;
            } else {
                return null;
            }
        } else {
            return data;
        }
    }

    /**
     * Sets the data and unlocks it if the calling thread is the current owner
     * of this resource. If the data is not atomic or the calling thread is not
     * the owner of the lock then nothing happens.
     *
     * @param data The new updated version of the data.
     */
    public void unlock(CellType data) {
        if (isAtomic) {
            if (Thread.currentThread().equals(currentOwner)) {
                this.data = data;
                currentOwner = null;
                cellLock.unlock();
            }
        }
    }

}
