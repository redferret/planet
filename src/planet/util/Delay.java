
package planet.util;

import java.io.Serializable;

/**
 * Delay objects are used when something needs a slower update
 * than what your main thread.
 * 
 * @author Richard DeSilvey
 */
public final class Delay implements Serializable {

    private int delayTime, maxDelay;
    
    private boolean reset;
    
    /**
     * Default constructor
     */
    public Delay(){
        delayTime = 0;
        maxDelay = 0;
        reset = true;
    }
    
    /**
     * Default constructor
     * @param reset
     */
    public Delay(boolean reset){
        delayTime = 0;
        maxDelay = 0;
        this.reset = reset;
    }
    
    /**
     * Initializes this delay with the number of frames to delay at.
     * @param frames
     */
    public Delay(int frames){
        assert frames <= 0 : "Invalid Delay Time";
        
        delayTime = 0;
        this.maxDelay = frames;
        reset = true;
    }
    
    /**
     * Initializes this delay with the number of frames to delay at.
     * @param frames
     * @param reset
     */
    public Delay(int frames, boolean reset){
        
        assert frames <= 0 : "Invalid Delay Time";
        
        delayTime = 0;
        this.maxDelay = frames;
        this.reset = reset;
    }

    /**
     * When the delay is finished, this boolean value
     * will determine whether or not to reset this delay.
     * By default the reset is turned off.
     * @param b
     */
    public void allowReset(boolean b){
        reset = b;
    }
    
    /**
     * Specifies the number of frames to delay and returns 1 if the delay has been met
     * or 0 if the delay has not yet been reached.
     * @param frames
     * @return 
     */
    public boolean delay(int frames){
        
        assert frames < 0 : "Invalid Delay Time";

        if (++delayTime >= frames) {

            if (!reset) {
                delayTime = frames;
            } else {
                reset();
            }

            return true;

        } else {
            return false;
        }
    }
    
    /**
     * Test delay only works if you initialize your delay object
     * with a delay value given in the constructor's parameter.
     * This does not increment the delay.
     * @return 1 if the delay is met, 0 if the delay is not met.
     */
    public boolean testDelay(){
        return delayTime >= maxDelay;
    }
    
    /**
     * Resets the delay. This method is not needed if you are allowing
     * the delay to reset itself.
     */
    public void reset(){
        delayTime = 0;
    }

    /**
     * Calls the delay and uses the initial delay value passed into
     * the constructor of this delay.
     * @return 
     */
    public boolean delay(){
        return delay(maxDelay);
    }

}


