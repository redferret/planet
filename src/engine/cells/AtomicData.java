package engine.cells;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import com.sun.istack.internal.logging.Logger;

/**
 * Places a lock on the data stored in this object. If other threads attempt to get the data
 * the thread will "be disabled for thread scheduling purposes and lies dormant until the 
 * lock has been acquired".
 * 
 * @author Richard DeSilvey
 * @param <CellType> The data type, must extend from Cell
 */
public class AtomicData<CellType extends Cell> {

//	private static Logger logger = Logger.getLogger(AtomicData.class);
	private CellType data;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock cellLock = readWriteLock.writeLock();
    
    public AtomicData(CellType data){
    	this.data = data;
    }
	
    /**
     * Any other thread accessing this cell will be blocked until the unlock method is called.
     * @return
     */
    public CellType getCellAndLock(){
//    	logger.log(Level.INFO, "Preparing to Lock for " + Thread.currentThread().getName() 
//    			+ " on cell " + data);
    	cellLock.lock();
//    	logger.log(Level.INFO, "Lock Aquired for " + Thread.currentThread().getName() 
//    			+ " on cell " + data);
    	return this.data;
    }
    
    /**
     * 
     * @param cell
     */
    public void unlock(){
//    	logger.log(Level.INFO, "Write Lock Released " + Thread.currentThread().getName() 
//    			+ " for cell " + data);
    	cellLock.unlock();
    }
    
}
