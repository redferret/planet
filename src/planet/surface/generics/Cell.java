package planet.surface.generics;

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
    private int x;
    
    // The y coordinate
    private int y;


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

    public abstract int getRenderIndex(int settings);
    
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

}
