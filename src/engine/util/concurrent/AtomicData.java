package engine.util.concurrent;

import engine.surface.Cell;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Places a lock on the data stored in this object. If other threads attempt to
 * get the data the thread will "be disabled for thread scheduling purposes and
 * lies dormant until the lock has been acquired".
 *
 * @author Richard DeSilvey
 * @param <CellType> The data type, must extend from Cell
 */
public class AtomicData<CellType extends Cell> {

    private CellType data;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock cellLock = readWriteLock.writeLock();

    public AtomicData(CellType data) {
        this.data = data;
    }

    /**
     * Threads needing to access this cell data will wait until the lock is
     * free following the fairness policy for concurrency.
     *
     * @return
     */
    public CellType waitForCell() {
        cellLock.lock();
        return data;
    }
    
    /**
     * If the cell has already been locked the thread calling this method
     * will not wait, instead if the data has already been locked by another
     * thread this method will return null.
     * @return The data or null if locked.
     */
    public CellType getCell(){
        return (cellLock.tryLock()? data : null);
    }

    /**
     * Unlocks the data for the next thread.
     */
    public void unlock() {
        cellLock.unlock();
    }

}
