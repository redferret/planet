package engine.surface;

import java.util.List;

import engine.util.Point;

/**
 * The cell is a base class for each cell contained in a SurfaceMap.
 * For example the Geosphere contains GeoCells which contain geological strata.
 * Other cells should extend from existing cells. The hydrosphere should
 * extend from the geosphere so a HydroCell is a GeoCell.
 *
 * @author Richard DeSilvey
 */
public abstract class Cell {	
    
    // The x coordinate
    private final int x;
    
    // The y coordinate
    private final int y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public Point getPosition(){
    	return new Point(x, y);
    }
    
    public abstract List<Integer[]> render(List<Integer[]> settings);
    
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

}
