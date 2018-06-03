package engine.surface;

import engine.util.Vec2;
import engine.util.concurrent.MThread;

/**
 * The cell is a base class for each cell contained in a SurfaceMap. The Cell
 * has two representations for position. It's grid position which is immutable
 * and the actual position. The actual position is mutable and can be used as a
 * way to describe an 'out of resolution' position.
 *
 * @author Richard DeSilvey
 */
public abstract class Cell {

  private final Vec2 gridPosition, actualPosition;
  private MThread parentThread;

  public Cell(int x, int y) {
    gridPosition = new Vec2(x, y);
    actualPosition = new Vec2(x, y);
  }
  
  public void setParentThread(MThread parentThread) {
    this.parentThread = parentThread;
  }
  
  public MThread getParentThread() {
    return parentThread;
  }
  
  public int getX() {
    return (int) gridPosition.getX();
  }

  public int getY() {
    return (int) gridPosition.getY();
  }

  /**
   * The position in the hash map as x and y componets.
   *
   * @return The copy of the cell position object.
   */
  public Vec2 getGridPosition() {
    return gridPosition.copy();
  }

  /**
   * The actual position of this cell. By default this position will be equal to
   * it's grid position.
   *
   * @return
   */
  public Vec2 getActualPosition() {
    return actualPosition;
  }

//  public abstract List<Integer[]> render(List<Integer[]> settings);

  public String toString() {
    return "[" + gridPosition.getX() + ", " + gridPosition.getY() + "]"
            + "[" + actualPosition.getX() + ", " + actualPosition.getY() + "]";
  }

}
