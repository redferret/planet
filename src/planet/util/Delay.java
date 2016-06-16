package planet.util;

import java.io.Serializable;

/**
 * Delays are counters that will return false until the counter has counted
 * to the desired value, the delay can reset or continue to return true.
 *
 * @author Richard DeSilvey
 */
public final class Delay implements Serializable {

    private int delayTime, maxDelay;

    private boolean reset;

    /**
     * Default constructor
     */
    public Delay() {
        delayTime = 0;
        maxDelay = 0;
        reset = true;
    }

    /**
     * Default constructor
     *
     * @param reset
     */
    public Delay(boolean reset) {
        delayTime = 0;
        maxDelay = 0;
        this.reset = reset;
    }

    /**
     * Initializes this check with the number of frames to check at.
     *
     * @param frames
     */
    public Delay(int frames) {
        delayTime = 0;
        this.maxDelay = frames;
        reset = true;
    }

    /**
     * Initializes this check with the number of frames to check at.
     *
     * @param frames
     * @param reset
     */
    public Delay(int frames, boolean reset) {
        delayTime = 0;
        this.maxDelay = frames;
        this.reset = reset;
    }

    /**
     * When the check is finished, this boolean value will determine whether or
     * not to reset this check. By default the reset is turned off.
     *
     * @param b
     */
    public void allowReset(boolean b) {
        reset = b;
    }

    /**
     * Specifies the number of frames to check and returns 1 if the check has
     * been met or 0 if the check has not yet been reached.
     *
     * @param frames
     * @return
     */
    public boolean check(int frames) {
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
     * Test check only works if you initialize your check object with a check
     * value given in the constructor's parameter. This does not increment the
     * check.
     *
     * @return 1 if the check is met, 0 if the check is not met.
     */
    public boolean testDelay() {
        return delayTime >= maxDelay;
    }

    /**
     * Resets the check. This method is not needed if you are allowing the check
     * to reset itself.
     */
    public void reset() {
        delayTime = 0;
    }

    /**
     * Calls the check and uses the initial check value passed into the
     * constructor of this check.
     *
     * @return
     */
    public boolean check() {
        return Delay.this.check(maxDelay);
    }

}
