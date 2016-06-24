package planet.util;

import java.io.Serializable;

/**
 * Delays are counters that will return false until the counter has counted
 * to the desired value, the delay can reset or continue to return true.
 *
 * @author Richard DeSilvey
 */
public class Delay implements Serializable {

    private int delayTime, maxDelay;

    private boolean reset;

    public Delay() {
        delayTime = 0;
        maxDelay = 0;
        reset = true;
    }

    public Delay(boolean reset) {
        delayTime = 0;
        maxDelay = 0;
        this.reset = reset;
    }

    public Delay(int frames) {
        delayTime = 0;
        this.maxDelay = frames;
        reset = true;
    }

    public Delay(int frames, boolean reset) {
        delayTime = 0;
        this.maxDelay = frames;
        this.reset = reset;
    }

    public void allowReset(boolean b) {
        reset = b;
    }

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

    public boolean testDelay() {
        return delayTime >= maxDelay;
    }

    public void reset() {
        delayTime = 0;
    }

    
    public boolean check() {
        return Delay.this.check(maxDelay);
    }

}
