package engine.surface;

import java.util.List;

import engine.util.Point;

/**
 * The cell is a base class for each cell contained in a SurfaceMap.
 * The Cell has two repersentations for position. It's grid position which
 * is imutable and the actual position. The actual position is mutable and
 * can be used as a way to describe an 'out of resoultion' position.
 *
 * @author Richard DeSilvey
 */
public abstract class Cell {	
    
    private final Point gridPosition, actualPosition;

    public Cell(int x, int y) {
        gridPosition = new Point(x, y);
        actualPosition = new Point(x, y);
    }

    public int getX() {
        return (int)gridPosition.getX();
    }

    public int getY() {
        return (int)gridPosition.getY();
    }
    
    /**
     * The position in the hash map as x and y componets.
     * @return The copy of the cell position object.
     */
    public Point getGridPosition(){
    	return gridPosition.copy();
    }
    
    /**
     * The actual position of this cell. By default this position will
     * be equal to it's grid position.
     * @return
     */
    public Point getActualPosition(){
        return actualPosition;
    }
    
    public abstract List<Integer[]> render(List<Integer[]> settings);
    
    public String toString() {
        return "[" + gridPosition.getX() + ", " + gridPosition.getY() + "]" +
                "[" + actualPosition.getX() + ", " + actualPosition.getY() + "]";
    }

}
