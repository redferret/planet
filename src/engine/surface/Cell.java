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
    
    private final Point position;

    public Cell(int x, int y) {
        position = new Point(x, y);
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }
    
    public Point getPosition(){
    	return position.copy();
    }
    
    public abstract List<Integer[]> render(List<Integer[]> settings);
    
    public String toString() {
        return "[" + position.getX() + ", " + position.getY() + "]";
    }

}
