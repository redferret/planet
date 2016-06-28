
package engine.util;

/**
 * TBuffer is a template class for creating transfer buffers for a Cell.
 * Instead of creating separate transfer maps to transfer data from one
 * cell to another each cell will contain a buffer that will hold information 
 * about what needs to be moved and then applied later after an
 * update has been made on a map.
 * @author Richard DeSilvey
 */
public abstract class TBuffer {
       
    /**
     * Determines if the buffer has been set or not
     */
    private boolean bufferSet;

    public TBuffer(){
        set();
    }

    /**
     * Sets the buffer, same as <code>resetBuffer()</code>
     */
    private void set(){
        resetBuffer();
    }
    
    /**
     * Resets the buffer by changing the bufferSet flag to false and 
     * re-initializing the buffer.
     */
    public void resetBuffer(){
        bufferSet = false;
        init();
    }

    /**
     * Implement and finalize this method. The <code>resetBuffer()</code> method
     * calls this method.
     */
    protected abstract void init();
    
    /**
     * Applies changes.
     */
    public abstract void applyBuffer();
    
    /**
     * Changes the bufferSet flag.
     * @param b Whether this buffer is set or not
     */
    public void bufferSet(boolean b){
        bufferSet = b;
    }
    
    /**
     * Returns true if the buffer has been set.
     * @return True if this buffer is set.
     */
    public boolean bufferSet(){
        return bufferSet;
    }
    
}
