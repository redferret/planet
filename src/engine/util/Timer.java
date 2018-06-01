package engine.util;

/**
 *
 * @author Richard DeSilvey
 */
public class Timer extends Delay {

  /**
   * Creates a new Timer that will count down until the number of frames have
   * been reached.
   *
   * @param countDown The number of frames to count down to
   */
  public Timer(int countDown) {
    super(countDown, false);
  }

  @Override
  public final boolean check() {
    return !super.check();
  }

}
