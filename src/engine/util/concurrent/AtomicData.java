package engine.util.concurrent;

import engine.util.exception.SurfaceThreadStarvationException;
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
 * @param <Data> The data type
 */
public class AtomicData<Data> {

    private Data data;
    private Thread currentOwner;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock cellLock = readWriteLock.writeLock();

    public AtomicData(Data data) {
        this.data = data;
        currentOwner = null;
    }

    /**
     * Performs thread synchronization on the data stored in this guard.
     *
     * @return The data that is now locked to the current thread.
     * @throws RuntimeException If starvation happens on the calling thread.
     */
    public Data waitForData() throws RuntimeException {
        try {
            String threadName = Thread.currentThread().getName();

            boolean acquired = cellLock.tryLock(10000, TimeUnit.MILLISECONDS);

            if (!acquired && currentOwner != null) {
                String ownerName = currentOwner.getName();
                Logger.getLogger("Starvation").log(Level.SEVERE, "Starvation"
                        + " occurred on thread {0} for resource {1},\n current"
                        + " owner of resource is {2}",
                        new Object[]{threadName, data.toString(), ownerName});

                throw new SurfaceThreadStarvationException(threadName, data);
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
    public Data getData() {
        boolean acquired = cellLock.tryLock();

        if (acquired) {
            currentOwner = Thread.currentThread();
            return data;
        } else {
            return null;
        }
    }

    /**
     * Sets the data and unlocks it if the calling thread is the current owner
     * of this resource. If the data is not atomic or the calling thread is not
     * the owner of the lock then nothing happens.
     *
     * @param data The new updated version of the data.
     */
    public void unlock(Data data) {
        if (Thread.currentThread().equals(currentOwner)) {
            this.data = data;
            currentOwner = null;
            cellLock.unlock();
        }
    }

}
