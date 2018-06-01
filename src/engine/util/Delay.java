package engine.util;

import java.io.Serializable;

/**
 * Delays are counters that will return false until the counter has counted to
 * the desired value, the delay can reset or continue to return true.
 *
 * @author Richard DeSilvey
 */
public class Delay implements Serializable {

  private int delayTime;
  private int maxDelay;

  private boolean reset;

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

  /**
   * Unlike check(), testDelay() will not increment
   *
   * @return
   */
  public boolean testDelay() {
    return delayTime >= maxDelay;
  }

  public void reset() {
    delayTime = 0;
  }

  public void setDelay(int delayTime) {
    this.maxDelay = delayTime;
    reset();
  }
  
  /**
   * Increment then check to see if this delay has reached it's trigger delay
   * (max delay defined by constructor)
   *
   * @return If the delay is triggered
   */
  public boolean check() {
    if (++delayTime >= maxDelay) {

      if (!reset) {
        delayTime = maxDelay;
      } else {
        reset();
      }

      return true;

    } else {
      return false;
    }
  }

}
