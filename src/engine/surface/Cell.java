package engine.surface;

import com.jme3.math.Vector2f;
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

  private final Vector2f gridPosition, actualPosition;
  private MThread parentThread;

  public Cell(int x, int y) {
    gridPosition = new Vector2f(x, y);
    actualPosition = new Vector2f(x, y);
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
  public Vector2f getGridPosition() {
    return gridPosition.clone();
  }

  /**
   * The actual position of this cell. By default this position will be equal to
   * it's grid position.
   *
   * @return
   */
  public Vector2f getActualPosition() {
    return actualPosition;
  }

//  public abstract List<Integer[]> render(List<Integer[]> settings);

  public String toString() {
    return "[" + gridPosition.getX() + ", " + gridPosition.getY() + "]"
            + "[" + actualPosition.getX() + ", " + actualPosition.getY() + "]";
  }

}
