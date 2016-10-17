package engine.surface;

import java.util.List;

import engine.util.Point;

/**
 * The cell is a base class for each cell contained in a SurfaceMap.
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
    
    public Point getGridPosition(){
    	return gridPosition.copy();
    }
    
    public Point getActualPosition(){
        return actualPosition;
    }
    
    public abstract List<Integer[]> render(List<Integer[]> settings);
    
    public String toString() {
        return "[" + gridPosition.getX() + ", " + gridPosition.getY() + "]" +
                "[" + actualPosition.getX() + ", " + actualPosition.getY() + "]";
    }

}
