package engine.task;

/**
 * Boundaries define the bounds for each SurfaceThread running. It contains an
 * upper and lower bounds for both axis.
 *
 * @author Richard DeSilvey
 */
public final class Boundaries {

  private int lowerXBound, upperXBound, lowerYBound, upperYBound;

  /**
   * Constructs boundaries where both axis have the same ranges.
   *
   * @param lowerBound The lower bounds
   * @param upperBound The upper bounds
   */
  public Boundaries(int lowerBound, int upperBound) {
    this(lowerBound, upperBound, lowerBound, upperBound);
  }

  public Boundaries(int lowerXBound, int upperXBound, int lowerYBound, int upperYBound) {

    if (lowerXBound < 0 || upperXBound < 0) {
      throw new IllegalArgumentException("Can't have negative bounds");
    }
    if (lowerYBound < 0 || upperYBound < 0) {
      throw new IllegalArgumentException("Can't have negative bounds");
    }

    this.lowerXBound = lowerXBound;
    this.upperXBound = upperXBound;
    this.lowerYBound = lowerYBound;
    this.upperYBound = upperYBound;
  }

  public int getLowerXBound() {
    return lowerXBound;
  }

  public int getLowerYBound() {
    return lowerYBound;
  }

  public int getUpperXBound() {
    return upperXBound;
  }

  public int getUpperYBound() {
    return upperYBound;
  }

}
